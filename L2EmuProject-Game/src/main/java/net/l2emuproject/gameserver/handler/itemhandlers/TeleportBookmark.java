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
package net.l2emuproject.gameserver.handler.itemhandlers;

import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Teleport Bookmark Slot Handler
 *
 * @author ShanSoft
 */
public class TeleportBookmark implements IItemHandler
{
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (playable == null || item == null || !(playable instanceof L2Player))
			return;
		
		L2Player player = (L2Player) playable;
		
		if(player.getPlayerBookmark().getBookMarkSlot() >= 9)
		{
			player.sendPacket(SystemMessageId.YOUR_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_REACHED_ITS_MAXIMUM_LIMIT);
			return;
		}
		
		player.destroyItem("Consume", item.getObjectId(), 1, null, false);
		
		player.getPlayerBookmark().setBookMarkSlot(player.getPlayerBookmark().getBookMarkSlot()+3);
		player.sendPacket(SystemMessageId.THE_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_BEEN_INCREASED);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(item.getItemId());
		player.sendPacket(sm);
	}

	@Override
	public int[] getItemIds()
	{
		return null;
	}
}
