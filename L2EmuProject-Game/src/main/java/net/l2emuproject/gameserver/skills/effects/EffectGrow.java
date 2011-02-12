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

import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.skills.AbnormalEffect;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;

public final class EffectGrow extends EffectBuff
{
	public EffectGrow(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	protected boolean onStart()
	{
		if (getEffected() instanceof L2Npc)
		{
			//L2Npc npc = (L2Npc) getEffected();
			//TODO: Uncomment lines when fix for mobs falling underground is found
			//npc.setCollisionHeight((int) (npc.getTemplate().getCollisionHeight() * 1.24));
			//npc.setCollisionRadius((int) (npc.getTemplate().getCollisionRadius() * 1.19));
			return true;
		}
		return false;
	}
	
	@Override
	protected void onExit()
	{
		//L2Npc npc = (L2Npc) getEffected();
		//TODO: Uncomment lines when fix for mobs falling underground is found
		//npc.setCollisionHeight(npc.getTemplate().getCollisionHeight());
		//npc.setCollisionRadius(npc.getTemplate().getCollisionRadius());
	}
	
	@Override
	protected int getTypeBasedAbnormalEffect()
	{
		return AbnormalEffect.GROW.getMask();
	}
}
