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
import net.l2emuproject.gameserver.services.attribute.Attributes;

/**
 * @author  Kerberos
 */
public final class ExChooseInventoryAttributeItem extends L2GameServerPacket
{
	private final int	_itemId;
	private final byte	_attribute;
	private final int	_level;

	public ExChooseInventoryAttributeItem(L2ItemInstance item)
	{
		_itemId = item.getItemId();
		_attribute = Attributes.getItemElementById(_itemId);
		if (_attribute == Attributes.NONE)
			throw new IllegalArgumentException("Undefined Atribute item: " + item);
		_level = Attributes.getMaxAttributeLevelById(_itemId);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x62);
		writeD(_itemId);
		// Structure for now
		// Must be 0x01 for stone/crystal attribute type
		writeD(_attribute == Attributes.FIRE ? 1 : 0); // Fire
		writeD(_attribute == Attributes.WATER ? 1 : 0); // Water
		writeD(_attribute == Attributes.WIND ? 1 : 0); // Wind
		writeD(_attribute == Attributes.EARTH ? 1 : 0); // Earth
		writeD(_attribute == Attributes.HOLY ? 1 : 0); // Holy
		writeD(_attribute == Attributes.DARK ? 1 : 0); // Unholy
		writeD(_level); // Item max attribute level
	}

	@Override
	public final String getType()
	{
		return "[S] FE:62 ExChooseInventoryAttributeItem";
	}
}
