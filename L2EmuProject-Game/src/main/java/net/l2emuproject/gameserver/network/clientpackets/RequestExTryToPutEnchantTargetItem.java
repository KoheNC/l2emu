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

import net.l2emuproject.gameserver.datatables.EnchantItemData;
import net.l2emuproject.gameserver.datatables.EnchantItemData.EnchantScroll;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExPutEnchantTargetItemResult;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author evill33t
 * 
 */
public class RequestExTryToPutEnchantTargetItem extends L2GameClientPacket
{
	private static final String	_C__D0_78_REQUESTEXTRYTOPUTENCHANTTARGETITEM	= "[C] D0 4F RequestExTryToPutEnchantTargetItem";

	private int _objectId = 0;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (_objectId == 0)
		{
			if (_log.isDebugEnabled())
				_log.info("ENCHANT: Nonexisting item dragged on. Bye.");
			sendAF();
			return;
		}

		if (activeChar.isEnchanting())
		{
			if (_log.isDebugEnabled())
				_log.info("ENCHANT: Item dragged on while enchanting. Bye.");
			requestFailed(SystemMessageId.ENCHANTMENT_ALREADY_IN_PROGRESS);
			return;
		}

		L2ItemInstance item = (L2ItemInstance) L2World.getInstance().findObject(_objectId);
		if (_log.isDebugEnabled())
			_log.info("ENCHANT: Trying to insert " + item + " to enchanting slot.");
		L2ItemInstance scroll = activeChar.getActiveEnchantItem();

		if (item == null || scroll == null)
		{
			if (_log.isDebugEnabled())
				_log.info("ENCHANT: Slot item is null. Bye.");
			requestFailed(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
			return;
		}

		// template for scroll
		EnchantItemData enchantData = EnchantItemData.getInstance();
		EnchantScroll scrollTemplate = enchantData.getEnchantScroll(scroll);
		if (!scrollTemplate.isValid(item) || !enchantData.isEnchantable(activeChar, item))
		{
			if (_log.isDebugEnabled())
				_log.info("ENCHANT: Slot item is invalid. Bye.");
			sendPacket(SystemMessageId.DOES_NOT_FIT_SCROLL_CONDITIONS);
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
			return;
		}
		if (_log.isDebugEnabled())
			_log.info("Item added to enchanting slot: " + item);
		activeChar.setIsEnchanting(true);
		activeChar.setActiveEnchantTimestamp(System.currentTimeMillis());
		activeChar.sendPacket(new ExPutEnchantTargetItemResult(_objectId));

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__D0_78_REQUESTEXTRYTOPUTENCHANTTARGETITEM;
	}
}
