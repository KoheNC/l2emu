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

import net.l2emuproject.gameserver.handler.IUserCommandHandler;
import net.l2emuproject.gameserver.handler.UserCommandHandler;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.world.object.L2Player;

public class BypassUserCmd extends L2GameClientPacket
{
	private static final String	_C__BYPASSUSERCMD = "[C] B3 BypassUserCmd c[d]";

	private int					_command;

	@Override
	protected void readImpl()
	{
		_command = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getActiveChar();
		if (player == null)
			return;

		IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(_command);
		if (handler == null)
			sendPacket(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
		else
			handler.useUserCommand(_command, player);

		// no AF here
	}

	@Override
	public String getType()
	{
		return _C__BYPASSUSERCMD;
	}
}
