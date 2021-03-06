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
package net.l2emuproject.gameserver.world.object;

import java.util.Iterator;
import java.util.List;

import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.entity.ai.L2CharacterAI;
import net.l2emuproject.gameserver.entity.stat.VehicleStat;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.L2GameServerPacket;
import net.l2emuproject.gameserver.system.taskmanager.MovementController;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.system.time.GameTimeController;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.templates.chars.L2CharTemplate;
import net.l2emuproject.gameserver.templates.item.L2Weapon;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.L2WorldRegion;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.VehiclePathPoint;
import net.l2emuproject.gameserver.world.knownlist.VehicleKnownList;
import net.l2emuproject.gameserver.world.mapregion.MapRegionManager;
import net.l2emuproject.gameserver.world.mapregion.TeleportWhereType;
import net.l2emuproject.gameserver.world.object.position.L2CharPosition;
import net.l2emuproject.util.SingletonList;

/**
 * @author DS
 *	remade for L2EmuProject by lord_rex
 */
public abstract class L2Vehicle extends L2Character
{
	protected int					_dockId			= 0;
	protected final List<L2Player>	_passengers		= new SingletonList<L2Player>();
	protected Location				_oustLoc		= null;
	private Runnable				_engine			= null;

	protected VehiclePathPoint[]	_currentPath	= null;
	protected int					_runState		= 0;

	public L2Vehicle(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		setIsFlying(true);
	}

	public boolean isBoat()
	{
		return false;
	}

	public boolean isAirShip()
	{
		return false;
	}

	public boolean canBeControlled()
	{
		return _engine == null;
	}

	public void registerEngine(Runnable r)
	{
		_engine = r;
	}

	public void runEngine(int delay)
	{
		if (_engine != null)
			ThreadPoolManager.getInstance().scheduleGeneral(_engine, delay);
	}

	public void executePath(VehiclePathPoint[] path)
	{
		_runState = 0;
		_currentPath = path;

		if (_currentPath != null && _currentPath.length > 0)
		{
			final VehiclePathPoint point = _currentPath[0];
			if (point.getMoveSpeed() > 0)
				getStat().setMoveSpeed(point.getMoveSpeed());
			if (point.getRotationSpeed() > 0)
				getStat().setRotationSpeed(point.getRotationSpeed());

			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(point.getX(), point.getY(), point.getZ(), 0));
			return;
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		_move = null;

		if (_currentPath != null)
		{
			_runState++;
			if (_runState < _currentPath.length)
			{
				final VehiclePathPoint point = _currentPath[_runState];
				if (!isMovementDisabled())
				{
					if (point.getMoveSpeed() == 0)
					{
						teleToLocation(point.getX(), point.getY(), point.getZ(), point.getRotationSpeed(), false);
						_currentPath = null;
					}
					else
					{
						if (point.getMoveSpeed() > 0)
							getStat().setMoveSpeed(point.getMoveSpeed());
						if (point.getRotationSpeed() > 0)
							getStat().setRotationSpeed(point.getRotationSpeed());

						MoveData m = new MoveData();
						m.disregardingGeodata = false;
						m.onGeodataPathIndex = -1;
						m._xDestination = point.getX();
						m._yDestination = point.getY();
						m._zDestination = point.getZ();
						m._heading = 0;

						final double dx = point.getX() - getX();
						final double dy = point.getY() - getY();
						final double distance = Math.sqrt(dx * dx + dy * dy);
						if (distance > 1) // vertical movement heading check
							setHeading(Util.calculateHeadingFrom(getX(), getY(), point.getX(), point.getY()));

						m._moveStartTime = GameTimeController.getGameTicks();
						_move = m;

						MovementController.getInstance().add(this);

						return true;
					}
				}
			}
			else
				_currentPath = null;
		}

		runEngine(10);
		return false;
	}

	@Override
	protected final VehicleKnownList initKnownList()
	{
		return new VehicleKnownList(this);
	}

	@Override
	public final VehicleKnownList getKnownList()
	{
		return (VehicleKnownList) _knownList;
	}

	@Override
	protected VehicleStat initStat()
	{
		return new VehicleStat(this);
	}

	@Override
	public VehicleStat getStat()
	{
		return (VehicleStat) super.getStat();
	}

	public boolean isInDock()
	{
		return _dockId > 0;
	}

	public int getDockId()
	{
		return _dockId;
	}

	public void setInDock(int d)
	{
		_dockId = d;
	}

	public void setOustLoc(Location loc)
	{
		_oustLoc = loc;
	}

	public Location getOustLoc()
	{
		return (Location) (_oustLoc != null ? _oustLoc : MapRegionManager.getInstance().getTeleToLocation(getActingPlayer(), TeleportWhereType.Town)); // TODO: Check...
	}

