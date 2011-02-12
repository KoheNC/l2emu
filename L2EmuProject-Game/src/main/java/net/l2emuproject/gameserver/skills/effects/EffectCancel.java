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
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author decad
 */
public class EffectCancel extends L2Effect
{
	public EffectCancel(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onStart()
	{
		L2Effect[] effects = getEffected().getAllEffects();
		double max = getSkill().getMaxNegatedEffects();
		if (max == 0)
			max = Integer.MAX_VALUE;
		if (effects.length >= max)
			effects = SortEffects(effects);
		double count = 1;
		for (L2Effect e : effects)
		{
			// do not delete signet effects!
			switch (e.getEffectType())
			{
				case SIGNET_GROUND:
				case SIGNET_EFFECT:
					continue;
			}
			if (!e.getSkill().canBeDispeled())
				continue;
			switch (e.getSkill().getSkillType())
			{
				case BUFF:
				case HEAL_PERCENT:
				case RECOVER:
				case REFLECT:
				case COMBATPOINTHEAL:
					break;
				default:
					continue;
			}
			double rate = 1 - (count / max);
			if (rate < 0.33)
				rate = 0.33;
			else if (rate > 0.95)
				rate = 0.95;
			if (Rnd.get(1000) < (rate * 1000))
				e.exit();
			if (count == max)
				break;
			count++;
		}
		return false;
	}

	private L2Effect[] SortEffects(L2Effect[] initial)
	{
		// this is just classic insert sort
		// If u can find better sort for max 20-30 units, rewrite this... :)
		int min, index = 0;
		L2Effect pom;
		for (int i = 0; i < initial.length; i++)
		{
			min = initial[i].getSkill().getMagicLevel();
			for (int j = i; j < initial.length; j++)
			{
				if (initial[j].getSkill().getMagicLevel() <= min)
				{
					min = initial[j].getSkill().getMagicLevel();
					index = j;
				}
			}
			pom = initial[i];
			initial[i] = initial[index];
			initial[index] = pom;
		}
		return initial;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CANCEL;
	}
}
