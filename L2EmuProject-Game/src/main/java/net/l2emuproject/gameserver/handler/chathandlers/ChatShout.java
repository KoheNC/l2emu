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
import net.l2emuproject.Config.ChatMode;
import net.l2emuproject.gameserver.handler.IChatHandler;
import net.l2emuproject.gameserver.instancemanager.MapRegionManager;
import net.l2emuproject.gameserver.model.BlockList;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.mapregion.L2MapRegion;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.world.L2World;

/**
 * @author  Noctarius
 */
public class ChatShout implements IChatHandler
{
	private final SystemChatChannelId[]	_chatTypes	=
												{ SystemChatChannelId.Chat_Shout };

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
		if (!activeChar.getFloodProtector().tryPerformAction(Protected.GLOBAL_CHAT) && !activeChar.isGM())
		{
			activeChar.sendMessage("Flood protection: Using global chat failed.");
			return;
		}

		String name = (activeChar.isGM() && Config.GM_NAME_HAS_BRACELETS)? "[GM]" + activeChar.getName() : activeChar.getName();
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), chatType, name, text);

		if (Config.DEFAULT_GLOBAL_CHAT == ChatMode.REGION)
		{
			L2MapRegion region = MapRegionManager.getInstance().getRegion(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (region == MapRegionManager.getInstance().getRegion(player.getX(), player.getY(), player.getZ())
						&& !(Config.REGION_CHAT_ALSO_BLOCKED && BlockList.isBlocked(player, activeChar))
						&& player.isSameInstance(activeChar))
				{
					player.sendPacket(cs);
				}
			}
		}
		else if (Config.DEFAULT_GLOBAL_CHAT == ChatMode.GLOBAL || Config.DEFAULT_GLOBAL_CHAT == ChatMode.GM && activeChar.isGM())
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (!(Config.REGION_CHAT_ALSO_BLOCKED
						&& BlockList.isBlocked(player, activeChar))
						&& player.isSameInstance(activeChar))
				{
					player.sendPacket(cs);
				}
			}
		}
	}
}
