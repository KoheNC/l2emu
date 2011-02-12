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
package net.l2emuproject.gameserver.handler.admincommandhandlers;

import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author lord_rex
 */
public final class AdminHide implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{ "admin_hide" };

	@Override
	public final boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_hide on"))
			activeChar.setHiding(true, false);
		else if (command.startsWith("admin_hide off"))
			activeChar.setHiding(false, false);

		return true;
	}

	@Override
	public final String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
