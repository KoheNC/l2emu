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
import net.l2emuproject.gameserver.network.serverpackets.ExPutIntensiveResultForVariationMake;

/**
 * Fromat(ch) dd
 * @author  -Wooden-
 */
public final class RequestConfirmRefinerItem extends AbstractRefinePacket
{
	private static final String	_C__D0_2A_REQUESTCONFIRMREFINERITEM	= "[C] D0:2A RequestConfirmRefinerItem";

	private int					_targetItemObjId;
	private int					_refinerItemObjId;

	@Override
	protected final void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
	}

	@Override
	protected final void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		final L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		if (activeChar == null || refinerItem == null || targetItem == null)
			return;

		if (!isValid(activeChar, targetItem, refinerItem))
		{
			activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		final int refinerItemId = refinerItem.getItem().getItemId();
		final int grade = targetItem.getItem().getItemGrade();
		final LifeStone ls = getLifeStone(refinerItemId);
		final int gemStoneId = getGemStoneId(grade);
		final int gemStoneCount = getGemStoneCount(grade, ls.getGrade());

		activeChar.sendPacket(new ExPutIntensiveResultForVariationMake(_refinerItemObjId, refinerItemId, gemStoneId, gemStoneCount));
	}

	@Override
	public final String getType()
	{
		return _C__D0_2A_REQUESTCONFIRMREFINERITEM;
	}
}
