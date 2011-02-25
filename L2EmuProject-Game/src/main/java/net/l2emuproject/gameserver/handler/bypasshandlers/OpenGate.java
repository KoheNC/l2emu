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
package net.l2emuproject.gameserver.handler.bypasshandlers;

import java.util.StringTokenizer;

import net.l2emuproject.gameserver.datatables.DoorTable;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

public class OpenGate implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "open_gate" };

	@Override
	public boolean useBypass(String command, L2Player activeChar, L2Character target)
	{
		try
		{
			final DoorTable doorTable = DoorTable.getInstance();
			int doorId;

			StringTokenizer st = new StringTokenizer(command.substring(10), ", ");

			while (st.hasMoreTokens())
			{
				doorId = Integer.parseInt(st.nextToken());

				if (doorTable.getDoor(doorId) != null)
				{
					doorTable.getDoor(doorId).openMe();
					doorTable.getDoor(doorId).onOpen();
				}
				else
				{
					_log.warn("Door Id does not exist.(" + doorId + ")");
				}
			}
			return true;
		}
		catch (Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName());
		}
		return false;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
