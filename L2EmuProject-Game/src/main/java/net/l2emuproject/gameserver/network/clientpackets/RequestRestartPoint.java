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

import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.instancemanager.CCHManager;
import net.l2emuproject.gameserver.instancemanager.CastleManager;
import net.l2emuproject.gameserver.instancemanager.ClanHallManager;
import net.l2emuproject.gameserver.instancemanager.FortManager;
import net.l2emuproject.gameserver.instancemanager.FortSiegeManager;
import net.l2emuproject.gameserver.instancemanager.MapRegionManager;
import net.l2emuproject.gameserver.instancemanager.SiegeManager;
import net.l2emuproject.gameserver.instancemanager.TerritoryWarManager;
import net.l2emuproject.gameserver.model.L2SiegeClan;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.l2emuproject.gameserver.model.entity.CCHSiege;
import net.l2emuproject.gameserver.model.entity.Castle;
import net.l2emuproject.gameserver.model.entity.ClanHall;
import net.l2emuproject.gameserver.model.entity.Fort;
import net.l2emuproject.gameserver.model.entity.FortSiege;
import net.l2emuproject.gameserver.model.entity.Siege;
import net.l2emuproject.gameserver.model.mapregion.TeleportWhereType;
import net.l2emuproject.gameserver.model.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.model.world.Location;
import net.l2emuproject.gameserver.model.zone.L2JailZone;
import net.l2emuproject.gameserver.model.zone.L2Zone;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.Die;

public class RequestRestartPoint extends L2GameClientPacket
{
	private static final String	_C__6d_REQUESTRESTARTPOINT	= "[C] 6d RequestRestartPoint";

	protected int				_requestedPointType;
	protected boolean			_continuation;

