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
package net.l2emuproject.gameserver.events.global.siege;

import net.l2emuproject.gameserver.datatables.ResidentialSkillTable;
import net.l2emuproject.gameserver.entity.Entity;
import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWarManager;
import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWarManager.Territory;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.zone.L2SiegeZone;
import net.l2emuproject.gameserver.world.zone.L2Zone;

public abstract class Siegeable<T extends AbstractSiege> extends Entity
{
	protected String _name;
	protected int _ownerId = 0;
	protected L2Clan _formerOwner = null;

	private L2Zone		_zoneHQ;
	private L2SiegeZone	_zoneBF;
	private L2Zone		_zoneDS;
	private L2Zone		_zoneTP;

	private L2Skill[] _residentialSkills = null;

	public Siegeable(int entityId)
	{
		super();
		_residentialSkills = ResidentialSkillTable.getInstance().getSkills(entityId);
	}

	public final String getName()
	{
		return _name;
	}

	public final int getOwnerId()
	{
		return _ownerId;
	}

	public void registerHeadquartersZone(L2Zone zone)
	{
		_zoneHQ = zone;
	}

	public void registerSiegeZone(L2SiegeZone zone)
	{
		_zoneBF = zone;
	}

	public void registerDefenderSpawn(L2Zone zone)
	{
		_zoneDS = zone;
	}

	public void registerTeleportZone(L2Zone zone)
	{
		_zoneTP = zone;
	}

	public void oustAllPlayers()
	{
		for (L2Character player : _zoneTP.getCharactersInside())
		{
			// To random spot in defender spawn zone
			if (player instanceof L2Player)
				player.teleToLocation(_zoneDS.getRandomLocation(), true);
		}
	}

	@Override
	public boolean checkBanish(L2Player cha)
	{
		return cha.getClanId() != getOwnerId() && !cha.isGM();
	}

	/**
	 * Return true if object is inside the zone
	 */
	public boolean checkIfInZoneBattlefield(L2Object obj)
	{
		return checkIfInZoneBattlefield(obj.getX(), obj.getY(), obj.getZ());
	}

	/**
	 * Return true if object is inside the zone
	 */
	public boolean checkIfInZoneBattlefield(int x, int y, int z)
	{
		return getBattlefield().isInsideZone(x, y, z);
	}
	
	/**
	 * Return true if object is inside the zone
	 */
	public boolean checkIfInZoneHeadQuarters(L2Object obj)
	{
		return checkIfInZoneHeadQuarters(obj.getX(), obj.getY(), obj.getZ());
	}

	/**
	 * Return true if object is inside the zone
	 */
	public boolean checkIfInZoneHeadQuarters(int x, int y, int z)
	{
		return getHeadQuarters().isInsideZone(x, y, z);
	}

	public final L2Zone getHeadQuarters()
	{
		return _zoneHQ;
	}
	
	public final L2SiegeZone getBattlefield()
	{
		return _zoneBF;
	}

	public final L2Zone getDefenderSpawn()
	{
		return _zoneDS;
	}

	public final L2Zone getTeleZone()
	{
		return _zoneTP;
	}
	
	public abstract T getSiege();

	public L2Skill[] getResidentialSkills()
	{
		return _residentialSkills;
	}

	public void giveResidentialSkills(L2Player player)
	{
		if (_residentialSkills != null)
		{
			for (L2Skill sk : _residentialSkills)
				player.addSkill(sk, false);
		}
		
		Territory territory = TerritoryWarManager.getInstance().getTerritory(getCastleId());
		if (territory != null && territory.getOwnedWardIds().contains(getCastleId() + 80))
			for (int wardId : territory.getOwnedWardIds())
				if (ResidentialSkillTable.getInstance().getSkills(wardId) != null)
					for (L2Skill sk : ResidentialSkillTable.getInstance().getSkills(wardId))
						player.addSkill(sk, false);
	}

	public void removeResidentialSkills(L2Player player)
	{
		if (_residentialSkills != null)
		{
			for (L2Skill sk : _residentialSkills)
				player.removeSkill(sk, false);
		}
		
		if (TerritoryWarManager.getInstance().getTerritory(getCastleId()) != null)
			for (int wardId : TerritoryWarManager.getInstance().getTerritory(getCastleId()).getOwnedWardIds())
				if (ResidentialSkillTable.getInstance().getSkills(wardId) != null)
					for (L2Skill sk : ResidentialSkillTable.getInstance().getSkills(wardId))
						player.removeSkill(sk, false);
	}
}
