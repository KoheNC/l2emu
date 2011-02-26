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
package net.l2emuproject.gameserver.handler.skillhandlers;

import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.handler.ISkillConditionChecker;
import net.l2emuproject.gameserver.instancemanager.CCHManager;
import net.l2emuproject.gameserver.instancemanager.FortSiegeManager;
import net.l2emuproject.gameserver.instancemanager.SiegeManager;
import net.l2emuproject.gameserver.model.entity.CCHSiege;
import net.l2emuproject.gameserver.model.entity.FortSiege;
import net.l2emuproject.gameserver.model.entity.Siege;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillSiegeFlag;
import net.l2emuproject.gameserver.system.idfactory.IdFactory;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2SiegeFlagInstance;
import net.l2emuproject.gameserver.world.zone.L2Zone;

/**
 * @author _drunk_
 * 
 */
public class SiegeFlag extends ISkillConditionChecker
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.SIEGEFLAG };

	@Override
	public boolean checkConditions(L2Character activeChar, L2Skill skill)
	{
		if (!(activeChar instanceof L2Player))
			return false;
		
		final L2Player player = (L2Player) activeChar;
		
		if (player.isInsideZone(L2Zone.FLAG_NO_HQ))
		{
			player.sendPacket(SystemMessageId.NOT_SET_UP_BASE_HERE);
			return false;
		}
		// awful idea
		else if (!SiegeManager.checkIfOkToPlaceFlag(player, true) &&
				!FortSiegeManager.checkIfOkToPlaceFlag(player, true) &&
				!CCHManager.checkIfOkToPlaceFlag(player, true))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
			player.sendPacket(sm);
			return false;
		}
		
		return super.checkConditions(activeChar, skill);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill0, L2Character... targets)
	{
		L2SkillSiegeFlag skill = (L2SkillSiegeFlag)skill0;
		
		if (!(activeChar instanceof L2Player))
			return;

		L2Player player = (L2Player) activeChar;

		Siege siege = SiegeManager.getInstance().getSiege(player);
		FortSiege fsiege = FortSiegeManager.getInstance().getSiege(player);
		CCHSiege csiege = CCHManager.getInstance().getSiege(player);
		// In a siege zone
		if (siege != null && SiegeManager.checkIfOkToPlaceFlag(player, false))
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(35062);
			if (skill != null && template != null)
			{
				// spawn a new flag
				L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), template, skill.isAdvanced(), skill.isOutpost());
				flag.setTitle(player.getClan().getName());
				flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
				flag.setHeading(player.getHeading());
				flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			}
		}
		else if (fsiege != null && FortSiegeManager.checkIfOkToPlaceFlag(player, false))
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(35062);
			if (skill != null && template != null)
			{
				// spawn a new flag
				L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), template, skill.isAdvanced(), skill.isOutpost());
				flag.setTitle(player.getClan().getName());
				flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
				flag.setHeading(player.getHeading());
				flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			}
		}
		else if (csiege != null && CCHManager.checkIfOkToPlaceFlag(player, false))
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(35062);
			if (skill != null && template != null)
			{
				L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), template, skill.isAdvanced(), skill.isOutpost());
				flag.setTitle(player.getClan().getName());
				flag.getStatus().setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
				flag.setHeading(player.getHeading());
				flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
