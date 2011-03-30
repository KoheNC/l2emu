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
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.world.object.L2Player;


/**
 * @author -Wooden-
 */
public class PackageSendableList extends L2GameServerPacket
{
	private static final String			_S__C3_PACKAGESENDABLELIST	= "[S] C3 PackageSendableList";

	private final FastList<L2ItemInstance>	_items;
	private final int						_targetPlayerObjId;
	private final long						_playerAdena;

	public PackageSendableList(L2Player sender, int playerOID)
	{
		_targetPlayerObjId = playerOID;
		_playerAdena = sender.getAdena();

		_items = new FastList<L2ItemInstance>();
		for (L2ItemInstance temp : sender.getInventory().getAvailableItems(true, false))
		{
			if (temp != null && temp.isDepositable(false))
				_items.add(temp);
		}
	}

	/**
	 * @see net.l2emuproject.gameserver.network.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xd2);

		writeD(_targetPlayerObjId);
		writeQ(_playerAdena);
		writeD(_items.size());
		for (L2ItemInstance item : _items) // format inside the for taken from SellList part use should be about the same
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemDisplayId());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(0x00);
			writeH(item.getCustomType2());
			writeD(item.getObjectId()); // Will be used in RequestPackageSend response packet
			//T1
			writeElementalInfo(item); //8x h or d
		}
		_items.clear();
	}

	/**
	 * @see net.l2emuproject.gameserver.BasePacket#getPostType()
	 */
	@Override
	public String getType()
	{
		return _S__C3_PACKAGESENDABLELIST;
	}
}
