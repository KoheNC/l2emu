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
 * @author DS
 * 
 * Effect will generate charges for L2PcInstance targets
 * Number of charges in "value", maximum number in "count" effect variables
 */
public class EffectIncreaseCharges extends L2Effect
{
	public EffectIncreaseCharges(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onStart()
	{
		if (getEffected() == null || !(getEffected() instanceof L2PcInstance))
			return false;

		((L2PcInstance) getEffected()).increaseCharges((int) calc(), getCount());

		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false; // abort effect even if count > 1
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.INCREASE_CHARGES;
	}
}