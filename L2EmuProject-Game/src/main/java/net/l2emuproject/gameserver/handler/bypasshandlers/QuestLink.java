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
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public class QuestLink implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "Quest" };

	@Override
	public boolean useBypass(String command, L2Player activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		String quest = "";
		try
		{
			quest = command.substring(5).trim();
		}
		catch (IndexOutOfBoundsException ioobe)
		{
		}

		if (quest.length() == 0)
			((L2Npc) target).showQuestWindow(activeChar);
		else
			((L2Npc) target).showQuestWindow(activeChar, quest);

		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
