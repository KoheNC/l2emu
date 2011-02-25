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
package custom.MissQueen;

import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class MissQueen extends QuestJython
{
	private static final String	QN		= "704_MissQueen";

	// NPCs
	public final int			QUEEN	= 31760;

	// Quest Items
	public final int[]			ITEM	=
										{ 7832, 7833 };

	public MissQueen(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(QUEEN);
		addTalkId(QUEEN);
		addFirstTalkId(QUEEN);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("31760-02.htm"))
		{
			if (st.getInt(CONDITION) == 0 && st.getPlayer().getLevel() <= 20 && st.getPlayer().getLevel() >= 6 && st.getPlayer().getPkKills() == 0)
			{
				st.giveItems(ITEM[0], 1);
				st.set(CONDITION, 1);
				htmltext = "c_1.htm";
				st.sendPacket(SND_ACCEPT);
			}
			else
				htmltext = "fail-01.htm";
		}
		else if (event.equalsIgnoreCase("31760-03.htm"))
		{
			if (st.getInt(CONDITION) == 0 && st.getPlayer().getLevel() <= 25 && st.getPlayer().getLevel() >= 20 && st.getPlayer().getPkKills() == 0)
			{
				st.giveItems(ITEM[1], 1);
				st.set(CONDITION, 1);
				htmltext = "c_2.htm";
				st.sendPacket(SND_ACCEPT);
			}
			else
				htmltext = "fail-02.htm";
		}
		return htmltext;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == QUEEN)
			htmltext = "31760-01.htm";

		return htmltext;
	}

	public static void main(String[] args)
	{
		new MissQueen(704, QN, "Miss Queen");
	}
}
