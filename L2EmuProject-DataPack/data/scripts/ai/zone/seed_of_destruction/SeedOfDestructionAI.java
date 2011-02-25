/* This program is free software: you can redistribute it and/or modify it under
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
package ai.zone.seed_of_destruction;

import ai.L2AttackableAIScript;

import org.apache.commons.lang.ArrayUtils;
import net.l2emuproject.tools.random.Rnd;
import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.model.L2CharPosition;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * @author lewzer
 * work in progress
 */
public final class SeedOfDestructionAI extends L2AttackableAIScript
{
	private static final int			SPAWN_PORTAL			= 18696;
	private static final int			TIAT					= 29163;
	private static final int			TIAT_GUARDS				= 29162;
	private static final int			GUARD_SPAWN_CHANCE		= 5;
	private L2Npc						spawner1, spawner2, spawner3, spawner4, guard1, guard2, guard3;
	private int							SPAWNED_MOB_COUNT		= 0;
	private int							GUARDS_SPAWNED			= 3;
	private int							TIAT_HEALED				= 0;
	private static final int			MOB_AMMOUNT_FROM_PORTAL	= 4;
	private static final int			MAX_SPAWNED_MOB_COUNT	= 100;
	private static final int[]			SPAWN_MOB_IDS			=
																{
			22536,
			22537,
			22538,
			22539,
			22540,
			22541,
			22542,
			22543,
			22544,
			22547,
			22550,
			22551,
			22552,
			22596												};

	private static final L2CharPosition	MOVE_TO_TIAT			= new L2CharPosition(-250403, 207273, -11952, 16384);

	public SeedOfDestructionAI(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addSpawnId(SPAWN_PORTAL);
		addSpawnId(TIAT);
		addKillId(SPAWN_PORTAL);
		addKillId(TIAT);
		addAttackId(TIAT);
		addKillId(TIAT_GUARDS);

		for (int i = 0; i < SPAWN_MOB_IDS.length; i++)
		{
			addKillId(SPAWN_MOB_IDS[i]);
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("Spawn1"))
		{
			if (SPAWNED_MOB_COUNT <= MAX_SPAWNED_MOB_COUNT)
			{
				L2Attackable mob = (L2Attackable) addSpawn(SPAWN_MOB_IDS[Rnd.get(SPAWN_MOB_IDS.length)], npc.getSpawn().getLocx(), npc.getSpawn().getLocy(),
						npc.getSpawn().getLocz(), npc.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
				SPAWNED_MOB_COUNT++;
				mob.setSeeThroughSilentMove(true);
				mob.setRunning();
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_TIAT);
				startQuestTimer("Spawn1", 20000, npc, null, true);
			}
		}
		else if (event.equalsIgnoreCase("Spawn2"))
		{
			if (SPAWNED_MOB_COUNT <= MAX_SPAWNED_MOB_COUNT)
			{
				L2Attackable mob = (L2Attackable) addSpawn(SPAWN_MOB_IDS[Rnd.get(SPAWN_MOB_IDS.length)], npc.getSpawn().getLocx(), npc.getSpawn().getLocy(),
						npc.getSpawn().getLocz(), npc.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
				SPAWNED_MOB_COUNT++;
				mob.setSeeThroughSilentMove(true);
				mob.setRunning();
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_TIAT);
				startQuestTimer("Spawn2", 20000, npc, null, true);
			}
		}
		else if (event.equalsIgnoreCase("Spawn3"))
		{
			if (SPAWNED_MOB_COUNT <= MAX_SPAWNED_MOB_COUNT)
			{
				L2Attackable mob = (L2Attackable) addSpawn(SPAWN_MOB_IDS[Rnd.get(SPAWN_MOB_IDS.length)], npc.getSpawn().getLocx(), npc.getSpawn().getLocy(),
						npc.getSpawn().getLocz(), npc.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
				SPAWNED_MOB_COUNT++;
				mob.setSeeThroughSilentMove(true);
				mob.setRunning();
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_TIAT);
				startQuestTimer("Spawn3", 20000, npc, null, true);
			}
		}
		else if (event.equalsIgnoreCase("Spawn4"))
		{
			if (SPAWNED_MOB_COUNT <= MAX_SPAWNED_MOB_COUNT)
			{
				L2Attackable mob = (L2Attackable) addSpawn(SPAWN_MOB_IDS[Rnd.get(SPAWN_MOB_IDS.length)], npc.getSpawn().getLocx(), npc.getSpawn().getLocy(),
						npc.getSpawn().getLocz(), npc.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
				SPAWNED_MOB_COUNT++;
				mob.setSeeThroughSilentMove(true);
				mob.setRunning();
				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_TIAT);
				startQuestTimer("Spawn4", 20000, npc, null, true);
			}
		}
		else if (event.equalsIgnoreCase("healTiat"))
		{
			if (!npc.isStunned() && !npc.isInvul())
			{
				npc.getStatus().setCurrentHp(npc.getMaxHp());
				cancelQuestTimer("healTiat", npc, null);
				TIAT_HEALED = 0;
			}
			else
			{
				TIAT_HEALED = 1;
				SPAWNED_MOB_COUNT = 0;
				cancelQuestTimer("healTiat", npc, null);
				spawn(npc, 1);
			}
		}
		else if (event.equalsIgnoreCase("respawnPortal1"))
		{
			spawn(npc, 2);
		}
		else if (event.equalsIgnoreCase("respawnPortal2"))
		{
			spawn(npc, 3);
		}
		else if (event.equalsIgnoreCase("respawnPortal3"))
		{
			spawn(npc, 4);
		}
		else if (event.equalsIgnoreCase("respawnPortal4"))
		{
			spawn(npc, 5);
		}
		return "";
	}

