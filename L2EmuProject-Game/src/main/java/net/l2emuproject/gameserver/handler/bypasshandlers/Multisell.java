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
import net.l2emuproject.gameserver.model.L2Multisell;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

public class Multisell implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "multisell", "exc_multisell" };

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		try
		{
			if (command.startsWith(COMMANDS[0]))
			{
				int listId = Integer.parseInt(command.substring(9).trim());
				L2Multisell.getInstance().separateAndSend(listId, activeChar, ((L2Npc) target).getNpcId(), false, ((L2Npc) target).getCastle().getTaxRate());
				return true;
			}
			else if (command.startsWith(COMMANDS[1]))
			{
				int listId = Integer.parseInt(command.substring(13).trim());
				L2Multisell.getInstance().separateAndSend(listId, activeChar, ((L2Npc) target).getNpcId(), true, ((L2Npc) target).getCastle().getTaxRate());
				return true;
			}
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
