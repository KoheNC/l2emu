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
package net.l2emuproject.gameserver.handler.skillhandlers;

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.entity.ai.CtrlEvent;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.handler.ICubicSkillHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2CubicInstance;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.formulas.Formulas;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Playable;

public class Continuous implements ICubicSkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{
			L2SkillType.BUFF,
			L2SkillType.DEBUFF,
			L2SkillType.DOT,
			L2SkillType.MDOT,
			L2SkillType.POISON,
			L2SkillType.BLEED,
			L2SkillType.HOT,
			L2SkillType.CPHOT,
			L2SkillType.MPHOT,
			L2SkillType.FEAR,
			L2SkillType.CONT,
			L2SkillType.WEAKNESS,
			L2SkillType.REFLECT,
			L2SkillType.AGGDEBUFF,
			L2SkillType.FUSION			};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		L2Player player = null;
		if (activeChar instanceof L2Player)
			player = (L2Player) activeChar;

		if (skill.getEffectId() != 0)
		{
			int skillLevel = (int) skill.getEffectLvl();
			int skillEffectId = skill.getEffectId();
			
			L2Skill skill2;
			if (skillLevel == 0)
			{
				skill2 = SkillTable.getInstance().getInfo(skillEffectId, 1);
			}
			else
			{
				skill2 = SkillTable.getInstance().getInfo(skillEffectId, skillLevel);
			}

			if (skill2 != null)
				skill = skill2;
		}

		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			boolean acted = true;
			boolean ss = false;
			boolean sps = false;
			boolean bss = false;
			byte shld = 0;
			
			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
				target = activeChar;

			// With Mystic Immunity you can't be buffed/debuffed
			if (target.isPreventedFromReceivingBuffs())
				continue;

			// Player holding a cursed weapon can't be buffed and can't buff
			if (skill.getSkillType() == L2SkillType.BUFF && !(activeChar instanceof L2ClanHallManagerInstance))
			{
				if (target != activeChar)
				{
					if (target instanceof L2Player && ((L2Player) target).isCursedWeaponEquipped())
						continue;
					else if (player != null && player.isCursedWeaponEquipped())
						continue;
					// Avoiding block checker players get buffed from outside
					else if(target.getActingPlayer() != null && target.getActingPlayer().getBlockCheckerArena() != -1)
						continue;
				}
				// TODO: boolean isn't good idea, could cause bugs
				else if (skill.getId() == 2168 && activeChar instanceof L2Player)
					((L2Player) activeChar).setCharmOfLuck(true);
			}

			if (skill.isOffensive())
			{
				if (skill.useSpiritShot())
				{
					if (activeChar.isBlessedSpiritshotCharged())
					{
						bss = true;
						activeChar.useBlessedSpiritshotCharge();
					}
					else if (activeChar.isSpiritshotCharged())
					{
						sps = true;
						activeChar.useSpiritshotCharge();
					}
				}
				else if (/*skill.useSoulShot() &&*/activeChar.isSoulshotCharged())
				{
					ss = true;
					activeChar.useSoulshotCharge();
				}
				
				shld = Formulas.calcShldUse(activeChar, target, skill);
				acted = Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss);
			}

			if (acted)
			{
				if (skill.isToggle())
				{
					L2Effect e = target.getEffects().getFirstEffect(skill);
					
					if (e != null)
					{
						e.exit();
						return;
					}
				}

				skill.getEffects(activeChar, target);

				if (skill.getSkillType() == L2SkillType.AGGDEBUFF)
				{
					if (target instanceof L2Attackable)
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
					else if (target instanceof L2Playable)
					{
						if (target.getTarget() == activeChar)
							target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
						else
							target.setTarget(activeChar);
					}
				}
			}
			else
				activeChar.sendResistedMyEffectMessage(target, skill);
		}
	}

	@Override
	public void useCubicSkill(L2CubicInstance activeCubic, L2Skill skill, L2Character... targets)
	{
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			if (skill.isOffensive())
			{
				byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
				boolean acted = Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld);
				if (!acted)
				{
					activeCubic.getOwner().sendResistedMyEffectMessage(target, skill);
					continue;
				}
			}
			
			skill.getEffects(activeCubic, target);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
