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
package net.l2emuproject.gameserver.handler.voicedcommandhandlers;

import net.l2emuproject.gameserver.handler.IVoicedCommandHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.entity.events.TvT;

/**
 * @author lord_rex
 */
public final class TvTCommand implements IVoicedCommandHandler
{
	private static final String[]	COMMANDS	=
												{ "jointvt", "leavetvt" };

	@Override
	public final boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equals("jointvt"))
		{
			TvT.getInstance().registerPlayer(activeChar);
		}
		else if (command.equals("leavetvt"))
		{
			TvT.getInstance().cancelRegistration(activeChar);
		}

		return true;
	}

	@Override
	public final String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}
