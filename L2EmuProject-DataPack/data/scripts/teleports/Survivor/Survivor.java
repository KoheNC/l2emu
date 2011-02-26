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
package teleports.Survivor;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class Survivor extends QuestJython
{
	public static final String	QN	= "Survivor";

	// NPC
	private static final int	NPC	= 32632;

	public Survivor(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addStartNpc(NPC);
		addTalkId(NPC);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return "32632-1.htm";

		if (player.getLevel() < 75)
			return "32632-3.htm";
		else if (st.getQuestItemsCount(57) >= 150000)
		{
			st.takeAdena(150000);
			player.teleToLocation(-149406, 255247, -80);
			return htmltext;
		}
		else
			return "32632-1.htm";
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		return "32632-1.htm";
	}

	public static void main(String[] args)
	{
		new Survivor(1003, QN, "Survivor Gracia", "teleports");
	}
}