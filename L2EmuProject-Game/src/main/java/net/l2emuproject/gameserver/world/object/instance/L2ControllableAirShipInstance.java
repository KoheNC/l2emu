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
package net.l2emuproject.gameserver.world.object.instance;

import java.util.concurrent.Future;

import net.l2emuproject.gameserver.entity.stat.ControllableAirShipStat;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.DeleteObject;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.system.idfactory.IdFactory;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.templates.chars.L2CharTemplate;
import net.l2emuproject.gameserver.world.object.L2Player;

public class L2ControllableAirShipInstance extends L2AirShipInstance
{
	private static final int	HELM		= 13556;
	private static final int	LOW_FUEL	= 40;

	private int					_fuel		= 0;
	private int					_maxFuel	= 0;

	private int					_ownerId;
	private int					_helmId;
	private L2Player		_captain	= null;

	private Future<?>			_consumeFuelTask;
	private Future<?>			_checkTask;

	public L2ControllableAirShipInstance(int objectId, L2CharTemplate template, int ownerId)
	{
		super(objectId, template);

		_ownerId = ownerId;
		_helmId = IdFactory.getInstance().getNextId(); // not forget to release !
	}

	@Override
	public ControllableAirShipStat getStat()
	{
		return (ControllableAirShipStat) super.getStat();
	}

	@Override
	protected final ControllableAirShipStat initStat()
	{
		return new ControllableAirShipStat(this);
	}

	@Override
	public boolean canBeControlled()
	{
		return super.canBeControlled() && !isInDock();
	}

	@Override
	public boolean isOwner(L2Player player)
	{
		if (_ownerId == 0)
			return false;

		return player.getClanId() == _ownerId || player.getObjectId() == _ownerId;
	}

	@Override
	public int getOwnerId()
	{
		return _ownerId;
	}

	@Override
	public boolean isCaptain(L2Player player)
	{
		return _captain != null && player == _captain;
	}

	@Override
	public int getCaptainId()
	{
		return _captain != null ? _captain.getObjectId() : 0;
	}

	@Override
	public int getHelmObjectId()
	{
		return _helmId;
	}

	@Override
	public int getHelmItemId()
	{
		return HELM;
	}

	@Override
	public boolean setCaptain(L2Player player)
	{
		if (player == null)
			_captain = null;
		else
		{
			if (_captain == null && player.getAirShip() == this)
			{
				final int x = player.getInVehiclePosition().getX() - 0x16e;
				final int y = player.getInVehiclePosition().getY();
				final int z = player.getInVehiclePosition().getZ() - 0x6b;
				if (x * x + y * y + z * z > 2500)
				{
					player.sendPacket(SystemMessageId.CANNOT_CONTROL_TOO_FAR);
					return false;
				}
				_captain = player;
				player.broadcastUserInfo();
			}
			else
				return false;
		}
		broadcastFullInfo();
		return true;
	}

	@Override
	public int getFuel()
	{
		return _fuel;
	}

	@Override
	public void setFuel(int f)
	{

		final int old = _fuel;
		if (f < 0)
			_fuel = 0;
		else if (f > _maxFuel)
			_fuel = _maxFuel;
		else
			_fuel = f;

		if (_fuel == 0 && old > 0)
			broadcastToPassengers(new SystemMessage(SystemMessageId.THE_AIRSHIP_FUEL_RUN_OUT));
		else if (_fuel < LOW_FUEL)
			broadcastToPassengers(new SystemMessage(SystemMessageId.THE_AIRSHIP_FUEL_SOON_RUN_OUT));
	}

	@Override
	public int getMaxFuel()
	{
		return _maxFuel;
	}

	@Override
	public void setMaxFuel(int mf)
	{
		_maxFuel = mf;
	}

	@Override
	public void oustPlayer(L2Player player)
	{
		if (player == _captain)
			setCaptain(null); // no need to broadcast userinfo here

		super.oustPlayer(player);
	}

	@Override
	public void onAction(L2Player player, boolean interact)
	{
		super.onAction(player, interact);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_checkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckTask(), 60000, 10000);
		_consumeFuelTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ConsumeFuelTask(), 60000, 60000);
	}

	@Override
	public void deleteMe()
	{
		super.deleteMe();

		if (_checkTask != null)
		{
			_checkTask.cancel(false);
			_checkTask = null;
		}
		if (_consumeFuelTask != null)
		{
			_consumeFuelTask.cancel(false);
			_consumeFuelTask = null;
		}

		try
		{
			broadcastPacket(new DeleteObject(_helmId));
		}
		catch (Exception e)
		{
			_log.warn("Failed decayMe():" + e.getMessage());
		}
	}

	@Override
	public void refreshID()
	{
		super.refreshID();
		IdFactory.getInstance().releaseId(_helmId);
		_helmId = IdFactory.getInstance().getNextId();
	}

	@Override
	public void sendInfo(L2Player activeChar)
	{
		super.sendInfo(activeChar);
		if (_captain != null)
			_captain.sendInfo(activeChar);
	}

	private final class ConsumeFuelTask implements Runnable
	{
		@Override
		public void run()
		{
			int fuel = getFuel();
			if (fuel > 0)
			{
				fuel -= 10;
				if (fuel < 0)
					fuel = 0;

				setFuel(fuel);
				updateAbnormalEffect();
			}
		}
	}

	private final class CheckTask implements Runnable
	{
		@Override
		public void run()
		{
			if (isVisible() && isEmpty() && !isInDock())
				// deleteMe() can't be called from CheckTask because task should not cancel itself
				ThreadPoolManager.getInstance().executeTask(new DecayTask());
		}
	}

	private final class DecayTask implements Runnable
	{
		@Override
		public void run()
		{
			deleteMe();
		}
	}
}
