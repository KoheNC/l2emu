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
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author UnAfraid
 */
public final class EffectTransferDamage extends L2Effect
{
	public EffectTransferDamage(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	public EffectTransferDamage(final Env env, final L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DAMAGE_TRANSFER;
	}

	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2Playable && getEffector() instanceof L2Player)
			((L2Playable) getEffected()).setTransferDamageTo((L2Player) getEffector());
		return true;
	}

	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2Playable && getEffector() instanceof L2Player)
			((L2Playable) getEffected()).setTransferDamageTo(null);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}