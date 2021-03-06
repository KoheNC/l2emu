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

import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.services.transformation.TransformationService;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author nBd
 */
public final class EffectTransformation extends L2Effect
{
	public EffectTransformation(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	// Special constructor to steal this effect
	public EffectTransformation(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TRANSFORMATION;
	}
	
	@Override
	protected boolean onStart()
	{
		if (!(getEffected() instanceof L2Player))
			return false;
		
		L2Player trg = (L2Player)getEffected();
		
		// No transformation if dead or cursed by cursed weapon
		if (trg.isAlikeDead() || trg.isCursedWeaponEquipped())
			return false;
		
		if (trg.getPlayerTransformation().getTransformation() != null)
		{
			trg.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			return false;
		}
		
		TransformationService.getInstance().transformPlayer(getSkill().getTransformId(), trg);
		return true;
	}
	
	@Override
	protected void onExit()
	{
		getEffected().stopTransformation(false);
	}
}
