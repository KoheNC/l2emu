/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.l2emuproject.gameserver.services.shortcuts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.L2DatabaseFactory;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.serverpackets.ExAutoSoulShot;
import net.l2emuproject.gameserver.network.serverpackets.ShortCutInit;
import net.l2emuproject.gameserver.network.serverpackets.ShortCutRegister;
import net.l2emuproject.gameserver.templates.item.L2EtcItemType;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.LookupTable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class ShortCuts
{
	private static final Log _log = LogFactory.getLog(ShortCuts.class);
	
	private final LookupTable<Map<Integer, L2ShortCut>> _storedShortCuts = new LookupTable<Map<Integer, L2ShortCut>>();
	private final L2Player _owner;
	
	public ShortCuts(L2Player owner)
	{
		_owner = owner;
	}
	
	public L2ShortCut[] getAllShortCuts()
	{
		return getShortCutMap().values().toArray(new L2ShortCut[getShortCutMap().size()]);
	}
	
	public synchronized void registerShortCut(L2ShortCut shortcut)
	{
		getShortCutMap().put(shortcut.getSlot() + 12 * shortcut.getPage(), shortcut);
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con
					.prepareStatement("REPLACE INTO character_shortcuts (charId,slot,page,type,shortcut_id,level,class_index) values(?,?,?,?,?,?,?)");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, shortcut.getType());
			statement.setInt(5, shortcut.getId());
			statement.setInt(6, shortcut.getLevel());
			statement.setInt(7, _owner.getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public synchronized void deleteShortCut(int slot, int page)
	{
		final L2ShortCut old = getShortCutMap().remove(slot + page * 12);
		if (old == null)
			return;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con
					.prepareStatement("DELETE FROM character_shortcuts WHERE charId=? AND slot=? AND page=? AND class_index=?");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, old.getSlot());
			statement.setInt(3, old.getPage());
			statement.setInt(4, _owner.getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		if (old.getType() == L2ShortCut.TYPE_ITEM)
		{
			L2ItemInstance item = _owner.getInventory().getItemByObjectId(old.getId());
			
			if (item != null && item.getItemType() == L2EtcItemType.SHOT)
				_owner.getShots().removeAutoSoulShot(item.getItemId());
		}
		
		_owner.sendPacket(new ShortCutInit(_owner));
		
		for (int shotId : _owner.getShots().getAutoSoulShots())
			_owner.sendPacket(new ExAutoSoulShot(shotId, 1));
	}
	
	public void deleteShortCutByObjectId(int objectId)
	{
		deleteShortCutByTypeAndId(L2ShortCut.TYPE_ITEM, objectId);
	}
	
	public synchronized void deleteShortCutByTypeAndId(int type, int id)
	{
		for (L2ShortCut sc : getShortCutMap().values())
			if (sc.getType() == type)
				if (sc.getId() == id)
					deleteShortCut(sc.getSlot(), sc.getPage());
	}
	
	private Map<Integer, L2ShortCut> getShortCutMap()
	{
		return getShortCutMap(_owner.getClassIndex());
	}
	
	private synchronized Map<Integer, L2ShortCut> getShortCutMap(int classIndex)
	{
		Map<Integer, L2ShortCut> map = _storedShortCuts.get(classIndex);
		
		if (map != null)
			return map;
		
		map = new FastMap<Integer, L2ShortCut>().shared();
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con
					.prepareStatement("SELECT slot,page,type,shortcut_id,level FROM character_shortcuts WHERE charId=? AND class_index=?");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, classIndex);
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int slot = rset.getInt("slot");
				final int page = rset.getInt("page");
				final int type = rset.getInt("type");
				final int id = rset.getInt("shortcut_id");
				final int level = rset.getInt("level");
				
				map.put(slot + page * 12, new L2ShortCut(slot, page, type, id, level, 1));
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		_storedShortCuts.set(classIndex, map);
		
		return map;
	}
	
	public synchronized void restore()
	{
		getShortCutMap();
		
		for (L2ShortCut sc : getShortCutMap().values())
			if (sc.getType() == L2ShortCut.TYPE_ITEM)
				if (_owner.getInventory().getItemByObjectId(sc.getId()) == null)
					deleteShortCut(sc.getSlot(), sc.getPage());
	}
	
	public synchronized void updateSkillShortcuts(int skillId)
	{
		// update all the shortcuts to this skill
		for (L2ShortCut sc : getShortCutMap().values())
		{
			if (sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				if (sc.getId() == skillId)
				{
					final int skillLvl = _owner.getSkillLevel(skillId);
					
					L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLvl, 1);
					_owner.sendPacket(new ShortCutRegister(newsc));
					registerShortCut(newsc);
				}
			}
		}
	}
	
	public synchronized void deleteShortCuts(Connection con, int classIndex) throws SQLException
	{
		PreparedStatement statement = con
				.prepareStatement("DELETE FROM character_shortcuts WHERE charId=? AND class_index=?");
		statement.setInt(1, _owner.getObjectId());
		statement.setInt(2, classIndex);
		statement.execute();
		statement.close();
		
		getShortCutMap(classIndex).clear();
	}
}
