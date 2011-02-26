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
package instances.NornilsGarden;

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.instancemanager.InstanceManager;
import net.l2emuproject.gameserver.instancemanager.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.instancemanager.QuestManager;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.model.party.L2Party;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.L2Effect;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2DoorInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.lang.ArrayUtils;

/**
 ** @author Gnacik
 **
 ** 2010-10-15 Based on official server Naia
 ** TODO: Targetable door in prepareInstance...
 */
public final class NornilsGarden extends QuestJython
{
	private static final String		QN					= "NornilsGarden";
	private static final int		INSTANCE_ID			= 11;

	private static final int		DURATION_TIME		= 70;
	private static final int		EMPTY_DESTROY_TIME	= 5;

	private static final int		INSTANCE_LVL_MIN	= 18;
	private static final int		INSTANCE_LVL_MAX	= 22;

	private static final int		FIRST_GUARD			= 18362;
	private static final int		GARDEN_GUARD		= 32330;

	private static final int[]		FINAL_GATES			=
														{ 32260, 32261, 32262 };

	private static final int[]		SPAWN_PPL			=
														{ -111184, 74540, -12430 };
	private static final int[]		EXIT_PPL			=
														{ -74058, 52040, -3680 };

	private static final int[][]	AUTO_GATES			=
														{
														{ 20110, 16200001 }, // Warriors gate
			{ 20111, 16200004 }, // Midway gate
			{ 20112, 16200013 }						// Gate
														};

	private static final L2Skill	SKILL1				= SkillTable.getInstance().getInfo(4322, 1);
	private static final L2Skill	SKILL2				= SkillTable.getInstance().getInfo(4327, 1);
	private static final L2Skill	SKILL3				= SkillTable.getInstance().getInfo(4329, 1);
	private static final L2Skill	SKILL4				= SkillTable.getInstance().getInfo(4324, 1);

	private static final int		HERB_JAR			= 18478;

	private static final int[][]	GATEKEEPERS			=
														{
														{ 18352, 9703, 0 }, // Kamael Guard
			{ 18353, 9704, 0 }, // Guardian of Records
			{ 18354, 9705, 0 }, // Guardian of Observation
			{ 18355, 9706, 0 }, // Spicula's Guard
			{ 18356, 9707, 16200024 }, // Harkilgamed's Gatekeeper
			{ 18357, 9708, 16200025 }, // Rodenpicula's Gatekeeper
			{ 18358, 9713, 0 }, // Guardian of Secrets
			{ 18359, 9709, 16200023 }, // Arviterre's Guardian
			{ 18360, 9710, 0 }, // Katenar's Gatekeeper
			{ 18361, 9711, 0 }, // Guardian of Prediction
			{ 25528, 9712, 0 }							// Tiberias
														};

	private static final int[][]	HP_HERBS_DROPLIST	=
														{
														// itemId, count, chance
			{ 8602, 1, 10 },
			{ 8601, 2, 40 },
			{ 8600, 3, 70 }							};

	private static final int[][]	GROUP1				=
														{
														{ 18363, -109899, 74431, -12528, 16488 },
														{ 18483, -109701, 74501, -12528, 24576 },
														{ 18483, -109892, 74886, -12528, 0 },
														{ 18363, -109703, 74879, -12528, 49336 }

														};

	private static final int[][]	GROUP2				=
														{
														{ 18363, -110393, 78276, -12848, 49152 },
														{ 18363, -110561, 78276, -12848, 49152 },
														{ 18362, -110414, 78495, -12905, 48112 },
														{ 18362, -110545, 78489, -12903, 48939 },
														{ 18483, -110474, 78601, -12915, 49488 },
														{ 18362, -110474, 78884, -12915, 49338 },
														{ 18483, -110389, 79131, -12915, 48539 },
														{ 18483, -110551, 79134, -12915, 49151 } };

	private static final int[][]	GROUP3				=
														{
														{ 18483, -107798, 80721, -12912, 0 },
														{ 18483, -107798, 80546, -12912, 0 },
														{ 18347, -108033, 80644, -12912, 0 },
														{ 18363, -108520, 80647, -12912, 0 },
														{ 18483, -108740, 80752, -12912, 0 },
														{ 18363, -109016, 80642, -12912, 0 },
														{ 18483, -108740, 80546, -12912, 0 } };

