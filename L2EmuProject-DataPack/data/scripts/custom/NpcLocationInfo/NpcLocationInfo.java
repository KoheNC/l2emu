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
package custom.NpcLocationInfo;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author L0ngh0rn
 */
public final class NpcLocationInfo extends QuestJython
{
	private static final String	QN	= "NpcLocationInfo";

	// NPCs
	private static final int[]	NPC	=
									{ 30598, 30599, 30600, 30601, 30602, 32135 };

	public NpcLocationInfo(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (int i : NPC)
		{
			addStartNpc(i);
			addTalkId(i);
		}
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		if (ArrayUtils.contains(NPC, npc.getNpcId()))
			htmltext = npc.getNpcId() + ".htm";
		return htmltext;
	}

	public static void main(String[] args)
	{
		new NpcLocationInfo(8001, QN, "NPC Location Information", "custom");
	}
}
