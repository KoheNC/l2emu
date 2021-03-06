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

import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.network.serverpackets.DeleteObject;
import net.l2emuproject.gameserver.skills.AbnormalEffect;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author ZaKaX - nBd
 */
public class EffectHide extends L2Effect
{
	public EffectHide(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public EffectHide(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HIDE;
	}
	
	@Override
	protected boolean onStart()
	{
		if (getEffected() instanceof L2Player)
		{
			L2Player activeChar = ((L2Player)getEffected());
			activeChar.getAppearance().setInvisible();

			if (activeChar.getAI().getNextCtrlIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

			final DeleteObject del = new DeleteObject(activeChar);
			for (L2Character obj : activeChar.getKnownList().getKnownCharacters())
			{
				if (obj == null)
					continue;
				
				if (obj.getTarget() == activeChar)
				{
					obj.setTarget(null);
					obj.abortAttack();
					obj.abortCast();
					obj.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
				
				if (obj instanceof L2Player)
					((L2Player)obj).sendPacket(del);
			}
		}
		
		return true;
	}
	
	@Override
	protected void onExit()
	{
		if (getEffected() instanceof L2Player)
		{
			L2Player activeChar = ((L2Player)getEffected());
			activeChar.getAppearance().setVisible();
		}
	}
	
	@Override
	protected int getTypeBasedAbnormalEffect()
	{
		return AbnormalEffect.STEALTH.getMask();
	}
}
