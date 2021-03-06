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
package net.l2emuproject.gameserver.handler.usercommandhandlers;

import net.l2emuproject.gameserver.handler.IUserCommandHandler;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.services.party.L2CommandChannel;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 *
 * @author Chris
 */
public class ChannelDelete implements IUserCommandHandler
{
	private static final int[]	COMMAND_IDS	=
											{ 93 };

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.l2emuproject.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useUserCommand(int id, L2Player activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;

		if (activeChar.isInParty())
		{
			if (activeChar.getParty().isLeader(activeChar) && activeChar.getParty().isInCommandChannel()
					&& activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar)
			{
				L2CommandChannel channel = activeChar.getParty().getCommandChannel();
				channel.broadcastToChannelMembers(SystemMessageId.COMMAND_CHANNEL_DISBANDED.getSystemMessage());
				channel.disbandChannel();
				return true;
			}

			activeChar.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_LEAVE_CHANNEL);
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
