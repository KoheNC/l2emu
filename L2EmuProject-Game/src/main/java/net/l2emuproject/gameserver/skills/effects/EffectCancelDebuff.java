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
package net.l2emuproject.gameserver.skills.effects;

import net.l2emuproject.gameserver.model.L2Effect;
import net.l2emuproject.gameserver.model.skill.L2Skill;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.Formulas;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;

/**
 * @author Angy
 */
public class EffectCancelDebuff extends L2Effect
{
	public EffectCancelDebuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onStart()
	{
		if (getEffected() == null || getEffector() == null 
				|| getEffected().isRaid())
			return false;

		// cancel debuffs with a not 100% chance and not all debuff
		L2Skill skill = getSkill();
		byte shld = 0;
		boolean ss = false;
		boolean sps = false;
		boolean bss = false;
		if (!getEffected().isDead())
		{
			boolean remove = false;
			for (L2Effect e : getEffected().getAllEffects())
			{
				if (e.getSkill().isDebuff())
				{
					remove = Formulas.calcSkillSuccess(getEffector(), getEffected(), skill, shld, ss, sps, bss);
					if (remove)
						e.exit();
				}
			}
		}

		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CANCEL_DEBUFF;
	}
}
