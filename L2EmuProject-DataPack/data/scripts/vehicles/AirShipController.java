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
package vehicles;

import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.instancemanager.AirShipManager;
import net.l2emuproject.gameserver.instancemanager.ZoneManager;
import net.l2emuproject.gameserver.model.VehiclePathPoint;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2AirShipInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2ControllableAirShipInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.zone.L2ScriptZone;
import net.l2emuproject.gameserver.world.zone.L2Zone;

public abstract class AirShipController extends Quest
{
	public static final Log					_log						= LogFactory.getLog(AirShipController.class);

	protected int							_dockZone					= 0;

	protected int							_shipSpawnX					= 0;
	protected int							_shipSpawnY					= 0;
	protected int							_shipSpawnZ					= 0;
	protected int							_shipHeading				= 0;

	protected Location						_oustLoc					= null;

	protected int							_locationId					= 0;
	protected VehiclePathPoint[]			_arrivalPath				= null;
	protected VehiclePathPoint[]			_departPath					= null;

	protected VehiclePathPoint[][]			_teleportsTable				= null;
	protected int[]							_fuelTable					= null;

	protected int							_movieId					= 0;

	protected boolean						_isBusy						= false;

	protected L2ControllableAirShipInstance	_dockedShip					= null;

	private final Runnable					_decayTask					= new DecayTask();
	private final Runnable					_departTask					= new DepartTask();
	private Future<?>						_departSchedule				= null;

	private NpcSay							_arrivalMessage				= null;

	private static final int				DEPART_INTERVAL				= 300000;																				// 5 min

	private static final int				LICENSE						= 13559;
	private static final int				STARSTONE					= 13277;
	private static final int				SUMMON_COST					= 5;

	private static final SystemMessage		SM_ALREADY_EXISTS			= new SystemMessage(SystemMessageId.THE_AIRSHIP_IS_ALREADY_EXISTS);
	private static final SystemMessage		SM_ALREADY_SUMMONED			= new SystemMessage(SystemMessageId.ANOTHER_AIRSHIP_ALREADY_SUMMONED);
	private static final SystemMessage		SM_NEED_LICENSE				= new SystemMessage(SystemMessageId.THE_AIRSHIP_NEED_LICENSE_TO_SUMMON);
	private static final SystemMessage		SM_NEED_CLANLVL5			= new SystemMessage(SystemMessageId.THE_AIRSHIP_NEED_CLANLVL_5_TO_SUMMON);
	private static final SystemMessage		SM_NO_PRIVS					= new SystemMessage(SystemMessageId.THE_AIRSHIP_NO_PRIVILEGES);
	private static final SystemMessage		SM_ALREADY_USED				= new SystemMessage(SystemMessageId.THE_AIRSHIP_ALREADY_USED);
	private static final SystemMessage		SM_LICENSE_ALREADY_ACQUIRED	= new SystemMessage(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ALREADY_ACQUIRED);
	private static final SystemMessage		SM_LICENSE_ENTERED			= new SystemMessage(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ENTERED);
	private static final SystemMessage		SM_NEED_MORE				= new SystemMessage(SystemMessageId.THE_AIRSHIP_NEED_MORE_S1).addItemName(STARSTONE);

	private static final String				ARRIVAL_MSG					= "The airship has been summoned. It will automatically depart in 5 minutes";

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("summon".equalsIgnoreCase(event))
		{
			if (_dockedShip != null)
			{
				if (_dockedShip.isOwner(player))
					player.sendPacket(SM_ALREADY_EXISTS);
				return null;
			}
			if (_isBusy)
			{
				player.sendPacket(SM_ALREADY_SUMMONED);
				return null;
			}
			if ((player.getClanPrivileges() & L2Clan.CP_CL_SUMMON_AIRSHIP) != L2Clan.CP_CL_SUMMON_AIRSHIP)
			{
				player.sendPacket(SM_NO_PRIVS);
				return null;
			}
			int ownerId = player.getClanId();
			if (!AirShipManager.getInstance().hasAirShipLicense(ownerId))
			{
				player.sendPacket(SM_NEED_LICENSE);
				return null;
			}
			if (AirShipManager.getInstance().hasAirShip(ownerId))
			{
				player.sendPacket(SM_ALREADY_USED);
				return null;
			}
			if (!player.destroyItemByItemId("AirShipSummon", STARSTONE, SUMMON_COST, npc, true))
			{
				player.sendPacket(SM_NEED_MORE);
				return null;
			}

			_isBusy = true;
			final L2AirShipInstance ship = AirShipManager.getInstance().getNewAirShip(_shipSpawnX, _shipSpawnY, _shipSpawnZ, _shipHeading, ownerId);
			if (ship != null)
			{
				if (_arrivalPath != null)
					ship.executePath(_arrivalPath);

				if (_arrivalMessage == null)
					_arrivalMessage = new NpcSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout.getId(), npc.getNpcId(), ARRIVAL_MSG);

				npc.broadcastPacket(_arrivalMessage);
			}
			else
				_isBusy = false;

