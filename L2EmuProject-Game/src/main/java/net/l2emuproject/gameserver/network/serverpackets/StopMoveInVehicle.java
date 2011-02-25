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

/**
 * @author Maktakien
 *
 */
public class StopMoveInVehicle  extends L2GameServerPacket
{
	private final L2Player _activeChar;
	private final int _boatId;
	/**
	 * @param player
	 * @param boatid
	 */
	public StopMoveInVehicle(L2Player player, int boatId)
	{
		_activeChar = player;
		_boatId = boatId;
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0x7f);
		writeD(_activeChar.getObjectId());
		writeD(_boatId);
		writeD(_activeChar.getInVehiclePosition().getX());
		writeD(_activeChar.getInVehiclePosition().getY());
		writeD(_activeChar.getInVehiclePosition().getZ());
		writeD(_activeChar.getPosition().getHeading());
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] 72 StopMoveInVehicle";
	}
}
