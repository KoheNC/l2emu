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

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import net.l2emuproject.gameserver.AutoAnnouncements;
import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author l2jfree / l2jdp team, 
 *     remade for L2EmuProject: lord_rex
 */
public class AdminAutoAnnouncements implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{ "admin_list_autoann", "admin_reload_autoann", "admin_add_autoann", "admin_del_autoann" };

	@Override
	public boolean useAdminCommand(String command, L2Player activeChar)
	{
		if (command.startsWith("admin_list_autoann"))
		{
			AutoAnnouncements.getInstance().listAutoAnnouncements(activeChar);
		}
		else if (command.startsWith("admin_reload_autoann"))
		{
			AutoAnnouncements.getInstance().restore();
			activeChar.sendMessage("AutoAnnouncement Reloaded.");
			AutoAnnouncements.getInstance().listAutoAnnouncements(activeChar);
		}
		else if (command.startsWith("admin_add_autoann"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();

			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Not enough parameters for adding autoannounce!");
				return false;
			}
			long initial = Long.parseLong(st.nextToken());
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Not enough parameters for adding autoannounce!");
				return false;
			}
			long delay = Long.parseLong(st.nextToken());
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Not enough parameters for adding autoannounce!");
				return false;
			}
			int repeat = Integer.parseInt(st.nextToken());
			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Not enough parameters for adding autoannounce!");
				return false;
			}
			TextBuilder memo = new TextBuilder();
			while (st.hasMoreTokens())
			{
				memo.append(st.nextToken());
				memo.append(" ");
			}

			AutoAnnouncements.getInstance().addAutoAnnounce(initial * 1000, delay * 1000, repeat, memo.toString().trim());
			AutoAnnouncements.getInstance().listAutoAnnouncements(activeChar);
		}

		else if (command.startsWith("admin_del_autoann"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();

			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Not enough parameters for deleting autoannounce!");
				return false;
			}

			AutoAnnouncements.getInstance().deleteAutoAnnounce(Integer.parseInt(st.nextToken()));
			AutoAnnouncements.getInstance().listAutoAnnouncements(activeChar);
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
