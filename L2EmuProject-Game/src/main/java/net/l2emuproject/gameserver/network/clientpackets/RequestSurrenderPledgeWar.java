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
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.world.object.L2Player;

public class RequestSurrenderPledgeWar extends L2GameClientPacket
{
	private static final String	_C__51_REQUESTSURRENDERPLEDGEWAR	= "[C] 51 RequestSurrenderPledgeWar";

	private String				_pledgeName;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			requestFailed(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
			return;
		}

		L2Clan warClan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (warClan == null)
		{
			requestFailed(SystemMessageId.CLAN_DOESNT_EXISTS);
			return;
		}

		_log.info("RequestSurrenderPledgeWar by " + clan.getName() + " with " + _pledgeName);

		if (!clan.isAtWarWith(warClan.getClanId()))
		{
			requestFailed(new SystemMessage(SystemMessageId.NO_CLAN_WAR_AGAINST_CLAN_S1).addString(warClan.getName()));
			return;
		}

		SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN);
		msg.addString(_pledgeName);
		sendPacket(msg);
		activeChar.deathPenalty(false, false, false);
		ClanTable.getInstance().deleteclanswars(clan.getClanId(), warClan.getClanId());

		sendAF();

		/*L2Player leader = L2World.getInstance().getPlayer(clan.getLeaderName());
		if(leader != null && leader.isOnline() == 0)
		{
		   _activeChar.sendMessage("Clan leader isn't online.");
		    _activeChar.sendPacket(new ActionFailed());
		    return;
		}
		
		if (leader.isTransactionInProgress())
		{
		    SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
		    sm.addString(leader.getName());
		    _activeChar.sendPacket(sm);
		    return;
		}
		
		leader.setTransactionRequester(player);
		_activeChar.setTransactionRequester(leader);
		leader.sendPacket(new SurrenderPledgeWar(_clan.getName(),_activeChar.getName()));*/
	}

	@Override
	public String getType()
	{
		return _C__51_REQUESTSURRENDERPLEDGEWAR;
	}
}
