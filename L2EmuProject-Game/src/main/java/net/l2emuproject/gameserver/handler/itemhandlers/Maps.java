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
import net.l2emuproject.gameserver.network.serverpackets.ShowMiniMap;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * This class provides handling for items that should display a map when double clicked.
 */
public final class Maps implements IItemHandler
{
	private static final int[] ITEM_IDS = { 1665, 1863 };
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2Player))
			return;
		
		((L2Player)playable).sendPacket(new ShowMiniMap(item.getItemId()));
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
