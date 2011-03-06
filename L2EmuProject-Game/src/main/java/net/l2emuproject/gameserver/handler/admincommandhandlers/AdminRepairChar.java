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
package net.l2emuproject.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;

import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AdminRepairChar implements IAdminCommandHandler
{
	private final static Log _log = LogFactory.getLog(AdminRepairChar.class);
	
	private static final String[] ADMIN_COMMANDS =
		{ "admin_restore", "admin_repair" };
	
	@Override
	public boolean useAdminCommand(String command, L2Player activeChar)
	{
		handleRepair(command, activeChar);
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleRepair(String command, L2Player activeChar)
	{
		String[] parts = command.split(" ");
		if (parts.length != 2)
			return;
		
		final Integer objId = CharNameTable.getInstance().getObjectIdByName(parts[1]);
		
		if (objId == 0)
			return;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=17867, y=170259, z=-3450 WHERE charId=?");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=? AND loc=\"PAPERDOLL\"");
			statement.setInt(1, objId);
			statement.execute();
			statement.close();
			
			activeChar.sendMessage("Character " + parts[1] + " got repaired.");
		}
		catch (Exception e)
		{
			_log.warn("Could not repair character: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
