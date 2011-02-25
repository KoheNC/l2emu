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

import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.ExShowBaseAttributeCancelWindow;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.templates.item.L2Weapon;
import net.l2emuproject.gameserver.world.object.L2Player;

public class RequestExRemoveItemAttribute extends L2GameClientPacket
{
	private static final String _C__D0_23_REQUESTEXREMOVEITEMATTRIBUTE = "[C] D0:23 RequestExRemoveItemAttribute";

	private int _objectId;
	private byte _element;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_element = (byte) readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null) return;

		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_objectId);

		if (targetItem == null || targetItem.getElementals() == null)
		{
			requestFailed(SystemMessageId.FAILED_TO_REMOVE_ELEMENTAL_POWER);
			return;
		}
		
		if (activeChar.reduceAdena("RemoveElement", getPrice(targetItem), activeChar, true))
		{
			if (targetItem.isEquipped())
				targetItem.getAttribute(_element).removeBonus(activeChar);
			targetItem.clearElementAttr();

			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(targetItem);
			sendPacket(iu);
			SystemMessage sm;
			if (targetItem.getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.S1_S2_ELEMENTAL_POWER_REMOVED);
				sm.addNumber(targetItem.getEnchantLevel());
				sm.addItemName(targetItem);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_ELEMENTAL_POWER_REMOVED);
				sm.addItemName(targetItem);
			}
			sendPacket(sm);
			activeChar.getInventory().reloadEquippedItems();
			activeChar.broadcastUserInfo();
			sendPacket(new ExShowBaseAttributeCancelWindow(activeChar));
		}

		sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static long getPrice(L2ItemInstance item)
	{
		switch (item.getItem().getCrystalType())
		{
			case L2Item.CRYSTAL_S:
			{
				if (item.getItem() instanceof L2Weapon)
					return 50000;
				else
					return 40000;
			}
			case L2Item.CRYSTAL_S80:
			{
				if (item.getItem() instanceof L2Weapon)
					return 100000;
				else
					return 80000;
			}
			case L2Item.CRYSTAL_S84:
			{
				if (item.getItem() instanceof L2Weapon)
					return 200000;
				else
					return 160000;
			}
		}
		
		return 0;
	}
	
	@Override
	public String getType()
	{
		return _C__D0_23_REQUESTEXREMOVEITEMATTRIBUTE;
	}
}
