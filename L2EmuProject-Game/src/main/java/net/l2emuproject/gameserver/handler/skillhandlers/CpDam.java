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
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.skill.L2Skill;
import net.l2emuproject.gameserver.skills.Formulas;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;

public class CpDam implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.CPDAM };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		for (L2Character target : targets)
		{
			if (target == null)
				continue;

			if (target.isAlikeDead())
			{
				if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isFakeDeath())
					target.stopFakeDeath(true);
				else
					continue;
			}

			final int damage = (int) skill.getPower();

			// Manage attack or cast break of the target (calculating rate, sending message...)
			if (Formulas.calcAtkBreak(target, damage))
			{
				target.breakAttack();
				target.breakCast();
			}

			skill.getEffects(activeChar, target);

			activeChar.sendDamageMessage(target, damage, false, false, false);
			target.getStatus().setCurrentCp(target.getStatus().getCurrentCp() - damage);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
