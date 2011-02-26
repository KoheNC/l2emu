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
package quests._131_BirdInACage;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/**
 * Rewritten by lewzer
 */
public final class BirdInACage extends QuestJython
{
	private static final String	QN				= "_131_BirdInACage";
	// NPC's
	private static final int	KANIS			= 32264;
	private static final int	PARME			= 32271;
	// ITEMS
	private static final int	KANIS_ECHO_CRY	= 9783;
	private static final int	PARMES_LETTER	= 9784;

	public BirdInACage(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(KANIS);
		addTalkId(KANIS);
		addTalkId(PARME);
		questItemIds = new int[]
		{ KANIS_ECHO_CRY, PARMES_LETTER };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);
		if (event.equals("32264-02.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
		}
		else if (event.equals("32264-08.htm"))
		{
			st.set(CONDITION, 2);
			st.giveItems(KANIS_ECHO_CRY, 1);
		}
		else if (event.equals("32271-03.htm"))
		{
			st.set(CONDITION, 3);
			st.giveItems(PARMES_LETTER, 1);
			st.getPlayer().teleToLocation(143472 + Rnd.get(100), 191040 + Rnd.get(100), -3696);
		}
		else if (event.equals("32264-12.htm"))
		{
			st.takeItems(PARMES_LETTER, 1);
		}
		else if (event.equals("32264-13.htm"))
		{
			st.takeItems(KANIS_ECHO_CRY, 1);
			st.addExpAndSp(250677, 25019);
			st.exitQuest(false);
			st.setState(State.COMPLETED);
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
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
			case KANIS:
				switch (cond)
				{
					case 0:
						if (st.getPlayer().getLevel() < 78)
							htmltext = "32264-00.htm";
						else
							htmltext = "32264-01.htm";
						break;
					case 1:
						htmltext = "32264-02.htm";
						break;
					case 2:
						htmltext = "32264-09.htm";
						break;
					case 3:
						htmltext = "32264-11.htm";
						break;
				}
				break;
			case PARME:
				switch (cond)
				{
					case 2:
						htmltext = "32271-01.htm";
						break;
					case 3:
						htmltext = "32271-03.htm";
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new BirdInACage(131, QN, "Bird In A Cage");
	}
}