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
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;

/**
 * @author demonia
 */
public final class EffectImmobilePetBuff extends L2Effect
{
	private L2Summon _pet;
	
	public EffectImmobilePetBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	/** Notify started */
	@Override
	protected boolean onStart()
	{
		_pet = null;
		
		if (getEffected() instanceof L2Summon && getEffector() instanceof L2Player
				&& ((L2Summon)getEffected()).getOwner() == getEffector())
		{
			_pet = (L2Summon)getEffected();
			_pet.setIsImmobilized(true);
			return true;
		}
		return false;
	}
	
	/** Notify exited */
	@Override
	protected void onExit()
	{
		_pet.setIsImmobilized(false);
	}
}
