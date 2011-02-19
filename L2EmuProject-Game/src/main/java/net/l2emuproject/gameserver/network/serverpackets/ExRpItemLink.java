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

/**
 * @author KenM
 */
public final class ExRpItemLink extends L2GameServerPacket
{
	private final static String		S_FE_6C_EXPRPITEMLINK	= "[S] FE:6C ExRpItemLink";
	private final L2ItemInstance	_item;

	public ExRpItemLink(L2ItemInstance item)
	{
		_item = item;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x6c);
		writeD(_item.getObjectId());
		writeD(_item.getItemId());
		writeD(_item.getLocationSlot());
		writeQ(_item.getCount());
		writeH(_item.getItem().getType2());
		writeH(_item.getCustomType1());
		writeH(0x00);
		writeD(_item.getItem().getBodyPart());
		writeH(_item.getEnchantLevel());
		writeH(_item.getCustomType2());
		if (_item.isAugmented())
			writeD(_item.getAugmentation().getAugmentationId());
		else
			writeD(0x00);
		writeD(_item.getMana());
		writeD(_item.isTimeLimitedItem() ? (int) (_item.getRemainingTime() / 1000) : -9999);
		// T1
		writeElementalInfo(_item);

		writeEnchantEffectInfo();
	}

	@Override
	public final String getType()
	{
		return S_FE_6C_EXPRPITEMLINK;
	}
}
