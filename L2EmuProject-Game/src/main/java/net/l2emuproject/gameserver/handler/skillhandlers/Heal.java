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

import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.handler.SkillHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2DoorInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.skills.formulas.Formulas;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillRecover;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.zone.L2Zone;

public class Heal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.HEAL, L2SkillType.HEAL_PERCENT,
			L2SkillType.HEAL_STATIC, L2SkillType.HEAL_MOB, L2SkillType.RECOVER };
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		SkillHandler.getInstance().useSkill(L2SkillType.BUFF, activeChar, skill, targets);
		
		L2Player player = null;
		if (activeChar instanceof L2Player)
			player = (L2Player)activeChar;
		
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			// We should not heal if char is dead
			if (target.isDead())
				continue;
			
			if (target.isInsideZone(L2Zone.FLAG_NOHEAL))
				continue;
			
			if (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
				continue;
			
			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar)
			{
				if (target instanceof L2Player && ((L2Player)target).isCursedWeaponEquipped())
					continue;
				else if (player != null && player.isCursedWeaponEquipped())
					continue;
			}
			
			if (skill.getSkillType() == L2SkillType.RECOVER)
			{
				((L2SkillRecover)skill).recover(target);
				continue;
			}
			
			double hp = skill.getPower();
			
			if (skill.getSkillType() == L2SkillType.HEAL_PERCENT)
			{
				hp = target.getMaxHp() * hp / 100.0;
			}
			else if (skill.getSkillType() == L2SkillType.HEAL_STATIC)
			{
				hp = skill.getPower();
			}
			else if (!skill.isPotion())
			{
				if (activeChar.isBlessedSpiritshotCharged())
				{
					hp *= 1.5;
					activeChar.useBlessedSpiritshotCharge();
				}
				else if (activeChar.isSpiritshotCharged())
				{
					hp *= 1.3;
					activeChar.useSpiritshotCharge();
				}
				
				hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
				// Healer proficiency (since CT1)
				hp *= activeChar.calcStat(Stats.HEAL_PROFICIENCY, 100, null, null) / 100;
				// Extra bonus (since CT1.5)
				hp += target.calcStat(Stats.HEAL_STATIC_BONUS, 0, null, null);
				
				// Heal critic, since CT2.3 Gracia Final
				if (skill.getSkillType() == L2SkillType.HEAL
						&& Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill)))
				{
					hp *= 3;
					activeChar.sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
				}
			}
			
			// From CT2 you will receive exact HP, you can't go over it, if you have full HP and you get HP buff, you will receive 0HP restored message
			hp = Math.min(hp, target.getMaxHp() - target.getStatus().getCurrentHp());
			
			if (hp > 0)
			{
				target.getStatus().increaseHp(hp);
			}
			
			if (target instanceof L2Player)
			{
				if (skill.getId() == 4051)
				{
					target.sendPacket(SystemMessageId.REJUVENATING_HP);
				}
				else
				{
					if (activeChar instanceof L2Player && activeChar != target)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
						sm.addString(activeChar.getName());
						sm.addNumber(hp);
						target.getActingPlayer().sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_HP_RESTORED);
						sm.addNumber(hp);
						target.getActingPlayer().sendPacket(sm);
					}
				}
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
