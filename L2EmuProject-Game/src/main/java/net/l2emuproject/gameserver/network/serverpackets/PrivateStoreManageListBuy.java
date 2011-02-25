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

import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.services.transactions.TradeList;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class PrivateStoreManageListBuy extends L2GameServerPacket
{
	private static final String			_S__D0_PRIVATESELLLISTBUY	= "[S] b7 PrivateSellListBuy";
	private final int					_objId;
	private final long					_playerAdena;
	private final L2ItemInstance[]		_itemList;
	private final TradeList.TradeItem[]	_buyList;

	public PrivateStoreManageListBuy(L2Player player)
	{
		_objId = player.getObjectId();
		_playerAdena = player.getAdena();
		_itemList = player.getInventory().getUniqueItems(false, true);
		_buyList = player.getBuyList().getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xbd);
		//section 1
		writeD(_objId);
		writeQ(_playerAdena);

		//section2
		writeD(_itemList.length); // inventory items for potential buy
		for (L2ItemInstance item : _itemList)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getLocationSlot());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeH(item.isEquipped() ? 0x01 : 0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			// Player cannot sell/buy augmented, shadow or time-limited items
			// probably so hardcode values here
			writeD(0x00); // Augment
			writeD(-1); // Mana
			writeD(-9999); // Time
			writeElementalInfo(item); // 8x h or d
			writeEnchantEffectInfo();
			writeQ(item.getItem().getReferencePrice() * 2);
		}

		//section 3
		writeD(_buyList.length); //count for all items already added for buy
		for (TradeList.TradeItem item : _buyList)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getLocationSlot());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchant());
			writeH(item.getCustomType2());
			// Player cannot sell/buy augmented, shadow or time-limited items
			// probably so hardcode values here
			writeD(0x00); // Augment
			writeD(-1); // Mana
			writeD(-9999); // Time
			writeElementalInfo(item);
			writeEnchantEffectInfo();
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() * 2);
			writeQ(item.getCount());
		}
	}

	@Override
	public final String getType()
	{
		return _S__D0_PRIVATESELLLISTBUY;
	}
}