	public void oustPlayers()
	{
		L2Player player;

		// Use iterator because oustPlayer will try to remove player from _passengers 
		final Iterator<L2Player> iter = _passengers.iterator();
		while (iter.hasNext())
		{
			player = iter.next();
			iter.remove();
			if (player != null)
				oustPlayer(player);
		}
	}

	public void oustPlayer(L2Player player)
	{
		player.setVehicle(null);
		player.setInVehiclePosition(null);
		removePassenger(player);
	}

	public boolean addPassenger(L2Player player)
	{
		if (player == null || _passengers.contains(player))
			return false;

		// already in other vehicle
		if (player.getVehicle() != null && player.getVehicle() != this)
			return false;

		_passengers.add(player);
		return true;
	}

	public void removePassenger(L2Player player)
	{
		try
		{
			_passengers.remove(player);
		}
		catch (Exception e)
		{
		}
	}

	public boolean isEmpty()
	{
		return _passengers.isEmpty();
	}

	public List<L2Player> getPassengers()
	{
		return _passengers;
	}

	public void broadcastToPassengers(L2GameServerPacket sm)
	{
		for (L2Player player : _passengers)
		{
			if (player != null)
				player.sendPacket(sm);
		}
	}

	/**
	 * Consume ticket(s) and teleport player from boat if no correct ticket
	 * @param itemId Ticket itemId
	 * @param count Ticket count
	 * @param oustX
	 * @param oustY
	 * @param oustZ
	 */
	public void payForRide(int itemId, int count, int oustX, int oustY, int oustZ)
	{
		final Iterable<L2Player> passengers = getKnownList().getKnownPlayersInRadius(1000);
		if (passengers != null)
		{
			L2ItemInstance ticket;
			InventoryUpdate iu;
			for (L2Player player : passengers)
			{
				if (player == null)
					continue;
				if (player.isInBoat() && player.getBoat() == this)
				{
					if (itemId > 0)
					{
						ticket = player.getInventory().getItemByItemId(itemId);
						if (ticket == null || player.getInventory().destroyItem("Boat", ticket, count, player, this) == null)
						{
							player.sendPacket(SystemMessageId.NOT_CORRECT_BOAT_TICKET);
							player.teleToLocation(oustX, oustY, oustZ, true);
							continue;
						}
						iu = new InventoryUpdate();
						iu.addModifiedItem(ticket);
						player.sendPacket(iu);
					}
					addPassenger(player);
				}
			}
		}
	}

	@Override
	public boolean updatePosition(int gameTicks)
	{
		final boolean result = super.updatePosition(gameTicks);

		for (L2Player player : _passengers)
		{
			if (player != null && player.getVehicle() == this)
			{
				player.getPosition().setXYZ(getX(), getY(), getZ());
				player.revalidateZone(false);
			}
		}

		return result;
	}

	@Override
	public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset)
	{
		if (isMoving())
			stopMove(null, false);

		setIsTeleporting(true);

		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

		for (L2Player player : _passengers)
		{
			if (player != null)
				player.teleToLocation(x, y, z);
		}

		decayMe();
		getPosition().setXYZ(x, y, z);

		// temporary fix for heading on teleports
		if (heading != 0)
			getPosition().setHeading(heading);

		onTeleported();
		revalidateZone(true);
	}

	@Override
	public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
	{
		_move = null;
		if (pos != null)
		{
			getPosition().setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
			revalidateZone(true);
		}

		if (updateKnownObjects)
			getKnownList().updateKnownObjects();
	}

	@Override
	public void deleteMe()
	{
		_engine = null;

		try
		{
			if (isMoving())
				stopMove(null);
		}
		catch (Exception e)
		{
			_log.error("Failed stopMove().", e);
		}

		try
		{
			oustPlayers();
		}
		catch (Exception e)
		{
			_log.error("Failed oustPlayers().", e);
		}

		final L2WorldRegion oldRegion = getWorldRegion();

		try
		{
			decayMe();
		}
		catch (Exception e)
		{
			_log.error("Failed decayMe().", e);
		}

		if (oldRegion != null)
			oldRegion.removeFromZones(this);

		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (Exception e)
		{
			_log.error("Failed cleaning knownlist.", e);
		}

		// Remove L2Object object from _allObjects of L2World
		L2World.getInstance().removeObject(this);

		super.deleteMe();
	}

	@Override
	public abstract void broadcastFullInfoImpl();

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public int getLevel()
	{
		return 0;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	protected L2CharacterAI initAI()
	{
		return new L2CharacterAI(new AIAccessor());
	}

	public final class AIAccessor extends L2Character.AIAccessor
	{
	}
}
