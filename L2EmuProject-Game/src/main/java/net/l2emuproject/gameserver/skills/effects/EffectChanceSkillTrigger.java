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
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.skills.ChanceCondition;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.IChanceSkillTrigger;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;

public final class EffectChanceSkillTrigger extends EffectBuff implements IChanceSkillTrigger
{
	public EffectChanceSkillTrigger(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	protected boolean onStart()
	{
		getEffected().addChanceSkillTrigger(this);
		
		// Removing every other effect with the same stack type (for improving buffs)
		for (L2Effect e : getEffected().getAllEffects())
			if (e != null && e != this && e instanceof EffectChanceSkillTrigger)
				if (e.stackTypesEqual(this))
					e.exit();
		
		return true;
	}
	
	@Override
	protected void onExit()
	{
		getEffected().removeChanceSkillTrigger(this);
	}
	
	@Override
	public ChanceCondition getChanceCondition()
	{
		return getEffectTemplate().chanceCondition;
	}
	
	@Override
	public L2Skill getChanceTriggeredSkill(L2Character activeChar, L2Character evtInitiator)
	{
		return getEffectTemplate().triggeredSkill.getTriggeredSkill();
	}
}
