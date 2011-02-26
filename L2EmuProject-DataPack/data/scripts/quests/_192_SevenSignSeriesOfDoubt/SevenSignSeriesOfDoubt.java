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
package quests._192_SevenSignSeriesOfDoubt;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Bloodshed
 *	Remade for L2EmuProject by lord_rex
 */
public final class SevenSignSeriesOfDoubt extends QuestJython
{
	public static final String	QN				= "_192_SevenSignSeriesOfDoubt";

	// NPCs
	private static final int	CROOP			= 30676;
	private static final int	HECTOR			= 30197;
	private static final int	STAN			= 30200;
	private static final int	CORPSE			= 32568;
	private static final int	HOLLINT			= 30191;

	// ITEMS
	private static final int	CROOP_INTRO		= 13813;
	private static final int	JACOB_NECK		= 13814;
	private static final int	CROOP_LETTER	= 13815;

	public SevenSignSeriesOfDoubt(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(CROOP);

		addTalkId(CROOP);
		addTalkId(HECTOR);
		addTalkId(STAN);
		addTalkId(CORPSE);
		addTalkId(HOLLINT);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("30676-03.htm"))
		{
			st.set(CONDITION, "1");
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		else if (Util.isDigit(event))
		{
			if (Integer.valueOf(event) == 8)
			{
				st.set(CONDITION, "2");
				st.sendPacket(SND_MIDDLE);
				player.showQuestMovie(Integer.valueOf(event));
				return "";
			}
		}
		else if (event.equalsIgnoreCase("30197-03.htm"))
		{
			st.set(CONDITION, "4");
			st.takeItems(CROOP_INTRO, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30200-04.htm"))
		{
			st.set(CONDITION, "5");
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32568-02.htm"))
		{
			st.set(CONDITION, "6");
			st.giveItems(JACOB_NECK, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30676-12.htm"))
		{
			st.set(CONDITION, "7");
			st.takeItems(JACOB_NECK, 1);
			st.giveItems(CROOP_LETTER, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30191-03.htm"))
		{
			if (player.getLevel() < 79)
				htmltext = "<html><body>Only characters who are <font color=\"LEVEL\">level 79</font> or higher may complete this quest.</body></html>";
			else
			{
				st.takeItems(CROOP_LETTER, 1);
				st.addExpAndSp(52518015, 5817677);
				st.unset(CONDITION);
				st.setState(State.COMPLETED);
				st.exitQuest(false);
				st.sendPacket(SND_FINISH);
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

		int cond = st.getInt(CONDITION);

		switch (npc.getNpcId())
		{
			case CROOP:
				if (st.getState() == State.CREATED && player.getLevel() >= 79)
					htmltext = "30676-01.htm";
				else if (st.getState() == State.COMPLETED)
					htmltext = "30676-13.htm";
				else if (player.getLevel() < 79)
				{
					htmltext = "30676-00.htm";
					st.exitQuest(true);
				}
				else
				{
					switch (cond)
					{
						case 1:
							htmltext = "30676-04.htm";
							break;
						case 2:
							htmltext = "30676-05.htm";
							st.set(CONDITION, "3");
							st.sendPacket(SND_MIDDLE);
							st.giveItems(CROOP_INTRO, 1);
							break;
						case 3:
						case 4:
						case 5:
							htmltext = "30676-06.htm";
							break;
						case 6:
							htmltext = "30676-07.htm";
							break;
					}
				}
				break;
			case HECTOR:
				switch (cond)
				{
					case 3:
						htmltext = "30197-01.htm";
						break;
					case 4:
					case 5:
					case 6:
					case 7:
						htmltext = "30197-04.htm";
						break;
				}
				break;
			case STAN:
				switch (cond)
				{
					case 4:
						htmltext = "30200-01.htm";
						break;
					case 5:
					case 6:
					case 7:
						htmltext = "30200-05.htm";
						break;
				}
				break;
			case CORPSE:
				if (cond == 5)
					htmltext = "32568-01.htm";
				break;
			case HOLLINT:
				if (cond == 7)
					htmltext = "30191-01.htm";
				break;
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new SevenSignSeriesOfDoubt(192, QN, "Seven Sign Series of Doubt");
	}
}
