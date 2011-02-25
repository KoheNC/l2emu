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

import net.l2emuproject.gameserver.Shutdown;
import net.l2emuproject.gameserver.Shutdown.DisableType;
import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExChooseInventoryAttributeItem;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Playable;

public class EnchantAttribute implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int	ITEM_IDS[]	=
											{
			9546,
			9547,
			9548,
			9549,
			9550,
			9551,
			9552,
			9553,
			9554,
			9555,
			9556,
			9557,
			9558,
			9559,
			9560,
			9561,
			9562,
			9563,
			9564,
			9565,
			9566,
			9567,
			9568,
			9569,
			10521,
			10522,
			10523,
			10524,
			10525,
			10526							};

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2Player))
			return;

		L2Player activeChar = (L2Player) playable;
		if (activeChar.isCastingNow())
			return;

		// Restrict enchant during restart/shutdown (because of an existing exploit)
		if (Shutdown.isActionDisabled(DisableType.ENCHANT))
		{
			activeChar.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			return;
		}

		//activeChar.sendPacket(SystemMessageId.SELECT_ITEM_TO_ADD_ELEMENTAL_POWER);
		activeChar.setActiveEnchantAttrItem(item);
		activeChar.sendPacket(new ExChooseInventoryAttributeItem(item));
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
