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

import net.l2emuproject.gameserver.dataholders.EnchantItemDataHolder;
import net.l2emuproject.gameserver.dataholders.EnchantItemDataHolder.EnchantItem;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExPutEnchantSupportItemResult;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;


/**
 * @author evill33t
 * 
 */
public class RequestExTryToPutEnchantSupportItem extends L2GameClientPacket
{
	private static final String	_C__D0_80_REQUESTEXTRYTOPUTENCHANTSUPPORTITEM	= "[C] D0 50 RequestExTryToPutEnchantSupportItem";

	private int _supportObjectId;
	private int _enchantObjectId;

	@Override
	protected void readImpl()
	{
		_supportObjectId = readD();
		_enchantObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.isEnchanting())
		{
			L2ItemInstance item = (L2ItemInstance) L2World.getInstance().findObject(_enchantObjectId);
			L2ItemInstance support = (L2ItemInstance) L2World.getInstance().findObject(_supportObjectId);

			if (item == null || support == null)
				return;

			EnchantItem supportTemplate = EnchantItemDataHolder.getInstance().getSupportItem(support);
			if (supportTemplate == null || !supportTemplate.isValid(item))
			{
				// message may be custom
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
				activeChar.setActiveEnchantSupportItem(null);
				activeChar.sendPacket(new ExPutEnchantSupportItemResult(0));
				return;
			}
			activeChar.setActiveEnchantSupportItem(support);
			activeChar.sendPacket(new ExPutEnchantSupportItemResult(_supportObjectId));
		}
	}

	@Override
	public String getType()
	{
		return _C__D0_80_REQUESTEXTRYTOPUTENCHANTSUPPORTITEM;
	}
}
