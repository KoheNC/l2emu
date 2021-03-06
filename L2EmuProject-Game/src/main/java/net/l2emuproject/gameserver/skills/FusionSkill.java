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
package net.l2emuproject.gameserver.skills;

import java.util.concurrent.Future;

import net.l2emuproject.gameserver.skills.effects.EffectFusion;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillFusion;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.object.L2Character;


/**
 * @author kombat/crion
 */
public final class FusionSkill implements Runnable
{
	private final L2Character _caster;
	private final L2Character _target;
	private final L2SkillFusion _skill;
	
	private final Future<?> _geoCheckTask;
	
	public L2Character getTarget()
	{
		return _target;
	}
	
	public FusionSkill(L2Character caster, L2Character target, L2SkillFusion skill)
	{
		_caster = caster;
		_target = target;
		_skill = skill;
		
		EffectFusion effect = getFusionTriggeredEffect();
		
		if (effect != null)
			effect.increaseEffect();
		
		else
			skill.getFusionTriggeredSkill().getEffects(_caster, _target);
		
		_geoCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 1000, 1000);
	}
	
	private EffectFusion getFusionTriggeredEffect()
	{
		return (EffectFusion)_target.getFirstEffect(_skill.getFusionTriggeredSkill().getId());
	}
	
	public void onCastAbort()
	{
		_caster.setFusionSkill(null);
		
		final EffectFusion effect = getFusionTriggeredEffect();
		if (effect != null)
			effect.decreaseEffect();
		
		_geoCheckTask.cancel(true);
	}
	
	@Override
	public void run()
	{
		if (!Util.checkIfInRange(_skill.getCastRange(), _caster, _target, true))
			_caster.abortCast();
		
		else if (!GeoData.getInstance().canSeeTarget(_caster, _target))
			_caster.abortCast();
	}
}
