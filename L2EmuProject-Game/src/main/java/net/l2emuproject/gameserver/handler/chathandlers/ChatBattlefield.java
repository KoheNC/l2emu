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

import java.util.Collection;

import net.l2emuproject.gameserver.handler.IChatHandler;
import net.l2emuproject.gameserver.manager.TerritoryWarManager;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * A chat handler
 *
 * @author  Gigiikun
 */
public class ChatBattlefield implements IChatHandler
{
	private static final SystemChatChannelId[]	COMMAND_IDS	=
															{ SystemChatChannelId.Chat_Battlefield };

	@Override
	public void useChatHandler(L2Player activeChar, String target, SystemChatChannelId chatType, String text)
	{
		if (TerritoryWarManager.getInstance().isTWChannelOpen() && activeChar.getSiegeSide() > 0)
		{
			CreatureSay cs = new CreatureSay(activeChar.getObjectId(), chatType, activeChar.getName(), text);
			Collection<L2Player> pls = L2World.getInstance().getAllPlayers();
			for (L2Player player : pls)
				if (player.getSiegeSide() == activeChar.getSiegeSide())
					player.sendPacket(cs);
		}
	}

	@Override
	public SystemChatChannelId[] getChatTypes()
	{
		return COMMAND_IDS;
	}
}
