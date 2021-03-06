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
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.SkillTargetTypes;
import net.l2emuproject.gameserver.skills.formulas.Formulas;
import net.l2emuproject.gameserver.skills.funcs.Func;
import net.l2emuproject.gameserver.skills.l2skills.L2SkillPdam;
import net.l2emuproject.gameserver.system.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class Pdam implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.PDAM, L2SkillType.FATALCOUNTER, L2SkillType.BLOW,
		L2SkillType.CHARGEDAM };
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill0, L2Character... targets)
	{
		// support for Double Shot, etc
		final L2SkillPdam skill = (L2SkillPdam)skill0;
		final int numberOfHits = skill.getNumberOfHits();
		
		if (activeChar.isAlikeDead())
			return;
		
		final L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
		final boolean soul = (weapon != null && weapon.isSoulshotCharged());
		final boolean isBlow = (skill.getSkillType() == L2SkillType.BLOW);
		final boolean isCharge = (skill.getSkillType() == L2SkillType.CHARGEDAM);
		
		final double modifier;
		
		if (activeChar instanceof L2Player && isCharge)
		{
			final L2Player player = (L2Player)activeChar;
			
			modifier = 0.8 + 0.201 * player.getCharges(); // thanks Diego Vargas of L2Guru: 70*((0.8+0.201*No.Charges) * (PATK+POWER)) / PDEF
			
			if (skill.getTargetType() != SkillTargetTypes.TARGET_AREA && skill.getTargetType() != SkillTargetTypes.TARGET_MULTIFACE)
				player.decreaseCharges(skill.getNeededCharges());
		}
		else
			modifier = 1.0;
		
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			// TODO: should be moved to skill target list generation
			if (GlobalRestrictions.isProtected(activeChar, target, skill, true))
				continue;
			
			if (target.isAlikeDead())
			{
				if (activeChar instanceof L2Player && target instanceof L2Player && target.isFakeDeath())
					target.stopFakeDeath(true);
				else
					continue;
			}
			
			// support for Double Shot, etc
			for (int i = 0; i < numberOfHits; i++)
			{
				if (isBlow && !Formulas.calcBlow(activeChar, target, skill))
				{
					activeChar.sendMissedDamageMessage(target);
					continue;
				}
				
				// Check firstly if target dodges skill
				if (Formulas.calcPhysicalSkillEvasion(target, skill))
				{
					activeChar.sendMissedDamageMessage(target);
					continue;
				}
				
				final byte shld = Formulas.calcShldUse(activeChar, target, skill);
				double damage = Formulas.calcPhysDam(activeChar, target, skill, shld, isBlow, soul);
				
				// PDAM critical chance not affected by buffs, only by STR. Only some skills are meant to crit.
				final boolean crit = Formulas.calcSkillCrit(activeChar, target, skill);
				if (crit)
				{
					damage *= 2; // PDAM Critical damage always 2x and not affected by buffs
					
					if (isBlow)
					{
						// Vicious Stance is special after C5, and only for BLOW skills
						// Adds directly to damage
						final L2Effect vicious = activeChar.getFirstEffect(312);
						if (vicious != null)
						{
							for (Func func : vicious.getStatFuncs())
							{
								Env env = new Env();
								env.setPlayer(activeChar);
								env.setTarget(target);
								env.setSkill(skill);
								env.setValue(damage);
								func.calcIfAllowed(env);
								damage = (int)env.getValue();
							}
						}
					}
				}
				
				if (isCharge)
				{
					damage *= modifier;
				}
				
				// support for Double Shot, etc
				damage /= numberOfHits;
				
				final byte reflect = Formulas.calcSkillReflect(target, skill);
				
				skill.dealDamage(activeChar, target, skill, damage, reflect, false, crit || isBlow);
				skill.getEffects(activeChar, target, reflect, shld, false, false, true);
			}
		}
		
		if (soul && weapon != null)
			weapon.useSoulshotCharge();
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
