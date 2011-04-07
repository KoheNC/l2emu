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
package quests._020_BringUpWithLove;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Gnacik 2010-09-29 Based on official server Franz
 * @since Converted by L0ngh0rn (2011-04-05)
 */
public final class BringUpWithLove extends QuestJython
{
	private static final String	QN			= "_020_BringUpWithLove";

	// Npc
	private static final int	TUNATUN		= 31537;

	// Item
	private static final int	BEAST_WHIP	= 15473;
	private static final int	CRYSTAL		= 9553;
	private static final int	JEWEL		= 7185;

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == TUNATUN)
		{
			if (event.equalsIgnoreCase("31537-12.htm"))
			{
				st.setState(State.STARTED);
				st.set(CONDITION, 1);
				st.sendPacket(SND_ACCEPT);
			}
			else if (event.equalsIgnoreCase("31537-03.htm"))
			{
				if (st.hasQuestItems(BEAST_WHIP))
					return "31537-03a.htm";
				else
					st.giveItems(BEAST_WHIP, 1);
			}
			else if (event.equalsIgnoreCase("31537-15.htm"))
			{
				st.unset(CONDITION);
				st.takeItems(JEWEL, -1);
				st.giveItems(CRYSTAL, 1);
				st.sendPacket(SND_FINISH);
				st.exitQuest(false);
			}
			else if (event.equalsIgnoreCase("31537-21.html"))
			{
				if (player.getLevel() < 82)
					return "31537-23.html";
				if (st.hasQuestItems(BEAST_WHIP))
					return "31537-22.html";
				st.giveItems(BEAST_WHIP, 1);
			}
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

		if (npc.getNpcId() == TUNATUN)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 82)
						htmltext = "31537-01.htm";
					else
						htmltext = "31537-00.htm";
					break;
				case State.STARTED:
					if (st.getInt(CONDITION) == 1)
						htmltext = "31537-13.htm";
					else if (st.getInt(CONDITION) == 2)
						htmltext = "31537-14.htm";
					break;
			}
		}
		return htmltext;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			st = newQuestState(player);
		return "31537-20.html";
	}

	public BringUpWithLove(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addStartNpc(TUNATUN);
		addTalkId(TUNATUN);
		addFirstTalkId(TUNATUN);
	}

	public static void main(String[] args)
	{
		new BringUpWithLove(20, QN, "Bring Up With Love", "quests");
	}
}
