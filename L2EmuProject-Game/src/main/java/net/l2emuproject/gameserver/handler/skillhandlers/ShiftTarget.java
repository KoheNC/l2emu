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

import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Sephiroth
 */
public class ShiftTarget implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.SHIFT_TARGET };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		L2Attackable attackerChar = null;
		L2Npc attacker = null;
		L2Player targetChar = null;

		boolean targetShifted = false;

		for (L2Object target : targets)
		{
			if (target instanceof L2Player)
			{
				targetChar = (L2Player) target;
				break;
			}
		}

		for (L2Object nearby : activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
		{
			if (!targetShifted)
			{
				if (nearby instanceof L2Attackable)
				{
					attackerChar = (L2Attackable) nearby;
					targetShifted = true;
					break;
				}
			}
		}
		
		if (targetShifted && attackerChar != null && targetChar != null)
		{
			attacker = attackerChar;
			int aggro = attackerChar.getHating(activeChar);

			if (aggro == 0)
			{
				if (targetChar.isRunning())
					attacker.setRunning();
				{
					attackerChar.addDamageHate(targetChar, 0, 1);
					attacker.setTarget(targetChar);
					attackerChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, targetChar);
				}
			}
			else
			{
				attackerChar.stopHating(activeChar);
				if (targetChar.isRunning())
					attacker.setRunning();
				{
					attackerChar.addDamageHate(targetChar, 0, aggro);
					attacker.setTarget(targetChar);
					attackerChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, targetChar);
				}
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
