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
import net.l2emuproject.gameserver.world.object.L2Player;

public final class EffectRelax extends L2Effect
{
	public EffectRelax(Env env, EffectTemplate template)
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
		if (getEffected() instanceof L2Player)
		{
			setRelax(true);
			((L2Player)getEffected()).sitDown();
		}
		else
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.l2emuproject.gameserver.model.L2Effect#onExit()
	 */
	@Override
	protected void onExit()
	{
		setRelax(false);
	}
	
	@Override
	protected boolean onActionTime()
	{
		boolean retval = true;
		if (getEffected().isDead())
			retval = false;
		
		L2Player effectedPlayer = getEffected() instanceof L2Player ? (L2Player)getEffected() : null;
		
		if (effectedPlayer != null)
		{
			if (!effectedPlayer.isSitting())
				retval = false;
		}
		
		if (getEffected().getStatus().getCurrentHp() + 1 > getEffected().getMaxHp())
		{
			if (getSkill().isToggle())
			{
				if (effectedPlayer != null)
					getEffected().sendPacket(SystemMessageId.SKILL_DEACTIVATED_HP_FULL);
				
				retval = false;
			}
		}
		
		double manaDam = calc();
		
		if (manaDam > getEffected().getStatus().getCurrentMp())
		{
			if (getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
				// if (getEffected() instanceof L2Player)
				// ((L2Player)getEffected()).standUp();
				retval = false;
			}
		}
		
		if (!retval)
			setRelax(retval);
		else
			getEffected().reduceCurrentMp(manaDam);
		
		return retval;
	}
	
	private void setRelax(boolean val)
	{
		if (getEffected() instanceof L2Player)
			((L2Player)getEffected()).setRelax(val);
	}
}