	/**
	 * packet type id 0x6d
	 * format: c
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_requestedPointType = readD();
	}

	private class DeathTask implements Runnable
	{
		private final L2PcInstance	activeChar;

		public DeathTask(L2PcInstance _activeChar)
		{
			activeChar = _activeChar;
		}

		@Override
		@SuppressWarnings("synthetic-access")
		public void run()
		{
			Location loc = null;
			Siege siege = null;
			FortSiege fsiege = null;
			CCHSiege csiege = null;

			if (activeChar.isInJail())
				_requestedPointType = 27;
			else if (activeChar.isFestivalParticipant())
				_requestedPointType = 5;

			switch (_requestedPointType)
			{
			case 1: // to clanhall
				if (activeChar.getClan() == null || activeChar.getClan().getHasHideout() == 0)
				{
					_log.warn("Player [" + activeChar.getName() + "] called RestartPointPacket - To Clanhall and he doesn't have Clanhall!");
					return;
				}
				ClanHall hideout = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
				if (hideout == null || (hideout.getSiege() != null && hideout.getSiege().getIsInProgress()))
					loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Town);
				else
					loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.ClanHall);

				if (hideout != null && hideout.getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
				{
					activeChar.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP)
							.getLvl());
				}
				break;

			case 2: // to castle
				siege = SiegeManager.getInstance().getSiege(activeChar);
				if (siege != null && siege.getIsInProgress())
				{
					// Siege in progress
					if (siege.checkIsDefender(activeChar.getClan()))
						loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Castle);
					// Just in case you lost castle while being dead.. Port to nearest Town.
					else if (siege.checkIsAttacker(activeChar.getClan()))
						loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Town);
					else
					{
						_log.warn("Player [" + activeChar.getName() + "] called RestartPointPacket - To Castle and he doesn't have Castle!");
						return;
					}
				}
				else
				{
					if (activeChar.getClan() == null || activeChar.getClan().getHasCastle() == 0)
						return;

					loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Castle);
				}
				Castle castle = CastleManager.getInstance().getCastleByOwner(activeChar.getClan());
				if (castle != null && castle.getFunction(Castle.FUNC_RESTORE_EXP) != null)
				{
					activeChar.restoreExp(castle.getFunction(Castle.FUNC_RESTORE_EXP).getLvl());
				}
				break;

			case 3: // to Fortress
				fsiege = FortSiegeManager.getInstance().getSiege(activeChar);
				if (fsiege != null && fsiege.getIsInProgress())
				{
					// Just in case you lost fort while beeing dead.. Port to nearest Town.
					if (fsiege.checkIsAttacker(activeChar.getClan()))
					{
						loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Town);
					}
					else
					{
						_log.warn("Player [" + activeChar.getName() + "] called RestartPointPacket - To Fortress and he doesn't have Fortress!");
						return;
					}
				}
				else
				{
					if (activeChar.getClan() == null || activeChar.getClan().getHasFort() == 0)
						return;

					loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Fortress);
				}
				Fort fort = FortManager.getInstance().getFortByOwner(activeChar.getClan());
				if (fort != null && fort.getFunction(Fort.FUNC_RESTORE_EXP) != null)
				{
					activeChar.restoreExp(fort.getFunction(Fort.FUNC_RESTORE_EXP).getLvl());
				}
				break;

			case 4: // to siege HQ
				L2SiegeClan siegeClan = null;
				siege = SiegeManager.getInstance().getSiege(activeChar);
				fsiege = FortSiegeManager.getInstance().getSiege(activeChar);
				csiege = CCHManager.getInstance().getSiege(activeChar);
				L2SiegeFlagInstance flag = TerritoryWarManager.getInstance().getFlagForClan(activeChar.getClan());

				if (fsiege == null && csiege == null && siege != null && siege.getIsInProgress())
					siegeClan = siege.getAttackerClan(activeChar.getClan());
				else if (siege == null && csiege == null && fsiege != null && fsiege.getIsInProgress())
					siegeClan = fsiege.getAttackerClan(activeChar.getClan());
				else if (siege == null && fsiege == null && csiege != null && csiege.getIsInProgress())
					siegeClan = csiege.getAttackerClan(activeChar.getClan());

				if ((siegeClan == null || siegeClan.getFlag().isEmpty()) && flag == null)
				{
					_log.warn("Player [" + activeChar.getName() + "] called RestartPointPacket - To Siege HQ and he doesn't have Siege HQ!");
					return;
				}
				loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.SiegeFlag);
				break;

			case 5: // Fixed or Player is a festival participant
				boolean can = false;
				if (activeChar.isGM() || activeChar.isFestivalParticipant())
					can = true;
				else if (activeChar.destroyItemByItemId("Fixed Resurrection", Die.FEATHER_OF_BLESSING_1, 1, activeChar, false)
						|| activeChar.destroyItemByItemId("Fixed Resurrection", Die.FEATHER_OF_BLESSING_2, 1, activeChar, false)
						|| activeChar.destroyItemByItemId("Fixed Resurrection", Die.PHOENIX_FEATHER, 1, activeChar, false))
				{
					activeChar.sendPacket(SystemMessageId.USED_FEATHER_TO_RESURRECT);
					can = true;
				}
				if (!can)
				{
					_log.warn("Player [" + activeChar.getName() + "] called RestartPointPacket - Fixed and he isn't GM/festival participant!");
					return;
				}
				if (activeChar.isGM())
					activeChar.restoreExp(100.0);
				loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()); // spawn them where they died
				break;

			case 27: // to jail
				if (!activeChar.isInJail())
					return;
				loc = L2JailZone.JAIL_LOCATION;

				break;

			default: // 0
				if (activeChar.isInsideZone(L2Zone.FLAG_JAIL))
					// From current zones I can't imagine why?
					//|| activeChar.isInsideZone(L2Zone.FLAG_NOESCAPE))
					loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()); // spawn them where they died
				else
					loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Town);
				break;
			}
			// Teleport and revive
			activeChar.setInstanceId(0);
			activeChar.setIsIn7sDungeon(false);
			activeChar.setIsPendingRevive(true);
			activeChar.teleToLocation(loc, true);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.isFakeDeath())
		{
			activeChar.stopFakeDeath(true);
			sendAF();
			return;
		}
		else if (!activeChar.isDead())
		{
			sendAF();
			return;
		}
		else if (!GlobalRestrictions.canRequestRevive(activeChar))
		{
			sendAF();
			return;
		}

		Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		if (castle != null && castle.getSiege().getIsInProgress())
		{
			if (activeChar.getClan() != null && castle.getSiege().checkIsAttacker(activeChar.getClan()))
			{
				// Schedule respawn delay for attacker
				ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getAttackerRespawnDelay());
				if (castle.getSiege().getAttackerRespawnDelay() > 0)
					activeChar.sendMessage("You will be re-spawned in " + castle.getSiege().getAttackerRespawnDelay() / 1000 + " seconds");
				sendAF();
				return;
			}
		}

		// run immediately (no need to schedule)
		new DeathTask(activeChar).run();

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__6d_REQUESTRESTARTPOINT;
	}
}
