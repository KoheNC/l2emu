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
package net.l2emuproject.gameserver.instancemanager.grandbosses;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.datatables.DoorTable;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Decoy;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.L2Playable;
import net.l2emuproject.gameserver.model.actor.L2Summon;
import net.l2emuproject.gameserver.model.actor.instance.L2GrandBossInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.entity.GrandBossState;
import net.l2emuproject.gameserver.model.party.L2Party;
import net.l2emuproject.gameserver.model.skill.L2Skill;
import net.l2emuproject.gameserver.model.spawn.L2Spawn;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.util.Util;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author lord_rex
 */
public final class BelethManager extends BossLair
{
	public static final int		BELETH					= 29118;
	public static final int		BELETH_CLONE			= 29119;

	public static final int		PRESENTATION_ELF		= 29128;
	public static final int		STONE_COFFIN			= 32470;
	public static final int		BELETH_SLAVE			= 18490;

	public static final int[]	BELETH_SLAVE_SPAWN		=
														{ -24185, 251087, -3281, 0 };

	private static final int[]	BELETH_SPAWN			=
														{ 16325, 214614, -9353, 49152 };

	public static final int[]	WAIT_ROOM				=
														{ 16323, 209864, -9356, 0 };

	public static final int		BELETH_DOOR				= 20240001;
	private static final int	CORRIDOR_DOOR			= 20240002;
	private static final int	COFFIN_DOOR				= 20240003;

	public static final String	BELETH_DOOR_MESSAGE		= "The door to Beleth will open in " + Config.BELETH_DOOR_WAIT_TIME / 1000 / 60 + " minutes.";

	private static final int[]	STONE_COFFIN_SPAWN		=
														{ 12471, 215602, -9360, 49152 };

	private static final int[]	ROOM_CENTER				=
														{ 16325, 213135, -9353, 49152 };
	private L2Spawn				_belethSpawn;
	private L2Spawn				_stoneCoffinSpawn;
	private L2Spawn				_presentationElfSpawn;

	private ScheduledFuture<?>	_intervalEndTask		= null;
	private ScheduledFuture<?>	_activityTimeEndTask	= null;

	public static final int		TASK_WAIT_ROOM			= 1;
	public static final int		TASK_OPEN_BELETH_DOOR	= 2;
	public static final int		TASK_SPAWN_BELETH		= 3;
	public static final int		TASK_BELETH_DEAD		= 4;

	private static final class SingletonHolder
	{
		private static final BelethManager	INSTANCE	= new BelethManager();
	}

	public static BelethManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private BelethManager()
	{
		_questName = "Beleth";
		_state = new GrandBossState(BELETH);
	}

	private final class BelethManagerTask implements Runnable
	{
		private int				_taskId	= 0;
		private L2PcInstance	_player;

		private BelethManagerTask(int taskId, L2PcInstance player)
		{
			_taskId = taskId;
			_player = player;
		}

		@Override
		public void run()
		{
			switch (_taskId)
			{
				case TASK_WAIT_ROOM:
					teleport(_player, WAIT_ROOM[0], WAIT_ROOM[1], WAIT_ROOM[2]);
					_log.info("BelethManager : " + BELETH_DOOR_MESSAGE);
					ThreadPoolManager.getInstance().scheduleGeneral(new BelethManagerTask(TASK_OPEN_BELETH_DOOR, _player), Config.BELETH_DOOR_WAIT_TIME);
					break;
				case TASK_OPEN_BELETH_DOOR:
					openDoor(BELETH_DOOR);
					ThreadPoolManager.getInstance().scheduleGeneral(new BelethManagerTask(TASK_SPAWN_BELETH, _player), Config.BELETH_SPAWN_WAIT_TIME);
					_log.info("BelethManager : Beleth is spawning in " + Config.BELETH_SPAWN_WAIT_TIME / 1000 / 60 + " minutes.");
					break;
				case TASK_SPAWN_BELETH:
					closeDoor(BELETH_DOOR);

					spawnBeleth();
					spawnClones();

					_log.info("BelethManager : Beleth and his clones are spawned.");
					break;
				case TASK_BELETH_DEAD:
					_state.setState(GrandBossState.StateEnum.DEAD);
					_state.update();

					openDoor(CORRIDOR_DOOR);
					openDoor(COFFIN_DOOR);

					despawnClones();

					spawnPresentationElf();
					spawnStoneCoffin();

					_log.info("BelethManager : Beleth is dead, clones are despawned.");
					break;
			}
		}
	}

