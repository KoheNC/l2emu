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
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;

/**
 * @author Kerberos
 */
public class EffectAfroHaircut extends L2Effect
{
	public EffectAfroHaircut(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * @see net.l2emuproject.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.AFROHAIR;
	}
	
	/**
	 * @see net.l2emuproject.gameserver.model.L2Effect#onStart()
	 */
	@Override
	protected boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance)getEffected()).setAfroHaircutId(getSkill().getAfroColor());
			return true;
		}
		return false;
	}
	
	/**
	 * @see net.l2emuproject.gameserver.model.L2Effect#onExit()
	 */
	@Override
	protected void onExit()
	{
		((L2PcInstance)getEffected()).setAfroHaircutId(0);
	}
}