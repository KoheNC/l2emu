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
package net.l2emuproject.gameserver.world.object;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.world.spawn.L2Spawn;
import net.l2emuproject.tools.random.Rnd;

public abstract class L2Boss extends L2MonsterInstance
{
	private static final short	BOSS_MAINTENANCE_INTERVAL	= 10000;

	public static final short	BOSS_INTERACTION_DISTANCE	= 500;
	public static final short	BOSS_PENALTY_SILENCE		= 4215;
	public static final short	BOSS_PENALTY_PETRIFICATION	= 4515;
	public static final short	BOSS_PENALTY_RESISTANCE		= 5479;

	public L2Boss(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public final boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	protected final int getMaintenanceInterval()
	{
		return BOSS_MAINTENANCE_INTERVAL;
	}

	/**
	 * Spawn all minions at a regular interval
	 * if minions are not near the raid boss, teleport them
	 */
	@Override
	protected final void startMaintenanceTask()
	{
		if (_minionList != null)
			_minionList.spawnMinions();

		_maintenanceTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run()
			{
				checkAndReturnToSpawn();

				if (_minionList != null)
					_minionList.maintainMinions();
			}
		}, 60000, getMaintenanceInterval() + Rnd.get(5000));
	}

	protected final void checkAndReturnToSpawn()
	{
		if (isDead() || isMovementDisabled())
			return;

		final L2Spawn spawn = getSpawn();
		if (spawn == null)
			return;

		final int spawnX = spawn.getLocx();
		final int spawnY = spawn.getLocy();
		final int spawnZ = spawn.getLocz();

		if (!isInCombat() && !isMovementDisabled())
			if (!isInsideRadius(spawnX, spawnY, spawnZ, Math.max(Config.MAX_DRIFT_RANGE, 200), true, false))
				teleToLocation(spawnX, spawnY, spawnZ, false);
	}
}
