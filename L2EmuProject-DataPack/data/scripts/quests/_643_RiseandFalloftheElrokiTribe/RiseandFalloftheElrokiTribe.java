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
package quests._643_RiseandFalloftheElrokiTribe;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * @author L0ngh0rn
 * @since Created by Gigiikun (.py)
 */
public final class RiseandFalloftheElrokiTribe extends QuestJython
{
	private static final String					QN							= "_643_RiseandFalloftheElrokiTribe";

	// NPCs
	private static final int					SINGSING					= 32106;
	private static final int					SHAMAN_KARAKAWEI			= 32117;

	// MOBs
	private static final int[]					PLAIN_DINOSAURS				=
																			{
			22201,
			22202,
			22204,
			22205,
			22209,
			22210,
			22212,
			22213,
			22219,
			22220,
			22221,
			22222,
			22224,
			22225,
			22742,
			22743,
			22744,
			22745															};

	// Quest Item
	private static final int					BONES_OF_A_PLAINS_DINOSAUR	= 8776;

	// Chance (100% = 1000)
	private static final int					DROP_CHANCE					= 750;

	// Rewards
	private static final int[]					REWARDS						=
																			{ 8712, 8713, 8714, 8715, 8716, 8717, 8718, 8719, 8720, 8721, 8722 };
	private static final FastMap<String, int[]>	REWARDS_DYNA				= new FastMap<String, int[]>();

	static
	{
		REWARDS_DYNA.put("1", new int[]
		{ 9492, 400 }); // Recipe: Sealed Dynasty Tunic (60%)
		REWARDS_DYNA.put("2", new int[]
		{ 9493, 250 }); // Recipe: Sealed Dynasty Stockings (60%)
		REWARDS_DYNA.put("3", new int[]
		{ 9494, 200 }); // Recipe: Sealed Dynasty Circlet (60%)
		REWARDS_DYNA.put("4", new int[]
		{ 9495, 134 }); // Recipe: Sealed Dynasty Gloves (60%)
		REWARDS_DYNA.put("5", new int[]
		{ 9496, 134 }); // Recipe: Sealed Dynasty Shoes (60%)
		REWARDS_DYNA.put("6", new int[]
		{ 10115, 287 }); // Recipe: Sealed Dynasty Sigil (60%)
	}

	public RiseandFalloftheElrokiTribe(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(SINGSING);

		addTalkId(SINGSING);
		addTalkId(SHAMAN_KARAKAWEI);

		for (int i : PLAIN_DINOSAURS)
			addKillId(i);

		questItemIds = new int[]
		{ BONES_OF_A_PLAINS_DINOSAUR };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);

		if (event.isEmpty() || event.equalsIgnoreCase("none"))
			return null;
		else if (event.equalsIgnoreCase("32106-03.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32117-03.htm"))
		{
			if (count >= 300)
			{
				st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, 300);
				st.rewardItems(REWARDS[st.getRandom(REWARDS.length - 1)], 5);
			}
			else
				htmltext = "32117-04.htm";
		}
		else if (REWARDS_DYNA.containsKey(event))
		{
			if (count >= REWARDS_DYNA.get(event)[1])
			{
				st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, REWARDS_DYNA.get(event)[1]);
				st.rewardItems(REWARDS_DYNA.get(event)[0], 1);
				htmltext = "32117-06.htm";
			}
			else
				htmltext = "32117-07.htm";
		}
		else if (event.equalsIgnoreCase("quit"))
		{
			st.sendPacket(SND_FINISH);
			st.exitQuest(true);
			return null;
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		int npcId = npc.getNpcId();
		long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);

		if (st.getInt(CONDITION) == 0 && npcId == SINGSING)
		{
			if (player.getLevel() >= 75)
				htmltext = "32106-01.htm";
			else
			{
				htmltext = "32106-00.htm";
				st.exitQuest(true);
			}
		}
		else if (st.getState() == State.STARTED)
		{
			switch (npcId)
			{
				case SINGSING:
					if (count == 0)
						htmltext = "32106-05.htm";
					else
					{
						htmltext = "32106-06.htm";
						st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, -1);
						st.giveAdena(count * 1374);
					}
					break;
				case SHAMAN_KARAKAWEI:
					htmltext = "32117-01.htm";
					break;
			}
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, "1");
		if (partyMember == null)
			return null;

		QuestState st = partyMember.getQuestState(QN);
		if (st == null)
			return null;

		if (st.getState() == State.STARTED)
		{
			long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);
			if (st.getInt(CONDITION) == 1)
			{
				int chance = (int) (DROP_CHANCE * Config.RATE_DROP_QUEST);
				int numItems = (int) (chance / 1000);
				chance = chance % 1000;
				if (st.getRandom(1000) < chance)
					numItems++;
				if (numItems > 0)
				{
					if ((count + numItems) / 300 > count / 300)
						st.sendPacket(SND_MIDDLE);
					else
						st.sendPacket(SND_ITEM_GET);
					st.giveItems(BONES_OF_A_PLAINS_DINOSAUR, numItems);
				}
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new RiseandFalloftheElrokiTribe(643, QN, "Rise and Fall of the Elroki Tribe");
	}
}
