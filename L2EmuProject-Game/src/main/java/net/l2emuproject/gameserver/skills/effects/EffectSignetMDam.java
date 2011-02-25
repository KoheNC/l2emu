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

import javolution.util.FastList;
import net.l2emuproject.gameserver.ai.CtrlEvent;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.idfactory.IdFactory;
import net.l2emuproject.gameserver.model.actor.L2Attackable;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Playable;
import net.l2emuproject.gameserver.model.actor.L2Summon;
import net.l2emuproject.gameserver.model.actor.instance.L2EffectPointInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillLaunched;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.formulas.Formulas;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillSignetCasttime;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.templates.effects.EffectTemplate;
import net.l2emuproject.gameserver.templates.skills.L2EffectType;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.tools.geometry.Point3D;


/**
 * @author Forsaiken
 */
public final class EffectSignetMDam extends L2Effect
{
	private L2EffectPointInstance _actor;
	
	public EffectSignetMDam(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}
	
	@Override
	protected boolean onStart()
	{
		L2NpcTemplate template;
		if (getSkill() instanceof L2SkillSignetCasttime)
			template = NpcTable.getInstance().getTemplate(((L2SkillSignetCasttime)getSkill())._effectNpcId);
		else
			return false;
		
		L2EffectPointInstance effectPoint = new L2EffectPointInstance(IdFactory.getInstance().getNextId(), template,
				getEffector());
		effectPoint.getStatus().setCurrentHp(effectPoint.getMaxHp());
		effectPoint.getStatus().setCurrentMp(effectPoint.getMaxMp());
		L2World.getInstance().storeObject(effectPoint);
		
		int x = getEffector().getX();
		int y = getEffector().getY();
		int z = getEffector().getZ();
		
		if (getEffector() instanceof L2PcInstance
				&& getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
		{
			Point3D wordPosition = ((L2PcInstance)getEffector()).getCurrentSkillWorldPosition();
			
			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		effectPoint.setIsInvul(true);
		effectPoint.spawnMe(x, y, z);
		
		_actor = effectPoint;
		return true;
	}
	
	@Override
	protected boolean onActionTime()
	{
		if (getCount() >= getTotalCount() - 2)
			return true; // do nothing first 2 times
		int mpConsume = getSkill().getMpConsume();
		
		L2PcInstance caster = (L2PcInstance)getEffector();
		
		boolean ss = false;
		boolean bss = false;
		
		caster.rechargeShot();
		
		if (caster.isBlessedSpiritshotCharged())
		{
			bss = true;
			caster.useBlessedSpiritshotCharge();
		}
		else if (caster.isSpiritshotCharged())
		{
			ss = true;
			caster.useSpiritshotCharge();
		}
		
		caster.rechargeShot();
		
		FastList<L2Character> targets = new FastList<L2Character>();
		
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (cha == null || cha == caster)
				continue;
			
			if (cha instanceof L2Attackable || cha instanceof L2Playable)
			{
				if (cha.isAlikeDead())
					continue;
				
				if (mpConsume > caster.getStatus().getCurrentMp())
				{
					caster.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
					return false;
				}
				
				caster.reduceCurrentMp(mpConsume);
				
				if (cha instanceof L2Playable)
				{
					if (cha instanceof L2Summon && ((L2Summon)cha).getOwner() == caster)
					{
					}
					else
						caster.updatePvPStatus(cha);
				}
				
				targets.add(cha);
			}
		}
		
		if (!targets.isEmpty())
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill(), targets.toArray(new L2Character[targets
					.size()])));
			for (L2Character target : targets)
			{
				boolean mcrit = Formulas.calcMCrit(caster.getMCriticalHit(target, getSkill()));
				byte shld = Formulas.calcShldUse(caster, target, getSkill());
				int mdam = (int)Formulas.calcMagicDam(caster, target, getSkill(), shld, ss, bss, mcrit);
				
				if (target instanceof L2Summon)
					target.broadcastStatusUpdate();
				
				if (mdam > 0)
				{
					if (!target.isRaid() && Formulas.calcAtkBreak(target, mdam))
					{
						target.breakAttack();
						target.breakCast();
					}
					caster.sendDamageMessage(target, mdam, mcrit, false, false);
					target.reduceCurrentHp(mdam, caster, getSkill());
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
			}
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
