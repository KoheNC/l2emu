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

import net.l2emuproject.gameserver.model.actor.L2Character;

/**
 * @author Maktakien
 */
public final class VehicleCheckLocation extends L2GameServerPacket
{
	private final L2Character	_boat;

	/**
	 * @param instance
	 */
	public VehicleCheckLocation(L2Character boat)
	{
		_boat = boat;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x6d);
		writeD(_boat.getObjectId());
		writeD(_boat.getX());
		writeD(_boat.getY());
		writeD(_boat.getZ());
		writeD(_boat.getHeading());
	}

	@Override
	public final String getType()
	{
		return "[S] 6D VehicleCheckLocation";
	}
}