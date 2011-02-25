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

public class GMViewItemList extends L2GameServerPacket
{
	private static final String		_S__AD_GMVIEWITEMLIST	= "[S] 94 GMViewItemList";
	private final L2ItemInstance[]	_items;
	private final L2Player		_cha;
	private final String			_playerName;
	
	public GMViewItemList(L2Player cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
		_cha = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9a);
		writeS(_playerName);
		writeD(_cha.getInventoryLimit()); // inventory limit
		writeH(0x01); // show window ??
		writeH(_items.length);
		
		for (L2ItemInstance temp : _items)
		{
			if (temp == null || temp.getItem() == null)
				continue;
			
			writeH(temp.getItem().getType1());
			
			writeD(temp.getObjectId());
			writeD(temp.getItemDisplayId());
			writeD(temp.getLocationSlot()); // T1
			writeQ(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			if (temp.isAugmented())
			{
				writeD(temp.getAugmentation().getAugmentationId());
			}
			else
			{
				writeD(0x00);
			}
			writeD(temp.getMana());
			
			// T1
			writeElementalInfo(temp);
			// T2
			writeD(temp.isTimeLimitedItem() ? (int) (temp.getRemainingTime() / 1000) : -1);
			
			writeEnchantEffectInfo();
		}
	}
	
	@Override
	public String getType()
	{
		return _S__AD_GMVIEWITEMLIST;
	}
}
