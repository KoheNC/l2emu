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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.instance.L2RaidBossInstance;

/**
 * @author lord_rex
 */
public final class RaidBossStat extends BossStat
{
	public RaidBossStat(L2RaidBossInstance boss)
	{
		super(boss);
	}

	@Override
	public final int getPAtk(final L2Character target)
	{
		final int val = super.getPAtk(target);
		return (int) (val * Config.RAID_PDAMAGE_MULTIPLIER);
	}

	@Override
	public final int getMAtk(final L2Character target, final L2Skill skill)
	{
		final int val = super.getMAtk(target, skill);
		return (int) (val * Config.RAID_MDAMAGE_MULTIPLIER);
	}

	@Override
	public final int getPDef(final L2Character target)
	{
		final int val = super.getPDef(target);
		return (int) (val * Config.RAID_PDEFENCE_MULTIPLIER);
	}

	@Override
	public final int getMDef(final L2Character target, final L2Skill skill)
	{
		final int val = super.getMDef(target, skill);
		return (int) (val * Config.RAID_MDEFENCE_MULTIPLIER);
	}

	@Override
	public final L2RaidBossInstance getActiveChar()
	{
		return (L2RaidBossInstance) _activeChar;
	}
}
