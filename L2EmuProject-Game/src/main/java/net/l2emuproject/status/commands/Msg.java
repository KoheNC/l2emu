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

import java.util.StringTokenizer;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.status.GameStatusCommand;

public final class Msg extends GameStatusCommand
{
	public Msg()
	{
		super("sends a whisper to char <nick> with <text>", "msg");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "nick text";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		try
		{
			String val = params;
			StringTokenizer st = new StringTokenizer(val);
			String name = st.nextToken();
			String message = val.substring(name.length() + 1);
			L2PcInstance reciever = L2World.getInstance().getPlayer(name);
			CreatureSay cs = new CreatureSay(0, SystemChatChannelId.Chat_Tell, "Telnet Priv", message);
			if (Config.ALT_TELNET)
				cs = new CreatureSay(0, SystemChatChannelId.Chat_Tell, getStatusThread().getGM() + "(offline)", message);
			if (reciever != null)
			{
				reciever.sendPacket(cs);
				println("Telnet Priv->" + name + ": " + message);
				if (Config.ALT_TELNET)
					println(getStatusThread().getGM() + "(offline): " + name + ": " + message);
				println("Message Sent!");
			}
			else
			{
				println("Unable To Find Username: " + name);
			}
		}
		catch (StringIndexOutOfBoundsException e)
		{
			println("Please Enter Some Text!");
		}
	}
}
