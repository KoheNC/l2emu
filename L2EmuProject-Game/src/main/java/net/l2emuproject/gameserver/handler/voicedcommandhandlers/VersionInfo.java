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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.handler.IVoicedCommandHandler;
import net.l2emuproject.gameserver.services.VersionService;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author evill33t
 * 
 */
public class VersionInfo  implements IVoicedCommandHandler
{
	private static final String[]	VOICED_COMMANDS	=
													{ "version" };

	@Override
	public boolean useVoicedCommand(String command, L2Player activeChar, String target)
	{
		if (command.startsWith("version"))
		{
			activeChar.sendMessage("L2Server: " + VersionService.getGameRevision());
			activeChar.sendMessage("L2DataPack: " + Config.DATAPACK_REVISION);
			return true;
		}
		return false;
	}
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
