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
 * @author Kerberos
 * JIV update 27.8.10
 */
public final class ExValidateLocationInAirShip extends L2GameServerPacket
{
	private final L2Player	_activeChar;
	private final int			shipId, x, y, z, h;

	public ExValidateLocationInAirShip(L2Player player)
	{
		_activeChar = player;
		shipId = _activeChar.getAirShip().getObjectId();
		x = player.getInVehiclePosition().getX();
		y = player.getInVehiclePosition().getY();
		z = player.getInVehiclePosition().getZ();
		h = player.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x6F);
		writeD(_activeChar.getObjectId());
		writeD(shipId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(h);
	}

	@Override
	public final String getType()
	{
		return "[S] FE:6F ExValidateLocationInAirShip";
	}
}
