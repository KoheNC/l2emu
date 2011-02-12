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

import net.l2emuproject.gameserver.model.TradeList;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

public final class PrivateStoreListSell extends L2GameServerPacket
{
	private static final String			_S__B4_PRIVATESTORELISTSELL	= "[S] 9b PrivateStoreListSell";
	private final int					_objId;
	private final long					_playerAdena;
	private final boolean				_packageSale;
	private final TradeList.TradeItem[]	_items;

	// player's private shop
	public PrivateStoreListSell(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_playerAdena = player.getAdena();
		_items = storePlayer.getSellList().getItems();
		_packageSale = storePlayer.getSellList().isPackaged();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xa1);
		writeD(_objId);
		writeD(_packageSale ? 1 : 0);
		writeQ(_playerAdena);
		writeD(_items.length);
		for (TradeList.TradeItem item : _items)
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
			writeElementalInfo(item); // 8x h or d
			// Enchant Effect
			writeEnchantEffectInfo();
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() * 2);
		}
	}

	@Override
	public final String getType()
	{
		return _S__B4_PRIVATESTORELISTSELL;
	}
}
