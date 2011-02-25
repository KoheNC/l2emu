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
package quests._692_HowtoOpposeEvil;

import gnu.trove.TIntObjectHashMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * @author Gigiikun
 * @since Adapted for L2EmuProject by L0ngh0rn 2011-02-10
 */
public final class HowtoOpposeEvil extends QuestJython
{
	public static final String							QN					= "_692_HowtoOpposeEvil";

	private static final int							DILIOS				= 32549;
	
	private static final int							LEKONS_CERTIFICATE	= 13857;
	private static final int[]							QUEST_ITEMS			=
																			{ 13863, 13864, 13865, 13866, 13867, 15535, 15536 };

	private static final TIntObjectHashMap<Integer[]>	QUEST_MOBS			= new TIntObjectHashMap<Integer[]>();

	static
	{
		// Seed of Infinity
		QUEST_MOBS.put(22509, new Integer[]
		{ 13863, 500 });
		QUEST_MOBS.put(22510, new Integer[]
		{ 13863, 500 });
		QUEST_MOBS.put(22511, new Integer[]
		{ 13863, 500 });
		QUEST_MOBS.put(22512, new Integer[]
		{ 13863, 500 });
		QUEST_MOBS.put(22513, new Integer[]
		{ 13863, 500 });
		QUEST_MOBS.put(22514, new Integer[]
		{ 13863, 500 });
		QUEST_MOBS.put(22515, new Integer[]
		{ 13863, 500 });

		// Seed of Destruction
		QUEST_MOBS.put(22537, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22538, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22539, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22540, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22541, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22542, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22543, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22544, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22546, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22547, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22548, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22549, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22550, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22551, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22552, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22593, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22596, new Integer[]
		{ 13865, 250 });
		QUEST_MOBS.put(22597, new Integer[]
		{ 13865, 250 });

		// Seed of Annihilation
		QUEST_MOBS.put(22746, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22747, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22748, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22749, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22750, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22751, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22752, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22753, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22754, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22755, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22756, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22757, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22758, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22759, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22760, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22761, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22762, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22763, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22764, new Integer[]
		{ 15536, 125 });
		QUEST_MOBS.put(22765, new Integer[]
		{ 15536, 125 });
	}

	public HowtoOpposeEvil(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (int i : QUEST_MOBS.keys())
			addKillId(i);
		addStartNpc(DILIOS);
		addTalkId(DILIOS);
		addTalkId(32550);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return "";

		if (event.equalsIgnoreCase("32549-03.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32550-04.htm"))
			st.set(CONDITION, 3);
		else if (event.equalsIgnoreCase("32550-07.htm"))
		{
			if (!giveReward(st, 13863, 5, 13796, 1))
				return "32550-08.htm";
		}
		else if (event.equalsIgnoreCase("32550-09.htm"))
		{
			if (!giveReward(st, 13798, 1, 57, 5000))
				return "32550-10.htm";
		}
		else if (event.equalsIgnoreCase("32550-12.htm"))
		{
			if (!giveReward(st, 13865, 5, 13841, 1))
				return "32550-13.htm";
		}
		else if (event.equalsIgnoreCase("32550-14.htm"))
		{
			if (!giveReward(st, 13867, 1, 57, 5000))
				return "32550-15.htm";
		}
		else if (event.equalsIgnoreCase("32550-17.htm"))
		{
			if (!giveReward(st, 15536, 5, 15486, 1))
				return "32550-18.htm";
		}
		else if (event.equalsIgnoreCase("32550-19.htm"))
		{
			if (!giveReward(st, 15535, 1, 57, 5000))
				return "32550-20.htm";
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(QN);
		if (st == null)
			return NO_QUEST;

		final byte id = st.getState();
		final int cond = st.getInt(CONDITION);
		String htmltext = "";
		if (id == State.CREATED)
		{
			if (player.getLevel() >= 75)
				htmltext = "32549-01.htm";
			else
				htmltext = "32549-00.htm";
		}
		else
		{
			if (npc.getNpcId() == DILIOS)
			{
				if (cond == 1 && st.getQuestItemsCount(LEKONS_CERTIFICATE) >= 1)
				{
					st.takeItems(LEKONS_CERTIFICATE, 1);
					htmltext = "32549-04.htm";
					st.set(CONDITION, 2);
				}
				else if (cond == 2)
					htmltext = "32549-05.htm";
			}
			else
			{
				if (cond == 2)
					htmltext = "32550-01.htm";
				else if (cond == 3)
				{
					for (int i : QUEST_ITEMS)
						if (st.getQuestItemsCount(i) > 0)
							return "32550-05.htm";
					htmltext = "32550-04.htm";
				}
			}
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "3");
		if (partyMember == null)
			return null;
		final QuestState st = partyMember.getQuestState(QN);
		if (st != null && QUEST_MOBS.containsKey(npc.getNpcId()))
		{
			int chance = (int) (QUEST_MOBS.get(npc.getNpcId())[1] * Config.RATE_DROP_QUEST);
			int numItems = chance / 1000;
			chance = chance % 1000;
			if (st.getRandom(1000) < chance)
				numItems++;
			if (numItems > 0)
			{
				st.giveItems(QUEST_MOBS.get(npc.getNpcId())[0], numItems);
				st.sendPacket(SND_ITEM_GET);
			}
		}
		return null;
	}

	private final boolean giveReward(QuestState st, int itemId, int minCount, int rewardItemId, long rewardCount)
	{
		long count = st.getQuestItemsCount(itemId);
		if (count >= minCount)
		{
			count = count / minCount;
			st.takeItems(itemId, count * minCount);
			if (rewardItemId == PcInventory.ADENA_ID)
				st.giveAdena(rewardCount * count);
			else
				st.giveItems(rewardItemId, rewardCount * count);
			return true;
		}
		return false;
	}

	public static void main(String[] args)
	{
		new HowtoOpposeEvil(692, QN, "How to Oppose Evil", QN);
	}
}
