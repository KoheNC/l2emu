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
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

public final class WareHouseWithdrawalList extends L2GameServerPacket
{
	public static final int			PRIVATE							= 1;
	public static final int			CLAN							= 4;
	public static final int			CASTLE							= 3;								// not sure
	public static final int			FREIGHT							= 1;

	private static final String		_S__54_WAREHOUSEWITHDRAWALLIST	= "[S] 42 WareHouseWithdrawalList";

	private final L2PcInstance		_activeChar;
	private final long				_activeCharAdena;
	private final L2ItemInstance[]	_items;
	private final int				_whType;

	public WareHouseWithdrawalList(L2PcInstance player, int type)
	{
		_activeChar = player;
		_whType = type;

		_activeCharAdena = _activeChar.getAdena();
		if (_activeChar.getActiveWarehouse() == null)
		{
			// Something went wrong!
			_log.warn("error while sending withdraw request to: " + _activeChar.getName());
			_items = L2ItemInstance.EMPTY_ARRAY;
		}
		else
			_items = _activeChar.getActiveWarehouse().getItems();

		if (_log.isDebugEnabled())
			for (L2ItemInstance item : _items)
				_log.info("item:" + item.getItem().getName() + " type1:" + item.getItem().getType1() + " type2:" + item.getItem().getType2());
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x42);
		/* 0x01-Private Warehouse
		 * 0x02-Clan Warehouse
		 * 0x03-Castle Warehouse
		 * 0x04-Warehouse */
		writeH(_whType);
		writeQ(_activeCharAdena);
		writeH(_items.length);

		for (L2ItemInstance item : _items)
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
			writeElementalInfo(item); // 8x h or d
			// Enchant Effects
			writeEnchantEffectInfo();
			writeD(item.getObjectId());
		}
	}

	@Override
	public final String getType()
	{
		return _S__54_WAREHOUSEWITHDRAWALLIST;
	}
}
