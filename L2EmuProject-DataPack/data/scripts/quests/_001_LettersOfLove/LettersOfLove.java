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
package quests._001_LettersOfLove;

import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Intrepid
 *
 */
public class LettersOfLove extends Quest
{
	private static final String	QN					= "_001_LettersOfLove";

	// NPCs
	private final int			DARIN				= 30048;
	private final int			ROXXY				= 30006;
	private final int			BAULRO				= 30033;

	// Quest Items
	private final int			DARINGS_LETTER		= 687;
	private final int			RAPUNZELS_KERCHIEF	= 688;
	private final int			DARINGS_RECEIPT		= 1079;
	private final int			BAULS_POTION		= 1080;

	// Item Rewards
	private final int			NECKLACE			= 906;
	private final int			ADENA				= 57;

	public LettersOfLove(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(DARIN);
		
		addTalkId(DARIN);
		addTalkId(ROXXY);
		addTalkId(BAULRO);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("30048-06.htm"))
		{
			st.set(CONDITION, "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
			if (!st.hasQuestItems(DARINGS_LETTER))
				st.giveItems(DARINGS_LETTER, 1);
		}

		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(QN);

		if (st.getState() == State.COMPLETED)
			htmltext = QUEST_DONE;
		else if (npcId == DARIN && st.getState() == State.CREATED)
		{
			if (player.getLevel() >= 2)
			{
				if (st.getInt(CONDITION) < 15)
					htmltext = "30048-02.htm";
				else
				{
					htmltext = "30048-01.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "<html><body>Quest for characters level 2 and above.</body></html>";
				st.exitQuest(true);
			}
		}
		else if (st.getState() == State.STARTED)
		{
			if (npcId == ROXXY)
			{
				if (st.getQuestItemsCount(RAPUNZELS_KERCHIEF) == 0 && st.getQuestItemsCount(DARINGS_LETTER) > 0)
				{
					htmltext = "30006-01.htm";
					st.takeItems(DARINGS_LETTER, 1);
					st.giveItems(RAPUNZELS_KERCHIEF, 1);
					st.set(CONDITION, "2");
					st.playSound("ItemSound.quest_middle");
				}
				else if (st.getQuestItemsCount(BAULS_POTION) > 0 || st.getQuestItemsCount(RAPUNZELS_KERCHIEF) > 0)
					htmltext = "30006-03.htm";
				else if (st.getQuestItemsCount(RAPUNZELS_KERCHIEF) > 0)
					htmltext = "30006-02.htm";
			}
			else if (npcId == DARIN && st.getQuestItemsCount(RAPUNZELS_KERCHIEF) > 0)
			{
				htmltext = "30048-08.htm";
				st.takeItems(RAPUNZELS_KERCHIEF, 1);
				st.giveItems(DARINGS_RECEIPT, 1);
				st.set(CONDITION, "3");
				st.playSound("ItemSound.quest_middle");
			}
			else if (npcId == BAULRO)
			{
				if (st.getQuestItemsCount(DARINGS_RECEIPT) > 0)
				{
					htmltext = "30033-01.htm";
					st.takeItems(DARINGS_RECEIPT, 1);
					st.giveItems(BAULS_POTION, 1);
					st.set(CONDITION, "4");
					st.playSound("ItemSound.quest_middle");
				}
				else if (st.getQuestItemsCount(BAULS_POTION) > 0)
					htmltext = "30033-02.htm";
			}
			else if (npcId == DARIN && st.getQuestItemsCount(RAPUNZELS_KERCHIEF) > 0)
			{
				if (st.getQuestItemsCount(DARINGS_RECEIPT) > 0)
					htmltext = "30048-09.htm";
				else if (st.getQuestItemsCount(BAULS_POTION) > 0)
				{
					htmltext = "30048-10.htm";
					st.takeItems(BAULS_POTION, 1);
					st.rewardItems(ADENA, 2466);
					st.giveItems(NECKLACE, 1);
					st.addExpAndSp(5672, 446);
					st.unset("cond");
					st.exitQuest(false);
					st.playSound("ItemSound.quest_finish");
				}
				else
					htmltext = "30048-07.htm";
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new LettersOfLove(1, QN, "Letters Of Love");
	}
}
