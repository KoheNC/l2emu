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

import net.l2emuproject.gameserver.Shutdown;
import net.l2emuproject.gameserver.Shutdown.DisableType;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExAskJoinPartyRoom;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.party.L2PartyRoom;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Format: (ch) S
 * @author -Wooden- (format)
 * @author Myzreal (implementation)
 */
public class RequestAskJoinPartyRoom extends L2GameClientPacket
{
    private static final String _C__D0_2F_REQUESTASKJOINPARTYROOM = "[C] D0:2F RequestAskJoinPartyRoom";

    private String _name;

    @Override
    protected void readImpl()
    {
    	_name = readS();
    }

    @Override
    protected void runImpl()
    {
    	L2Player activeChar = getActiveChar();
    	if (activeChar == null)
    		return;

    	if (Shutdown.isActionDisabled(DisableType.PC_ITERACTION))
        {
        	requestFailed(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
            return;
        }

    	L2PartyRoom room = activeChar.getPartyRoom();
    	L2Player target = L2World.getInstance().getPlayer(_name);
    	if (target == null || target == activeChar || room == null)
    	{
    		sendAF();
    		return;
    	}
    	else if (target.getPartyRoom() != null)
    	{
    		requestFailed(new SystemMessage(SystemMessageId.C1_NOT_MEET_CONDITIONS_FOR_PARTY_ROOM).addString(_name));
    		return;
    	}
    	else if (activeChar != room.getLeader())
    	{
    		requestFailed(SystemMessageId.ONLY_ROOM_LEADER_CAN_INVITE);
    		return;
    	}
    	else if (room.getMemberCount() >= room.getMaxMembers())
    	{
    		requestFailed(SystemMessageId.PARTY_ROOM_FULL);
    		return;
    	}
    	else if (activeChar.isProcessingRequest())
    	{
    		requestFailed(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
    		return;
    	}
    	else if (target.isProcessingRequest())
    	{
    		requestFailed(new SystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(_name));
    		return;
    	}

    	activeChar.onTransactionRequest(target);
		target.sendPacket(new SystemMessage(SystemMessageId.C1_INVITED_YOU_TO_PARTY_ROOM).addPcName(activeChar));
    	target.sendPacket(new ExAskJoinPartyRoom(activeChar.getName()));

		sendAF();
    }

    @Override
    public String getType()
    {
        return _C__D0_2F_REQUESTASKJOINPARTYROOM;
    }
}
