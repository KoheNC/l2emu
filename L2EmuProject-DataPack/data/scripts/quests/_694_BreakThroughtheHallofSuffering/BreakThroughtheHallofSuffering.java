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
package quests._694_BreakThroughtheHallofSuffering;

import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * @author L0ngh0rn
 */
public final class BreakThroughtheHallofSuffering extends QuestJython
{
	private static final String	QN				= "_694_BreakThroughtheHallofSuffering";

	// NPCs
	private static final int	TEPIOS			= 32603;
	private static final int	TEPIOSINST		= 32530;
	private static final int	MOUTHOFEKIMUS	= 32537;

	// Quet Item
	private static final int	MARK			= 13691;

	public BreakThroughtheHallofSuffering(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(TEPIOS);
		addStartNpc(MOUTHOFEKIMUS);

		addTalkId(TEPIOS);
		addTalkId(MOUTHOFEKIMUS);

		questItemIds = new int[]
		{ MARK };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32603-02.htm"))
		{
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
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

		byte state = st.getState();
		if (state == State.COMPLETED)
			htmltext = "32603-03.htm";
		else if (state == State.CREATED && npc.getNpcId() == TEPIOS)
		{
			if (player.getLevel() >= 75 && player.getLevel() <= 82)
				htmltext = "32603-01.htm";
			else if (player.getLevel() > 82 && st.getQuestItemsCount(MARK) == 0)
			{
				st.giveItems(13691, 1);
				st.sendPacket(SND_MIDDLE);
				st.setState(State.COMPLETED);
				htmltext = "32603-05.htm";
			}
			else
				htmltext = "32603-00.htm";
		}
		else if (state == State.STARTED)
		{
			switch (npc.getNpcId())
			{
				case MOUTHOFEKIMUS:
					htmltext = "32537-01.htm";
					break;
				case TEPIOSINST:
					htmltext = "32530-1.htm";
					break;
				case TEPIOS:
					htmltext = "32603-04.htm";
					st.exitQuest(true);
					if (st.getQuestItemsCount(MARK) == 0)
						st.giveItems(13691, 1);
					st.giveItems(736, 1);
					st.sendPacket(SND_FINISH);
					break;
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new BreakThroughtheHallofSuffering(694, QN, "Break Through the Hall of Suffering");
	}
}
