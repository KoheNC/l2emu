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
package quests._133_ThatsBloodyHot;

import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import teleports.HellboundWarpGate.HellboundWarpGate;

/**
 * Rewritten by lewzer
 */
public final class ThatsBloodyHot extends QuestJython
{
	private static final String	QN				= "_133_ThatsBloodyHot";
	// NPC's
	private static final int	KANIS			= 32264;
	private static final int	GALATE			= 32292;
	// ITEMS
	private static final int	CRYSTAL_SAMPLE	= 9785;

	public ThatsBloodyHot(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(KANIS);
		addTalkId(KANIS);
		addTalkId(GALATE);
		questItemIds = new int[]
		{ CRYSTAL_SAMPLE };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		String htmltext = event;

		if (event.equalsIgnoreCase("32264-02.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
		}

		else if (event.equalsIgnoreCase("32264-07.htm"))
		{
			st.set(CONDITION, 2);
			st.giveItems(CRYSTAL_SAMPLE, 1);
		}

		else if (event.equals("32292-04.htm"))
		{
			st.takeItems(CRYSTAL_SAMPLE, 1);
			st.giveAdena(254247);
			st.addExpAndSp(331457, 32524);
			st.setState(State.COMPLETED);
			HellboundWarpGate.getInstance().openHbGates();
			st.exitQuest(false);
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
		final int npcId = npc.getNpcId();
		final int cond = st.getInt(CONDITION);
		QuestState qs131 = st.getPlayer().getQuestState("_131_BirdInACage");
		if (st.isCompleted())
		{
			return QUEST_DONE;
		}
		switch (npcId)
		{
			case KANIS:
				switch (cond)
				{
					case 0:
						if (qs131 != null && qs131.isCompleted() && st.getPlayer().getLevel() >= 78)
							htmltext = "32264-01.htm";
						else
							htmltext = "32264-00.htm";
						break;
					case 1:
						htmltext = "32264-02.htm";
						break;
					case 2:
						htmltext = "32264-07.htm";
						break;
				}
				break;
			case GALATE:
				switch (cond)
				{
					case 2:
						htmltext = "32292-01.htm";
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new ThatsBloodyHot(133, QN, "Thats Bloody Hot");
	}
}