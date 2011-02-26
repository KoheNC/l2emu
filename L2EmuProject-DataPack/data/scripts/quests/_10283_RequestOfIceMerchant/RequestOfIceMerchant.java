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
package quests._10283_RequestOfIceMerchant;

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
public final class RequestOfIceMerchant extends QuestJython
{
	private static final String	QN			= "_10283_RequestOfIceMerchant";

	// NPC's
	private static final int	RAFFORTY	= 32020;
	private static final int	KIER		= 32022;
	private static final int	JINIA		= 32760;

	public RequestOfIceMerchant(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY);
		addTalkId(KIER);
		addFirstTalkId(JINIA);
		addTalkId(JINIA);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == RAFFORTY)
		{
			if (event.equalsIgnoreCase("32020-03.htm"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
			else if (event.equalsIgnoreCase("32020-07.htm"))
			{
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		else if (npc.getNpcId() == KIER && event.equalsIgnoreCase("spawn"))
		{
			addSpawn(JINIA, 104322, -107669, -3680, 44954, false, 60000);
			return null;
		}
		else if (npc.getNpcId() == JINIA && event.equalsIgnoreCase("32760-04.html"))
		{
			st.giveItems(57, 190000);
			st.addExpAndSp(627000, 50300);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);
			npc.deleteMe();
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

		if (npc.getNpcId() == RAFFORTY)
		{
			switch (st.getState())
			{
				case State.CREATED:
					QuestState _prev = player.getQuestState("115_TheOtherSideOfTruth");
					if ((_prev != null) && (_prev.getState() == State.COMPLETED) && (player.getLevel() >= 82))
						htmltext = "32020-01.htm";
					else
						htmltext = "32020-00.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
						htmltext = "32020-04.htm";
					else if (st.getInt("cond") == 2)
						htmltext = "32020-08.htm";
					break;
				case State.COMPLETED:
					htmltext = "31350-08.htm";
					break;
			}
		}
		else if (npc.getNpcId() == KIER && st.getInt("cond") == 2)
		{
			htmltext = "32022-01.html";
		}
		else if (npc.getNpcId() == JINIA && st.getInt("cond") == 2)
		{
			htmltext = "32760-02.html";
		}
		return htmltext;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return null;
		if (npc.getNpcId() == JINIA && st.getInt("cond") == 2)
			return "32760-01.html";
		return null;
	}

	public static void main(String[] args)
	{
		new RequestOfIceMerchant(10283, QN, "Request of Ice Merchant");
	}
}