	private final void spawn(L2Npc npc, int type)
	{
		switch (type)
		{
			case 0:
				for (int i = 0; i < MOB_AMMOUNT_FROM_PORTAL; i++)
				{
					if (SPAWNED_MOB_COUNT < MAX_SPAWNED_MOB_COUNT)
					{
						L2Attackable mob = (L2Attackable) addSpawn(SPAWN_MOB_IDS[Rnd.get(SPAWN_MOB_IDS.length)], npc.getSpawn().getLocx(), npc.getSpawn()
								.getLocy(), npc.getSpawn().getLocz(), npc.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
						SPAWNED_MOB_COUNT++;
						mob.setSeeThroughSilentMove(true);
						mob.setRunning();
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_TIAT);
					}
				}
				break;
			case 1:
				spawner1 = addSpawn(SPAWN_PORTAL, -252022, 210130, -11968, 0, false, 0, false, npc.getInstanceId());
				spawner2 = addSpawn(SPAWN_PORTAL, -248782, 210130, -11968, 0, false, 0, false, npc.getInstanceId());
				spawner3 = addSpawn(SPAWN_PORTAL, -252022, 206875, -11968, 0, false, 0, false, npc.getInstanceId());
				spawner4 = addSpawn(SPAWN_PORTAL, -248782, 206875, -11968, 0, false, 0, false, npc.getInstanceId());
				break;
			case 2:
				if (spawner1.isDead() || spawner1.getCurrentHp() < 1)
					spawner1 = addSpawn(SPAWN_PORTAL, -252022, 210130, -11968, 0, false, 0, false, npc.getInstanceId());
				break;
			case 3:
				if (spawner2.isDead() || spawner2.getCurrentHp() < 1)
					spawner2 = addSpawn(SPAWN_PORTAL, -248782, 210130, -11968, 0, false, 0, false, npc.getInstanceId());
				break;
			case 4:
				if (spawner3.isDead() || spawner3.getCurrentHp() < 1)
					spawner3 = addSpawn(SPAWN_PORTAL, -252022, 206875, -11968, 0, false, 0, false, npc.getInstanceId());
				break;
			case 5:
				if (spawner4.isDead() || spawner4.getCurrentHp() < 1)
					spawner4 = addSpawn(SPAWN_PORTAL, -248782, 206875, -11968, 0, false, 0, false, npc.getInstanceId());
				break;
			case 6:
				guard1 = addSpawn(TIAT_GUARDS, npc.getSpawn().getLocx() + Rnd.get(200), npc.getSpawn().getLocy() + Rnd.get(200), npc.getSpawn().getLocz(), npc
						.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
				guard2 = addSpawn(TIAT_GUARDS, npc.getSpawn().getLocx() + Rnd.get(200), npc.getSpawn().getLocy() + Rnd.get(200), npc.getSpawn().getLocz(), npc
						.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
				guard3 = addSpawn(TIAT_GUARDS, npc.getSpawn().getLocx() + Rnd.get(200), npc.getSpawn().getLocy() + Rnd.get(200), npc.getSpawn().getLocz(), npc
						.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
				break;
		}
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		switch (npc.getNpcId())
		{
			case SPAWN_PORTAL:
				npc.setIsNoRndWalk(true);
				npc.setIsImmobilized(true);
				npc.disableCoreAI(true);
				spawn(npc, 0);
				if (npc.getSpawn().getLocx() == -252022 && npc.getSpawn().getLocy() == 210130)
					startQuestTimer("Spawn1", 20000, npc, null, true);
				else if (npc.getSpawn().getLocx() == -248782 && npc.getSpawn().getLocy() == 210130)
					startQuestTimer("Spawn2", 20000, npc, null, true);
				else if (npc.getSpawn().getLocx() == -252022 && npc.getSpawn().getLocy() == 206875)
					startQuestTimer("Spawn3", 20000, npc, null, true);
				else if (npc.getSpawn().getLocx() == -248782 && npc.getSpawn().getLocy() == 206875)
					startQuestTimer("Spawn4", 20000, npc, null, true);
				break;
			case TIAT:
				npc.setIsNoRndWalk(true);
				npc.setIsImmobilized(true);
				spawn(npc, 6);
				GUARDS_SPAWNED = 3;
				TIAT_HEALED = 0;
				break;
		}
		return "";
	}

	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		switch (npc.getNpcId())
		{
			case TIAT:
				if (TIAT_HEALED == 0 && npc.getCurrentHp() < (npc.getMaxHp() / 2))
				{
					TIAT_HEALED = 1;
					npc.doCast(SkillTable.getInstance().getInfo(5974, 1));
					startQuestTimer("healTiat", 6000, npc, null, true);
				}
				else if (GUARDS_SPAWNED < 3 && Rnd.get(100) < GUARD_SPAWN_CHANCE)
				{
					if (guard1.isDead() || guard1.getCurrentHp() < 1)
					{
						GUARDS_SPAWNED++;
						guard1 = addSpawn(TIAT_GUARDS, npc.getSpawn().getLocx() + Rnd.get(200), npc.getSpawn().getLocy() + Rnd.get(200), npc.getSpawn()
								.getLocz(), npc.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
					}
					else if (guard2.isDead() || guard2.getCurrentHp() < 1)
					{
						GUARDS_SPAWNED++;
						guard2 = addSpawn(TIAT_GUARDS, npc.getSpawn().getLocx() + Rnd.get(200), npc.getSpawn().getLocy() + Rnd.get(200), npc.getSpawn()
								.getLocz(), npc.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
					}
					else if (guard3.isDead() || guard3.getCurrentHp() < 1)
					{
						GUARDS_SPAWNED++;
						guard3 = addSpawn(TIAT_GUARDS, npc.getSpawn().getLocx() + Rnd.get(200), npc.getSpawn().getLocy() + Rnd.get(200), npc.getSpawn()
								.getLocz(), npc.getSpawn().getHeading(), false, 0, false, npc.getInstanceId());
					}
				}
				break;
		}
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == SPAWN_PORTAL)
		{
			if (npc.getSpawn().getLocx() == -252022 && npc.getSpawn().getLocy() == 210130)
			{
				cancelQuestTimer("Spawn1", npc, null);
				startQuestTimer("respawnPortal1", 120000, npc, null, true);
			}
			else if (npc.getSpawn().getLocx() == -248782 && npc.getSpawn().getLocy() == 210130)
			{
				cancelQuestTimer("Spawn2", npc, null);
				startQuestTimer("respawnPortal2", 120000, npc, null, true);
			}
			else if (npc.getSpawn().getLocx() == -252022 && npc.getSpawn().getLocy() == 206875)
			{
				cancelQuestTimer("Spawn3", npc, null);
				startQuestTimer("respawnPortal3", 120000, npc, null, true);
			}
			else if (npc.getSpawn().getLocx() == -248782 && npc.getSpawn().getLocy() == 206875)
			{
				cancelQuestTimer("Spawn4", npc, null);
				startQuestTimer("respawnPortal4", 120000, npc, null, true);
			}
		}
		else if (ArrayUtils.contains(SPAWN_MOB_IDS, npcId))
		{
			SPAWNED_MOB_COUNT--;
		}
		else if (npcId == TIAT_GUARDS)
		{
			GUARDS_SPAWNED--;
		}
		else if (npcId == TIAT)
		{
			cancelQuestTimer("Spawn1", spawner1, null);
			cancelQuestTimer("Spawn2", spawner2, null);
			cancelQuestTimer("Spawn3", spawner3, null);
			cancelQuestTimer("Spawn4", spawner4, null);
			cancelQuestTimer("healTiat", npc, null);
			cancelQuestTimer("respawnPortal1", spawner1, null);
			cancelQuestTimer("respawnPortal2", spawner2, null);
			cancelQuestTimer("respawnPortal3", spawner3, null);
			cancelQuestTimer("respawnPortal4", spawner4, null);
			guard1.deleteMe();
			guard2.deleteMe();
			guard3.deleteMe();
			spawner1.deleteMe();
			spawner2.deleteMe();
			spawner3.deleteMe();
			spawner4.deleteMe();
			TIAT_HEALED = 0;
			SPAWNED_MOB_COUNT = 0;
		}
		return "";
	}

	public static void main(String[] args)
	{
		new SeedOfDestructionAI(-1, SeedOfDestructionAI.class.getSimpleName(), "ai");
	}
}