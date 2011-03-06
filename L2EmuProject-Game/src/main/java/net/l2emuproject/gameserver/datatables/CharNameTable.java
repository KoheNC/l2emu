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
package net.l2emuproject.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.network.serverpackets.L2GameServerPacket;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2Integer;
import net.l2emuproject.util.L2Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class CharNameTable
{
	private static final Log _log = LogFactory.getLog(CharNameTable.class);
	
	public static CharNameTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final Map<Integer, CharacterInfo> _mapByObjectId = new FastMap<Integer, CharacterInfo>().shared();
	private final Map<String, CharacterInfo> _mapByName = new FastMap<String, CharacterInfo>().shared();
	
	private CharNameTable()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			final PreparedStatement statement = con.prepareStatement("SELECT charId, account_name, char_name, accesslevel FROM characters");
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int objectId = rset.getInt("charId");
				final String accountName = rset.getString("account_name");
				final String name = rset.getString("char_name");
				final int accessLevel = rset.getInt("accesslevel");
				
				update(objectId, accountName, name, accessLevel);
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		_log.info(getClass().getSimpleName() + " : Loaded " + _mapByObjectId.size() + " character name(s).");
	}
	
	public final String getNameByObjectId(Integer objectId)
	{
		final CharacterInfo characterInfo = _mapByObjectId.get(objectId);
		
		return characterInfo == null ? null : characterInfo._name;
	}
	
	public final String getNameByName(String name)
	{
		final CharacterInfo characterInfo = _mapByName.get(name.toLowerCase());
		
		return characterInfo == null ? null : characterInfo._name;
	}
	
	public final Integer getObjectIdByName(String name)
	{
		final CharacterInfo characterInfo = _mapByName.get(name.toLowerCase());
		
		return characterInfo == null ? null : characterInfo._objectId;
	}
	
	public final int getAccessLevelByObjectId(Integer objectId)
	{
		final CharacterInfo characterInfo = _mapByObjectId.get(objectId);
		
		return characterInfo == null ? 0 : characterInfo._accessLevel;
	}
	
	public final int getAccessLevelByName(String name)
	{
		final CharacterInfo characterInfo = _mapByName.get(name.toLowerCase());
		
		return characterInfo == null ? 0 : characterInfo._accessLevel;
	}
	
	public final void update(L2Player player)
	{
		update(player.getObjectId(), player.getAccountName(), player.getName(), player.getAccessLevel());
	}
	
	public final void update(int objectId, String accountName, String name, int accessLevel)
	{
		CharacterInfo characterInfo = _mapByObjectId.get(objectId);
		if (characterInfo == null)
			characterInfo = new CharacterInfo(objectId);
		
		characterInfo.updateNames(accountName, name, accessLevel);
	}
	
	public final ICharacterInfo getICharacterInfoByObjectId(Integer objectId)
	{
		final L2Player player = L2World.getInstance().getPlayer(objectId);
		
		if (player != null)
			return player;
		
		return _mapByObjectId.get(objectId);
	}
	
	public final ICharacterInfo getICharacterInfoByName(String name)
	{
		final L2Player player = L2World.getInstance().getPlayer(name);
		
		if (player != null)
			return player;
		
		return _mapByName.get(name.toLowerCase());
	}
	
	public interface ICharacterInfo
	{
		public Integer getObjectId();
		
		public String getAccountName();
		
		public String getName();
		
		public int getAccessLevel();
		
		public boolean isGM();
		
		public void sendPacket(L2GameServerPacket gsp);
	}
	
	private final class CharacterInfo implements ICharacterInfo
	{
		private final Integer _objectId;
		
		private String _accountName;
		private String _name;
		private int _accessLevel;
		
		@Override
		public final Integer getObjectId()
		{
			return _objectId;
		}
		
		@Override
		public final String getAccountName()
		{
			return _accountName;
		}
		
		@Override
		public final String getName()
		{
			return _name;
		}
		
		@Override
		public final int getAccessLevel()
		{
			return _accessLevel;
		}
		
		@Override
		public final boolean isGM()
		{
			return getAccessLevel() >= Config.GM_MIN;
		}
		
		@Override
		public final void sendPacket(L2GameServerPacket gsp)
		{
			// do nothing
		}
		
		private CharacterInfo(int objectId)
		{
			_objectId = L2Integer.valueOf(objectId);
			
			CharacterInfo characterInfo = _mapByObjectId.put(_objectId, this);
			if (characterInfo != null)
				_log.warn("CharNameTable: Duplicated objectId: [" + this + "] - [" + characterInfo + "]");
		}
		
		private final void updateNames(String accountName, String name, int accessLevel)
		{
			_accountName = accountName;
			
			if (_name != null)
				_mapByName.remove(_name.toLowerCase());
			
			_name = name.intern();
			
			final CharacterInfo characterInfo = _mapByName.put(_name.toLowerCase(), this);
			if (characterInfo != null)
				_log.warn("CharNameTable: Duplicated hashName: [" + this + "] - [" + characterInfo + "]");
			
			_accessLevel = accessLevel;
		}
		
		@Override
		public final String toString()
		{
			return "objectId: " + _objectId + ", accountName: " + _accountName + ", name: " + _name;
		}
	}
	
	public final boolean doesCharNameExist(String name)
	{
		return getObjectIdByName(name) != null;
	}
	
	public final int accountCharNumber(String account)
	{
		int count = 0;
		
		for (CharacterInfo characterInfo : _mapByObjectId.values())
			if (characterInfo._accountName.equalsIgnoreCase(account))
				count++;
		
		return count;
	}
	
	public final Iterable<Integer> getObjectIdsForAccount(final String account)
	{
		return L2Collections.convertingIterable(_mapByObjectId.values(),
				new L2Collections.Converter<CharacterInfo, Integer>() {
					@Override
					public Integer convert(CharacterInfo characterInfo)
					{
						if (characterInfo._accountName.equalsIgnoreCase(account))
							return characterInfo._objectId;
						
						return null;
					}
				});
	}
	
	@SuppressWarnings("synthetic-access")
	private static final class SingletonHolder
	{
		private static final CharNameTable _instance = new CharNameTable();
	}
}
