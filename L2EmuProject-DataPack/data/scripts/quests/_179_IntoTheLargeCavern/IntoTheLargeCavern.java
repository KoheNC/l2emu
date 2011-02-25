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
package quests._179_IntoTheLargeCavern;

import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 ** @author Gnacik
 **
 ** 2010-10-15 Based on official server Naia
 */
public final class IntoTheLargeCavern extends QuestJython
{
	private static final String	QN			= "_179_IntoTheLargeCavern";
	// NPC's
	private static final int	KEKROPUS	= 32138;
	private static final int	NORNIL		= 32258;

	public IntoTheLargeCavern(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(KEKROPUS);
		addTalkId(KEKROPUS);
		addTalkId(NORNIL);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		switch (npc.getNpcId())
		{
			case KEKROPUS:
				if (event.equalsIgnoreCase("32138-03.htm"))
				{
					st.setState(State.STARTED);
					st.set(CONDITION, 1);
					st.sendPacket(SND_ACCEPT);
				}
				break;
			case NORNIL:
				if (event.equalsIgnoreCase("32258-08.htm"))
				{
					st.giveItems(391, 1);
					st.giveItems(413, 1);
					st.sendPacket(SND_FINISH);
					st.exitQuest(false);
				}
				else if (event.equalsIgnoreCase("32258-09.htm"))
				{
					st.giveItems(847, 2);
					st.giveItems(890, 2);
					st.giveItems(910, 1);
					st.sendPacket(SND_FINISH);
					st.exitQuest(false);
				}
				break;
		}

		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return htmltext;

		QuestState prev = player.getQuestState("178_IconicTrinity");
		if (prev != null && prev.getState() == State.COMPLETED && player.getLevel() >= 17 && player.getRace().ordinal() == 5
				&& player.getClassId().level() == 0)
		{
			if (npc.getNpcId() == KEKROPUS)
			{
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = "32138-01.htm";
						break;
					case State.STARTED:
						if (st.getInt(CONDITION) == 1)
							htmltext = "32138-03.htm";
						break;
					case State.COMPLETED:
						htmltext = QUEST_DONE;
						break;
				}
			}
			else if (npc.getNpcId() == NORNIL && st.getState() == State.STARTED)
			{
				htmltext = "32258-01.htm";
			}
		}
		else
			htmltext = "32138-00.htm";

		return htmltext;
	}

	public static void main(String[] args)
	{
		new IntoTheLargeCavern(179, QN, "Into The Large Cavern");
	}
}
