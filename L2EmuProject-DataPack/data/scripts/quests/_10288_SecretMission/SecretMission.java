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
package quests._10288_SecretMission;

import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestService;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 ** @author Gnacik
 **
 ** 2010-08-07 Based on Freya PTS
 */
public final class SecretMission extends QuestJython
{
	private static final String	QN			= "_10288_SecretMission";

	// NPC's
	private static final int	DOMINIC		= 31350;
	private static final int	AQUILANI	= 32780;
	private static final int	GREYMORE	= 32757;

	// Items
	private static final int	LETTER		= 15529;

	public SecretMission(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(DOMINIC);
		addStartNpc(AQUILANI);
		addTalkId(DOMINIC);
		addTalkId(GREYMORE);
		addTalkId(AQUILANI);
		addFirstTalkId(AQUILANI);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == DOMINIC)
		{
			if (event.equalsIgnoreCase("31350-05.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.giveItems(LETTER, 1);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npc.getNpcId() == GREYMORE && event.equalsIgnoreCase("32757-03.htm"))
		{
			st.unset("cond");
			st.takeItems(LETTER, -1);
			st.giveItems(57, 106583);
			st.addExpAndSp(417788, 46320);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
		}
		else if (npc.getNpcId() == AQUILANI)
		{
			if (st.getState() == State.STARTED)
			{
				if (event.equalsIgnoreCase("32780-05.html"))
				{
					st.set("cond", "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if (st.getState() == State.COMPLETED && event.equalsIgnoreCase("teleport"))
			{
				player.teleToLocation(118833, -80589, -2688);
				return null;
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

		if (npc.getNpcId() == DOMINIC)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 82)
						htmltext = "31350-01.htm";
					else
						htmltext = "31350-00.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
						htmltext = "31350-06.htm";
					else if (st.getInt("cond") == 2)
						htmltext = "31350-07.htm";
					break;
				case State.COMPLETED:
					htmltext = "31350-08.htm";
					break;
			}
		}
		else if (npc.getNpcId() == AQUILANI)
		{
			if (st.getInt("cond") == 1)
			{
				htmltext = "32780-03.html";
			}
			else if (st.getInt("cond") == 2)
			{
				htmltext = "32780-06.html";
			}
		}
		else if (npc.getNpcId() == GREYMORE && st.getInt("cond") == 2)
		{
			return "32757-01.htm";
		}
		return htmltext;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
		{
			Quest q = QuestService.getInstance().getQuest(QN);
			st = q.newQuestState(player);
		}
		if (npc.getNpcId() == AQUILANI)
		{
			if (st.getState() == State.COMPLETED)
				return "32780-01.html";
			else
				return "32780-00.html";
		}
		return null;
	}

	public static void main(String[] args)
	{
		new SecretMission(10288, QN, "Secret Mission");
	}
}
