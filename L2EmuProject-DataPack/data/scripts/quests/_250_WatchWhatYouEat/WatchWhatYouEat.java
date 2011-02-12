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
package quests._250_WatchWhatYouEat;

import net.l2emuproject.gameserver.instancemanager.QuestManager;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 ** @author Gnacik
 **
 ** 2010-08-05 Based on Freya PTS
 */
public final class WatchWhatYouEat extends QuestJython
{
	private static final String		QN			= "_250_WatchWhatYouEat";
	// NPCs
	private static final int		SALLY		= 32743;
	// Mobs - Items
	private static final int[][]	MONSTERS	=
												{
												{ 18864, 15493 },
												{ 18865, 15494 },
												{ 18868, 15495 } };

	public WatchWhatYouEat(int questId, String name, String descr)
	{
		super(questId, name, descr);

		questItemIds = new int[]
		{ 15493, 15494, 15495 };

		addStartNpc(SALLY);
		addFirstTalkId(SALLY);
		addTalkId(SALLY);

		for (int i[] : MONSTERS)
			addKillId(i[0]);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == SALLY)
		{
			if (event.equalsIgnoreCase("32743-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32743-end.htm"))
			{
				st.unset("cond");
				st.rewardItems(57, 135661);
				st.addExpAndSp(698334, 76369);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
			}
			else if (event.equalsIgnoreCase("32743-22.html") && st.getState() == State.COMPLETED)
			{
				htmltext = "32743-23.html";
			}
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

		if (npc.getNpcId() == SALLY)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 82)
						htmltext = "32743-01.htm";
					else
						htmltext = "32743-00.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
					{
						htmltext = "32743-04.htm";
					}
					else if (st.getInt("cond") == 2)
					{
						if (st.hasQuestItems(MONSTERS[0][1]) && st.hasQuestItems(MONSTERS[1][1]) && st.hasQuestItems(MONSTERS[2][1]))
						{
							htmltext = "32743-05.htm";
							for (int items[] : MONSTERS)
								st.takeItems(items[1], -1);
						}
						else
							htmltext = "32743-06.htm";
					}
					break;
				case State.COMPLETED:
					htmltext = "32743-done.htm";
					break;
			}
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return null;
		if (st.getState() == State.STARTED && st.getInt("cond") == 1)
		{
			for (int mob[] : MONSTERS)
			{
				if (npc.getNpcId() == mob[0])
				{
					if (!st.hasQuestItems(mob[1]))
					{
						st.giveItems(mob[1], 1);
						st.playSound("ItemSound.quest_itemget");
					}
				}
			}
			if (st.hasQuestItems(MONSTERS[0][1]) && st.hasQuestItems(MONSTERS[1][1]) && st.hasQuestItems(MONSTERS[2][1]))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		return null;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(QN);
			st = q.newQuestState(player);
		}

		if (npc.getNpcId() == SALLY)
			return "32743-20.html";

		return null;
	}

	public static void main(String[] args)
	{
		new WatchWhatYouEat(250, QN, "Watch What You Eat");
	}
}
