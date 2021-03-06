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

/**
 * Format: (ch)ddd
 *
 */
public final class ExPutItemResultForVariationMake extends L2GameServerPacket
{
	private static final String _S__FE_53_EXPUTITEMRESULTFORVARIATIONMAKE = "[S] FE:53 ExPutItemResultForVariationMake";

	private final int _itemObjId;
	private final int _itemId;

	public ExPutItemResultForVariationMake(int itemObjId, int itemId)
	{
		_itemObjId = itemObjId;
		_itemId = itemId;
	}

	/**
	 * @see net.l2emuproject.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x53);
		writeD(_itemObjId);
		writeD(_itemId);
		writeD(1);
	}

	/**
	 * @see net.l2emuproject.gameserver.BasePacket#getPostType()
	 */
	@Override
	public final String getType()
	{
		return _S__FE_53_EXPUTITEMRESULTFORVARIATIONMAKE;
	}
}
