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
import net.l2emuproject.gameserver.instancemanager.CastleManager;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.model.entity.Castle;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.SiegeDefenderList;

/**
 * This class represents a packet sent by the client when a player clicks the "Approve"
 * button in the siege defender list
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestConfirmSiegeWaitingList extends L2GameClientPacket{
    
    private static final String _C__A5_RequestConfirmSiegeWaitingList = "[C] a5 RequestConfirmSiegeWaitingList";

    private int _approved;
    private int _castleId;
    private int _clanId;

    @Override
    protected void readImpl()
    {
        _castleId = readD();
        _clanId = readD();
        _approved = readD();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
        	return;

        L2Clan clan = ClanTable.getInstance().getClan(_clanId);
        // Check if the player has a clan
        if (clan == null || !L2Clan.checkPrivileges(activeChar, L2Clan.CP_CS_MANAGE_SIEGE))
        {
        	requestFailed(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
        	return;
        }

        Castle castle = CastleManager.getInstance().getCastleById(_castleId);
        if (castle == null)
        {
        	requestFailed(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
        	return;
        }
        // Check if leader of the clan who owns the castle?
        else if (castle.getOwnerId() != activeChar.getClanId())
        {
        	sendAF();
        	return;
        }

        if (!castle.getSiege().getIsRegistrationOver())
        {
            if (_approved == 1)
            {
                if (!castle.getSiege().checkIsDefenderWaiting(clan))
                {
                	sendPacket(ActionFailed.STATIC_PACKET);
                	return;
                }
                else
                	castle.getSiege().approveSiegeDefenderClan(_clanId);
            }
            else
            {
                if ((castle.getSiege().checkIsDefenderWaiting(clan)) || (castle.getSiege().checkIsDefender(clan)))
                    castle.getSiege().removeSiegeClan(_clanId);
        	}
    	}
        else
        {
        	requestFailed(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME1);
        	return;
        }

        //Update the defender list
        sendPacket(new SiegeDefenderList(castle));
        sendAF();
    }

    @Override
    public String getType()
    {
        return _C__A5_RequestConfirmSiegeWaitingList;
    }
}
