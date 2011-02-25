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

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.model.actor.instance.L2NpcInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.l2emuproject.gameserver.network.serverpackets.StartRotation;
import net.l2emuproject.gameserver.network.serverpackets.StopRotation;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * Implementation of the Bluff Effect
 * 
 * @author decad
 */
public final class EffectBluff extends L2Effect
{
	public EffectBluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLUFF; // test for bluff effect
	}
	
	@Override
	protected boolean onStart()
	{
		if (getEffected() instanceof L2NpcInstance)
			return false;
		
		if (getEffected() instanceof L2Npc && ((L2Npc)getEffected()).getNpcId() == 35062)
			return false;
		
		if (getEffected() instanceof L2SiegeSummonInstance)
			return false;
		
		if (getEffected().hasAI())
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, getEffector());
		
		getEffected().broadcastPacket(
				new StartRotation(getEffected().getObjectId(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new StopRotation(getEffected().getObjectId(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
