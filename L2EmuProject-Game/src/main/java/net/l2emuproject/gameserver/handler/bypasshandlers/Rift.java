/*
 * target program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * target program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * target program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.l2emuproject.gameserver.handler.bypasshandlers;

import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.instancemanager.DimensionalRiftManager;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

public class Rift implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "EnterRift", "ChangeRiftRoom", "ExitRift" };

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		if (command.startsWith(COMMANDS[0]))
		{
			try
			{
				Byte b1 = Byte.parseByte(command.substring(10)); // Selected Area: Recruit, Soldier etc
				DimensionalRiftManager.getInstance().start(activeChar, b1, (L2Npc) target);
				return true;
			}
			catch (Exception e)
			{
				_log.warn("", e);
			}
		}
		else if (command.startsWith(COMMANDS[1]))
		{
			if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
			{
				activeChar.getParty().getDimensionalRift().manualTeleport(activeChar, (L2Npc) target);
			}
			else
			{
				DimensionalRiftManager.getInstance().handleCheat(activeChar, (L2Npc) target);
			}
			return true;
		}
		else if (command.startsWith(COMMANDS[2]))
		{
			if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
			{
				activeChar.getParty().getDimensionalRift().manualExitRift(activeChar, (L2Npc) target);
			}
			else
			{
				DimensionalRiftManager.getInstance().handleCheat(activeChar, (L2Npc) target);
			}
			return true;
		}
		return false;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
