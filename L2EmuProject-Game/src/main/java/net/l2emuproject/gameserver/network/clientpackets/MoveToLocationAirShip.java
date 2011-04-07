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

import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.manager.AirShipManager;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.VehiclePathPoint;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2AirShipInstance;
import net.l2emuproject.gameserver.world.object.position.L2CharPosition;

public final class MoveToLocationAirShip extends L2GameClientPacket
{
	public static final int		MIN_Z							= -895;
	public static final int		MAX_Z							= 6105;
	public static final int		STEP							= 300;

	private static final String	_C__D0_38_MOVETOLOCATIONAIRSHIP	= "[C] D0:38 MoveToLocationAirShip";

	private int					_command;
	private int					_param1;
	private int					_param2							= 0;

	@Override
	protected final void readImpl()
	{
		_command = readD();
		_param1 = readD();
		if (getByteBuffer().remaining() > 0)
			_param2 = readD();
	}

	@Override
	protected final void runImpl()
	{
		final L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (!activeChar.isInAirShip())
			return;

		final L2AirShipInstance ship = activeChar.getAirShip();
		if (!ship.isCaptain(activeChar))
			return;

		int z = ship.getZ();

		switch (_command)
		{
			case 0:
				if (!ship.canBeControlled())
					return;
				if (_param1 < L2World.GRACIA_MAX_X)
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_param1, _param2, z, 0));
				break;
			case 1:
				if (!ship.canBeControlled())
					return;
				ship.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				break;
			case 2:
				if (!ship.canBeControlled())
					return;
				if (z < L2World.GRACIA_MAX_Z)
				{
					z = Math.min(z + STEP, L2World.GRACIA_MAX_Z);
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(ship.getX(), ship.getY(), z, 0));
				}
				break;
			case 3:
				if (!ship.canBeControlled())
					return;
				if (z > L2World.GRACIA_MIN_Z)
				{
					z = Math.max(z - STEP, L2World.GRACIA_MIN_Z);
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(ship.getX(), ship.getY(), z, 0));
				}
				break;
			case 4:
				if (!ship.isInDock() || ship.isMoving())
					return;

				final VehiclePathPoint[] dst = AirShipManager.getInstance().getTeleportDestination(ship.getDockId(), _param1);
				if (dst == null)
					return;

				// Consume fuel, if needed
				final int fuelConsumption = AirShipManager.getInstance().getFuelConsumption(ship.getDockId(), _param1);
				if (fuelConsumption > 0)
				{
					if (fuelConsumption > ship.getFuel())
					{
						activeChar.sendPacket(SystemMessageId.AIRSHIP_CANT_TELEPORT_NOT_ENOUGH_FUEL);
						return;
					}
					ship.setFuel(ship.getFuel() - fuelConsumption);
				}

				ship.executePath(dst);
				break;
		}
	}

	@Override
	public final String getType()
	{
		return _C__D0_38_MOVETOLOCATIONAIRSHIP;
	}
}
