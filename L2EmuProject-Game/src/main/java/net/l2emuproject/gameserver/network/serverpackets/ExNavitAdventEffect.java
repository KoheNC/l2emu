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
 * @author mochitto
 *
 * Format: (ch)d
 * d: time to left effect in seconds
 */
public final class ExNavitAdventEffect extends L2GameServerPacket
{
	private final int	_timeToLeft;

	public ExNavitAdventEffect(final int timeToLeft)
	{
		_timeToLeft = timeToLeft;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xE0);
		writeD(_timeToLeft);
	}

	@Override
	public String getType()
	{
		return "[S] FE:E0 ExNavitAdventEffect";
	}
}
