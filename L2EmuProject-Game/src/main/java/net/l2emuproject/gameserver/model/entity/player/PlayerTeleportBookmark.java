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
package net.l2emuproject.gameserver.model.entity.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.l2emuproject.L2DatabaseFactory;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.util.SingletonList;

public final class PlayerTeleportBookmark extends PlayerExtension
{
	private static final String			INSERT_TP_BOOKMARK	= "INSERT INTO character_tpbookmark (charId,Id,x,y,z,icon,tag,name) values (?,?,?,?,?,?,?,?)";
	private static final String			UPDATE_TP_BOOKMARK	= "UPDATE character_tpbookmark SET icon=?,tag=?,name=? where charId=? AND Id=?";
	private static final String			RESTORE_TP_BOOKMARK	= "SELECT Id,x,y,z,icon,tag,name FROM character_tpbookmark WHERE charId=?";
	private static final String			DELETE_TP_BOOKMARK	= "DELETE FROM character_tpbookmark WHERE charId=? AND Id=?";

	public int							_bookmarkslot		= 0;																							// The Teleport Bookmark Slot
	public final List<TeleportBookmark>	tpbookmark			= new SingletonList<TeleportBookmark>();

	public PlayerTeleportBookmark(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	public static final class TeleportBookmark
	{
		public int	_id, _x, _y, _z, _icon;
		public String	_name, _tag;

		public TeleportBookmark(int id, int x, int y, int z, int icon, String tag, String name)
		{
			_id = id;
			_x = x;
			_y = y;
			_z = z;
			_icon = icon;
			_name = name;
			_tag = tag;
		}
	}

	public final void teleportBookmarkModify(int Id, int icon, String tag, String name)
	{
		int count = 0;
		int size = tpbookmark.size();
		while (size > count)
		{
			if (tpbookmark.get(count)._id == Id)
			{
				tpbookmark.get(count)._icon = icon;
				tpbookmark.get(count)._tag = tag;
				tpbookmark.get(count)._name = name;

				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(UPDATE_TP_BOOKMARK);

					statement.setInt(1, icon);
					statement.setString(2, tag);
					statement.setString(3, name);
					statement.setInt(4, getPlayer().getObjectId());
					statement.setInt(5, Id);

					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.error("Could not update character teleport bookmark data.", e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
			}
			count++;
		}
		getPlayer().sendPacket(new ExGetBookMarkInfoPacket(getPlayer()));
	}

	public final void teleportBookmarkDelete(int Id)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_TP_BOOKMARK);

			statement.setInt(1, getPlayer().getObjectId());
			statement.setInt(2, Id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not delete character teleport bookmark data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		int count = 0;
		int size = tpbookmark.size();

		while (size > count)
		{
			if (tpbookmark.get(count)._id == Id)
			{
				tpbookmark.remove(count);
				break;
			}
			count++;
		}

		getPlayer().sendPacket(new ExGetBookMarkInfoPacket(getPlayer()));
	}

	public final void teleportBookmarkGo(int Id)
	{
		if (!teleportBookmarkCondition(0))
			return;
		if (getPlayer().getInventory().getInventoryItemCount(20025, 0) == 0)
		{
			getPlayer().sendPacket(SystemMessageId.CANNOT_TELEPORT_WITHOUT_TELEPORT_ITEM);
			return;
		}
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(20025);
		getPlayer().sendPacket(sm);

		int count = 0;
		int size = tpbookmark.size();
		while (size > count)
		{
			if (tpbookmark.get(count)._id == Id)
			{
				getPlayer().destroyItem("Consume", getPlayer().getInventory().getItemByItemId(20025).getObjectId(), 1, null, false);
				getPlayer().teleToLocation(tpbookmark.get(count)._x, tpbookmark.get(count)._y, tpbookmark.get(count)._z);
				break;
			}
			count++;
		}
		getPlayer().sendPacket(new ExGetBookMarkInfoPacket(getPlayer()));
	}

	public final boolean teleportBookmarkCondition(int type)
	{
		if (getPlayer().isInCombat())
		{
			getPlayer().sendPacket(SystemMessageId.UNNAMED_2348);
			return false;
		}
		else if (getPlayer().isInSiege() || getPlayer().getSiegeState() != 0)
		{
			getPlayer().sendPacket(SystemMessageId.UNNAMED_2349);
			return false;
		}
		else if (getPlayer().getPlayerDuel().isInDuel())
		{
			getPlayer().sendPacket(SystemMessageId.UNNAMED_2350);
			return false;
		}
		else if (getPlayer().isFlying())
		{
			getPlayer().sendPacket(SystemMessageId.UNNAMED_2351);
			return false;
		}
		else if (getPlayer().getPlayerOlympiad().isInOlympiadMode())
		{
			getPlayer().sendPacket(SystemMessageId.UNNAMED_2352);
			return false;
		}
		else if (getPlayer().isParalyzed())
		{
			getPlayer().sendPacket(SystemMessageId.UNNAMED_2353);
			return false;
		}
		else if (getPlayer().isDead())
		{
			getPlayer().sendPacket(SystemMessageId.UNNAMED_2354);
			return false;
		}
		else if (getPlayer().isInBoat() || getPlayer().isInAirShip() || getPlayer().isInJail() || getPlayer().isInsideZone(L2Zone.FLAG_NOSUMMON))
		{
			if (type == 0)
				getPlayer().sendPacket(SystemMessageId.UNNAMED_2355);
			else if (type == 1)
				getPlayer().sendPacket(SystemMessageId.UNNAMED_2410);
			return false;
		}
		else if (getPlayer().isInWater())
		{
			getPlayer().sendPacket(SystemMessageId.UNNAMED_2356);
			return false;
		}
		/* TODO: Instant Zone still not implement
		else if (this.isInsideZone(ZONE_INSTANT))
		{
			sendPacket(SystemMessageId.UNNAMED_2357);
			return;
		}
		 */
		else
			return true;
	}

	public final void teleportBookmarkAdd(int x, int y, int z, int icon, String tag, String name)
	{
		if (!teleportBookmarkCondition(1))
			return;

		if (tpbookmark.size() >= _bookmarkslot)
		{
			getPlayer().sendPacket(SystemMessageId.UNNAMED_2358);
			return;
		}

		if (getPlayer().getInventory().getInventoryItemCount(20033, 0) == 0)
		{
			getPlayer().sendPacket(SystemMessageId.YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG);
			return;
		}

		int count = 0;
		int id = 1;
		List<Integer> idlist = new ArrayList<Integer>();

		int size = tpbookmark.size();
		while (size > count)
		{
			idlist.add(tpbookmark.get(count)._id);
			count++;
		}

		for (int i = 1; i < 10; i++)
		{
			if (!idlist.contains(i))
			{
				id = i;
				break;
			}
		}

		TeleportBookmark tpadd = new TeleportBookmark(id, x, y, z, icon, tag, name);
		tpbookmark.add(tpadd);

		getPlayer().destroyItem("Consume", getPlayer().getInventory().getItemByItemId(20033).getObjectId(), 1, null, false);

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(20033);
		getPlayer().sendPacket(sm);

		Connection con = null;
		try
		{

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_TP_BOOKMARK);

			statement.setInt(1, getPlayer().getObjectId());
			statement.setInt(2, id);
			statement.setInt(3, x);
			statement.setInt(4, y);
			statement.setInt(5, z);
			statement.setInt(6, icon);
			statement.setString(7, tag);
			statement.setString(8, name);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not insert character teleport bookmark data.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		getPlayer().sendPacket(new ExGetBookMarkInfoPacket(getPlayer()));
	}

	public final void restoreTeleportBookmark()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_TP_BOOKMARK);
			statement.setInt(1, getPlayer().getObjectId());
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				tpbookmark.add(new TeleportBookmark(rset.getInt("Id"), rset.getInt("x"), rset.getInt("y"), rset.getInt("z"), rset.getInt("icon"), rset
						.getString("tag"), rset.getString("name")));
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Failed restoring character teleport bookmark.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final int getBookMarkSlot()
	{
		return _bookmarkslot;
	}

	public final void setBookMarkSlot(int slot)
	{
		_bookmarkslot = slot;
		getPlayer().sendPacket(new ExGetBookMarkInfoPacket(getPlayer()));
	}
}
