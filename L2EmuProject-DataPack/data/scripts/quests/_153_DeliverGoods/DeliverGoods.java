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
package quests._153_DeliverGoods;

import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Based on Naia (EURO)
 * @author Zoey76
 * @version 1.0 Freya (11/16/2010)
 */
public final class DeliverGoods extends Quest
{
	private static final String	QN					= "_153_DeliverGoods";

	// NPCs
	private static final int	JACKSON				= 30002;
	private static final int	SILVIA				= 30003;
	private static final int	ARNOLD				= 30041;
	private static final int	RANT				= 30054;

	// ITEMs
	private static final int	DELIVERY_LIST		= 1012;
	private static final int	HEAVY_WOOD_BOX		= 1013;
	private static final int	CLOTH_BUNDLE		= 1014;
	private static final int	CLAY_POT			= 1015;
	private static final int	JACKSON_RECEIPT		= 1016;
	private static final int	SILVIAS_RECEIPT		= 1017;
	private static final int	RANTS_RECEIPT		= 1018;

	// REWARDs
	private static final int	SOULSHOT_NO_GRADE	= 1835;				// You get 3 Soulshots no grade.
	private static final int	RING_OF_KNOWLEDGE	= 875;

	public DeliverGoods(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(ARNOLD);
		addTalkId(JACKSON);
		addTalkId(SILVIA);
		addTalkId(ARNOLD);
		addTalkId(RANT);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2Player player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(QN);

		if ((st != null) && (npc.getNpcId() == ARNOLD))
		{
			if (event.equalsIgnoreCase("30041-02.html"))
			{
				st.setState(State.STARTED);
				st.set(CONDITION, "1");
				st.playSound("ItemSound.quest_accept");
				st.giveItems(DELIVERY_LIST, 1);
				st.giveItems(HEAVY_WOOD_BOX, 1);
				st.giveItems(CLOTH_BUNDLE, 1);
				st.giveItems(CLAY_POT, 1);
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2Player player)
	{
		String htmltext = NO_QUEST;
		final QuestState st = player.getQuestState(QN);
		final int npcId = npc.getNpcId();
		if (st != null)
		{
			if (npcId == ARNOLD)
			{
				switch (st.getState())
				{
					case State.CREATED:
						if (player.getLevel() >= 2)
						{
							htmltext = "30041-01.htm";
						}
						else
						{
							htmltext = "30041-00.htm";
						}
						break;
					case State.STARTED:
						if (st.getInt(CONDITION) == 1)
						{
							htmltext = "30041-03.html";
						}
						else if (st.getInt(CONDITION) == 2)
						{
							htmltext = "30041-04.html";
							st.takeItems(DELIVERY_LIST, 1);
							st.takeItems(JACKSON_RECEIPT, 1);
							st.takeItems(SILVIAS_RECEIPT, 1);
							st.takeItems(RANTS_RECEIPT, 1);
							// On retail it gives 2 rings but one at the time.
							st.giveItems(RING_OF_KNOWLEDGE, 1);
							st.giveItems(RING_OF_KNOWLEDGE, 1);
							st.addExpAndSp(600, 0);
							st.exitQuest(false);
						}
						break;
					case State.COMPLETED:
						htmltext = QUEST_DONE;
						break;
				}
			}
			else
			{
				switch (npcId)
				{
					case JACKSON:
						if (st.getQuestItemsCount(HEAVY_WOOD_BOX) == 1)
						{
							htmltext = "30002-01.html";
							st.takeItems(HEAVY_WOOD_BOX, 1);
							st.giveItems(JACKSON_RECEIPT, 1);
						}
						else
						{
							htmltext = "30002-02.html";
						}
						break;
					case SILVIA:
						if (st.getQuestItemsCount(CLOTH_BUNDLE) == 1)
						{
							htmltext = "30003-01.html";
							st.takeItems(CLOTH_BUNDLE, 1);
							st.giveItems(SILVIAS_RECEIPT, 1);
							st.giveItems(SOULSHOT_NO_GRADE, 3);
						}
						else
						{
							htmltext = "30003-02.html";
						}
						break;
					case RANT:
						if (st.getQuestItemsCount(CLAY_POT) == 1)
						{
							htmltext = "30054-01.html";
							st.takeItems(CLAY_POT, 1);
							st.giveItems(RANTS_RECEIPT, 1);
						}
						else
						{
							htmltext = "30054-02.html";
						}
						break;
				}

				if ((st.getInt(CONDITION) == 1) && (st.getQuestItemsCount(JACKSON_RECEIPT) == 1) && (st.getQuestItemsCount(SILVIAS_RECEIPT) == 1)
						&& (st.getQuestItemsCount(RANTS_RECEIPT) == 1))
				{
					st.set(CONDITION, "2");
					st.sendPacket(SND_MIDDLE);
				}
			}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new DeliverGoods(153, QN, "Deliver Goods");
	}
}
