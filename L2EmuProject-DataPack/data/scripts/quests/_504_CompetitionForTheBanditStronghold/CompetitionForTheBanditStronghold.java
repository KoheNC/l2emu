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
package quests._504_CompetitionForTheBanditStronghold;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class CompetitionForTheBanditStronghold extends QuestJython
{
	private static final String	QN			= "_504_CompetitionForTheBanditStronghold";

	// NPCs
	private static final int	MESSENGER	= 35437;

	// MOBs
	private static final int[]	MOBS		=
											{ 20570, 20571, 20572, 20573, 20574 };

	// Item Quest
	private static final int[]	ITEMS		=
											{ 4332, 5009 };

	public CompetitionForTheBanditStronghold(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(MESSENGER);

		addTalkId(MESSENGER);

		for (int i : MOBS)
			addKillId(i);

		questItemIds = new int[]
		{ ITEMS[0] };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("a2.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("a4.htm"))
		{
			if (st.getQuestItemsCount(ITEMS[0]) == 30)
			{
				st.takeItems(ITEMS[0], -30);
				st.giveItems(ITEMS[1], 1);
				st.sendPacket(SND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "a5.htm";
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
		L2Clan clan = player.getClan();
		if (clan == null)
			return "a6.htm";

		if (clan.getLevel() < 4)
			return "a6.htm";

		if (clan.getLeaderName() != player.getName())
			return "a6.htm";
		// TODO: Need support in core.
		//if (BanditStrongholdSiege.getInstance().isRegistrationPeriod())
		{
			if (npc.getNpcId() == MESSENGER)
			{
				switch (cond)
				{
					case 1:
						htmltext = "a1.htm";
						break;
					case 2:
						htmltext = "a3.htm";
						break;
				}
			}
		}
		//else
		//{
		//htmltext = null;
		//npc.showChatWindow(player, 3);
		//}

		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return null;

		if (ArrayUtils.contains(MOBS, npc.getNpcId()) && st.getQuestItemsCount(ITEMS[0]) < 30)
		{
			st.giveItems(ITEMS[0], 1);
			st.sendPacket(SND_ITEM_GET);

			if (st.getQuestItemsCount(ITEMS[0]) == 30)
				st.set(CONDITION, 2);
		}

		return null;
	}

	public static void main(String[] args)
	{
		new CompetitionForTheBanditStronghold(504, QN, "Competition For The Bandit Stronghold");
	}
}