	public void belethManagerTask(int taskId, L2PcInstance player)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new BelethManagerTask(taskId, player), 100);
	}

	@Override
	public void init()
	{
		try
		{
			createSpawns();
			spawnBelethSlave();
		}
		catch (RuntimeException e)
		{
			_log.warn("", e);
		}

		_log.info(getClass().getSimpleName() + " : State of Beleth is " + _state.getState() + ".");
		if (!_state.getState().equals(GrandBossState.StateEnum.NOTSPAWN))
			setIntervalEndTask();

		Date date = new Date(_state.getRespawnDate());

		_log.info(getClass().getSimpleName() + " : Next spawn date of Beleth is " + date + ".");
		_log.info(getClass().getSimpleName() + " : Init BelethManager.");
	}

	// At end of activity time.
	private final class ActivityTimeEnd implements Runnable
	{
		@Override
		public void run()
		{
			setUnspawn();
		}
	}

	private void startActivityTimeEndTask()
	{
		if (_activityTimeEndTask != null)
		{
			_activityTimeEndTask.cancel(true);
			_activityTimeEndTask = null;
		}
		_activityTimeEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new ActivityTimeEnd(), Config.BELETH_ACTIVITY_TIME);
	}

	@Override
	public void setUnspawn()
	{
		banishForeigners();
		closeDoor(BELETH_DOOR);
		closeDoor(CORRIDOR_DOOR);
		closeDoor(COFFIN_DOOR);

		if (_intervalEndTask != null)
		{
			_intervalEndTask.cancel(true);
			_intervalEndTask = null;
		}
		if (_activityTimeEndTask != null)
		{
			_activityTimeEndTask.cancel(true);
			_activityTimeEndTask = null;
		}

		// delete spawns
		if (_belethSpawn != null)
			_belethSpawn.stopRespawn();

		_belethSpawn = null;

		// delete spawns
		if (_presentationElfSpawn != null)
			_presentationElfSpawn.stopRespawn();

		_presentationElfSpawn = null;

		// delete spawns
		if (_stoneCoffinSpawn != null)
			_stoneCoffinSpawn.stopRespawn();

		_stoneCoffinSpawn = null;

		// Interval begin.
		setIntervalEndTask();
	}

	// Task of interval of Beleth spawn.
	public void setIntervalEndTask()
	{
		if (!_state.getState().equals(GrandBossState.StateEnum.INTERVAL))
		{
			_state.setRespawnDate(Config.BELETH_RESPAWN_TIME);
			_state.setState(GrandBossState.StateEnum.INTERVAL);
			_state.update();
		}

		_intervalEndTask = ThreadPoolManager.getInstance().scheduleGeneral(new IntervalEnd(), _state.getInterval());
	}

	// Interval end.
	private final class IntervalEnd implements Runnable
	{
		@Override
		public void run()
		{
			_state.setState(GrandBossState.StateEnum.NOTSPAWN);
			_state.update();
		}
	}

	private void createSpawns()
	{
		_belethSpawn = createNewSpawn(BELETH, BELETH_SPAWN[0], BELETH_SPAWN[1], BELETH_SPAWN[2], BELETH_SPAWN[3], (int) Config.BELETH_RESPAWN_TIME);
		_presentationElfSpawn = createNewSpawn(PRESENTATION_ELF, ROOM_CENTER[0], ROOM_CENTER[1], ROOM_CENTER[2], ROOM_CENTER[3], 0);
		_stoneCoffinSpawn = createNewSpawn(STONE_COFFIN, STONE_COFFIN_SPAWN[0], STONE_COFFIN_SPAWN[1], STONE_COFFIN_SPAWN[2], STONE_COFFIN_SPAWN[3], 0);
	}

	private void spawnBeleth()
	{
		_belethSpawn.doSpawn();

		_state.setState(GrandBossState.StateEnum.ALIVE);
		_state.setRespawnDate(Config.BELETH_RESPAWN_TIME);
		_state.update();

		startActivityTimeEndTask();
	}

	private void spawnPresentationElf()
	{
		_presentationElfSpawn.doSpawn();
	}

	private void spawnStoneCoffin()
	{
		_stoneCoffinSpawn.doSpawn();
	}

	/**
	 * TODO: Need retail coordinates...
	 * 
	 * This is not added to this zone, so don't need to remove it by unspawn.
	 */
	private void spawnBelethSlave()
	{
		L2Spawn belethSlaveSpawn = createNewSpawn(BELETH_SLAVE, BELETH_SLAVE_SPAWN[0], BELETH_SLAVE_SPAWN[1], BELETH_SLAVE_SPAWN[2], BELETH_SLAVE_SPAWN[3], 60);
		belethSlaveSpawn.doSpawn();
		_log.info(getClass().getSimpleName() + " : Beleth Slave is spawned to " + BELETH_SLAVE_SPAWN[0] + " " + BELETH_SLAVE_SPAWN[1] + " "
				+ BELETH_SLAVE_SPAWN[2] + ".");
	}

	public void relocateBeleth(int id)
	{
		// TODO: Implement...
		_log.info(getClass().getSimpleName() + " : Beleth can't relocate his position yet, but it's in TODO list.");
	}

	public void spawnClone(int npcId)
	{
		// TODO: Implement...
		_log.info(getClass().getSimpleName() + " : Beleth Clone can't spawn yet, but it's in TODO list.");
	}

	private void spawnClones()
	{
		// TODO: Implement...
		_log.info(getClass().getSimpleName() + " : Beleth Clones are not spawned yet, but it's in TODO list.");
	}

	public void respawnColne()
	{
		// TODO: Implement...
		_log.info(getClass().getSimpleName() + " : Beleth Clone can't respawn yet, but it's in TODO list.");
	}

	public void respawnColnes()
	{
		// TODO: Implement...
		_log.info(getClass().getSimpleName() + " : Beleth Clones can't respawn yet, but it's in TODO list.");
	}

	private void despawnClones()
	{
		// TODO: Implement...
	}

	/**
	 * Creates a single spawn from the parameter values and returns the L2Spawn created
	 * 
	 * @param templateId
	 *            int value of the monster template id number
	 * @param x
	 *            int value of the X position
	 * @param y
	 *            int value of the Y position
	 * @param z
	 *            int value of the Z position
	 * @param heading
	 *            int value of where is the monster facing to...
	 * @param respawnDelay
	 *            int value of the respawn of this L2Spawn
	 * @return L2Spawn created
	 */
	private L2Spawn createNewSpawn(int templateId, int x, int y, int z, int heading, int respawnDelay)
	{
		L2Spawn tempSpawn = null;

		L2NpcTemplate template1;

		try
		{
			template1 = NpcTable.getInstance().getTemplate(templateId);
			tempSpawn = new L2Spawn(template1);

			tempSpawn.setLocx(x);
			tempSpawn.setLocy(y);
			tempSpawn.setLocz(z);
			tempSpawn.setHeading(heading);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(respawnDelay);
			tempSpawn.stopRespawn();

			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
		}
		catch (RuntimeException e)
		{
			_log.warn("", e);
		}
		return tempSpawn;
	}

	/**
	 * Handles skill casting for Beleth and his clones.
	 */
	public void useSkill(L2Npc npc)
	{
		final int npcId = npc.getNpcId();
		if (npcId != BELETH && npcId != BELETH_CLONE || npc.isInvul() || npc.isCastingNow())
			return; // These are the only NPCs which require casting.

		int skillId = -1;
		L2Character target;
		target = castAoE(npc);
		if (target != null)
			skillId = 5495;
		else
			target = ((L2GrandBossInstance) npc).getMostHated();

		if (target == null || target.isDead())
		{
			npc.setIsCastingNow(false);
			return;
		}

		if (skillId == -1)
		{
			if (Util.checkIfInRange(250, npc, target, false) && npcId != BELETH)
			{
				if (Rnd.get(100) < 50)
					skillId = 5499;
				else
					skillId = 5496;
			}
			else if (Rnd.get(100) < 20)
				skillId = 5497;
			else
				skillId = 5496;
		}

		final L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
		if (Util.checkIfInRange(skill.getCastRange(), npc, target, true))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			npc.setTarget(target);
			npc.setIsCastingNow(true);
			try
			{
				Thread.sleep(1000);
				npc.stopMove(null);
				npc.doCast(skill);
			}
			catch (Exception e)
			{
				_log.error("", e);
			}
		}
		else
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, null);
			npc.setIsCastingNow(false);
		}
	}

	private final L2Character castAoE(L2Npc npc)
	{
		final FastList<L2Character> result = new FastList<L2Character>();
		final Iterable<L2Character> chars = npc.getKnownList().getKnownCharactersInRadius(250);

		// Adds players / summons / decoys to the result list
		for (final L2Character ch : chars)
			if (ch instanceof L2Playable || ch instanceof L2Decoy)
				result.add(ch);

		// If there are more than 4 players around the NPC, cast AoE skill
		if (result.size() > 2 && Rnd.get(100) < 90)
		{
			final L2Character[] inRange = new L2Character[result.size()];
			result.toArray(inRange);
			return inRange[Rnd.get(inRange.length)];
		}

		return null;
	}

	public void openDoor(int doorId)
	{
		DoorTable.getInstance().getDoor(doorId).openMe();

		switch (doorId)
		{
			case BELETH_DOOR:
				_log.info(getClass().getSimpleName() + " : Beleth door is open now!");
				break;
			case CORRIDOR_DOOR:
				_log.info(getClass().getSimpleName() + " : Corridor door is open now!");
				break;
			case COFFIN_DOOR:
				_log.info(getClass().getSimpleName() + " : Coffin door is open now!");
				break;
		}
	}

	public void closeDoor(int doorId)
	{
		DoorTable.getInstance().getDoor(doorId).closeMe();

		switch (doorId)
		{
			case BELETH_DOOR:
				_log.info(getClass().getSimpleName() + " : Beleth door is close now!");
				break;
			case CORRIDOR_DOOR:
				_log.info(getClass().getSimpleName() + " : Corridor door is close now!");
				break;
			case COFFIN_DOOR:
				_log.info(getClass().getSimpleName() + " : Coffin door is close now!");
				break;
		}
	}

	public void teleport(L2PcInstance player, int x, int y, int z)
	{
		player.teleToLocation(x, y, z);

		L2Party party = player.getParty();
		if (party != null)
		{
			for (L2PcInstance partyMembers : party.getPartyMembers())
			{
				partyMembers.teleToLocation(x, y, z);
			}
		}

		L2Summon pet = player.getPet();
		if (pet != null)
		{
			pet.teleToLocation(x, y, z);
		}
	}
}
