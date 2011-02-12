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

import net.l2emuproject.gameserver.handler.ISkillConditionChecker;
import net.l2emuproject.gameserver.handler.SkillHandler;
import net.l2emuproject.gameserver.instancemanager.CCHManager;
import net.l2emuproject.gameserver.instancemanager.FortSiegeManager;
import net.l2emuproject.gameserver.instancemanager.SiegeManager;
import net.l2emuproject.gameserver.model.L2Skill;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;

/**
 * @author _tomciaaa_
 */
public final class StrSiegeAssault extends ISkillConditionChecker
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.STRSIEGEASSAULT };
	
	@Override
	public boolean checkConditions(L2Character activeChar, L2Skill skill)
	{
		if (!(activeChar instanceof L2PcInstance))
			return false;
		
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		if (!SiegeManager.checkIfOkToUseStriderSiegeAssault(player, false) && !FortSiegeManager.checkIfOkToUseStriderSiegeAssault(player, false))
			return false;
		
		return super.checkConditions(activeChar, skill);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance)activeChar;
		
		if (SiegeManager.checkIfOkToUseStriderSiegeAssault(player, false) ||
				FortSiegeManager.checkIfOkToUseStriderSiegeAssault(player, false) ||
				CCHManager.checkIfOkToUseStriderSiegeAssault(player, false))
		{
			SkillHandler.getInstance().useSkill(L2SkillType.PDAM, activeChar, skill, targets);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
