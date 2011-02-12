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
package events.Christmas;

import java.text.SimpleDateFormat;
import java.util.Locale;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.EventDroplist;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.script.DateRange;

public class Christmas extends QuestJython
{
	private static final String		QN					= "Christmas";
	private static final int		NPC_ID				= 31863;

	private static final int[]		GLOBAL_DROP			=
														{ 5556, 5557, 5558, 5559 };
	// Minimum and maximum count of dropped items
	private static final int[]		GLOBAL_DROP_COUNT	=
														{ 1, 1 };
	private static final DateRange	EVENT_DATES			= DateRange.parse(Config.CHRISTMAS_EVENT_DATE, new SimpleDateFormat("dd MM yyyy", Locale.US));

	public Christmas(int questId, String name, String descr)
	{
		super(questId, name, descr);

		EventDroplist.getInstance().addGlobalDrop(GLOBAL_DROP, GLOBAL_DROP_COUNT, (Config.CHRISTMAS_EVENT_DROP_CHANCE * 10000), EVENT_DATES);

		addStartNpc(NPC_ID);
		addTalkId(NPC_ID);
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = "";
		htmltext = event;

		if (event.equalsIgnoreCase("1"))
		{
			if (st.getQuestItemsCount(5556) >= 4 && st.getQuestItemsCount(5557) >= 4 && st.getQuestItemsCount(5558) >= 10 && st.getQuestItemsCount(5559) >= 1)
			{
				st.takeItems(5556, 4);
				st.takeItems(5557, 4);
				st.takeItems(5558, 10);
				st.takeItems(5559, 1);
				st.giveItems(5560, 1);
				htmltext = "Merry Christmas!";
				st.exitQuest(true);
			}
			else
			{
				htmltext = "You do not have all the ornaments!";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("2"))
		{
			if (st.getQuestItemsCount(5560) >= 10)
			{
				st.takeItems(5560, 10);
				st.giveItems(5561, 1);
				st.giveItems(9138, 1);
				htmltext = "Merry Christmas!";
				st.exitQuest(true);
			}
			else
			{
				htmltext = "You do not have all items!";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("0"))
		{
			htmltext = "Trade has been canceled.";

			if (!(htmltext == event))
				st.exitQuest(true);
		}

		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QN);
		st.setState(State.STARTED);

		return "1.htm";
	}

	@Override
	public String onEnterWorld(L2PcInstance player)
	{
		player.sendMessage("Christmas Event: Collect 4 Star Ornaments, 4 Bead Ornaments, 10 Fir Tree Branches, 1 Flower Pot and recieve a gift!");
		return "";
	}

	public static void main(String[] args)
	{
		if (Config.ALLOW_CHRISTMAS_EVENT)
		{
			new Christmas(-1, QN, "events");
			_log.info("Events: Christmas Event is loaded.");
		}
		else
			_log.info("Events: Christmas Event is disabled.");
	}
}
