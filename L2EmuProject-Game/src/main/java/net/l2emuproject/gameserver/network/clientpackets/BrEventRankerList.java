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
package net.l2emuproject.gameserver.network.clientpackets;

import net.l2emuproject.gameserver.network.serverpackets.ExBrLoadEventTopRankers;

/**
 * Halloween rank list client packet.
 * 
 * Format: (ch)ddd
 */
public final class BrEventRankerList extends L2GameClientPacket
{
	private int	_eventId;
	private int	_day;

	@Override
	protected final void readImpl()
	{
		_eventId = readD();
		_day = readD(); // 0 - current, 1 - previous
		readD();
	}

	@Override
	protected final void runImpl()
	{
		// TODO count, bestScore, myScore
		getClient().sendPacket(new ExBrLoadEventTopRankers(_eventId, _day, 0, 0, 0));
	}

	@Override
	public String getType()
	{
		return "[C] D0:7D BrEventRankerList";
	}
}
