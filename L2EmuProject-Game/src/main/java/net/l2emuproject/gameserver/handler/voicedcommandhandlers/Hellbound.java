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
import net.l2emuproject.gameserver.manager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class Hellbound implements IVoicedCommandHandler
{
	private static final String[]	VOICED_COMMANDS	=
													{ "hellbound" };

	@Override
	public final boolean useVoicedCommand(String command, L2Player activeChar, String params)
	{
		if (!HellboundManager.getInstance().isWarpgateActive())
		{
			activeChar.sendMessage("Hellbound is locked.");
			return true;
		}

		activeChar.sendMessage("Hellbound level: " + HellboundManager.getInstance().getHellboundLevel() + " trust: "
				+ HellboundManager.getInstance().getTrustPoints());
		return true;
	}

	@Override
	public final String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
