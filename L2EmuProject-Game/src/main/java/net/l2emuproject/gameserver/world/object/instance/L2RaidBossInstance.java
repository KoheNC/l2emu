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
package net.l2emuproject.gameserver.world.object.instance;

import net.l2emuproject.gameserver.entity.stat.RaidBossStat;
import net.l2emuproject.gameserver.manager.boss.RaidBossManager;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Boss;
import net.l2emuproject.gameserver.world.object.L2Character;

public class L2RaidBossInstance extends L2Boss
{
	public L2RaidBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected final RaidBossStat initStat()
	{
		return new RaidBossStat(this);
	}

	@Override
	public final RaidBossStat getStat()
	{
		return (RaidBossStat) super.getStat();
	}

	@Override
	public final boolean doDie(final L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		RaidBossManager.getInstance().updateDeathStatus(this);

		return true;
	}

	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}

	@Override
	public float getVitalityPoints(int damage)
	{
		return -super.getVitalityPoints(damage) / 100;
	}

	@Override
	public boolean useVitalityRate()
	{
		return false;
	}
}
