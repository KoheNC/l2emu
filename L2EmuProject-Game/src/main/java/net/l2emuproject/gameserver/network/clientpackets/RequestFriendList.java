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

import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.world.L2World;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendList extends L2GameClientPacket
{
	private static final String _C__60_REQUESTFRIENDLIST = "[C] 60 RequestFriendList";

	/**
	 * packet type id 0x60
	 * format: c
	 */
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getActiveChar();
		if (activeChar == null) return;

		sendPacket(SystemMessageId.FRIEND_LIST_HEADER);

		SystemMessage sm;
		for (Integer objId : activeChar.getFriendList().getFriendIds())
		{
			L2PcInstance friend = L2World.getInstance().findPlayer(objId);
			if (friend == null)
			{
				sm = new SystemMessage(SystemMessageId.S1_OFFLINE);
				sm.addString(CharNameTable.getInstance().getNameByObjectId(objId));
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_ONLINE);
				sm.addPcName(friend);
			}
			sendPacket(sm);
		}

		sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__60_REQUESTFRIENDLIST;
	}
}
