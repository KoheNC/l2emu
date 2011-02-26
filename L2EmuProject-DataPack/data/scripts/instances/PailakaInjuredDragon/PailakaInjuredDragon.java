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
package instances.PailakaInjuredDragon;

import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.manager.instances.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author Unknown-Person, Angy, Q.I, lord_rex
 */
public class PailakaInjuredDragon extends QuestJython
{
	private static final String			QN				= "144_PailakaInjuredDragon";

	private static final int			MIN_LEVEL		= 73;
	private static final int			MAX_LEVEL		= 77;
	private static final int			INSTANCE_ID		= 45;
	private static final int[]			TELEPORT		=
														{ 125757, -40928, -3736 };

	// NPC
	private static final int			KETRAOSHAMAN	= 32499;
	private static final int			KOSUPPORTER		= 32502;
	private static final int			KOIO			= 32509;
	private static final int			KOSUPPORTER2	= 32512;
	private static final int[]			PAILAKA_THIRD	=
														{
			18635,
			18636,
			18638,
			18639,
			18640,
			18641,
			18642,
			18644,
			18645,
			18646,
			18648,
			18649,
			18650,
			18652,
			18653,
			18654,
			18655,
			18656,
			18657,
			18658,
			18659										};
	private static final int[]			ANTELOPES		=
														{ 18637, 18643, 18647, 18651 };
	// BOSS
	private static final int			LATANA			= 18660;
	// ITEMS
	private static final int			SPEAR			= 13052;
	private static final int			ENCHSPEAR		= 13053;
	private static final int			LASTSPEAR		= 13054;
	private static final int			STAGE1			= 13056;
	private static final int			STAGE2			= 13057;
	private static final int[]			PAILAKA3DROP	=
														{ 8600, 8601, 8603, 8604 };
	private static final int[]			ANTELOPDROP		=
														{ 13032, 13033 };
	// REWARDS
	private static final int			PSHIRT			= 13296;
	// ETC
	private static final int[]			AMOUNTS1		=
														{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	private static final int[][]		BUFFS			=
														{
														{ 4357, 2 }, // Haste Lv2
			{ 4342, 2 }, // Wind Walk Lv2
			{ 4356, 3 }, // Empower Lv3
			{ 4355, 3 }, // Acumen Lv3
			{ 4351, 6 }, // Concentration Lv6
			{ 4345, 3 }, // Might Lv3
			{ 4358, 3 }, // Guidance Lv3
			{ 4359, 3 }, // Focus Lv3
			{ 4360, 3 }, // Death Wisper Lv3
			{ 4352, 2 }, // Berserker Spirit Lv2
			{ 4354, 4 }, // Vampiric Rage Lv4
			{ 4347, 6 }								// Blessed Body Lv6
														};

	private final Map<Integer, Integer>	_action			= new FastMap<Integer, Integer>();
	private static final int[]			ITEMS			=
														{ SPEAR, ENCHSPEAR, LASTSPEAR, STAGE1, STAGE2, 8600, 8601, 8603, 8604, 13032, 13033 };
	
	public PailakaInjuredDragon(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(KETRAOSHAMAN);
		addTalkId(KETRAOSHAMAN);
		addTalkId(KOSUPPORTER);
		addTalkId(KOIO);
		addTalkId(KOSUPPORTER2);
		addKillId(18654);
		addKillId(18649);
		addKillId(LATANA);
		for (int mobId : PAILAKA_THIRD)
			addKillId(mobId);
		for (int mobId : ANTELOPES)
			addKillId(mobId);

		questItemIds = ITEMS;
	}

	private static final void teleportPlayer(L2Player player, int[] coords, int instanceId)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	private final synchronized void enterInstance(L2Player player)
	{
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if (world != null)
		{
			if (world.templateId != INSTANCE_ID)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if (inst != null)
				teleportPlayer(player, TELEPORT, world.instanceId);
			return;
		}
		//New instance
		else
		{
			final int instanceId = InstanceManager.getInstance().createDynamicInstance("PailakaInjuredDragon.xml");

			world = InstanceManager.getInstance().new InstanceWorld();
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());
			teleportPlayer(player, TELEPORT, instanceId);

			_log.info("PailakaInjuredDragon (Lvl 73-77): " + instanceId + " created by player: " + player.getName());
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return "";

		if (event.equalsIgnoreCase("enter"))
		{
			enterInstance(player);
			return null;
		}

		String htmltext = event;
		int playerId = player.getCharId();
		int buffId = 0;
		try
		{
			buffId = Integer.parseInt(event);
		}
		catch (Exception e)
		{
		}

		if (buffId > 0 && buffId < 13)
		{
			buffId--;
			int skillId = BUFFS[buffId][0];
			int level = BUFFS[buffId][1];
			int times = 0;
			if (_action.containsKey(playerId))
				times = _action.get(playerId);

			if (times < 4)
			{
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(skillId, level));
				_action.put(playerId, times + 1);
				htmltext = "32509-06.htm";
				return htmltext;
			}
			if (times == 4)
			{
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(skillId, level));
				_action.put(playerId, 5);
				htmltext = "32509-05.htm";
				return htmltext;
			}
			return "32509-04.htm";
		}

