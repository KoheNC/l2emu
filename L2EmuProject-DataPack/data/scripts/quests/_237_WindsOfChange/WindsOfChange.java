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
package quests._237_WindsOfChange;

import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/*
 * @author  lewzer
 */

public final class WindsOfChange extends QuestJython
{
	private static final String	QN					= "_237_WindsOfChange";

	//NPC
	private static final int	FLAUEN				= 30899;
	private static final int	IASON				= 30969;
	private static final int	ROMAN				= 30897;
	private static final int	MORELYN				= 30925;
	private static final int	HELVETICA			= 32641;
	private static final int	ATHENIA				= 32643;

	//QUEST ITEMS
	private static final int	FLAUEN_LETTER		= 14862;
	private static final int	HELVETICA_LETTER	= 14863;
	private static final int	ATHENIA_LETTER		= 14864;
	private static final int	HELVETICA_CERT		= 14865;
	private static final int	ATHENIA_CERT		= 14866;

	public WindsOfChange(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(FLAUEN);
		addTalkId(FLAUEN);
		addTalkId(IASON);
		addTalkId(ROMAN);
		addTalkId(MORELYN);
		addTalkId(HELVETICA);
		addTalkId(ATHENIA);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (event.equalsIgnoreCase("30899-02.htm"))
		{
			htmltext = "30899-02.htm";
		}
		else if (event.equalsIgnoreCase("30899-03.htm"))
		{
			htmltext = "30899-03.htm";
		}
		else if (event.equalsIgnoreCase("30899-04.htm"))
		{
			htmltext = "30899-04.htm";
		}
		else if (event.equalsIgnoreCase("30899-05.htm"))
		{
			htmltext = "30899-05.htm";
		}
		else if (event.equalsIgnoreCase("30899-06.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, 1);
			st.giveItems(FLAUEN_LETTER, 1);
			st.sendPacket(SND_ACCEPT);
			htmltext = "30899-06.htm";
		}
		else if (event.equalsIgnoreCase("30969-02.htm"))
		{
			htmltext = "30969-02.htm";
		}
		else if (event.equalsIgnoreCase("30969-03.htm"))
		{
			htmltext = "30969-03.htm";
		}
		else if (event.equalsIgnoreCase("30969-04.htm"))
		{
			htmltext = "30969-04.htm";
		}
		else if (event.equalsIgnoreCase("30969-03a.htm"))
		{
			htmltext = "30969-03a.htm";
		}
		else if (event.equalsIgnoreCase("30969-03b.htm"))
		{
			htmltext = "30969-03b.htm";
		}
		else if (event.equalsIgnoreCase("30969-05.htm"))
		{
			st.takeItems(FLAUEN_LETTER, 1);
			st.set(CONDITION, 2);
			st.sendPacket(SND_MIDDLE);
			htmltext = "30969-05.htm";
		}
		else if (event.equalsIgnoreCase("30897-02.htm"))
		{
			htmltext = "30897-02.htm";
		}
		else if (event.equalsIgnoreCase("30897-03.htm"))
		{
			st.set(CONDITION, 3);
			st.sendPacket(SND_MIDDLE);
			htmltext = "30897-03.htm";
		}
		else if (event.equalsIgnoreCase("30925-02.htm"))
		{
			htmltext = "30925-02.htm";
		}
		else if (event.equalsIgnoreCase("30925-03.htm"))
		{
			st.set(CONDITION, 4);
			st.sendPacket(SND_MIDDLE);
			htmltext = "30925-03.htm";
		}
		else if (event.equalsIgnoreCase("30969-08.htm"))
		{
			htmltext = "30969-08.htm";
		}
		else if (event.equalsIgnoreCase("30969-08a.htm"))
		{
			htmltext = "30969-08a.htm";
		}
		else if (event.equalsIgnoreCase("30969-08b.htm"))
		{
			htmltext = "30969-08b.htm";
		}
		else if (event.equalsIgnoreCase("30969-09.htm"))
		{
			st.set(CONDITION, 5);
			st.giveItems(HELVETICA_LETTER, 1);
			st.sendPacket(SND_MIDDLE);
			htmltext = "30969-09.htm";
		}
		else if (event.equalsIgnoreCase("30969-08c.htm"))
		{
			htmltext = "30969-08c.htm";
		}
		else if (event.equalsIgnoreCase("30969-10.htm"))
		{
			st.set(CONDITION, 6);
			st.giveItems(ATHENIA_LETTER, 1);
			st.sendPacket(SND_MIDDLE);
			htmltext = "30969-10.htm";
		}
		else if (event.equalsIgnoreCase("32643-02.htm"))
		{
			st.addExpAndSp(892773, 60012);
			st.giveAdena(213876);
			st.takeItems(ATHENIA_LETTER, 1);
			st.giveItems(ATHENIA_CERT, 1);
			st.exitQuest(false);
			st.setState(State.COMPLETED);
			htmltext = "32643-02.htm";
		}
		else if (event.equalsIgnoreCase("32641-02.htm"))
		{
			st.addExpAndSp(892773, 60012);
			st.giveAdena(213876);
			st.takeItems(HELVETICA_LETTER, 1);
			st.giveItems(HELVETICA_CERT, 1);
			st.exitQuest(false);
			st.setState(State.COMPLETED);
			htmltext = "32641-02.htm";
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		int cond = st.getInt(CONDITION);

		if (st.isCompleted())
		{
			return QUEST_DONE;
		}
		else if (npc.getNpcId() == FLAUEN)
		{
			switch (cond)
			{
				case 0:
					if (st.getPlayer().getLevel() < 82)
					{
						st.exitQuest(true);
						htmltext = "30899-00.htm";
					}
					else
					{
						htmltext = "30899-01.htm";
					}
					break;
				case 1:
					htmltext = "30899-06.htm";
					break;
			}
		}
		else if (npc.getNpcId() == IASON)
		{
			switch (cond)
			{
				case 1:
					htmltext = "30969-01.htm";
					break;
				case 2:
					htmltext = "30969-06.htm";
					break;
				case 3:
					htmltext = "30969-06.htm";
					break;
				case 4:
					htmltext = "30969-07.htm";
					break;
				case 5:
					htmltext = "30969-11.htm";
					break;
				case 6:
					htmltext = "30969-11.htm";
					break;

			}
		}
		else if (npc.getNpcId() == ROMAN)
		{
			switch (cond)
			{
				case 2:
					htmltext = "30897-01.htm";
					break;
				case 3:
					htmltext = "30897-04.htm";
					break;
			}
		}
		else if (npc.getNpcId() == MORELYN)
		{
			switch (cond)
			{
				case 3:
					htmltext = "30925-01.htm";
					break;
				case 4:
					htmltext = "30925-04.htm";
					break;
			}
		}
		else if (npc.getNpcId() == ATHENIA)
		{
			switch (cond)
			{
				case 6:
					if (st.getQuestItemsCount(ATHENIA_LETTER) == 1)
					{
						htmltext = "32643-01.htm";
					}
					break;

			}
		}
		else if (npc.getNpcId() == HELVETICA)
		{
			switch (cond)
			{
				case 5:
					if (st.getQuestItemsCount(HELVETICA_LETTER) == 1)
					{
						htmltext = "32641-01.htm";
					}
					break;
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new WindsOfChange(237, QN, "Winds of Change");
	}
}