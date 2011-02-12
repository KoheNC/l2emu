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
package net.l2emuproject.gameserver.network.serverpackets;

import net.l2emuproject.gameserver.model.L2ItemInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PetInstance;

public final class PetItemList extends L2GameServerPacket
{
	private static final String	_S__cb_PETITEMLIST	= "[S] b2  PetItemList";

	private final L2PetInstance	_activeChar;

	public PetItemList(L2PetInstance character)
	{
		_activeChar = character;
		if (_log.isDebugEnabled())
		{
			L2ItemInstance[] items = _activeChar.getInventory().getItems();
			for (L2ItemInstance temp : items)
				_log.info("item:" + temp.getItem().getName() + " type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xb3);

		L2ItemInstance[] items = _activeChar.getInventory().getItems();
		int count = items.length;
		writeH(count);

		for (L2ItemInstance temp : items)
		{
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getLocationSlot());
			writeQ(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			if (temp.isAugmented())
				writeD(temp.getAugmentation().getAugmentationId());
			else
				writeD(0x00);
			writeD(temp.getMana());
			writeD(temp.isTimeLimitedItem() ? (int) (temp.getRemainingTime() / 1000) : -9999);
			writeElementalInfo(temp);
			writeEnchantEffectInfo();
		}
	}

	@Override
	public final String getType()
	{
		return _S__cb_PETITEMLIST;
	}
}
