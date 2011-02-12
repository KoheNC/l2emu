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

/**
 * @author mkizub
 */
public final class EffectImmobileBuff extends EffectBuff
{
	public EffectImmobileBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	// Special constructor to steal this effect
	public EffectImmobileBuff(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	/** Notify started */
	@Override
	protected boolean onStart()
	{
		getEffected().setIsImmobilized(true);
		return true;
	}
	
	/** Notify exited */
	@Override
	protected void onExit()
	{
		getEffected().setIsImmobilized(false);
	}
}
