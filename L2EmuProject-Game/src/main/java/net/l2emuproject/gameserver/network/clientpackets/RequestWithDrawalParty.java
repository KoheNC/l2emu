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

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;

public final class RequestWithDrawalParty extends L2GameClientPacket
{
	private static final String	_C__44_REQUESTWITHDRAWALPARTY	= "[C] 44 RequestWithDrawalParty";

	@Override
	protected final void readImpl()
	{
		// trigger
	}

	@Override
	protected final void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (player.isInParty())
			if (player.getParty().isInDimensionalRift() && !player.getParty().getDimensionalRift().getRevivedAtWaitingRoom().contains(player))
				sendPacket(SystemMessageId.COULD_NOT_LEAVE_PARTY);
			else
				player.getParty().removePartyMember(player, false);

		sendAF();
	}
	
	@Override
	public final String getType()
	{
		return _C__44_REQUESTWITHDRAWALPARTY;
	}
}
