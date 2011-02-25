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
package quests._10282_GuardianOfSafety;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class GuardianOfSafety extends QuestJython
{
	private static final String	QN		= "_10282_GuardianOfSafety";

	// NPCs
	private static final int	LEKON	= 32557;

	// MOBs
	private static final int[]	MOBS	=
										{ 22614, 22615, 25633, 25623 };

	// Quest Item        
	private static final int	FEATHER	= 13871;

	// Chance
	private static final int	CHANCE	= 90;

	public GuardianOfSafety(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(LEKON);

		addTalkId(LEKON);

		for (int i : MOBS)
			addKillId(i);

		questItemIds = new int[]
		{ FEATHER };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("30892-00a.htm"))
		{
			htmltext = "30892-00a.htm";
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30892-02.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, 1);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("Quest Finished"))
		{
			st.exitQuest(true);
			st.sendPacket(SND_FINISH);
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

		if (st.getState() == State.CREATED)
		{
			QuestState qs = player.getQuestState("10273_GoodDayToFly");
			if (qs != null && qs.getState() == State.COMPLETED && player.getLevel() >= 75)
				htmltext = "30892-01.htm";
			else
				htmltext = "32557-00.htm";
		}
		else if (st.getInt(CONDITION) == 1)
		{
			long count = st.getQuestItemsCount(FEATHER);
			if (count > 0)
			{
				htmltext = "30892-03.htm";
				st.takeItems(FEATHER, -1);
				st.giveAdena(count * 1500);
			}
			else
				htmltext = "30892-04.htm";
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return null;

		if (st.getState() == State.STARTED && ArrayUtils.contains(MOBS, npc.getNpcId()) && st.getRandom(100) < CHANCE)
		{
			st.giveItems(FEATHER, 1);
			st.sendPacket(SND_ITEM_GET);
		}

		return null;
	}

	public static void main(String[] args)
	{
		new GuardianOfSafety(10282, QN, "Guardian Of Safety");
	}
}
