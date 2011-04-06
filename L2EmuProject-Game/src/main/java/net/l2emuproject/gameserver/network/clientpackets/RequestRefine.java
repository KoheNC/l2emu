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

import net.l2emuproject.gameserver.dataholders.AugmentationDataHolder;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExVariationResult;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.services.augmentation.L2Augmentation;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Format:(ch) dddd
 * @author  -Wooden-
 */
public final class RequestRefine extends AbstractRefinePacket
{
	private static final String	_C__D0_2C_REQUESTREFINE	= "[C] D0:2C RequestRefine";
	private int					_targetItemObjId;
	private int					_refinerItemObjId;
	private int					_gemStoneItemObjId;
	@SuppressWarnings("unused")
	private long				_gemStoneCount;

	@Override
	protected final void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemStoneItemObjId = readD();
		_gemStoneCount = readQ();
	}

	@Override
	protected final void runImpl()
	{
		final L2Player activeChar = getClient().getActiveChar();
		final L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		final L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		final L2ItemInstance gemStoneItem = activeChar.getInventory().getItemByObjectId(_gemStoneItemObjId);

		if (activeChar == null || targetItem == null || refinerItem == null || gemStoneItem == null)
			return;

		if (!isValid(activeChar, targetItem, refinerItem, gemStoneItem))
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}

		final LifeStone ls = getLifeStone(refinerItem.getItemId());
		if (ls == null)
			return;

		final int lifeStoneLevel = ls.getLevel();
		final int lifeStoneGrade = ls.getGrade();
		// unequip item
		if (targetItem.isEquipped())
		{
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
				iu.addModifiedItem(itm);
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
		}

		// consume the life stone
		if (!activeChar.destroyItem("RequestRefine", refinerItem, 1, null, false))
			return;

		// consume the gemstones
		if (!activeChar.destroyItem("RequestRefine", gemStoneItem, getGemStoneCount(targetItem.getItem().getItemGrade(), lifeStoneGrade), null, false))
			return;

		final L2Augmentation aug = AugmentationDataHolder.getInstance()
				.generateRandomAugmentation(lifeStoneLevel, lifeStoneGrade, targetItem.getItem().getBodyPart());
		targetItem.setAugmentation(aug);

		final int stat12 = 0x0000FFFF & aug.getAugmentationId();
		final int stat34 = aug.getAugmentationId() >> 16;
		activeChar.sendPacket(new ExVariationResult(stat12, stat34, 1));

		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		activeChar.sendPacket(iu);

		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
	}

	@Override
	public final String getType()
	{
		return _C__D0_2C_REQUESTREFINE;
	}
}
