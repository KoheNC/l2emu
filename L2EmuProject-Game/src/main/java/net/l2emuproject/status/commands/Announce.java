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
package net.l2emuproject.status.commands;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.Announcements;
import net.l2emuproject.status.GameStatusCommand;

public final class Announce extends GameStatusCommand
{
	public Announce()
	{
		super("announces <text> in game", "announce");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "text";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		try
		{
			if (Config.ALT_TELNET && Config.ALT_TELNET_GM_ANNOUNCER_NAME)
				params += " [" + getStatusThread().getGM() + "(offline)]";
			Announcements.getInstance().announceToAll(params);
			println("Announcement Sent!");
		}
		catch (StringIndexOutOfBoundsException e)
		{
			println("Please Enter Some Text To Announce!");
		}
	}
}
