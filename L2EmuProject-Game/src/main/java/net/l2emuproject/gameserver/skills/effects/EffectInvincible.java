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

import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;

public class EffectInvincible extends L2Effect
{
	public EffectInvincible(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * @see net.l2emuproject.gameserver.skills.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.INVINCIBLE;
	}
	
	/**
	 * @see net.l2emuproject.gameserver.skills.L2Effect#onStart()
	 */
	@Override
	protected boolean onStart()
	{
		getEffected().setIsInvulByEffect(this);
		return super.onStart();
	}
	
	/**
	 * @see net.l2emuproject.gameserver.skills.L2Effect#onExit()
	 */
	@Override
	protected void onExit()
	{
		getEffected().setIsInvulByEffect(null);
		super.onExit();
	}
}
