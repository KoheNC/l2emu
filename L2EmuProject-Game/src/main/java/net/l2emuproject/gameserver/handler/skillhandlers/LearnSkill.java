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

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillLearnSkill;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

public class LearnSkill implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.LEARN_SKILL };
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill0, L2Character... targets)
	{
		L2SkillLearnSkill skill = (L2SkillLearnSkill)skill0;
		
		if (!(activeChar instanceof L2Player))
			return;
		
		final L2Player player = ((L2Player)activeChar);
		
		for (int i = 0; i < skill.getNewSkillId().length; i++)
		{
			if (player.getSkillLevel(skill.getNewSkillId()[i]) < 0 && skill.getNewSkillId()[i] != 0)
			{
				L2Skill newSkill = SkillTable.getInstance().getInfo(skill.getNewSkillId()[i], skill.getNewSkillLvl()[i]);
				if (newSkill != null)
					player.addSkill(newSkill, true);
			}
		}
	}
}
