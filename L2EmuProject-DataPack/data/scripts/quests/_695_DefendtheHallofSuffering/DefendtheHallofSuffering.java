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
package quests._695_DefendtheHallofSuffering;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class DefendtheHallofSuffering extends QuestJython
{
	private static final String	QN				= "_695_DefendtheHallofSuffering";

	// NPCs
	private static final int	TEPIOS			= 32603;
	private static final int	TEPIOSINST		= 32530;
	private static final int	MOUTHOFEKIMUS	= 32537;

	// Quest Item
	private static final int	MARK			= 13691;

	public DefendtheHallofSuffering(int questId, String name, String descr)
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
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
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
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		int npcId = npc.getNpcId();
		byte state = st.getState();
		if (state == State.COMPLETED)
			htmltext = "32603-03.htm";
		else if (state == State.CREATED && npcId == TEPIOS)
		{
			boolean readLvl = (player.getLevel() >= 75 && player.getLevel() <= 82);
			if (readLvl && st.getQuestItemsCount(MARK) == 1)
				htmltext = "32603-01.htm";
			else if (readLvl && st.getQuestItemsCount(MARK) == 0)
				htmltext = "32603-05.htm";
			else
				htmltext = "32603-00.htm";
		}
		else if (state == State.STARTED)
		{
			switch (npcId)
			{
				case MOUTHOFEKIMUS:
					htmltext = "32537-01.htm";
					break;
				case TEPIOSINST:
					htmltext = "32530-01.htm";
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
		new DefendtheHallofSuffering(695, QN, "Defend the Hall of Suffering");
	}
}
