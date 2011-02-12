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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.handler.IChatHandler;
import net.l2emuproject.gameserver.model.BlockList;
import net.l2emuproject.gameserver.model.L2World;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;

/**
 *
 * @author  Noctarius
 */
public class ChatWhisper implements IChatHandler
{
	private final SystemChatChannelId[]	_chatTypes	=
												{ SystemChatChannelId.Chat_Tell };

	/**
	 * @see net.l2emuproject.gameserver.handler.IChatHandler#getChatType()
	 */
	@Override
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	/**
	 * @see net.l2emuproject.gameserver.handler.IChatHandler#useChatHandler(net.l2emuproject.gameserver.character.player.L2PcInstance, net.l2emuproject.gameserver.network.enums.SystemChatChannelId, java.lang.String)
	 */
	@Override
	public void useChatHandler(L2PcInstance activeChar, String target, SystemChatChannelId chatType, String text)
	{
		L2PcInstance receiver = L2World.getInstance().getPlayer(target);

		if (receiver != null)
		{
			if ((!receiver.getMessageRefusal() && !BlockList.isBlocked(receiver, activeChar)) || activeChar.isGM())
			{
				if (receiver.isInOfflineMode())
				{
					activeChar.sendMessage(receiver.getName() + " is in offline mode!");
					return;
				}
				if (Config.JAIL_DISABLE_CHAT && receiver.isInJail())
				{
					activeChar.sendMessage(receiver.getName()+" is currently in jail.");
					return;
				}
				if (receiver.isChatBanned())
				{
					activeChar.sendMessage(receiver.getName()+" is currently muted.");
					return;
				}
				receiver.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType, activeChar.getName(), text));
				activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), chatType, "->" + receiver.getName(), text));
			}
			else
				activeChar.sendPacket(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_ONLINE);
			sm.addString(target);
			activeChar.sendPacket(sm);
		}
	}
}
