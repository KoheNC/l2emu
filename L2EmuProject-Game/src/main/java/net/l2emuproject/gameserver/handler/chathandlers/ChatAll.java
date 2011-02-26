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
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.services.blocklist.BlockList;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 *
 * @author  Noctarius
 */
public class ChatAll implements IChatHandler
{
	private final SystemChatChannelId[]	_chatTypes	=
												{ SystemChatChannelId.Chat_Normal };

	/**
	 * @see net.l2emuproject.gameserver.handler.IChatHandler#getChatType()
	 */
	@Override
	public SystemChatChannelId[] getChatTypes()
	{
		return _chatTypes;
	}

	/**
	 * @see net.l2emuproject.gameserver.handler.IChatHandler#useChatHandler(net.l2emuproject.gameserver.world.object.L2Player.player.L2PcInstance, net.l2emuproject.gameserver.network.enums.SystemChatChannelId, java.lang.String)
	 */
	@Override
	public void useChatHandler(L2Player activeChar, String target, SystemChatChannelId chatType, String text)
	{
		String name = (activeChar.isGM() && Config.GM_NAME_HAS_BRACELETS)? "[GM]" + activeChar.getAppearance().getVisibleName() : activeChar.getAppearance().getVisibleName();
		
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), chatType, name, text);

		for (L2Player player : activeChar.getKnownList().getKnownPlayers().values())
		{
			if (player != null && activeChar.isInsideRadius(player, 1250, false, true)
					&& !(Config.REGION_CHAT_ALSO_BLOCKED && BlockList.isBlocked(player, activeChar)))
			{
				player.sendPacket(cs);
			}
		}
		activeChar.sendPacket(cs);
	}
}
