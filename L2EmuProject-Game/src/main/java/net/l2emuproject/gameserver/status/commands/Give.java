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
package net.l2emuproject.gameserver.status.commands;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;

import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.status.GameStatusCommand;
import net.l2emuproject.gameserver.system.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.idfactory.IdFactory;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class Give extends GameStatusCommand
{
	public Give()
	{
		super("", "give");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "player itemid amount";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		StringTokenizer st = new StringTokenizer(params);
		
		String playername = st.nextToken();
		try
		{
			L2Player player = L2World.getInstance().getPlayer(playername);
			int itemId = Integer.parseInt(st.nextToken());
			int amount = Integer.parseInt(st.nextToken());
			
			if (player != null)
			{
				L2ItemInstance item = player.getInventory().addItem("Status-Give", itemId, amount, null, null);
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(item);
				player.sendPacket(iu);
				if (item.getCount() > 1)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
					sm.addItemName(item);
					sm.addItemNumber(item.getCount());
					player.sendPacket(sm);
				}
				else if (item.getEnchantLevel() > 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item);
					player.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
					sm.addItemName(item);
					player.sendPacket(sm);
				}
				println("ok - was online");
			}
			else
			{
				Integer playerId = CharNameTable.getInstance().getObjectIdByName(playername);
				if (playerId != null)
				{
					java.sql.Connection con = null;
					con = L2DatabaseFactory.getInstance().getConnection(con);
					addItemToInventory(con, playerId, IdFactory.getInstance().getNextId(), itemId, amount, 0);
					println("ok - was offline");
				}
				else
				{
					println("player not found");
				}
			}
		}
		catch (Exception e)
		{
			
		}
	}
	
	private void addItemToInventory(java.sql.Connection con, int charId, int objectId, int currency, long count,
			int enchantLevel) throws SQLException
	{
		PreparedStatement statement = con
				.prepareStatement("INSERT INTO items (owner_id, object_id, item_id, count, enchant_level, loc, loc_data) VALUES (?,?,?,?,?,?,?)");
		statement.setInt(1, charId);
		statement.setInt(2, objectId);
		statement.setInt(3, currency);
		statement.setLong(4, count);
		statement.setInt(5, enchantLevel);
		statement.setString(6, "INVENTORY");
		statement.setInt(7, 0);
		statement.execute();
		statement.close();
	}
}
