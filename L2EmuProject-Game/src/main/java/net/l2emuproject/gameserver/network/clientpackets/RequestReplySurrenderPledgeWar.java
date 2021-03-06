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

import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.world.object.L2Player;

public class RequestReplySurrenderPledgeWar extends L2GameClientPacket
{
	private static final String	_C__52_REQUESTREPLYSURRENDERPLEDGEWAR	= "[C] 52 RequestReplySurrenderPledgeWar";

	int							_answer;

	@Override
	protected void readImpl()
	{
		readS();
		_answer = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Player requestor = activeChar.getActiveRequester();
		if (requestor == null)
		{
			sendAF();
			return;
		}

		//TODO: is this incomplete?
		if (_answer == 1)
		{
			requestor.deathPenalty(false, false, false);
			ClanTable.getInstance().deleteclanswars(requestor.getClanId(), activeChar.getClanId());
		}
		else
		{
		}

		activeChar.onTransactionRequest(null);

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__52_REQUESTREPLYSURRENDERPLEDGEWAR;
	}
}
