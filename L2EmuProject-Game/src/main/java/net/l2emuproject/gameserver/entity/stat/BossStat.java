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
package net.l2emuproject.gameserver.entity.stat;

import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.world.object.L2Boss;
import net.l2emuproject.gameserver.world.object.L2Character;

/**
 * @author lord_rex
 */
public class BossStat extends NpcStat
{
	public BossStat(L2Boss boss)
	{
		super(boss);
	}

	@Override
	public int getPAtk(final L2Character target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, _activeChar.getTemplate().getBasePAtk(), target, null);
	}

	@Override
	public int getMAtk(final L2Character target, final L2Skill skill)
	{
		// Get the base MAtk of the L2Character
		double attack = _activeChar.getTemplate().getBaseMAtk();

		// Add the power of the skill to the attack effect
		if (skill != null)
			attack += skill.getPower();

		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}

	@Override
	public int getPDef(final L2Character target)
	{
		return (int) calcStat(Stats.POWER_DEFENCE, _activeChar.getTemplate().getBasePDef(), target, null);
	}

	@Override
	public int getMDef(final L2Character target, final L2Skill skill)
	{
		// Get the base MAtk of the L2Character
		double defence = _activeChar.getTemplate().getBaseMDef();

		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}

	@Override
	public L2Boss getActiveChar()
	{
		return (L2Boss) _activeChar;
	}
}