	private static final int[][]	GROUP4				=
														{
														{ 18362, -110082, 83998, -12928, 0 },
														{ 18362, -110082, 84210, -12928, 0 },
														{ 18363, -109963, 84102, -12896, 0 },
														{ 18347, -109322, 84102, -12880, 0 },
														{ 18362, -109131, 84097, -12880, 0 },
														{ 18483, -108932, 84101, -12880, 0 },
														{ 18483, -109313, 84488, -12880, 0 },
														{ 18362, -109122, 84490, -12880, 0 },
														{ 18347, -108939, 84489, -12880, 0 } };

	private static final int[][]	MP_HERBS_DROPLIST	=
														{
														// itemId, count, chance
			{ 8605, 1, 10 },
			{ 8604, 2, 40 },
			{ 8603, 3, 70 }							};

	private final class NornilsWorld extends InstanceWorld
	{
		public L2Npc	first_npc	= null;
		public boolean	spawned_1	= false;
		public boolean	spawned_2	= false;
		public boolean	spawned_3	= false;
		public boolean	spawned_4	= false;

		public NornilsWorld()
		{
			InstanceManager.getInstance().super();
		}
	}

	private static final void dropHerb(L2Npc mob, L2Player player, int[][] drop)
	{
		final int chance = Rnd.get(100);
		for (int i = 0; i < drop.length; i++)
		{
			if (chance < drop[i][2])
				((L2MonsterInstance) mob).dropItem(player, drop[i][0], drop[i][1]);
		}
	}

	private static final void removeBuffs(L2Character ch)
	{
		for (L2Effect e : ch.getAllEffects())
		{
			if (e == null)
				continue;
			L2Skill skill = e.getSkill();
			if (skill.isDebuff() || skill.isStayAfterDeath())
				continue;
			e.exit();
		}
	}

	private static final void giveBuffs(L2Character ch)
	{
		if (SKILL1 != null)
			SKILL1.getEffects(ch, ch);
		if (SKILL2 != null)
			SKILL2.getEffects(ch, ch);
		if (SKILL3 != null)
			SKILL3.getEffects(ch, ch);
		if (SKILL4 != null)
			SKILL4.getEffects(ch, ch);
	}

