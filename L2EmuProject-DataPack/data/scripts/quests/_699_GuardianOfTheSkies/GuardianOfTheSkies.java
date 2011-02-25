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
package quests._699_GuardianOfTheSkies;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class GuardianOfTheSkies extends QuestJython
{
	private static final String	QN				= "_699_GuardianOfTheSkies";

	// NPCs
	private static final int	LEKON			= 32557;

	// Quet Item
	private static final int	GOLDEN_FEATHER	= 13871;

	// MOBs
	private static final int[]	MOBS			=
												{ 22614, 22615, 25623, 25633 };

	// Chance
	private static final int	DROP_CHANCE		= 80;

	public GuardianOfTheSkies(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(LEKON);
		addTalkId(LEKON);

		for (int i : MOBS)
			addKillId(i);

		questItemIds = new int[]
		{ GOLDEN_FEATHER };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32557-03.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, 1);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32557-quit.htm"))
		{
			st.unset(CONDITION);
			st.exitQuest(true);
			st.sendPacket(SND_FINISH);
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

		if (npc.getNpcId() == LEKON)
		{
			QuestState first = player.getQuestState("10273_GoodDayToFly");
			if (first != null && first.getState() == State.COMPLETED && st.getState() == State.CREATED && player.getLevel() >= 75)
				htmltext = "32557-01.htm";
			else
			{
				switch (st.getInt(CONDITION))
				{
					case 0:
						htmltext = "32557-00.htm";
						break;
					case 1:
						long count = st.getQuestItemsCount(GOLDEN_FEATHER);
						if (count > 0)
						{
							st.takeItems(GOLDEN_FEATHER, -1);
							st.giveAdena(count * 2300);
							st.sendPacket(SND_ITEM_GET);
							htmltext = "32557-06.htm";
						}
						else
							htmltext = "32557-04.htm";
						break;
				}
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

		if (st.getInt(CONDITION) == 1 && ArrayUtils.contains(MOBS, npc.getNpcId()))
		{
			int chance = (int) (DROP_CHANCE * Config.RATE_DROP_QUEST);
			int numItems = (int) (chance / 100);
			chance = chance % 100;
			if (st.getRandom(100) < chance)
				numItems++;
			if (numItems > 0)
			{
				st.giveItems(GOLDEN_FEATHER, 1);
				st.sendPacket(SND_ITEM_GET);
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new GuardianOfTheSkies(699, QN, "Guardian of the Skies");
	}
}
