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
package net.l2emuproject.gameserver.world.npc;

import java.util.Map;
import java.util.Set;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.system.idfactory.IdFactory;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.instance.L2MinionInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.tools.random.Rnd;
import net.l2emuproject.util.SingletonMap;
import net.l2emuproject.util.SingletonSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author luisantonioa
 */
public final class MinionList
{
	private final static Log					_log				= LogFactory.getLog(L2MonsterInstance.class);

	/** List containing the current spawned minions for this L2MonsterInstance */
	private final Set<L2MinionInstance>			_minionReferences	= new SingletonSet<L2MinionInstance>().shared();
	private final Map<L2MinionInstance, Long>	_respawnTasks		= new SingletonMap<L2MinionInstance, Long>().shared();
	private final L2MonsterInstance				_master;

	public MinionList(L2MonsterInstance pMaster)
	{
		_master = pMaster;
	}

	public int countSpawnedMinions()
	{
		return _minionReferences.size();
	}

	private int countSpawnedMinionsById(final int minionId)
	{
		int count = 0;
		for (L2MinionInstance minion : getSpawnedMinions())
			if (minion != null && minion.getNpcId() == minionId)
				count++;

		return count;
	}

	public boolean hasMinions()
	{
		return !getSpawnedMinions().isEmpty();
	}

	public Set<L2MinionInstance> getSpawnedMinions()
	{
		return _minionReferences;
	}

	public void addSpawnedMinion(final L2MinionInstance minion)
	{
		_minionReferences.add(minion);
	}

	public int lazyCountSpawnedMinionsGroups()
	{
		final Set<Integer> seenGroups = new SingletonSet<Integer>();
		for (L2MinionInstance minion : getSpawnedMinions())
			if (minion != null)
				seenGroups.add(minion.getNpcId());

		return seenGroups.size();
	}

	public void moveMinionToRespawnList(final L2MinionInstance minion)
	{
		_minionReferences.remove(minion);

		_respawnTasks.put(minion, System.currentTimeMillis());
	}

	public void clearRespawnList()
	{
		_respawnTasks.clear();
	}

	/**
	 * Manage respawning of minions for this RaidBoss.<BR><BR>
	 */
	public void maintainMinions()
	{
		if (_master.isAlikeDead())
			return;

		final long current = System.currentTimeMillis();

		for (Map.Entry<L2MinionInstance, Long> entry : _respawnTasks.entrySet())
		{
			if (current - entry.getValue() > Config.RAID_MINION_RESPAWN_TIMER)
			{
				spawnSingleMinion(entry.getKey().getNpcId(), _master.getInstanceId());

				_respawnTasks.remove(entry.getKey());
			}
		}
	}

	/**
	 * Manage the spawn of all Minions of this RaidBoss.<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Minion data of all Minions that must be spawn </li>
	 * <li>For each Minion type, spawn the amount of Minion needed </li><BR><BR>
	 * 
	 * @param player The L2PcInstance to attack
	 * 
	 */
	public void spawnMinions()
	{
		if (_master.isAlikeDead())
			return;

		final L2MinionData[] minions = _master.getTemplate().getMinionData();
		if (minions == null)
			return;

		int minionCount = 0, minionId = 0, minionsToSpawn;
		for (L2MinionData minion : minions)
		{
			minionCount = minion.getAmount();
			minionId = minion.getMinionId();

			minionsToSpawn = minionCount - countSpawnedMinionsById(minionId);

			for (int i = 0; i < minionsToSpawn; i++)
				spawnSingleMinion(minionId, _master.getInstanceId());
		}
	}

	/**
	 * Init a Minion and add it in the world as a visible object.<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the template of the Minion to spawn </li>
	 * <li>Create and Init the Minion and generate its Identifier </li>
	 * <li>Set the Minion HP, MP and Heading </li>
	 * <li>Set the Minion leader to this RaidBoss </li>
	 * <li>Init the position of the Minion and add it in the world as a visible object </li><BR><BR>
	 * 
	 * @param minionid The I2NpcTemplate Identifier of the Minion to spawn
	 * 
	 */
	private void spawnSingleMinion(final int minionid, final int instanceId)
	{
		// Get the template of the Minion to spawn
		final L2NpcTemplate minionTemplate = NpcTable.getInstance().getTemplate(minionid);

		// Create and Init the Minion and generate its Identifier
		L2MinionInstance monster = null;
		if (minionTemplate.isAssignableTo(L2MinionInstance.class))
		{
			try
			{
				monster = (L2MinionInstance) minionTemplate.getDefaultConstructor().newInstance(IdFactory.getInstance().getNextId(), minionTemplate);
			}
			catch (Exception e)
			{
			}
		}
		if (monster == null) // generally not a minion, but assigned as one
			monster = new L2MinionInstance(IdFactory.getInstance().getNextId(), minionTemplate);

		if (Config.CHAMPION_MINIONS && _master.isChampion())
			monster.setChampion(true);

		// Set the Minion HP, MP and Heading
		monster.getStatus().setCurrentHpMp(monster.getMaxHp(), monster.getMaxMp());
		monster.setHeading(_master.getHeading());

		// Set the Minion leader to this RaidBoss
		monster.setLeader(_master);

		//move monster to masters instance
		monster.setInstanceId(instanceId);

		// Init the position of the Minion and add it in the world as a visible object
		int spawnConstant;
		int randSpawnLim = 170;
		int randPlusMin = 1;
		spawnConstant = Rnd.nextInt(randSpawnLim);
		//randomize +/-
		randPlusMin = Rnd.nextInt(2);
		if (randPlusMin == 1)
			spawnConstant *= -1;
		final int newX = _master.getX() + Math.round(spawnConstant);
		spawnConstant = Rnd.nextInt(randSpawnLim);
		//randomize +/-
		randPlusMin = Rnd.nextInt(2);
		if (randPlusMin == 1)
			spawnConstant *= -1;
		final int newY = _master.getY() + Math.round(spawnConstant);

		monster.spawnMe(newX, newY, _master.getZ());

		if (_log.isDebugEnabled())
			_log.debug("Spawned minion template " + minionTemplate.getNpcId() + " with objid: " + monster.getObjectId() + " to boss " + _master.getObjectId()
					+ " ,at: " + monster.getX() + " x, " + monster.getY() + " y, " + monster.getZ() + " z");
	}
}
