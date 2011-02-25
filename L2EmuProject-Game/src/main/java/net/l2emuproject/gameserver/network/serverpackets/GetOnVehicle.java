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

import net.l2emuproject.gameserver.model.actor.instance.L2BoatInstance;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Maktakien
 *
 */
public class GetOnVehicle extends L2GameServerPacket
{

	private final int _x;
	private final int _y;
	private final int _z;
	private final L2Player _activeChar;
	private final L2BoatInstance _boat;
	/**
	 * @param activeChar
	 * @param boat
	 * @param x
	 * @param y
	 * @param z
	 */
	public GetOnVehicle(L2Player activeChar, L2BoatInstance boat, int x, int y, int z)
	{
		_activeChar = activeChar;
		_boat = boat;
		_x = x;
		_y = y;
		_z = z;
		
		_activeChar.setVehicle(_boat);
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0x6e);
		writeD(_activeChar.getObjectId());
		writeD(_boat.getObjectId());
        writeD(_x);
        writeD(_y);
        writeD(_z);
		
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] 5C GetOnVehicle";
	}
}
