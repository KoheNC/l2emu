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

import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * This class handles following admin commands:
 * - help path = shows /data/html/admin/path file to char, should not be used by GM's directly
 * 
 * @version $Revision: 1.2.4.3 $ $Date: 2005/04/11 10:06:02 $
 */
public final class AdminHelpPage implements IAdminCommandHandler
{
	public static final String		ADMIN_HELP_PAGE	= "data/html/admin/";
	private static final String[]	ADMIN_COMMANDS	=
													{ "admin_help" };

	@Override
	public final boolean useAdminCommand(String command, L2Player activeChar)
	{
		if (command.startsWith("admin_help"))
		{
			try
			{
				String val = command.substring(11);
				activeChar.showHTMLFile(ADMIN_HELP_PAGE + val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				//case of empty filename
			}
		}

		return true;
	}

	@Override
	public final String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
