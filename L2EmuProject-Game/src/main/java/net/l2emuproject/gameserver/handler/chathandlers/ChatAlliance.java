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
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 *
 * @author  Noctarius
 */
public class ChatAlliance implements IChatHandler
{
	private final SystemChatChannelId[]	_chatTypes	=
												{ SystemChatChannelId.Chat_Alliance };

	/**
	 * @see net.l2emuproject.gameserver.handler.IChatHandler#getChatType()
	 */
	@Override
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	/**
	 * @see net.l2emuproject.gameserver.handler.IChatHandler#useChatHandler(net.l2emuproject.gameserver.world.object.L2Player.player.L2PcInstance, java.lang.String, net.l2emuproject.gameserver.network.enums.SystemChatChannelId, java.lang.String)
	 */
	@Override
	public void useChatHandler(L2Player activeChar, String target, SystemChatChannelId chatType, String text)
	{
		if (activeChar.getClan() == null)
			return;

		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), chatType, activeChar.getName(), text);
		activeChar.getClan().broadcastToOnlineAllyMembers(cs);
	}
}
