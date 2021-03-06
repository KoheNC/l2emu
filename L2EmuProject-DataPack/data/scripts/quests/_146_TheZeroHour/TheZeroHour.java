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
package quests._146_TheZeroHour;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 ** @author Gnacik
 ** Remade for L2EmuProject in Java: lord_rex
 ** fixed by lewzer
 */
public final class TheZeroHour extends QuestJython
{
	private static final String	QN			= "_146_TheZeroHour";

	// NPCs
	private static final int	KAHMAN		= 31554;
	// Items
	private static final int	FANG		= 14859;
	private static final int	BOX			= 14849;

	// Mobs
	private static final int	SHYEED[]	=
											{ 25514, 25671 };

	public TheZeroHour(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		addStartNpc(KAHMAN);
		addTalkId(KAHMAN);

		for (int i : SHYEED)
			addKillId(i);

		questItemIds = new int[]
		{ FANG };
	}

	@Override
	public final String onAdvEvent(final String event, final L2Npc npc, final L2Player player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31554-02.htm"))
		{
			st.set(CONDITION, "1");
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}

		return htmltext;
	}

	@Override
	public final String onTalk(final L2Npc npc, final L2Player player)
	{
		String htmltext = NO_QUEST;
		final QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.STARTED:
				if (st.getQuestItemsCount(FANG) == 0)
				{
					htmltext = "31554-05.htm";
				}
				else if (st.getQuestItemsCount(FANG) > 0)
				{
					st.takeItems(FANG, 1);
					st.giveItems(BOX, 1);
					st.unset(CONDITION);
					st.setState(State.COMPLETED);
					htmltext = "31554-04.htm";
				}
				break;
			case State.COMPLETED:
				htmltext = QUEST_DONE;
				break;
			default:
				if (player.getLevel() >= 61)
					htmltext = "31554-01.htm";
				else
					htmltext = "31554-00.htm";
				break;
		}

		return htmltext;
	}

	@Override
	public final String onKill(final L2Npc npc, final L2Player player, final boolean isPet)
	{
		String htmltext = "";
		final QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (st.getInt(CONDITION) == 1 && ArrayUtils.contains(SHYEED, npc.getNpcId()) && st.getQuestItemsCount(FANG) < 1)
		{
			st.giveItems(FANG, 1);
			st.sendPacket(SND_ITEM_GET);
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new TheZeroHour(146, QN, "The Zero Hour");
	}
}
