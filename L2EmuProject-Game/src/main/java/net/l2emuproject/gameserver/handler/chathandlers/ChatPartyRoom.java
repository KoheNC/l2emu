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
package net.l2emuproject.gameserver.handler.chathandlers;

import net.l2emuproject.gameserver.handler.IChatHandler;
import net.l2emuproject.gameserver.model.L2CommandChannel;
import net.l2emuproject.gameserver.model.L2Party;
import net.l2emuproject.gameserver.model.L2PartyRoom;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;

/**
 *
 * @author Noctarius
 */
public class ChatPartyRoom implements IChatHandler
{
	private final SystemChatChannelId[]	_chatTypes	=
												{ SystemChatChannelId.Chat_Party_Room };

	@Override
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	@Override
	public void useChatHandler(L2PcInstance activeChar, String target, SystemChatChannelId chatType, String text)
	{
		if (activeChar == null)
			return;

		String charName = activeChar.getName();
		int charObjId = activeChar.getObjectId();

		L2PartyRoom room = activeChar.getPartyRoom();
		if (room != null)
		{
			CreatureSay cs = new CreatureSay(charObjId, chatType, charName, text);
			room.broadcastPacket(cs);
		}

		// According to ChatCommander comments, this might not belong here?
		L2Party party = activeChar.getParty();
		if (party != null)
		{
			L2CommandChannel chan = party.getCommandChannel();
			if (chan != null && party.isLeader(activeChar))
			{
				CreatureSay cs = new CreatureSay(charObjId, chatType, charName, text);
				chan.broadcastCSToChannelMembers(cs, activeChar);
			}
		}
	}
}
