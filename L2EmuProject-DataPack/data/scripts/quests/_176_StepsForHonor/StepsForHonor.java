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
package quests._176_StepsForHonor;

import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWarManager;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 ** @author Gnacik
 **
 ** 2010-08-01 Based on official server Franz
 ** 
 ** Remade for L2EmuProject in Java: lord_rex
 */
public final class StepsForHonor extends QuestJython
{
	private static final String	QN		= "_176_StepsForHonor";

	// NPCs
	private static final int	RAPIDUS	= 36479;

	// ITEMs
	private static final int	CLOAK	= 14603;

	public StepsForHonor(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(RAPIDUS);
		addTalkId(RAPIDUS);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("36479-02.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, "1");
			st.sendPacket(SND_ACCEPT);
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

		if (npc.getNpcId() == RAPIDUS)
		{
			switch (st.getState())
			{
				case State.STARTED:
					if (TerritoryWarManager.getInstance().isTWInProgress())
						htmltext = "36479-tw.htm";
					else
					{
						switch (cond)
						{
							case 1:
								htmltext = "36479-03.htm";
								break;
							case 2:
								st.sendPacket(SND_MIDDLE);
								st.set(CONDITION, "3");
								htmltext = "36479-04.htm";
								break;
							case 3:
								htmltext = "36479-05.htm";
								break;
							case 4:
								st.sendPacket(SND_MIDDLE);
								st.set(CONDITION, "5");
								htmltext = "36479-06.htm";
								break;
							case 5:
								htmltext = "36479-07.htm";
								break;
							case 6:
								st.sendPacket(SND_MIDDLE);
								st.set(CONDITION, "7");
								htmltext = "36479-08.htm";
								break;
							case 7:
								htmltext = "36479-09.htm";
								break;
							case 8:
								st.giveItems(CLOAK, 1);
								st.exitQuest(false);
								st.sendPacket(SND_FINISH);
								htmltext = "36479-10.htm";
								break;
						}
					}
					break;
				case State.CREATED:
					if (player.getLevel() >= 80)
						htmltext = "36479-01.htm";
					else
						htmltext = "36479-00.htm";
					break;
				case State.COMPLETED:
					htmltext = "36479-11.htm";
					break;
			}
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new StepsForHonor(176, QN, "Steps For Honor");
	}
}
