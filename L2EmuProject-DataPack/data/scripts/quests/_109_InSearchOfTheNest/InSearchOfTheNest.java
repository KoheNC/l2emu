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
package quests._109_InSearchOfTheNest;

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * Rewritten and fixed by lewzer
 */
public final class InSearchOfTheNest extends QuestJython
{
	private static final String	QN						= "_109_InSearchOfTheNest";

	// NPC
	private static final int	PIERCE					= 31553;
	private static final int	CORPSE					= 32015;
	private static final int	KAHMAN					= 31554;

	// QUEST ITEMS
	private static final int	MEMO					= 8083;
	private static final int	GOLDEN_BADGE_RECRUIT	= 7246;
	private static final int	GOLDEN_BADGE_SOLDIER	= 7247;
	private static final int	ADENA					= 57;

	public InSearchOfTheNest(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(PIERCE);
		addTalkId(PIERCE);
		addTalkId(CORPSE);
		addTalkId(KAHMAN);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (event.equalsIgnoreCase("32015-02.htm"))
		{
			st.giveItems(MEMO, 1);
			st.set(CONDITION, 2);
			st.sendPacket(SND_MIDDLE);
			htmltext = "32015-02.htm";
		}
		else if (event.equalsIgnoreCase("31553-04.htm"))
		{
			st.takeItems(MEMO, -1);
			st.set(CONDITION, 3);
			st.sendPacket(SND_MIDDLE);
			htmltext = "31553-04.htm";
		}
		else if (event.equalsIgnoreCase("31554-02.htm"))
		{
			st.addExpAndSp(701500, 50000);
			st.giveItems(ADENA, 161500);
			st.exitQuest(false);
			st.setState(State.COMPLETED);
			htmltext = "31554-02.htm";
		}
		else if (event.equalsIgnoreCase("31553-02.htm"))
		{
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
			st.set(CONDITION, 1);
			htmltext = "31553-02.htm";
		}

		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QN);
		int cond = st.getInt(CONDITION);
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST;
		if (st.isCompleted())
		{
			return QUEST_DONE;
		}
		else
		{
			switch (npcId)
			{
				case PIERCE:
					switch (cond)
					{
						case 0:
							if (st.getPlayer().getLevel() >= 81
									&& (st.getQuestItemsCount(GOLDEN_BADGE_RECRUIT) > 0 || st.getQuestItemsCount(GOLDEN_BADGE_SOLDIER) > 0))
							{
								htmltext = "31553-01.htm";
								st.exitQuest(true);
							}
							else
							{
								htmltext = "31553-00.htm";
								st.exitQuest(true);
							}
							break;
						case 1:
							htmltext = "31553-02.htm";
							break;
						case 2:
							htmltext = "31553-03.htm";
							break;
						case 3:
							htmltext = "31553-04.htm";
							break;
					}
					break;
				case CORPSE:
					if (cond == 1)
						htmltext = "32015-01.htm";
					else if (cond == 2)
						htmltext = "32015-02.htm";
					break;
				case KAHMAN:
					if (cond == 3)
						htmltext = "31554-01.htm";
					break;
				default:
					htmltext = QUEST_DONE;
					break;
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new InSearchOfTheNest(109, QN, "In Search of the Nest");
	}
}
