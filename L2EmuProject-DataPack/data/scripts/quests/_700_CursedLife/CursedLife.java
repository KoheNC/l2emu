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
package quests._700_CursedLife;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 * @since 2010-08-27 by Gnacik (Based on official server Franz)
 */
public final class CursedLife extends QuestJython
{
	private static final String	QN					= "_700_CursedLife";

	// NPCs
	private static final int	ORBYU				= 32560;

	// MOBs
	private static final int[]	MOBS				=
													{ 22602, 22603, 22604, 22605 };

	// Quest Item
	private static final int	SWALLOWED_SKULL		= 13872;
	private static final int	SWALLOWED_STERNUM	= 13873;
	private static final int	SWALLOWED_BONES		= 13874;

	public CursedLife(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(ORBYU);
		addTalkId(ORBYU);

		for (int i : MOBS)
			addKillId(i);

		questItemIds = new int[]
		{ SWALLOWED_SKULL, SWALLOWED_STERNUM, SWALLOWED_BONES };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32560-03.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, 1);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32560-quit.htm"))
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

		if (npc.getNpcId() == ORBYU)
		{
			QuestState first = player.getQuestState("10273_GoodDayToFly");
			if (first != null && first.getState() == State.COMPLETED && st.getState() == State.CREATED && player.getLevel() >= 75)
				htmltext = "32560-01.htm";
			else
			{
				switch (st.getInt(CONDITION))
				{
					case 0:
						htmltext = "32560-00.htm";
						break;
					case 1:
						long count1 = st.getQuestItemsCount(SWALLOWED_BONES);
						long count2 = st.getQuestItemsCount(SWALLOWED_STERNUM);
						long count3 = st.getQuestItemsCount(SWALLOWED_SKULL);
						if (count1 > 0 || count2 > 0 || count3 > 0)
						{
							long reward = ((count1 * 500) + (count2 * 5000) + (count3 * 50000));
							st.takeItems(SWALLOWED_BONES, -1);
							st.takeItems(SWALLOWED_STERNUM, -1);
							st.takeItems(SWALLOWED_SKULL, -1);
							st.giveAdena(reward);
							st.sendPacket(SND_ITEM_GET);
							htmltext = "32560-06.htm";
						}
						else
							htmltext = "32560-04.htm";
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
			int chance = st.getRandom(100);
			if (chance < 5)
				st.giveItems(SWALLOWED_SKULL, 1);
			else if (chance < 20)
				st.giveItems(SWALLOWED_STERNUM, 1);
			else
				st.giveItems(SWALLOWED_BONES, 1);
			st.sendPacket(SND_ITEM_GET);
		}
		return null;
	}

	public static void main(String[] args)
	{
		new CursedLife(700, QN, "Cursed Life");
	}
}