		if (event.equalsIgnoreCase("Support"))
		{
			if (!_action.containsKey(playerId))
			{
				htmltext = "32509-06.htm";
				_action.put(playerId, 0);
			}
			if (_action.get(playerId) < 5)
				htmltext = "32509-06.htm";
			else
				htmltext = "32509-04.htm";
			return htmltext;
		}
		//int cond = st.getInt(CONDITION);

		if (event.equalsIgnoreCase("32499-02.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32499-05.htm"))
		{
			st.set(CONDITION, 2);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32499-07.htm"))
		{
			htmltext = "32499-07.htm";
		}
		else if (event.equalsIgnoreCase("32502-05.htm"))
		{
			st.set(CONDITION, 3);
			st.sendPacket(SND_MIDDLE);
			st.giveItems(SPEAR, 1);
		}
		else if (event.equalsIgnoreCase("32512-02.htm"))
		{
			st.takeItems(SPEAR, 1);
			st.takeItems(ENCHSPEAR, 1);
			st.takeItems(LASTSPEAR, 1);
		}
		return htmltext;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		return npc.getNpcId() + ".htm";
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;

		QuestState st = player.getQuestState(QN);
		if (st == null)
			return htmltext;

		int id = st.getState();
		int cond = st.getInt(CONDITION);

		if (id == State.CREATED)
		{
			st.setState(State.STARTED);
			st.set(CONDITION, 0);
		}

		switch (npc.getNpcId())
		{
			case KETRAOSHAMAN:
				if (cond == 0)
				{
					if (player.getLevel() < MIN_LEVEL || player.getLevel() > MAX_LEVEL)
					{
						htmltext = "32499-no.htm";
						st.exitQuest(true);
					}
					else
					{
						_action.put(player.getCharId(), 0);
						htmltext = "32499-01.htm";
					}
				}
				else if (id == State.COMPLETED)
					htmltext = QUEST_DONE;
				else if (cond == 1 || cond == 2 || cond == 3)
					htmltext = "32499-06.htm";
				break;
			case KOSUPPORTER:
				if (cond == 1 || cond == 2)
					htmltext = "32502-01.htm";
				else
					htmltext = "32502-05.htm";
				break;
			case KOIO:
				if (st.getQuestItemsCount(SPEAR) > 0 && st.getQuestItemsCount(STAGE1) == 0)
					htmltext = "32509-01.htm";
				if (st.getQuestItemsCount(ENCHSPEAR) > 0 && st.getQuestItemsCount(STAGE2) == 0)
					htmltext = "32509-01.htm";
				if (st.getQuestItemsCount(SPEAR) == 0 && st.getQuestItemsCount(STAGE1) > 0)
					htmltext = "32509-07.htm";
				if (st.getQuestItemsCount(ENCHSPEAR) == 0 && st.getQuestItemsCount(STAGE2) > 0)
					htmltext = "32509-07.htm";
				if (st.getQuestItemsCount(SPEAR) == 0 && st.getQuestItemsCount(ENCHSPEAR) == 0)
					htmltext = "32509-07.htm";
				if (st.getQuestItemsCount(STAGE1) == 0 && st.getQuestItemsCount(STAGE2) == 0)
					htmltext = "32509-01.htm";
				if (st.getQuestItemsCount(SPEAR) > 0 && st.getQuestItemsCount(STAGE1) > 0)
				{
					st.takeItems(SPEAR, 1);
					st.takeItems(STAGE1, 1);
					st.giveItems(ENCHSPEAR, 1);
					htmltext = "32509-02.htm";
				}
				if (st.getQuestItemsCount(ENCHSPEAR) > 0 && st.getQuestItemsCount(STAGE2) > 0)
				{
					st.takeItems(ENCHSPEAR, 1);
					st.takeItems(STAGE2, 1);
					st.giveItems(LASTSPEAR, 1);
					htmltext = "32509-03.htm";
				}
				if (st.getQuestItemsCount(LASTSPEAR) > 0)
					htmltext = "32509-03.htm";
				break;
			case KOSUPPORTER2:
				if (cond == 4)
				{
					st.giveItems(13129, 1);
					st.takeItems(13032, st.getQuestItemsCount(13032));
					st.takeItems(13033, st.getQuestItemsCount(13033));
					st.giveItems(PSHIRT, 1);
					st.addExpAndSp(28000000, 2850000);
					st.set(CONDITION, 5);
					st.setState(State.COMPLETED);
					st.sendPacket(SND_FINISH);
					st.exitQuest(false);
					Instance instanceObj = InstanceManager.getInstance().getInstance(player.getInstanceId());
					instanceObj.setDuration(300000);
					htmltext = "32512-01.htm";
					player.getPlayerVitality().setVitalityPoints(20000, true);
				}
				else if (id == State.COMPLETED)
					htmltext = "32512-03.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return "";

		int npcId = npc.getNpcId();
		//int cond = st.getInt(CONDITION);
		if (npcId == 18654)
		{
			if (st.getQuestItemsCount(STAGE1) < 1 && st.getQuestItemsCount(SPEAR) > 0)
				st.giveItems(STAGE1, 1);
		}
		else if (npcId == 18649 && st.getQuestItemsCount(ENCHSPEAR) > 0)
		{
			if (st.getQuestItemsCount(STAGE2) < 1)
				st.giveItems(STAGE2, 1);
		}
		else if (npcId == LATANA)
		{
			st.set(CONDITION, 4);
			st.sendPacket(SND_MIDDLE);
			addSpawn(KOSUPPORTER2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false, npc.getInstanceId());
		}
		else if (ArrayUtils.contains(PAILAKA_THIRD, npcId))
		{
			if (Rnd.get(100) < 30)
				st.dropItem((L2MonsterInstance) npc, player, PAILAKA3DROP[Rnd.get(PAILAKA3DROP.length)], 1);
		}
		else if (ArrayUtils.contains(ANTELOPES, npcId))
			st.dropItem((L2MonsterInstance) npc, player, ANTELOPDROP[Rnd.get(ANTELOPDROP.length)], AMOUNTS1[Rnd.get(AMOUNTS1.length)]);

		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onExitZone(L2Character character, L2Zone zone)
	{
		if (character instanceof L2Player && !character.isDead() && !character.isTeleporting() && ((L2Player) character).isOnline() > 0)
		{
			InstanceWorld world = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if (world != null && world.templateId == INSTANCE_ID)
				ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(character, world.instanceId), 1000);
		}
		return super.onExitZone(character, zone);
	}

	private static final class Teleport implements Runnable
	{
		private final L2Character	_char;
		private final int			_instanceId;

		public Teleport(L2Character c, int id)
		{
			_char = c;
			_instanceId = id;
		}

		@Override
		public void run()
		{
			try
			{
				teleportPlayer((L2Player) _char, TELEPORT, _instanceId);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		new PailakaInjuredDragon(144, QN, "Pailaka - Injured Dragon");
	}
}