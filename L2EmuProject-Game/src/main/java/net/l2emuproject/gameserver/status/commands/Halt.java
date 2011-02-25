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

import net.l2emuproject.gameserver.Shutdown;
import net.l2emuproject.gameserver.status.GameStatusCommand;

public final class Halt extends GameStatusCommand
{
	public Halt()
	{
		super("halts the server", "halt");
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		try
		{
			print("Halting...");
			Shutdown.halt(getHostAddress());
		}
		finally
		{
			println("\t\t[OK]");
		}
	}
}
