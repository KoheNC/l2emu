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

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.model.actor.instance.L2EffectPointInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillSignet;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillSignetCasttime;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.gameserver.world.object.L2Character;

/**
 * @authors Forsaiken, Sami
 */
public final class EffectSignet extends L2Effect
{
	private L2Skill _skill;
	private L2EffectPointInstance _actor;
	
	public EffectSignet(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_EFFECT;
	}
	
	@Override
	protected boolean onStart()
	{
		if (getSkill() instanceof L2SkillSignet)
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignet)getSkill()).getSignetEffectId(), getLevel());
		else if (getSkill() instanceof L2SkillSignetCasttime)
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignetCasttime)getSkill()).effectId, getLevel());
		_actor = (L2EffectPointInstance)getEffected();
		return true;
	}
	
	@Override
	protected boolean onActionTime()
	{
		//if (getCount() == getTotalCount() - 1) return true; // do nothing first time
		if (_skill == null)
			return true;
		int mpConsume = _skill.getMpConsume();
		
		if (mpConsume > getEffector().getStatus().getCurrentMp())
		{
			getEffector().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			return false;
		}
		
		getEffector().reduceCurrentMp(mpConsume);
		
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null)
				continue;
			_skill.getEffects(_actor, cha);
			// there doesn't seem to be a visible effect with MagicSkillLaunched packet...
			_actor.broadcastPacket(new MagicSkillUse(_actor, cha, _skill.getId(), _skill.getLevel(), 0, 0));
		}
		return true;
	}
	
	@Override
	protected void onExit()
	{
		if (_actor != null)
		{
			_actor.deleteMe();
		}
	}
}
