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

import java.util.List;

import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Migi, DS
 */
public final class ExReplayPostItemList extends L2GameServerPacket
{
	private static final String			_S__FE_B2_EXPOSTITEMLIST	= "[S] FE:B2 ExPostItemList";

	private final L2Player			_activeChar;
	private final List<L2ItemInstance>	_itemList;

	public ExReplayPostItemList(L2Player activeChar)
	{
		_activeChar = activeChar;
		_itemList = _activeChar.getInventory().getAvailableItems(true, false);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0xb2);
		writeD(_itemList.size());
		for (L2ItemInstance item : _itemList)
		{
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getLocationSlot());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeH(item.isEquipped() ? 0x01 : 0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			if (item.isAugmented())
				writeD(item.getAugmentation().getAugmentationId());
			else
				writeD(0x00);
			writeD(item.getMana());
			writeD(item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999);
			writeH(item.getAttackElementType());
			writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				writeH(item.getElementDefAttr(i));
			}

			writeEnchantEffectInfo();
		}
	}

	@Override
	public final String getType()
	{
		return _S__FE_B2_EXPOSTITEMLIST;
	}
}
