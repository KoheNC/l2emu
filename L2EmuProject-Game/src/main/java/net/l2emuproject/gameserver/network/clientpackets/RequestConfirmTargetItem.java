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
package net.l2emuproject.gameserver.network.clientpackets;

import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExPutItemResultForVariationMake;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Format:(ch) d
 * @author  -Wooden-
 */
public final class RequestConfirmTargetItem extends AbstractRefinePacket
{
	private static final String	_C__D0_26_REQUESTCONFIRMTARGETITEM	= "[C] D0:26 RequestConfirmTargetItem";
	private int					_itemObjId;

	@Override
	protected final void readImpl()
	{
		_itemObjId = readD();
	}

	@Override
	protected final void runImpl()
	{
		final L2Player activeChar = getClient().getActiveChar();
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);

		if (activeChar == null || item == null)
			return;

		if (!isValid(activeChar, item))
		{
			// Different system message here
			if (item.isAugmented())
			{
				activeChar.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
				return;
			}

			activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		activeChar.sendPacket(new ExPutItemResultForVariationMake(_itemObjId, item.getItemId()));
	}

	@Override
	public final String getType()
	{
		return _C__D0_26_REQUESTCONFIRMTARGETITEM;
	}
}
