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

import net.l2emuproject.gameserver.status.GameStatusCommand;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class IP extends GameStatusCommand
{
	public IP()
	{
		super("gets IP of player <name>", "ip");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "name";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		try
		{
			L2Player player = L2World.getInstance().getPlayer(params);
			if (player != null)
			{
				try
				{
					println("IP of " + player + ": " + player.getClient().getHostAddress());
				}
				catch (RuntimeException e)
				{
					println(e.toString());
				}
			}
			else
			{
				println("No player online with that name!");
			}
		}
		catch (StringIndexOutOfBoundsException e)
		{
			println("Please enter player name to get IP");
		}
	}
}
