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
package quests._452_FindingtheLostSoldiers;

import java.util.Calendar;

import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

import org.apache.commons.lang.ArrayUtils;

/**
 ** @author Gigiikun
 **
 ** 2010-08-17 Based on Freya PTS
 */
public final class FindingtheLostSoldiers extends QuestJython
{
	private static final String	QN				= "_452_FindingtheLostSoldiers";
	private static final int	JAKAN			= 32773;
	private static final int	TAG_ID			= 15513;
	private static final int[]	SOLDIER_CORPSES	=
												{ 32769, 32770, 32771, 32772 };

	public FindingtheLostSoldiers(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		questItemIds = new int[]
		{ TAG_ID };
		addStartNpc(JAKAN);
		addTalkId(JAKAN);
		for (int i : SOLDIER_CORPSES)
			addTalkId(i);
	}

	/**
	 * Reset time for Quest
	 * Default: 6:30AM on server time
	 */
	private static final int	RESET_HOUR	= 6;
	private static final int	RESET_MIN	= 30;

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == JAKAN)
		{
			if (event.equalsIgnoreCase("32773-3.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (ArrayUtils.contains(SOLDIER_CORPSES, npc.getNpcId()))
		{
			if (st.getInt("cond") == 1)
			{
				st.giveItems(TAG_ID, 1);
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
				npc.deleteMe();
			}
			else
				htmltext = NO_QUEST;
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

		if (npc.getNpcId() == JAKAN)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 84)
						htmltext = "32773-1.htm";
					else
						htmltext = "32773-0.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
						htmltext = "32773-4.htm";
					else if (st.getInt("cond") == 2)
					{
						htmltext = "32773-5.htm";
						st.unset("cond");
						st.takeItems(TAG_ID, 1);
						st.giveItems(57, 95200);
						st.addExpAndSp(435024, 50366);
						st.playSound("ItemSound.quest_finish");
						st.exitQuest(false);

						Calendar reDo = Calendar.getInstance();
						reDo.set(Calendar.MINUTE, RESET_MIN);
						if (reDo.get(Calendar.HOUR_OF_DAY) >= RESET_HOUR)
							reDo.add(Calendar.DATE, 1);
						reDo.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
						st.set("reDoTime", String.valueOf(reDo.getTimeInMillis()));
					}
					break;
				case State.COMPLETED:
					Long reDoTime = Long.parseLong((String) st.get("reDoTime"));
					if (reDoTime > System.currentTimeMillis())
						htmltext = "32773-6.htm";
					else
					{
						st.setState(State.CREATED);
						if (player.getLevel() >= 84)
							htmltext = "32773-1.htm";
						else
							htmltext = "32773-0.htm";
					}
					break;
			}
		}
		else if (ArrayUtils.contains(SOLDIER_CORPSES, npc.getNpcId()))
		{
			if (st.getInt("cond") == 1)
				htmltext = "corpse-1.htm";
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new FindingtheLostSoldiers(452, QN, "Finding the Lost Soldiers", "quests");
	}
}