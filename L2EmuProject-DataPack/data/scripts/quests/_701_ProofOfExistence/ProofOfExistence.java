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
package quests._701_ProofOfExistence;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * @author L0ngh0rn
 * @since 2010-06-26 by Gnacik (Based on official server Franz)
 */
public final class ProofOfExistence extends QuestJython
{
	private static final String	QN					= "_701_ProofOfExistence";

	// NPCs
	private static final int	ARTIUS				= 32559;

	// Quest Item
	private static final int	DEADMANS_REMAINS	= 13875;

	// MOBs
	private static final int[]	MOBS				=
													{ 22606, 22607, 22608, 22609 };

	// Chance
	private static final int	DROP_CHANCE			= 80;

	public ProofOfExistence(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(ARTIUS);
		addTalkId(ARTIUS);

		for (int i : MOBS)
			addKillId(i);

		questItemIds = new int[]
		{ DEADMANS_REMAINS };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32559-03.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, 1);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32559-quit.htm"))
		{
			st.unset(CONDITION);
			st.exitQuest(true);
			st.sendPacket(SND_FINISH);
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

		if (npc.getNpcId() == ARTIUS)
		{
			QuestState first = player.getQuestState("10273_GoodDayToFly");
			if (first != null && first.getState() == State.COMPLETED && st.getState() == State.CREATED && player.getLevel() >= 78)
				htmltext = "32559-01.htm";
			else
			{
				switch (st.getInt(CONDITION))
				{
					case 0:
						htmltext = "32559-00.htm";
						break;
					case 1:
						long count = st.getQuestItemsCount(DEADMANS_REMAINS);
						if (count > 0)
						{
							st.takeItems(DEADMANS_REMAINS, -1);
							st.giveAdena(count * 2500);
							st.sendPacket(SND_ITEM_GET);
							htmltext = "32559-06.htm";
						}
						else
							htmltext = "32559-04.htm";
						break;
				}
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

		if (st.getInt(CONDITION) == 1 && ArrayUtils.contains(MOBS, npc.getNpcId()))
		{
			int chance = (int) (DROP_CHANCE * Config.RATE_DROP_QUEST);
			int numItems = (int) (chance / 1000);
			chance = chance % 1000;
			if (st.getRandom(1000) < chance)
				numItems++;
			if (numItems > 0)
			{
				st.giveItems(DEADMANS_REMAINS, 1);
				st.sendPacket(SND_ITEM_GET);
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new ProofOfExistence(701, QN, "Proof of Existence");
	}
}
