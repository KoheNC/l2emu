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
package net.l2emuproject.gameserver.skills.l2skills;

import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.events.global.fortsiege.Fort;
import net.l2emuproject.gameserver.events.global.fortsiege.FortManager;
import net.l2emuproject.gameserver.events.global.siege.Castle;
import net.l2emuproject.gameserver.events.global.siege.CastleManager;
import net.l2emuproject.gameserver.manager.TerritoryWarManager;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.system.idfactory.IdFactory;
import net.l2emuproject.gameserver.templates.StatsSet;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2SiegeFlagInstance;

public class L2SkillSiegeFlag extends L2Skill
{
	private final boolean	_isAdvanced;
	private final boolean	_isOutpost;

	public L2SkillSiegeFlag(StatsSet set)
	{
		super(set);
		_isAdvanced = set.getBool("isAdvanced", false);
		_isOutpost = set.getBool("isOutpost", false);
	}

	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (!(activeChar instanceof L2Player))
			return;

		L2Player player = (L2Player) activeChar;

		if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId())
			return;

		// Territory War
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			try
			{
				// Spawn a new flag
				L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(
						(_isOutpost ? 36590 : 35062)), _isAdvanced, _isOutpost);
				flag.setTitle(player.getClan().getName());
				flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
				flag.setHeading(player.getHeading());
				flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
				if (_isOutpost)
					TerritoryWarManager.getInstance().setHQForClan(player.getClan(), flag);
				else
					TerritoryWarManager.getInstance().addClanFlag(player.getClan(), flag);
			}
			catch (Exception e)
			{
				player.sendMessage("Error placing flag:" + e);
				_log.warn("Error placing flag:" + e);
			}
			return;
		}
		// Fortress/Castle siege
		try
		{
			// Spawn a new flag
			L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062),
					_isAdvanced, false);
			flag.setTitle(player.getClan().getName());
			flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
			flag.setHeading(player.getHeading());
			flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			Castle castle = CastleManager.getInstance().getCastle(activeChar);
			Fort fort = FortManager.getInstance().getFort(activeChar);
			if (castle != null)
				castle.getSiege().getFlag(player.getClan()).add(flag);
			else
				fort.getSiege().getFlag(player.getClan()).add(flag);

		}
		catch (Exception e)
		{
			player.sendMessage("Error placing flag:" + e);
			_log.warn("Error placing flag:" + e);
		}
	}
	
	public boolean isAdvanced()
	{
		return _isAdvanced;
	}

	public boolean isOutpost()
	{
		return _isOutpost;
	}
}
