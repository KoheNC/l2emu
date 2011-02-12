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
package quests._132_MatrasCuriosity;

import net.l2emuproject.gameserver.model.L2Party;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * @author L0ngh0rn
 */
public final class MatrasCuriosity extends QuestJython
{
	private static final String	QN					= "_132_MatrasCuriosity";

	// NPC
	private static final int	MATRAS				= 32245;

	// MOBs
	private static final int	DEMONPRINCE			= 25540;
	private static final int	RANKU				= 25542;

	// Items
	private static final int	RANKUSBLUEPRINT		= 9800;
	private static final int	PRINCESBLUEPRINT	= 9801;

	private static final int	ROUGHOREOFFIRE		= 10521;
	private static final int	ROUGHOREOFWATER		= 10522;
	private static final int	ROUGHOREOFTHEEARTH	= 10523;
	private static final int	ROUGHOREOFWIND		= 10524;
	private static final int	ROUGHOREOFDARKNESS	= 10525;
	private static final int	ROUGHOREOFDIVINITY	= 10526;

	public MatrasCuriosity(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(MATRAS);

		addTalkId(MATRAS);

		addKillId(DEMONPRINCE);
		addKillId(RANKU);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32245-02.htm"))
			if (st.getPlayer().getLevel() >= 76)
			{
				st.setState(State.STARTED);
				st.set(CONDITION, 1);
				st.sendPacket(SND_ACCEPT);
			}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		int cond = st.getInt(CONDITION);
		int npcId = npc.getNpcId();
		byte id = st.getState();

		if (id == State.COMPLETED && npcId == MATRAS)
			htmltext = QUEST_DONE;
		else if (id == State.CREATED && npcId == MATRAS)
		{
			if (player.getLevel() >= 76)
				htmltext = "32245-01.htm";
			else
			{
				htmltext = "32245-00.htm";
				st.exitQuest(true);
			}
		}
		else
		{
			switch (cond)
			{
				case 1:
					if (st.getQuestItemsCount(PRINCESBLUEPRINT) == 1 && st.getQuestItemsCount(RANKUSBLUEPRINT) == 1)
					{
						st.set(CONDITION, 2);
						htmltext = null;
						st.sendPacket(SND_MIDDLE);
					}
					else
						htmltext = "32245-03.htm";
					break;
				case 2:
					htmltext = "32245-04.htm";
					st.takeItems(RANKUSBLUEPRINT, -1);
					st.takeItems(PRINCESBLUEPRINT, -1);
					st.set(CONDITION, 3);
					st.sendPacket(SND_MIDDLE);
					break;
				case 3:
					htmltext = "32245-05.htm";
					st.giveAdena(65884);
					st.giveItems(ROUGHOREOFFIRE, 1);
					st.giveItems(ROUGHOREOFWATER, 1);
					st.giveItems(ROUGHOREOFTHEEARTH, 1);
					st.giveItems(ROUGHOREOFWIND, 1);
					st.giveItems(ROUGHOREOFDARKNESS, 1);
					st.giveItems(ROUGHOREOFDIVINITY, 1);
					st.addExpAndSp(50541, 5094);
					st.exitQuest(false);
					st.sendPacket(SND_FINISH);
					break;
			}
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return null;
		L2Party party = player.getParty();
		switch (npc.getNpcId())
		{
			case DEMONPRINCE:
				if (party != null)
				{
					for (L2PcInstance partyMember : party.getPartyMembers())
					{
						QuestState stp = partyMember.getQuestState(QN);
						if (stp == null)
							stp = newQuestState(partyMember);
						stp.giveItems(PRINCESBLUEPRINT, 1);
						stp.sendPacket(SND_ITEM_GET);
					}
				}
				break;
			case RANKU:
				if (party != null)
				{
					for (L2PcInstance partyMember : party.getPartyMembers())
					{
						QuestState stp = partyMember.getQuestState(QN);
						if (stp == null)
							stp = newQuestState(partyMember);
						stp.giveItems(RANKUSBLUEPRINT, 1);
						stp.sendPacket(SND_ITEM_GET);
					}
				}
				break;
		}
		if (st.getQuestItemsCount(PRINCESBLUEPRINT) >= 1 && st.getQuestItemsCount(RANKUSBLUEPRINT) >= 1)
		{
			st.set(CONDITION, 2);
			st.sendPacket(SND_MIDDLE);
		}
		return null;
	}

	public static void main(String[] args)
	{
		new MatrasCuriosity(132, QN, "Ma curiosity Truss");
	}
}