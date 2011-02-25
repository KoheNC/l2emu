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

import net.l2emuproject.gameserver.handler.AdminCommandHandler;
import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author  poltomb
 */
public class AdminSummon implements IAdminCommandHandler
{
	protected static Log			_log			= LogFactory.getLog(AdminSummon.class);

	public static final String[]	ADMIN_COMMANDS	=
													{ "admin_summon" };

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	@Override
	public boolean useAdminCommand(String command, L2Player activeChar)
	{
		int id;
		int count = 1;
		String[] data = command.split(" ");
		try
		{
			id = Integer.parseInt(data[1]);
			if (data.length > 2)
			{
				count = Integer.parseInt(data[2]);
			}
		}
		catch (NumberFormatException nfe)
		{
			activeChar.sendMessage("Incorrect format for command 'summon'");
			return false;
		}

		String subCommand;
		if (id < 1000000)
		{
			subCommand = "admin_create_item";
			AdminCommandHandler.getInstance().useAdminCommand(activeChar, subCommand + " " + id + " " + count);
		}
		else
		{
			subCommand = "admin_spawn";
			activeChar.sendMessage("This is only a temporary spawn.  The mob(s) will NOT respawn.");
			id -= 1000000;
			AdminCommandHandler.getInstance().useAdminCommand(activeChar, subCommand + " " + id + " " + count);
		}
		return true;
	}
}
