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
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.formulas.Formulas;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Class handling the Mana damage skill
 *
 * @author slyce
 */
public class Manadam implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.MANADAM };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;

		boolean ss = false;
		boolean bss = false;
		
		if (activeChar.isBlessedSpiritshotCharged())
		{
			bss = true;
			activeChar.useBlessedSpiritshotCharge();
		}
		else if (activeChar.isSpiritshotCharged())
		{
			ss = true;
			activeChar.useSpiritshotCharge();
		}
		
		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
				target = activeChar;

			boolean acted = Formulas.calcMagicAffected(activeChar, target, skill);
			if (!acted || target.isInvul())
			{
				activeChar.sendResistedMyEffectMessage(target, skill);
			}
			else
			{
				double damage = Formulas.calcManaDam(activeChar, target, skill, ss, bss);
				
				if (Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill)))
				{
					damage *= 3D;
					activeChar.sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
				}
				
				double mp = (damage > target.getStatus().getCurrentMp() ? target.getStatus().getCurrentMp() : damage);
				target.reduceCurrentMp(mp);
				if (damage > 0)
				{
					if (target.isSleeping())
						target.stopSleeping(true);
					if (target.isImmobileUntilAttacked())
						target.stopImmobileUntilAttacked(true);
				}

				if (target instanceof L2Player)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_C1);
					sm.addCharName(activeChar);
					sm.addNumber((int) mp);
					target.getActingPlayer().sendPacket(sm);
				}

				if (activeChar instanceof L2Player)
				{
					SystemMessage sm2 = new SystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1);
					sm2.addNumber((int) mp);
					activeChar.getActingPlayer().sendPacket(sm2);
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
