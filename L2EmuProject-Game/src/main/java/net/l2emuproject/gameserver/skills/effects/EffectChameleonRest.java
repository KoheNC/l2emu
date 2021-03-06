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

import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class EffectChameleonRest extends L2Effect
{
	public EffectChameleonRest(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RELAXING;
	}
	
	/** Notify started */
	@Override
	protected boolean onStart()
	{
		L2Character effected = getEffected();
		if (effected instanceof L2Player)
		{
			setChameleon(true);
			((L2Player)effected).setSilentMoving(true);
			((L2Player)effected).sitDown();
		}
		else
			effected.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.model.L2Effect#onExit()
	 */
	@Override
	protected void onExit()
	{
		setChameleon(false);
		
		L2Character effected = getEffected();
		if (effected instanceof L2Player)
			((L2Player)effected).setSilentMoving(false);
	}
	
	@Override
	protected boolean onActionTime()
	{
		L2Character effected = getEffected();
		boolean retval = true;
		
		if (effected.isDead())
			retval = false;
		
		// Only cont skills shouldn't end
		if (getSkill().getSkillType() != L2SkillType.CONT)
			return false;
		
		if (effected instanceof L2Player)
		{
			if (!((L2Player)effected).isSitting())
				retval = false;
		}
		
		double manaDam = calc();
		
		if (manaDam > effected.getStatus().getCurrentMp())
		{
			effected.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			return false;
		}
		
		if (!retval)
			setChameleon(retval);
		else
			effected.reduceCurrentMp(manaDam);
		
		return retval;
	}
	
	private void setChameleon(boolean val)
	{
		L2Character effected = getEffected();
		if (effected instanceof L2Player)
			((L2Player)effected).setRelax(val);
	}
}
