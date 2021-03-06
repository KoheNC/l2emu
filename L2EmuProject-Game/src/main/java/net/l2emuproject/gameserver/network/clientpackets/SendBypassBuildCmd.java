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
package net.l2emuproject.gameserver.network.clientpackets;

import net.l2emuproject.gameserver.handler.AdminCommandHandler;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Client send this packet if it's in builder mode and //text
 * was written in the chat window.
 */
public final class SendBypassBuildCmd extends L2GameClientPacket
{
	private static final String	_C__SENDBYPASSBUILDCMD	= "[C] 74 SendBypassBuildCmd c[s]";

	private String				_command;

	@Override
	protected void readImpl()
	{
		_command = readS();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		AdminCommandHandler.getInstance().useAdminCommand(activeChar, "admin_" + _command);

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__SENDBYPASSBUILDCMD;
	}
}
