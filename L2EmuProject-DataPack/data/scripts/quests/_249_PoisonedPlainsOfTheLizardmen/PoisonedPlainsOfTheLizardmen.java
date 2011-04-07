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
package quests._249_PoisonedPlainsOfTheLizardmen;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 ** @author Gnacik
 **
 ** 2010-08-04 Based on Freya PTS
 */
public final class PoisonedPlainsOfTheLizardmen extends QuestJython
{
	private static final String	QN		= "_249_PoisonedPlainsOfTheLizardmen";

	private static final int	MOUEN	= 30196;
	private static final int	JOHNNY	= 32744;

	public PoisonedPlainsOfTheLizardmen(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(MOUEN);
		addTalkId(MOUEN);
		addTalkId(JOHNNY);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == MOUEN)
		{
			if (event.equalsIgnoreCase("30196-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npc.getNpcId() == JOHNNY && event.equalsIgnoreCase("32744-03.htm"))
		{
			st.unset("cond");
			st.giveItems(57, 83056);
			st.addExpAndSp(477496, 58743);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
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

		if (npc.getNpcId() == MOUEN)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 82)
						htmltext = "30196-01.htm";
					else
						htmltext = "30196-00.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
						htmltext = "30196-04.htm";
					break;
				case State.COMPLETED:
					htmltext = "30196-05.htm";
					break;
			}
		}
		else if (npc.getNpcId() == JOHNNY)
		{
			if (st.getInt("cond") == 1)
				htmltext = "32744-01.htm";
			else if (st.getState() == State.COMPLETED)
				htmltext = "32744-04.htm";
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new PoisonedPlainsOfTheLizardmen(249, QN, "Poisoned Plains of the Lizardmen");
	}
}