	private static final void teleportPlayer(L2Player player, int[] coords, int instanceId)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(QN);
			st = q.newQuestState(player);
		}
		removeBuffs(player);
		giveBuffs(player);
		if (player.getPet() != null)
		{
			removeBuffs(player.getPet());
			giveBuffs(player.getPet());
		}
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	private void exitInstance(L2Player player)
	{
		InstanceWorld inst = InstanceManager.getInstance().getWorld(player.getInstanceId());
		if (inst instanceof NornilsWorld)
		{
			NornilsWorld world = ((NornilsWorld) inst);
			world.allowed.remove(Integer.valueOf(player.getObjectId()));
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.setInstanceId(0);
			player.teleToLocation(EXIT_PPL[0], EXIT_PPL[1], EXIT_PPL[2], true);
		}
	}

	private final synchronized String enterInstance(L2Npc npc, L2Player player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (!(world instanceof NornilsWorld) || world.templateId != INSTANCE_ID)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return null;
			}
			// check for level difference again on reenter
			if (player.getLevel() > INSTANCE_LVL_MAX || player.getLevel() < INSTANCE_LVL_MIN)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(player);
				player.sendPacket(sm);
				return null;
			}
			// check what instance still exist
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
			{
				teleportPlayer(player, SPAWN_PPL, world.instanceId);
			}
			return null;
		}
		// Creating new instance
		else
		{
			String result = checkConditions(npc, player);
			if (!(result.equalsIgnoreCase("ok")))
				return result;

			final int instanceId = InstanceManager.getInstance().createDynamicInstance("NornilsGarden.xml");
			final Instance inst = InstanceManager.getInstance().getInstance(instanceId);

			inst.setName(InstanceManager.getInstance().getInstanceIdName(INSTANCE_ID));
			final int[] returnLoc =
			{ player.getX(), player.getY(), player.getZ() };
			inst.setSpawnLoc(returnLoc);
			inst.setAllowSummon(false);
			inst.setDuration(DURATION_TIME * 60000);
			inst.setEmptyDestroyTime(EMPTY_DESTROY_TIME * 60000);
			world = new NornilsWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID;
			InstanceManager.getInstance().addWorld(world);
			_log.info("Nornils Garden: started, Instance: " + instanceId + " created by player: " + player.getName());

			prepareInstance((NornilsWorld) world);

			// and finally teleport party into instance
			final L2Party party = player.getParty();
			if (party != null)
			{
				for (L2Player partyMember : party.getPartyMembers())
				{
					world.allowed.add(partyMember.getObjectId());
					teleportPlayer(partyMember, SPAWN_PPL, instanceId);
				}
			}
			return null;
		}
	}

	private void prepareInstance(NornilsWorld world)
	{
		world.first_npc = addSpawn(18362, -109702, 74696, -12528, 49568, false, 0, false, world.instanceId);
	}

	private void spawn1(L2Npc npc)
	{
		InstanceWorld inst = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (inst instanceof NornilsWorld)
		{
			NornilsWorld world = ((NornilsWorld) inst);
			if (npc.equals(world.first_npc) && !world.spawned_1)
			{
				world.spawned_1 = true;

				for (int mob[] : GROUP1)
				{
					addSpawn(mob[0], mob[1], mob[2], mob[3], mob[4], false, 0, false, world.instanceId);
				}
			}
		}
	}

	private void spawn2(L2Npc npc)
	{
		InstanceWorld inst = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (inst instanceof NornilsWorld)
		{
			NornilsWorld world = ((NornilsWorld) inst);
			if (!world.spawned_2)
			{
				world.spawned_2 = true;

				for (int mob[] : GROUP2)
				{
					addSpawn(mob[0], mob[1], mob[2], mob[3], mob[4], false, 0, false, world.instanceId);
				}
			}
		}
	}

	private void spawn3(L2Character cha)
	{
		InstanceWorld inst = InstanceManager.getInstance().getWorld(cha.getInstanceId());
		if (inst instanceof NornilsWorld)
		{
			NornilsWorld world = ((NornilsWorld) inst);
			if (!world.spawned_3)
			{
				world.spawned_3 = true;

				for (int mob[] : GROUP3)
				{
					addSpawn(mob[0], mob[1], mob[2], mob[3], mob[4], false, 0, false, world.instanceId);
				}
			}
		}
	}

	private void spawn4(L2Character cha)
	{
		InstanceWorld inst = InstanceManager.getInstance().getWorld(cha.getInstanceId());
		if (inst instanceof NornilsWorld)
		{
			NornilsWorld world = ((NornilsWorld) inst);
			if (!world.spawned_4)
			{
				world.spawned_4 = true;

				for (int mob[] : GROUP4)
				{
					addSpawn(mob[0], mob[1], mob[2], mob[3], mob[4], false, 0, false, world.instanceId);
				}
			}
		}
	}

	private void openDoor(QuestState st, L2Player player, int doorId)
	{
		st.unset("correct");
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
		if (tmpworld instanceof NornilsWorld)
		{
			L2DoorInstance door = InstanceManager.getInstance().getInstance(tmpworld.instanceId).getDoor(doorId);
			if (door != null)
				door.openMe();
		}
	}

	private static final String checkConditions(L2Npc npc, L2Player player)
	{
		final L2Party party = player.getParty();
		// player must be in party
		if (party == null)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			return "32330-05.html";
		}
		// ...and be party leader
		if (party.getLeader() != player)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
			return "32330-08.html";
		}
		boolean _kamael = false;
		// for each party member
		for (L2Player partyMember : party.getPartyMembers())
		{
			// player level must be in range
			if (partyMember.getLevel() > INSTANCE_LVL_MAX)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				player.sendPacket(sm);
				return "32330-06.html";
			}
			if (partyMember.getLevel() < INSTANCE_LVL_MIN)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				player.sendPacket(sm);
				return "32330-07.html";
			}
			if (partyMember.getClassId().level() != 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				player.sendPacket(sm);
				return "32330-06.html";
			}
			// player must be near party leader
			if (!partyMember.isInsideRadius(player, 500, true, true))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				player.sendPacket(sm);
				return "32330-08.html";
			}
			if (partyMember.getRace().ordinal() == 5)
			{
				QuestState checkst = partyMember.getQuestState("179_IntoTheLargeCavern");
				if (checkst != null && checkst.getState() == State.STARTED)
				{
					_kamael = true;
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(partyMember);
					player.sendPacket(sm);
					return "32330-08.html";
				}
			}
		}
		if (!_kamael)
		{
			return "32330-08.html";
		}
		return "ok";
	}

	@Override
	public String onEnterZone(L2Character character, L2Zone zone)
	{
		if (character instanceof L2Player && !character.isDead() && !character.isTeleporting())
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if (tmpworld instanceof NornilsWorld)
			{
				for (int _auto[] : AUTO_GATES)
				{
					if (zone.getId() == _auto[0])
					{
						L2DoorInstance door = InstanceManager.getInstance().getInstance(tmpworld.instanceId).getDoor(_auto[1]);
						if (door != null)
							door.openMe();
					}
					if (zone.getId() == 20111)
						spawn3(character);
					else if (zone.getId() == 20112)
						spawn4(character);

				}
			}
		}
		return super.onEnterZone(character, zone);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		player.sendMessage("On Event");

		String htmltext = event;
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return NO_QUEST;

		if (npc.getNpcId() == GARDEN_GUARD && event.equalsIgnoreCase("enter_instance"))
		{
			try
			{
				htmltext = enterInstance(npc, player);
			}
			catch (Exception e)
			{
			}
		}
		else if (npc.getNpcId() == 32258 && event.equalsIgnoreCase("exit"))
		{
			try
			{
				exitInstance(player);
			}
			catch (Exception e)
			{
			}
		}
		else if (ArrayUtils.contains(FINAL_GATES, npc.getNpcId()))
		{
			if (event.equalsIgnoreCase("32260-02.html") || event.equalsIgnoreCase("32261-02.html") || event.equalsIgnoreCase("32262-02.html"))
			{
				st.unset("correct");
			}
			else if (Util.isDigit(event))
			{
				int correct = st.getInt("correct");
				correct++;
				st.set("correct", String.valueOf(correct));
				htmltext = npc.getNpcId() + "-0" + String.valueOf(correct + 2) + ".html";
			}
			else if (event.equalsIgnoreCase("check"))
			{
				int correct = st.getInt("correct");
				if (npc.getNpcId() == 32260 && correct == 3)
					openDoor(st, player, 16200014);
				else if (npc.getNpcId() == 32261 && correct == 3)
					openDoor(st, player, 16200015);
				else if (npc.getNpcId() == 32262 && correct == 4)
					openDoor(st, player, 16200016);
				else
					return npc.getNpcId() + "-00.html";
			}
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		if (ArrayUtils.contains(FINAL_GATES, npc.getNpcId()))
		{
			QuestState cst = player.getQuestState("179_IntoTheLargeCavern");
			if (cst != null && cst.getState() == State.STARTED)
			{
				return npc.getNpcId() + "-01.html";
			}
			else
				return NO_QUEST;
		}

		return null;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(QN);
			st = q.newQuestState(player);
		}
		return npc.getNpcId() + ".html";
	}

	@Override
	public final String onAttack(L2Npc npc, L2Player attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == HERB_JAR && !npc.isDead())
		{
			dropHerb(npc, attacker, HP_HERBS_DROPLIST);
			dropHerb(npc, attacker, MP_HERBS_DROPLIST);
			npc.doDie(attacker);
		}
		else if (npc.getNpcId() == 18362 && npc.getInstanceId() > 0)
		{
			spawn1(npc);
		}
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return null;

		for (int _gk[] : GATEKEEPERS)
		{
			if (npc.getNpcId() == _gk[0])
			{
				// Drop key
				((L2MonsterInstance) npc).dropItem(player, _gk[1], 1);

				// Check if gatekeeper should open bridge, and open it
				if (_gk[2] > 0)
				{
					InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
					if (tmpworld instanceof NornilsWorld)
					{
						L2DoorInstance door = InstanceManager.getInstance().getInstance(tmpworld.instanceId).getDoor(_gk[2]);
						if (door != null)
						{
							door.openMe();
							door.sendInfo(player);
						}
					}
				}
			}
			if (npc.getNpcId() == 18355)
				spawn2(npc);
		}
		return super.onKill(npc, player, isPet);
	}

	public NornilsGarden(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(GARDEN_GUARD);
		addFirstTalkId(GARDEN_GUARD);
		addTalkId(GARDEN_GUARD);

		for (int i[] : GATEKEEPERS)
			addKillId(i[0]);
		for (int i[] : AUTO_GATES)
			addEnterZoneId(i[0]);
		for (int i : FINAL_GATES)
			addTalkId(i);

		addAttackId(HERB_JAR);
		addAttackId(FIRST_GUARD); // first garden guard
	}

	public static void main(String[] args)
	{
		new NornilsGarden(-1, QN, "instances");
	}
}