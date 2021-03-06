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
package teleports.MithrilMines;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class MithrilMines extends QuestJython
{
	private static final String		QN			= "MithrilMines";

	private static final int[][]	LOCATION	=
												{
												{ 171946, -173352, 3440 },
												{ 175499, -181586, -904 },
												{ 173462, -174011, 3480 },
												{ 179299, -182831, -224 },
												{ 178591, -184615, 360 },
												{ 175499, -181586, -904 } };

	private final static int		NPC			= 32652;

	public MithrilMines(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());

		final int loc = Integer.parseInt(event) - 1;
		if (LOCATION.length > loc)
		{
			int x = LOCATION[loc][0];
			int y = LOCATION[loc][1];
			int z = LOCATION[loc][2];

			player.teleToLocation(x, y, z);
			st.exitQuest(true);
		}

		return htmltext;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);

		if (npc.isInsideRadius(173147, -173762, L2Npc.INTERACTION_DISTANCE, true))
			htmltext = "32652-01.htm";
		else if (npc.isInsideRadius(181941, -174614, L2Npc.INTERACTION_DISTANCE, true))
			htmltext = "32652-02.htm";
		else if (npc.isInsideRadius(179560, -182956, L2Npc.INTERACTION_DISTANCE, true))
			htmltext = "32652-03.htm";

		return htmltext;
	}

	public static void main(String[] args)
	{
		new MithrilMines(-1, QN, "teleports");
	}
}
