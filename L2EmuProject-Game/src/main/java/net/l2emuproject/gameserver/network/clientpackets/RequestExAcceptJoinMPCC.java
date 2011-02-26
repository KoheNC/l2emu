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

import net.l2emuproject.gameserver.entity.party.L2CommandChannel;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author -Wooden-
 */
public class RequestExAcceptJoinMPCC extends L2GameClientPacket
{
	private static final String _C__D0_0E_REQUESTEXASKJOINMPCC = "[C] D0:0E RequestExAcceptJoinMPCC";

	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if (player == null) return;
		L2Player requestor = player.getActiveRequester();
        if (requestor == null)
        {
        	sendPacket(ActionFailed.STATIC_PACKET);
        	return;
        }

		SystemMessage sm;
		if (_response == 1)
		{
			boolean newCc = false;
			if (!requestor.getParty().isInCommandChannel())
			{
				new L2CommandChannel(requestor); // Create new CC
				newCc = true;
			}
			requestor.getParty().getCommandChannel().addParty(player.getParty());
			if (!newCc)
			{
				sm = SystemMessageId.JOINED_COMMAND_CHANNEL.getSystemMessage();
				player.getParty().broadcastToPartyMembers(sm);
			}
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.S1_DECLINED_CHANNEL_INVITATION);
			sm.addString(player.getName());
			requestor.sendPacket(sm);
		}
		sm = null;

		sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}

	@Override
	public String getType()
	{
		return _C__D0_0E_REQUESTEXASKJOINMPCC;
	}
}
