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

import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.handler.SkillHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;

public class CombatPointHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.COMBATPOINTHEAL, L2SkillType.CPHEAL_PERCENT };
	
	@Override
	public void useSkill(L2Character actChar, L2Skill skill, L2Character... targets)
	{
		SkillHandler.getInstance().useSkill(L2SkillType.BUFF, actChar, skill, targets);
		
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			double cp = skill.getPower();
			
			if (skill.getSkillType() == L2SkillType.CPHEAL_PERCENT)
				cp = target.getMaxCp() * cp / 100;
			
			// From CT2 u will receive exact CP, you can't go over it, if you have full CP and you get CP buff, you will receive 0CP restored message
			cp = Math.min(cp, target.getMaxCp() - target.getStatus().getCurrentCp());
			
			if (target instanceof L2PcInstance)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
				sm.addNumber((int)cp);
				((L2PcInstance)target).sendPacket(sm);
			}
			
			target.getStatus().setCurrentCp(cp + target.getStatus().getCurrentCp());
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
