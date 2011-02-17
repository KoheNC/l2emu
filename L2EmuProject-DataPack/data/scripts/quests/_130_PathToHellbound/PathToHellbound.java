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
package quests._130_PathToHellbound;

import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * Rewritten by lewzer
 */
public final class PathToHellbound extends QuestJython
{
	public static final String	QN				= "_130_PathToHellbound";
	// NPC's
	private static final int	CASIAN			= 30612;
	private static final int	GALATE			= 32292;
	// ITEMS
	private static final int	CASIAN_BLUE_CRY	= 12823;

	public PathToHellbound(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(CASIAN);
		addTalkId(CASIAN);
		addTalkId(GALATE);
		questItemIds = new int[]
		{ CASIAN_BLUE_CRY };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (event.equals("30612-03.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
		}
		if (event.equals("32292-03.htm"))
		{
			st.set(CONDITION, 2);
		}
		if (event.equals("30612-05.htm"))
		{
			st.set(CONDITION, 3);
			st.giveItems(CASIAN_BLUE_CRY, 1);
		}
		if (event.equals("32292-06.htm"))
		{
			st.takeItems(CASIAN_BLUE_CRY, 1);
			st.setState(State.COMPLETED);
			st.exitQuest(false);
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		final int npcId = npc.getNpcId();
		final int cond = st.getInt(CONDITION);
		if (st.isCompleted())
		{
			return QUEST_DONE;
		}

		switch (npcId)
		{
			case CASIAN:
				switch (cond)
				{
					case 0:
						if (st.getPlayer().getLevel() >= 78)
							htmltext = "30612-01.htm";
						else
							htmltext = "30612-00.htm";
						break;
					case 1:
						htmltext = "30612-03.htm";
						break;
					case 2:
						htmltext = "30612-04.htm";
						break;
					case 3:
						htmltext = "30612-05.htm";
						break;
				}
				break;
			case GALATE:
				switch (cond)
				{
					case 1:
						htmltext = "32292-01.htm";
						break;
					case 2:
						htmltext = "32292-03.htm";
						break;
					case 3:
						htmltext = "32292-04.htm";
						break;
				}
				break;
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new PathToHellbound(130, QN, "Path To Hellbound");
	}
}