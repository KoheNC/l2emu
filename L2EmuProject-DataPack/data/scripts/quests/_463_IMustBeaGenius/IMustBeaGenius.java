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
package quests._463_IMustBeaGenius;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/**
 ** @author Gnacik
 **
 ** 2010-08-19 Based on Freya PTS
 */
public final class IMustBeaGenius extends QuestJython
{
	private static final String	QN				= "_463_IMustBeaGenius";
	private static final int	GUTEN_HANGEN	= 32069;
	private static final int	CORPSE_LOG		= 15510;
	private static final int	COLLECTION		= 15511;
	private static final int[]	MONSTERS		=
												{ 22801, 22802, 22804, 22805, 22807, 22808, 22809, 22810, 22811, 22812 };

	public IMustBeaGenius(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addStartNpc(GUTEN_HANGEN);
		addTalkId(GUTEN_HANGEN);
		for (int monsters : MONSTERS)
			addKillId(monsters);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == GUTEN_HANGEN)
		{
			if (event.equalsIgnoreCase("32069-03.htm"))
			{
				st.playSound("ItemSound.quest_accept");
				st.setState(State.STARTED);
				st.set("cond", "1");
				// Generate random daily number for player
				final int number = Rnd.get(500, 600);
				st.set("number", String.valueOf(number));
				// Set drop for mobs
				for (int monsters : MONSTERS)
				{
					int rand = Rnd.get(-2, 4);
					if (rand == 0)
						rand = 5;
					st.set(String.valueOf(monsters), String.valueOf(rand));
				}
				// One with higher chance
				st.set(String.valueOf(MONSTERS[Rnd.get(0, MONSTERS.length - 1)]), String.valueOf(Rnd.get(1, 100)));
				htmltext = getHtm("32069-03.htm");
				htmltext = htmltext.replace("%num%", String.valueOf(number));
			}
			else if (event.equalsIgnoreCase("32069-05.htm"))
			{
				htmltext = getHtm("32069-05.htm");
				htmltext = htmltext.replace("%num%", (CharSequence) st.get("number"));
			}
			else if (event.equalsIgnoreCase("32069-07.htm"))
			{
				st.addExpAndSp(317961, 25427);
				st.unset("cond");
				st.unset("number");
				for (int _mob : MONSTERS)
					st.unset(String.valueOf(_mob));
				st.takeItems(COLLECTION, -1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(false);
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

		if (npc.getNpcId() == GUTEN_HANGEN)
		{
			switch (st.getState())
			{
				case State.CREATED:
					if (player.getLevel() >= 70)
						htmltext = "32069-01.htm";
					else
						htmltext = "32069-00.htm";
					break;
				case State.STARTED:
					if (st.getInt("cond") == 1)
						htmltext = "32069-04.htm";
					else if (st.getInt("cond") == 2)
						htmltext = "32069-06.htm";
					break;
				case State.COMPLETED:
					htmltext = "32069-08.htm";
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

		if (st.getState() == State.STARTED && st.getInt("cond") == 1 && ArrayUtils.contains(MONSTERS, npc.getNpcId()))
		{
			final int dayNumber = st.getInt("number");
			final int number = st.getInt(String.valueOf(npc.getNpcId()));

			if (number > 0)
			{
				st.giveItems(CORPSE_LOG, number);
				st.playSound("ItemSound.quest_itemget");
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Att... attack... " + player.getName() + "... Ro... rogue... " + number
						+ ".."));
			}
			else if (number < 0 && ((st.getQuestItemsCount(CORPSE_LOG) + number) > 0))
			{
				st.takeItems(CORPSE_LOG, Math.abs(number));
				st.playSound("ItemSound.quest_itemget");
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Att... attack... " + player.getName() + "... Ro... rogue... " + number
						+ ".."));
			}

			if (st.getQuestItemsCount(CORPSE_LOG) == dayNumber)
			{
				st.takeItems(CORPSE_LOG, -1);
				st.giveItems(COLLECTION, 1);
				st.set("cond", "2");
			}

		}
		return "";
	}

	public static void main(String[] args)
	{
		new IMustBeaGenius(463, QN, "I Must Be a Genius", "quests");
	}
}
