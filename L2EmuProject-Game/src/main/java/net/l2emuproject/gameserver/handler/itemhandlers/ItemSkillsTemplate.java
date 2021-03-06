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
import net.l2emuproject.gameserver.world.object.L2Playable;

/**
 * Template for item skills handler
 * Only minimum of checks
 */
public final class ItemSkillsTemplate implements IItemHandler
{
	private static final int[]	ITEM_IDS	= {};

	@Override
	public final void useItem(L2Playable playable, L2ItemInstance item)
	{
		// TODO: Implement me...
	}

	@Override
	public final int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
