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
import net.l2emuproject.gameserver.world.object.L2Npc;

public class NpcStat extends CharStat
{
	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	public NpcStat(L2Npc activeChar)
	{
		super(activeChar);

		setLevel(getActiveChar().getTemplate().getLevel());
	}

	// =========================================================
	// Method - Public

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	@Override
	public L2Npc getActiveChar()
	{
		return (L2Npc) _activeChar;
	}

	@Override
	public final int getMaxHp()
	{
		return super.getMaxHp() * (getActiveChar().isChampion() ? Config.CHAMPION_HP : 1);
	}

	@Override
	public float getMovementSpeedMultiplier()
	{
		if (getActiveChar().isRunning())
		{
			int base = getActiveChar().getTemplate().getBaseRunSpd();
			
			if (base == 0)
				return 1;
			
			return getRunSpeed() * 1f / base;
		}
		else
		{
			int base = getActiveChar().getTemplate().getBaseWalkSpd();
			
			if (base == 0)
				return 1;
			
			return getWalkSpeed() * 1f / base;
		}
	}
	
	@Override
	public final int getPAtk(final L2Character target)
	{
		final int val = super.getPAtk(target);		
		return (int) (val * Config.ALT_NPC_PHYSICAL_DAMAGE_MULTI);
	}
	
	@Override
	public final int getMAtk(final L2Character target, final L2Skill skill)
	{
		final int val = super.getMAtk(target, skill);		
		return (int) (val * Config.ALT_NPC_MAGICAL_DAMAGE_MULTI);
	}
}
