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

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.gameserver.world.object.L2Playable;

/**
 * @author -Nemesiss-
 */
public final class EffectTargetMe extends L2Effect
{
	public EffectTargetMe(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TARGET_ME;
	}
	
	/** Notify started */
	@Override
	protected boolean onStart()
	{
		if (getEffected() instanceof L2Playable)
		{
			if (getEffected() instanceof L2SiegeSummonInstance)
				return false;
			
			if (getEffected().getTarget() != getEffector())
			{
				// to be able to set the target even if it's already locked
				((L2Playable)getEffected()).setLockedTarget(false);
				// Target is different - stop autoattack and break cast
				getEffected().setTarget(getEffector());
				getEffected().abortAttack();
				getEffected().abortCast();
				getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
			((L2Playable)getEffected()).setLockedTarget(true);
			return true;
		}
		return false;
	}
	
	@Override
	protected void onExit()
	{
		if (getEffected() instanceof L2Playable)
			((L2Playable)getEffected()).setLockedTarget(false);
	}
}
