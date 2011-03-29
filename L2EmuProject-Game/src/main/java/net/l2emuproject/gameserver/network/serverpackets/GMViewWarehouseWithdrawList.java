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

import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
	private static final String		_S__95_GMViewWarehouseWithdrawList	= "[S] 95 GMViewWarehouseWithdrawList";
	private final L2ItemInstance[]	_items;
	private final String			_playerName;
	private final L2Player			_player;
	private final long				_money;

	public GMViewWarehouseWithdrawList(final L2Player player)
	{
		_player = player;
		_items = _player.getWarehouse().getItems();
		_playerName = _player.getName();
		_money = _player.getAdena();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x9b);
		writeS(_playerName);
		writeQ(_money);
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
			writeElementalInfo(item);
			writeEnchantEffectInfo();
			writeD(item.getObjectId());
		}
	}

	@Override
	public final String getType()
	{
		return _S__95_GMViewWarehouseWithdrawList;
	}
}
