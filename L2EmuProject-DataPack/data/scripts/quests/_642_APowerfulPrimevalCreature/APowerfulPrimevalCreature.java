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
package quests._642_APowerfulPrimevalCreature;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/* 
 * @author lewzer
 */

public final class APowerfulPrimevalCreature extends QuestJython
{
	private static final String	QN					= "_642_APowerfulPrimevalCreature";
	// NPCs
	private static final int	DINN				= 32105;
	// Mobs
	private static final int	ANCIENT_EGG			= 18344;
	private static final int[]	DINOS				=
													{
			22196,
			22197,
			22198,
			22199,
			22200,
			22201,
			22202,
			22203,
			22204,
			22205,
			22218,
			22219,
			22220,
			22223,
			22224,
			22225,									};
	// Items
	private static final int	ADENA				= 57;
	private static final int[]	REWARDS				=
													{ 8690, 8692, 8694, 8696, 8698, 8700, 8702, 8704, 8706, 8708, 8710 };
	private static final int[]	REWARDS_S80			=
													{ 9967, 9968, 9969, 9970, 9971, 9972, 9973, 9974, 9975 };
	// Quest Items
	private static final int	DINO_TISSUE			= 8774;
	private static final int	DINO_EGG			= 8775;
	// Chances
	private static final int	DINO_TISSUE_CHANCE	= 33;
	private static final int	DINO_EGG_CHANCE		= 1;

	public APowerfulPrimevalCreature(int questId, String name, String descr)
	{

		super(questId, name, descr);
		addStartNpc(DINN);
		addTalkId(DINN);
		addKillId(ANCIENT_EGG);
		for (int i : DINOS)
			addKillId(i);
		questItemIds = new int[]
		{ DINO_TISSUE, DINO_EGG };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		if (event.equalsIgnoreCase("32105-02.htm"))
			htmltext = "32105-02.htm";
		else if (event.equalsIgnoreCase("32105-00b.htm"))
		{
			htmltext = "32105-00b.htm";
		}
		else if (event.equalsIgnoreCase("32105-03.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, 1);
			st.sendPacket(SND_ACCEPT);
			htmltext = "32105-03.htm";
		}
		else if (event.equalsIgnoreCase("32105-05.htm"))
		{
			htmltext = "32105-05.htm";
			st.set("rcpTRADE", "1");
		}
		else if (st.getInt("rcpTRADE") == 1)
		{
			int reward_A = Integer.valueOf(event);
			if (ArrayUtils.contains(REWARDS, reward_A) && st.getQuestItemsCount(DINO_TISSUE) >= 150 && st.getQuestItemsCount(DINO_EGG) >= 1)
			{
				st.giveItems(reward_A, 1);
				st.takeItems(DINO_TISSUE, 150);
				st.takeItems(DINO_EGG, 1);
				htmltext = "32105-08.htm";
				st.set("rcpTRADE", "0");
			}
			else
			{
				htmltext = "32105-06.htm";
			}
		}
		else if (event.equalsIgnoreCase("32105-07.htm"))
		{
			if (st.getQuestItemsCount(DINO_TISSUE) > 0)
			{
				st.giveItems(ADENA, st.getQuestItemsCount(DINO_TISSUE) * 3000);
				st.takeItems(DINO_TISSUE, st.getQuestItemsCount(DINO_TISSUE));
				htmltext = "32105-08.htm";
			}
			else
			{
				htmltext = "32105-00a.htm";
			}
		}
		else if (event.equalsIgnoreCase("32105-09.htm"))
		{
			htmltext = "32105-09.htm";
			st.set("rcpTRADE", "2");
		}
		else if (st.getInt("rcpTRADE") == 2)
		{
			int reward_S80 = Integer.valueOf(event);
			if (ArrayUtils.contains(REWARDS_S80, reward_S80) && st.getQuestItemsCount(DINO_TISSUE) >= 450)
			{
				st.giveItems(reward_S80, 1);
				st.takeItems(DINO_TISSUE, 450);
				htmltext = "32105-08.htm";
				st.set("rcpTRADE", "0");
			}
			else
			{
				htmltext = "32105-07.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return htmltext;

		if (npc.getNpcId() == DINN)
		{
			st.set("rcpTRADE", "0");
			int cond = st.getInt(CONDITION);
			switch (cond)
			{
				case 0:
					if (st.getPlayer().getLevel() < 75)
					{
						st.exitQuest(true);
						htmltext = "32105-00.htm";
					}
					else
					{
						htmltext = "32105-01.htm";
					}
					break;
				case 1:
					if (st.getQuestItemsCount(DINO_TISSUE) == 0 && st.getQuestItemsCount(DINO_EGG) == 0)
					{
						htmltext = "32105-00a.htm";
					}
					else
					{
						htmltext = "32105-04.htm";
					}
					break;
			}
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return null;
		if (ArrayUtils.contains(DINOS, npc.getNpcId()) && DINO_TISSUE_CHANCE >= Rnd.get(100) && st.getInt(CONDITION) == 1)
		{
			st.giveItems(DINO_TISSUE, 1);
			st.sendPacket(SND_ITEM_GET);
		}
		if (npc.getNpcId() == ANCIENT_EGG && DINO_EGG_CHANCE >= Rnd.get(100) && st.getInt(CONDITION) == 1)
		{
			st.giveItems(DINO_EGG, 1);
			st.sendPacket(SND_ITEM_GET);
		}
		return null;
	}

	public static void main(String[] args)
	{
		new APowerfulPrimevalCreature(642, QN, "A powerful Primeval Creature");
	}
}