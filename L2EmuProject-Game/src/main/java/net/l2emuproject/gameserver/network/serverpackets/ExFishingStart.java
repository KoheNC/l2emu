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

import net.l2emuproject.gameserver.world.object.L2Character;

/**
 * @author -Wooden-
 */
public final class ExFishingStart extends L2GameServerPacket
{
	private static final String	_S__FE_1E_EXFISHINGSTART	= "[S] FE:1e ExFishingStart [ddddd cccc]";
	private final L2Character	_activeChar;
	private final int			_x, _y, _z, _fishType;
	private final boolean		_isNightLure;

	public ExFishingStart(final L2Character character, final int fishType, final int x, final int y, final int z, final boolean isNightLure)
	{
		_activeChar = character;
		_fishType = fishType;
		_x = x;
		_y = y;
		_z = z;
		_isNightLure = isNightLure;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x1e);
		writeD(_activeChar.getObjectId());
		writeD(_fishType); // fish type
		writeD(_x); // x position
		writeD(_y); // y position
		writeD(_z); // z position
		writeC(_isNightLure ? 0x01 : 0x00); // night lure
		writeC(0x00); //show fish rank result button
	}

	@Override
	public String getType()
	{
		return _S__FE_1E_EXFISHINGSTART;
	}
}
