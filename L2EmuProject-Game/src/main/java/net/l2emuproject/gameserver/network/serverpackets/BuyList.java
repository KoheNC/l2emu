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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.L2ItemInstance;
import net.l2emuproject.gameserver.model.L2TradeList;

public final class BuyList extends L2GameServerPacket
{
	private static final String		_S__07_BUYLIST	= "[S] 07 BuyList [ddh (hdddhhdhhhdddddddd)]";
	private final int				_listId;
	private final L2ItemInstance[]	_list;
	private final long				_money;
	private double					_taxRate		= 1.;

	public BuyList(L2TradeList list, long currentMoney)
	{
		_listId = list.getListId();
		List<L2ItemInstance> lst = list.getItems();
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
	}

	public BuyList(L2TradeList list, long currentMoney, double taxRate)
	{
		_listId = list.getListId();
		List<L2ItemInstance> lst = list.getItems();
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
		_taxRate = taxRate;
	}

	public BuyList(List<L2ItemInstance> lst, int listId, long currentMoney)
	{
		_listId = listId;
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xB7);
		writeD(0x00);
		writeQ(_money); // current money
		writeD(_listId);

		writeH(_list.length);

		for (L2ItemInstance item : _list)
		{
			if (item.getCount() > 0 || item.getCount() == -1)
			{
				writeD(item.getItemId());
				writeD(item.getItemId()); // FIXME: This is the item.getObjectID()?
				writeD(0); // FIXME: This is the item.getObjectID()?
				writeQ(item.getCount() < 0 ? 0 : item.getCount());
				writeH(item.getItem().getType2());
				writeH(item.getItem().getType1()); // Custom Type 1
				writeH(0x00); // isEquipped
				writeD(item.getItem().getBodyPart()); // Body Part
				writeH(0x00); // Enchant
				writeH(0x00); // Custom Type
				writeD(0x00); // Augment
				writeD(-1); // Mana
				writeD(-9999); // Time

				writeElementalInfo(item);

				writeEnchantEffectInfo();

				if (item.getItemId() >= 3960 && item.getItemId() <= 4026)// Config.RATE_SIEGE_GUARDS_PRICE-//'
					writeQ((long) (item.getPriceToSell() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
				else
					writeQ((long) (item.getPriceToSell() * (1 + _taxRate)));
			}
		}
	}

	@Override
	public final String getType()
	{
		return _S__07_BUYLIST;
	}
}
