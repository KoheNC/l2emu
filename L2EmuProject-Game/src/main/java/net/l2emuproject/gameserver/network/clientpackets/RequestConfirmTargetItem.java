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

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExPutItemResultForVariationMake;

/**
 * Format:(ch) d
 * @author  -Wooden-
 */
public final class RequestConfirmTargetItem extends AbstractRefinePacket
{
	private static final String _C__D0_26_REQUESTCONFIRMTARGETITEM = "[C] D0:26 RequestConfirmTargetItem";

	private int _itemObjId;

	@Override
	protected void readImpl()
	{
		_itemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) return;
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);
		if (item == null)
		{
			requestFailed(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}

		if (!isValid(activeChar))
		{
			sendAF();
			return;
		}
		if (!isValid(activeChar, item))
		{
			// Different system message here
			if (item.isAugmented())
				requestFailed(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
			else
				requestFailed(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		sendPacket(new ExPutItemResultForVariationMake(_itemObjId, item.getItemId()));
		sendPacket(SystemMessageId.SELECT_THE_CATALYST_FOR_AUGMENTATION);

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__D0_26_REQUESTCONFIRMTARGETITEM;
	}
}
