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

import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.geometry.Point3D;

public final class ExMoveToLocationInAirShip extends L2GameServerPacket
{
	private final int		_charObjId;
	private final int		_airShipId;
	private final Point3D	_destination;
	private final int		_heading;

	/**
	 * @param actor
	 * @param destination
	 * @param origin
	 */
	public ExMoveToLocationInAirShip(L2Player player)
	{
		_charObjId = player.getObjectId();
		_airShipId = player.getAirShip().getObjectId();
		_destination = player.getInVehiclePosition();
		_heading = player.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x6D);
		writeD(_charObjId);
		writeD(_airShipId);
		writeD(_destination.getX());
		writeD(_destination.getY());
		writeD(_destination.getZ());
		writeD(_heading);
	}

	@Override
	public final String getType()
	{
		return "[S] 6D MoveToLocationInAirShip";
	}
}
