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

import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.AskJoinAlly;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * This class represents a packet sent by the client when a player requests alliance with
 * another clan.
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinAlly extends L2GameClientPacket
{
	private static final String _C__82_REQUESTJOINALLY	= "[C] 82 RequestJoinAlly";

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null) return;

		if (activeChar.getClan() == null)
		{
			//requestFailed(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
			requestFailed(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return;
		}

		L2Object obj = null;
		// Get object from target
		if (activeChar.getTargetId() == _objectId)
			obj = activeChar.getTarget();
		// Try to get object from world
		if (obj == null)
			obj = L2World.getInstance().getPlayer(_objectId);

		if (!(obj instanceof L2Player))
		{
			requestFailed(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}

		L2Player target = (L2Player) obj;
		if (!L2Clan.checkAllyJoinCondition(activeChar, target) ||
				!activeChar.getRequest().setRequest(target, this))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		SystemMessage sm = new SystemMessage(SystemMessageId.S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE);
		sm.addString(activeChar.getClan().getAllyName());
		sm.addString(activeChar.getName());
		target.sendPacket(sm);
		target.sendPacket(new AskJoinAlly(activeChar.getObjectId(), activeChar.getClan().getAllyName()));
		sendPacket(SystemMessageId.YOU_INVITED_FOR_ALLIANCE);

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__82_REQUESTJOINALLY;
	}
}
