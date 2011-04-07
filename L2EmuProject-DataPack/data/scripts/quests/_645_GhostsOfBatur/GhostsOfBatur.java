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
package quests._645_GhostsOfBatur;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/* 
 * @author lewzer
 */
public final class GhostsOfBatur extends QuestJython
{
	private static final String	QN			= "_645_GhostsOfBatur";

	// NPCs
	private static final int	KARUDA		= 32017;

	// MOBs
	private static final int[]	MOBS		=
											{ 22703, 22704, 22705 };

	// Quest item
	private static final int	BURIAL		= 14861;

	// REWARDS
	private static final int	LEONARD		= 9628;
	private static final int	ORICHALCUM	= 9630;
	private static final int	ADAMANTINE	= 9629;
	private static final int[]	S80_WEAPS	=
											{ 9967, 9968, 9969, 9970, 9971, 9972, 9973, 9974, 9975 };

	// Chance
	private static final int	CHANCE		= 50;

	public GhostsOfBatur(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(KARUDA);
		addTalkId(KARUDA);
		for (int i : MOBS)
			addKillId(i);

		questItemIds = new int[]
		{ BURIAL };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		if (event.equalsIgnoreCase("32017-02.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, 1);
			st.sendPacket(SND_ACCEPT);
			htmltext = "32017-02.htm";
		}
		else if (event.equalsIgnoreCase("32017-05.htm"))
		{
			htmltext = "32017-05.htm";
		}
		else if (event.equalsIgnoreCase("leonard"))
		{
			if (st.getQuestItemsCount(BURIAL) >= 8)
			{
				st.giveItems(LEONARD, 1);
				st.takeItems(BURIAL, 8);
				htmltext = "32017-07.htm";
			}
			else
			{
				htmltext = "32017-00a.htm";
			}
		}
		else if (event.equalsIgnoreCase("orichalcum"))
		{
			if (st.getQuestItemsCount(BURIAL) >= 12)
			{
				st.giveItems(ORICHALCUM, 1);
				st.takeItems(BURIAL, 12);
				htmltext = "32017-07.htm";
			}
			else
			{
				htmltext = "32017-00a.htm";
			}
		}
		else if (event.equalsIgnoreCase("adamantine"))
		{
			if (st.getQuestItemsCount(BURIAL) >= 15)
			{
				st.giveItems(ADAMANTINE, 1);
				st.takeItems(BURIAL, 15);
				htmltext = "32017-07.htm";
			}
			else
			{
				htmltext = "32017-00a.htm";
			}
		}
		else if (event.equalsIgnoreCase("32017-06.htm"))
		{
			htmltext = "32017-06.htm";
			st.set("rcpTRADE", "1");
		}
		else if (st.getInt("rcpTRADE") == 1)
		{
			int s80_weapon = Integer.valueOf(event);
			if (ArrayUtils.contains(S80_WEAPS, s80_weapon) && st.getQuestItemsCount(BURIAL) >= 500)
			{
				st.giveItems(s80_weapon, 1);
				st.takeItems(BURIAL, 500);
				htmltext = "32017-07.htm";
				st.set("rcpTRADE", "0");
			}
			else
			{
				htmltext = "32017-00a.htm";
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

		if (npc.getNpcId() == KARUDA)
		{
			st.set("rcpTRADE", "0");
			int cond = st.getInt(CONDITION);
			switch (cond)
			{
				case 0:
					if (st.getPlayer().getLevel() < 80)
					{
						st.exitQuest(true);
						htmltext = "32017-00.htm";
					}
					else
					{
						htmltext = "32017-01.htm";
					}
					break;
				case 1:
					if (st.getQuestItemsCount(BURIAL) == 0)
					{
						htmltext = "32017-03.htm";
					}
					else
					{
						htmltext = "32017-04.htm";
					}
					break;
			}
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return null;
		if (ArrayUtils.contains(MOBS, npc.getNpcId()) && CHANCE >= Rnd.get(100) && st.getInt(CONDITION) == 1)
		{
			st.giveItems(BURIAL, 1);
			st.sendPacket(SND_ITEM_GET);
		}
		return null;
	}

	public static void main(String[] args)
	{
		new GhostsOfBatur(645, QN, "Ghosts of Batur");
	}
}
