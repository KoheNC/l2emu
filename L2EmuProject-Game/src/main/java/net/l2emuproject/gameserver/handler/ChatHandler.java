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
package net.l2emuproject.gameserver.handler;

import net.l2emuproject.gameserver.handler.chathandlers.ChatAll;
import net.l2emuproject.gameserver.handler.chathandlers.ChatAlliance;
import net.l2emuproject.gameserver.handler.chathandlers.ChatAnnounce;
import net.l2emuproject.gameserver.handler.chathandlers.ChatBattlefield;
import net.l2emuproject.gameserver.handler.chathandlers.ChatClan;
import net.l2emuproject.gameserver.handler.chathandlers.ChatCommander;
import net.l2emuproject.gameserver.handler.chathandlers.ChatHero;
import net.l2emuproject.gameserver.handler.chathandlers.ChatParty;
import net.l2emuproject.gameserver.handler.chathandlers.ChatPartyRoom;
import net.l2emuproject.gameserver.handler.chathandlers.ChatPetition;
import net.l2emuproject.gameserver.handler.chathandlers.ChatShout;
import net.l2emuproject.gameserver.handler.chathandlers.ChatSystem;
import net.l2emuproject.gameserver.handler.chathandlers.ChatTrade;
import net.l2emuproject.gameserver.handler.chathandlers.ChatWhisper;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.util.EnumHandlerRegistry;


/**
 * @author Noctarius
 */
public final class ChatHandler extends EnumHandlerRegistry<SystemChatChannelId, IChatHandler>
{
	public static ChatHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private ChatHandler()
	{
		super(SystemChatChannelId.class);
		
		registerChatHandler(new ChatAll());
		registerChatHandler(new ChatAlliance());
		registerChatHandler(new ChatBattlefield());
		registerChatHandler(new ChatAnnounce());
		registerChatHandler(new ChatClan());
		registerChatHandler(new ChatCommander());
		registerChatHandler(new ChatHero());
		registerChatHandler(new ChatParty());
		registerChatHandler(new ChatPartyRoom());
		registerChatHandler(new ChatPetition());
		registerChatHandler(new ChatShout());
		registerChatHandler(new ChatSystem());
		registerChatHandler(new ChatTrade());
		registerChatHandler(new ChatWhisper());
		
		_log.info(getClass().getSimpleName() + " : Loaded " + size() + " handler(s).");
	}
	
	public void registerChatHandler(IChatHandler handler)
	{
		registerAll(handler, handler.getChatTypes());
	}
	
	public IChatHandler getChatHandler(SystemChatChannelId chatId)
	{
		return get(chatId);
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ChatHandler _instance = new ChatHandler();
	}
}
