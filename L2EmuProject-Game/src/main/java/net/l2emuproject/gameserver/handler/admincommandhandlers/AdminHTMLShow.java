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

import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author lord_rex
 */
public final class AdminHTMLShow implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{ "admin_html_show_message", "admin_html_show_file" };

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		L2PcInstance target = (L2PcInstance) activeChar.getTarget();
		if (target instanceof L2PcInstance)
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();

			try
			{
				if (command.startsWith("admin_html_show_message"))
				{
					String message = command.substring(23);
					target.showHTMLMessage(message);
					activeChar.sendMessage("HTML message sent: " + message + " to " + target.getName());
				}
				else if (command.startsWith("admin_html_show_file"))
				{
					target.showHTMLFile(st.nextToken());
					activeChar.sendMessage("HTML file sent: " + target.getName());
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("" + e);
			}
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
