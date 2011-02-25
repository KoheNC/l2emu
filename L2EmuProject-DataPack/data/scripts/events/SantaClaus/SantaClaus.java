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
package events.SantaClaus;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public class SantaClaus extends QuestJython
{
	private static final String	QN		= "SantaClaus";

	private static final int	NPC_ID1	= 31863;
	private static final int	NPC_ID2	= 31864;

	public SantaClaus(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC_ID1);
		addStartNpc(NPC_ID2);
		addTalkId(NPC_ID1);
		addTalkId(NPC_ID2);
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = "";
		htmltext = event;

		if (event.equalsIgnoreCase("1"))
		{
			if (st.getQuestItemsCount(5556) >= 1 && st.getQuestItemsCount(5557) >= 1 && st.getQuestItemsCount(5558) >= 1 && st.getQuestItemsCount(5559) >= 1)
			{
				st.takeItems(5556, 1);
				st.takeItems(5557, 1);
				st.takeItems(5558, 1);
				st.takeItems(5559, 1);
				st.giveItems(5283, 3);
				htmltext = "Merry Christmas!";
				st.exitQuest(true);
			}
			else
			{
				htmltext = "You do not have all four ornaments!";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("0"))
		{
			htmltext = "Trade has been canceled.";
			st.exitQuest(true);
		}

		if (htmltext != event)
			st.exitQuest(true);

		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		st.set(CONDITION, 0);
		st.setState(State.STARTED);

		return "1.htm";
	}

	public static void main(String[] args)
	{
		if (Config.ALLOW_SANTA_CLAUS_EVENT)
		{
			new SantaClaus(5007, QN, "events");
			_log.info("Events: Santa Claus Event is loaded.");
		}
		else
			_log.info("Events: Santa Claus Event is disabled.");
	}
}
