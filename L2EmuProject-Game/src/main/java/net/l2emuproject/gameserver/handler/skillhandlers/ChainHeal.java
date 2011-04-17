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

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.handler.SkillHandler;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.ValueSortMap;

/**
 * @author Nik, UnAfraid
 */
public final class ChainHeal implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.CHAIN_HEAL };

	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Character... targets)
	{
		//check for other effects
		final ISkillHandler handler = SkillHandler.getInstance().get(L2SkillType.BUFF);

		if (handler != null)
			handler.useSkill(activeChar, skill, targets);

		SystemMessage sm;
		double amount = 0;

		L2Character[] characters = getTargetsToHeal((L2Character[]) targets);
		double power = skill.getPower();

		// Get top 10 most damaged and iterate the heal over them
		for (L2Character character : characters)
		{
			//1505 - sublime self sacrifice
			if ((character == null || character.isDead() || character.isInvul()) && skill.getId() != 1505)
				continue;

			// Cursed weapon owner can't heal or be healed
			if (character != activeChar)
			{
				if (character instanceof L2Player && ((L2Player) character).isCursedWeaponEquipped())
					continue;
			}

			if (power == 100.)
				amount = character.getMaxHp();
			else
				amount = character.getMaxHp() * power / 100.0;

			amount = Math.min(amount, character.getMaxHp() - character.getCurrentHp());
			character.getStatus().setCurrentHp(amount + character.getCurrentHp());

			if (activeChar != character)
			{
				sm = new SystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
				sm.addCharName(activeChar);
			}
			else
				sm = new SystemMessage(SystemMessageId.S1_HP_RESTORED);
			sm.addNumber((int) amount);
			character.getActingPlayer().sendPacket(sm);

			StatusUpdate su = new StatusUpdate(character);
			su.addAttribute(StatusUpdate.CUR_HP, (int) character.getCurrentHp());
			character.getActingPlayer().sendPacket(su);

			power -= 3;
		}
	}

	private L2Character[] getTargetsToHeal(final L2Character[] targets)
	{
		final Map<L2Character, Double> tmpTargets = new FastMap<L2Character, Double>();
		final List<L2Character> sortedListToReturn = new FastList<L2Character>();
		int curTargets = 0;

		for (L2Character target : targets)
		{
			//1505 - sublime self sacrifice
			if ((target == null || target.isDead() || target.isInvul()))
				continue;

			if (target.getMaxHp() == target.getCurrentHp()) // Full hp ..
				continue;

			double hpPercent = target.getCurrentHp() / target.getMaxHp();
			tmpTargets.put(target, hpPercent);

			curTargets++;
			if (curTargets >= 10) // Unhardcode?
				break;
		}

		// Sort in ascending order then add the values to the list
		ValueSortMap.sortMapByValue(tmpTargets, true);
		sortedListToReturn.addAll(tmpTargets.keySet());

		return sortedListToReturn.toArray(new L2Character[sortedListToReturn.size()]);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}