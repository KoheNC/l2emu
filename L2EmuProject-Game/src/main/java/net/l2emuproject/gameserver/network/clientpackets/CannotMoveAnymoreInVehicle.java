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

import net.l2emuproject.gameserver.network.serverpackets.StopMoveInVehicle;
import net.l2emuproject.gameserver.system.util.Broadcast;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.geometry.Point3D;


/**
 * Similarly to CannotMoveAnymore, this one is sent when a character is on a boat.
 * 
 * @author Maktakien
 */
public class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
	private static final String	_C__CANNOTMOVEANYMOREINVEHICLE	= "[C] 76 CannotMoveAnymoreInVehicle c[ddddd]";

	private int					_x;
	private int					_y;
	private int					_z;
	private int					_heading;
	private int					_boatId;

    @Override
    protected void readImpl()
    {
        _boatId = readD();
        _x = readD();
        _y = readD();
        _z = readD();
        _heading = readD();
    }

	@Override
    protected void runImpl()
	{
		L2Player player = getActiveChar();
		if (player == null)
			return;

		if (player.isInBoat() && player.getBoat().getObjectId() == _boatId)
		{
			player.setInVehiclePosition(new Point3D(_x, _y, _z));
			player.getPosition().setHeading(_heading);
			StopMoveInVehicle stop = new StopMoveInVehicle(player, _boatId);
			Broadcast.toSelfAndKnownPlayers(player, stop);
			//XXX: is PartyMemberPosition necessary here or it's auto when on boat?
		}

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__CANNOTMOVEANYMOREINVEHICLE;
	}
}
