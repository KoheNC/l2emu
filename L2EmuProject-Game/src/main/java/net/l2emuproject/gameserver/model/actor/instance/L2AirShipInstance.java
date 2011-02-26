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
package net.l2emuproject.gameserver.model.actor.instance;

import net.l2emuproject.gameserver.entity.ai.L2AirShipAI;
import net.l2emuproject.gameserver.instancemanager.AirShipManager;
import net.l2emuproject.gameserver.network.serverpackets.ExAirShipInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExGetOffAirShip;
import net.l2emuproject.gameserver.network.serverpackets.ExGetOnAirShip;
import net.l2emuproject.gameserver.network.serverpackets.ExMoveToLocationAirShip;
import net.l2emuproject.gameserver.network.serverpackets.ExStopMoveAirShip;
import net.l2emuproject.gameserver.templates.chars.L2CharTemplate;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Vehicle;
import net.l2emuproject.gameserver.world.object.position.L2CharPosition;
import net.l2emuproject.tools.geometry.Point3D;

/**
 * Flying airships. Very similar to Maktakien boats (see L2BoatInstance) but these do fly :P
 *
 * @author  DrHouse, reworked by DS
 */
public class L2AirShipInstance extends L2Vehicle
{
	public L2AirShipInstance(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		
		setAI(new L2AirShipAI(new AIAccessor()));
	}

	@Override
	public boolean isAirShip()
	{
		return true;
	}

	public boolean isOwner(L2Player player)
	{
		return false;
	}

	public int getOwnerId()
	{
		return 0;
	}

	public boolean isCaptain(L2Player player)
	{
		return false;
	}

	public int getCaptainId()
	{
		return 0;
	}

	public int getHelmObjectId()
	{
		return 0;
	}

	public int getHelmItemId()
	{
		return 0;
	}

	public boolean setCaptain(L2Player player)
	{
		return false;
	}

	public int getFuel()
	{
		return 0;
	}

	public void setFuel(int f)
	{

	}

	public int getMaxFuel()
	{
		return 0;
	}

	public void setMaxFuel(int mf)
	{

	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		final boolean result = super.moveToNextRoutePoint();
		if (result)
			broadcastPacket(new ExMoveToLocationAirShip(this));

		return result;
	}

	@Override
	public boolean addPassenger(L2Player player)
	{
		if (!super.addPassenger(player))
			return false;

		player.setVehicle(this);
		player.setInVehiclePosition(new Point3D(0, 0, 0));
		player.broadcastPacket(new ExGetOnAirShip(player, this));
		player.getKnownList().removeAllKnownObjects();
		player.getPosition().setXYZ(getX(), getY(), getZ());
		player.revalidateZone(true);
		return true;
	}

	@Override
	public void oustPlayer(L2Player player)
	{
		super.oustPlayer(player);
		final Location loc = getOustLoc();
		if (player.isOnline() > 0)
		{
			player.broadcastPacket(new ExGetOffAirShip(player, this, loc.getX(), loc.getY(), loc.getZ()));
			player.getKnownList().removeAllKnownObjects();
			player.getPosition().setXYZ(loc.getX(), loc.getY(), loc.getZ());
			player.revalidateZone(true);
		}
		else
			player.getPosition().setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
	}

	@Override
	public void deleteMe()
	{
		super.deleteMe();
		AirShipManager.getInstance().removeAirShip(this);
	}

	@Override
	public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
	{
		super.stopMove(pos, updateKnownObjects);

		broadcastPacket(new ExStopMoveAirShip(this));
	}

	@Override
	public final void broadcastFullInfoImpl()
	{
		broadcastPacket(new ExAirShipInfo(this));
	}

	@Override
	public void sendInfo(L2Player activeChar)
	{
		activeChar.sendPacket(new ExAirShipInfo(this));
	}
}