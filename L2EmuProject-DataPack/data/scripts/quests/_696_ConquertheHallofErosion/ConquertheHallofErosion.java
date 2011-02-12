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
package quests._696_ConquertheHallofErosion;

import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * @author L0ngh0rn
 */
public final class ConquertheHallofErosion extends QuestJython
{
	private static final String	QN				= "_696_ConquertheHallofErosion";

	// NPCs
	private static final int	TEPIOS			= 32603;
	private static final int	MOUTHOFEKIMUS	= 32537;

	// Quest Item
	private static final int	MARK			= 13692;

	public ConquertheHallofErosion(int questId, String name, String descr)
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

		if (event.equalsIgnoreCase("32530-02.htm"))
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

		int npcId = npc.getNpcId();
		byte state = st.getState();

		if (state == State.COMPLETED)
			htmltext = "32530-03.htm";
		else if (state == State.CREATED && npcId == TEPIOS)
		{
			if (player.getLevel() >= 80)
				htmltext = "32530-01.htm";
			else
				htmltext = "32530-00.htm";
		}
		else if (state == State.STARTED)
		{
			switch (npcId)
			{
				case MOUTHOFEKIMUS:
					htmltext = "32537-01.htm";
					break;
				case TEPIOS:
					htmltext = "32530-04.htm";
					st.exitQuest(true);
					if (st.getQuestItemsCount(MARK) == 0)
						st.giveItems(13692, 1);
					st.sendPacket(SND_FINISH);
					break;
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new ConquertheHallofErosion(696, QN, "Conquer the Hall of Erosion");
	}
}