			return null;
		}
		else if ("board".equalsIgnoreCase(event))
		{
			if (player.getPlayerTransformation().isTransformed())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_TRANSFORMED);
				return null;
			}
			else if (player.isParalyzed())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_PETRIFIED);
				return null;
			}
			else if (player.isDead())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_DEAD);
				return null;
			}
			else if (player.getPlayerFish().isFishing())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_FISHING);
				return null;
			}
			else if (player.getPvpFlag() != 0)
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_BATTLE);
				return null;
			}
			else if (player.getPlayerDuel().isInDuel())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_IN_A_DUEL);
				return null;
			}
			else if (player.isSitting())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_SITTING);
				return null;
			}
			else if (player.isCastingNow())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_SKILL_CASTING);
				return null;
			}
			else if (player.isCursedWeaponEquipped())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_A_CURSED_WEAPON_IS_EQUIPPED);
				return null;
			}
			else if (player.isCombatFlagEquipped())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_HOLDING_A_FLAG);
				return null;
			}
			else if (player.getPet() != null)
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_A_PET_OR_A_SERVITOR_IS_SUMMONED);
				return null;
			}
			else if (player.isFlyingMounted())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_NOT_MEET_REQUEIREMENTS);
				return null;
			}

			if (_dockedShip != null)
				_dockedShip.addPassenger(player);

			return null;
		}
		else if ("register".equalsIgnoreCase(event))
		{
			if (player.getClan() == null || player.getClan().getLevel() < 5)
			{
				player.sendPacket(SM_NEED_CLANLVL5);
				return null;
			}
			if (!player.isClanLeader())
			{
				player.sendPacket(SM_NO_PRIVS);
				return null;
			}
			final int ownerId = player.getClanId();
			if (AirShipManager.getInstance().hasAirShipLicense(ownerId))
			{
				player.sendPacket(SM_LICENSE_ALREADY_ACQUIRED);
				return null;
			}
			if (!player.destroyItemByItemId("AirShipLicense", LICENSE, 1, npc, true))
			{
				player.sendPacket(SM_NEED_MORE);
				return null;
			}

			AirShipManager.getInstance().registerLicense(ownerId);
			player.sendPacket(SM_LICENSE_ENTERED);
			return null;
		}
		else
			return event;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);

		return npc.getNpcId() + ".htm";
	}

	@Override
	public String onEnterZone(L2Character character, L2Zone zone)
	{
		if (character instanceof L2ControllableAirShipInstance)
		{
			if (_dockedShip == null)
			{
				_dockedShip = (L2ControllableAirShipInstance) character;
				_dockedShip.setInDock(_dockZone);
				_dockedShip.setOustLoc(_oustLoc);

				// Ship is not empty - display movie to passengers and dock
				if (!_dockedShip.isEmpty())
				{
					if (_movieId != 0)
					{
						for (L2PcInstance passenger : _dockedShip.getPassengers())
						{
							if (passenger != null)
								passenger.showQuestMovie(_movieId);
						}
					}

					ThreadPoolManager.getInstance().scheduleGeneral(_decayTask, 1000);
				}
				else
					_departSchedule = ThreadPoolManager.getInstance().scheduleGeneral(_departTask, DEPART_INTERVAL);
			}
		}
		return null;
	}

	@Override
	public String onExitZone(L2Character character, L2Zone zone)
	{
		if (character instanceof L2ControllableAirShipInstance)
		{
			if (character.equals(_dockedShip))
			{
				if (_departSchedule != null)
				{
					_departSchedule.cancel(false);
					_departSchedule = null;
				}

				_dockedShip.setInDock(0);
				_dockedShip = null;
				_isBusy = false;
			}
		}
		return null;
	}

	protected final void validityCheck()
	{
		L2Zone zone = ZoneManager.getInstance().getZoneById(_dockZone);
		if (zone == null || !(zone instanceof L2ScriptZone))
		{
			_log.warn(getName() + ": Invalid zone " + _dockZone + ", controller disabled");
			_isBusy = true;
			return;
		}

		VehiclePathPoint p;
		if (_arrivalPath != null)
		{
			if (_arrivalPath.length == 0)
			{
				_log.warn(getName() + ": Zero arrival path length.");
				_arrivalPath = null;
			}
			else
			{
				p = _arrivalPath[_arrivalPath.length - 1];
				if (!zone.isInsideZone(p.getX(), p.getY(), p.getZ()))
				{
					_log.warn(getName() + ": Arrival path finish point (" + p.getX() + "," + p.getY() + "," + p.getZ() + ") not in zone " + _dockZone);
					_arrivalPath = null;
				}
			}
		}
		if (_arrivalPath == null)
		{
			if (!ZoneManager.getInstance().getZoneById(_dockZone).isInsideZone(_shipSpawnX, _shipSpawnY, _shipSpawnZ))
			{
				_log.warn(getName() + ": Arrival path is null and spawn point not in zone " + _dockZone + ", controller disabled");
				_isBusy = true;
				return;
			}
		}

		if (_departPath != null)
		{
			if (_departPath.length == 0)
			{
				_log.warn(getName() + ": Zero depart path length.");
				_departPath = null;
			}
			else
			{
				p = _departPath[_departPath.length - 1];
				if (zone.isInsideZone(p.getX(), p.getY(), p.getZ()))
				{
					_log.warn(getName() + ": Departure path finish point (" + p.getX() + "," + p.getY() + "," + p.getZ() + ") in zone " + _dockZone);
					_departPath = null;
				}
			}
		}

		if (_teleportsTable != null)
		{
			if (_fuelTable == null)
				_log.warn(getName() + ": Fuel consumption not defined.");
			else
			{
				if (_teleportsTable.length != _fuelTable.length)
					_log.warn(getName() + ": Fuel consumption not match teleport list.");
				else
					AirShipManager.getInstance().registerAirShipTeleportList(_dockZone, _locationId, _teleportsTable, _fuelTable);
			}
		}
	}

	private final class DecayTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_dockedShip != null)
				_dockedShip.deleteMe();
		}
	}

	private final class DepartTask implements Runnable
	{
		@Override
		public void run()
		{
			if (_dockedShip != null && _dockedShip.isInDock() && !_dockedShip.isMoving())
			{
				if (_departPath != null)
					_dockedShip.executePath(_departPath);
				else
					_dockedShip.deleteMe();
			}
		}
	}

	public AirShipController(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}
}