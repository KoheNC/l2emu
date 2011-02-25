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
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Playable;

public final class BlessedSpiritShot implements IItemHandler
{
	private static final int[]	ITEM_IDS	=
											{ 3947, 3948, 3949, 3950, 3951, 3952, 22072, 22073, 22074, 22075, 22076 };

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (playable instanceof L2Player)
			playable.getShots().chargeBlessedSpiritshot(item);
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
