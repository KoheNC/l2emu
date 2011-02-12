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
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

public final class TradeStart extends L2GameServerPacket
{
	private static final String			_S__2E_TRADESTART	= "[S] 1E TradeStart";
	private final L2PcInstance			_activeChar;
	private final List<L2ItemInstance>	_itemList;

	public TradeStart(L2PcInstance player)
	{
		_activeChar = player;
		_itemList = _activeChar.getInventory().getAvailableItems(true, (_activeChar.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS));
	}

	@Override
	protected final void writeImpl()
	{
		//0x2e TradeStart   d h (h dddhh dhhh)
		if (_activeChar.getActiveTradeList() == null || _activeChar.getActiveTradeList().getPartner() == null)
			return;

		writeC(0x14);
		writeD(_activeChar.getActiveTradeList().getPartner().getObjectId());
		//writeD((_activeChar != null || _activeChar.getTransactionRequester() != null)? _activeChar.getTransactionRequester().getObjectId() : 0);

		writeH(_itemList.size());
		for (L2ItemInstance item : _itemList)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getLocationSlot());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			// Player cannot sell/buy augmented, shadow or time-limited items
			// probably so hardcode values here
			writeD(0x00); // Augment
			writeD(-1); // Mana
			writeD(-9999); // Time
			writeElementalInfo(item); // 8x h or d
			// Enchant Effects
			writeEnchantEffectInfo();
		}
	}

	@Override
	public final String getType()
	{
		return _S__2E_TRADESTART;
	}
}
