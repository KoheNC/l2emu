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

import javolution.util.FastList;
import net.l2emuproject.gameserver.model.L2ItemInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

/**
 * 0x53 WareHouseDepositList dh (h dddhh dhhh d)
 * 
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public final class WareHouseDepositList extends L2GameServerPacket
{
	public static final int					PRIVATE						= 1;
	public static final int					CLAN						= 4;
	public static final int					CASTLE						= 3;								//not sure
	public static final int					FREIGHT						= 1;

	private static final String				_S__41_WAREHOUSEDEPOSITLIST	= "[S] 41 WareHouseDepositList";

	private final long						_playerAdena;
	private final FastList<L2ItemInstance>	_items;
	private final int						_whType;

	public WareHouseDepositList(L2PcInstance player, int type)
	{
		_whType = type;

		final boolean isPrivate = _whType == PRIVATE;
		_playerAdena = player.getAdena();

		_items = new FastList<L2ItemInstance>();

		for (L2ItemInstance temp : player.getInventory().getAvailableItems(true, isPrivate))
		{
			if (temp != null && temp.isDepositable(isPrivate))
				_items.add(temp);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x41);
		/* 0x01-Private Warehouse
		 * 0x02-Clan Warehouse
		 * 0x03-Castle Warehouse
		 * 0x04-Warehouse */
		writeH(_whType);
		writeQ(_playerAdena);
		final int count = _items.size();
		if (_log.isDebugEnabled())
			_log.debug("Count: " + count);
		writeH(count);

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

			// T1
			writeElementalInfo(item); // 8x h or d
			// Enchant Effects
			writeEnchantEffectInfo();
			writeD(item.getObjectId());
		}
		_items.clear();
	}

	@Override
	public final String getType()
	{
		return _S__41_WAREHOUSEDEPOSITLIST;
	}
}
