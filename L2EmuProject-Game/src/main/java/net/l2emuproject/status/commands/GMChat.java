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

import net.l2emuproject.gameserver.datatables.GmListTable;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.status.GameStatusCommand;

public final class GMChat extends GameStatusCommand
{
	public GMChat()
	{
		super("sends a message to all GMs with <text>", "gmchat");
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
			CreatureSay cs = new CreatureSay(0, SystemChatChannelId.Chat_Alliance, "Telnet GM Broadcast from "
					+ getHostAddress(), params);
			GmListTable.broadcastToGMs(cs);
			println("Your Message Has Been Sent To " + GmListTable.getAllGms(true).size() + " GM(s).");
		}
		catch (StringIndexOutOfBoundsException e)
		{
			println("Please Enter Some Text To Announce!");
		}
	}
}
