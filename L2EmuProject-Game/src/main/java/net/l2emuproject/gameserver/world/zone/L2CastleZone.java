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
package net.l2emuproject.gameserver.world.zone;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.instancemanager.CastleManager;
import net.l2emuproject.gameserver.instancemanager.SiegeManager;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.model.entity.Castle;
import net.l2emuproject.gameserver.model.entity.Siege;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.object.L2Character;

public class L2CastleZone extends SiegeableEntityZone
{
	@Override
	protected void register() throws Exception
	{
		_entity = initCastle();
		_entity.registerZone(this);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(FLAG_CASTLE, true);
		alertCastle(character, true);
		super.onEnter(character);

		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			L2Clan clan = player.getClan();
			if (clan != null)
			{
				Siege s = getSiege();
				if (s.getIsInProgress() && (s.checkIsAttacker(clan) || s.checkIsDefender(clan)))
				{
					player.startFameTask(Config.CASTLE_ZONE_FAME_TASK_FREQUENCY * 1000, Config.CASTLE_ZONE_FAME_AQUIRE_POINTS);
				}
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(FLAG_CASTLE, false);

		super.onExit(character);

		if (character instanceof L2PcInstance)
			((L2PcInstance) character).stopFameTask();
	}

	private void alertCastle(L2Character character, boolean entering)
	{
		Castle castle = CastleManager.getInstance().getCastleById(_entity.getCastleId());
		int ownerId = castle.getOwnerId();

		Siege siege = SiegeManager.getInstance().getSiege(character);
		if (siege != null && siege.getIsInProgress())
			return;
		else if (ownerId == 0)
			return;
		else if (castle.getFunction(Castle.FUNC_SECURITY) == null)
			return;
		else if (character.isInInstance())
			return;
		else if (character instanceof L2PcInstance)
		{
			if (((L2PcInstance) character).isGM())
				return;
		}
		else
			return;

		L2PcInstance activeChar = (L2PcInstance) character;
		L2Clan castleClan = ClanTable.getInstance().getClan(ownerId);
		L2Clan activeCharClan = activeChar.getClan();
		SystemMessage sm = null;

		if (entering)
			sm = SystemMessage.sendString("Castle Alert: " + activeChar.getName() + " has entered the castle's premises!");
		else
			sm = SystemMessage.sendString("Castle Alert: " + activeChar.getName() + " has exited the castle's premises!");

		switch (castle.getFunction(Castle.FUNC_SECURITY).getLvl())
		{
			case 1: // low
				if (activeCharClan != null)
				{
					if (activeCharClan == castleClan)
						return;
					else if (castleClan.getAllyId() != 0)
						if (activeCharClan.getAllyId() == castleClan.getAllyId())
							return;
				}
				castleClan.broadcastToOnlineMembers(sm);
				break;
			case 2: // med
				if (activeCharClan != null)
					if (activeCharClan == castleClan)
						return;
				castleClan.broadcastToOnlineMembers(sm);
				break;
			case 3: // high
				castleClan.broadcastToOnlineMembers(sm);
				break;
			default: // off
				break;
		}
		return;
	}
}
