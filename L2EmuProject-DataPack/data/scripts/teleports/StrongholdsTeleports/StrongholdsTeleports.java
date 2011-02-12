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
package teleports.StrongholdsTeleports;

import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * @author L0ngh0rn
 */
public final class StrongholdsTeleports extends QuestJython
{
	public static final String	QN		= "StrongholdsTeleports";

	// NPCs
	private static final int[]	NPCS	=
										{ 32163, 32181, 32184, 32186 };

	public StrongholdsTeleports(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (int NPC : NPCS)
			addFirstTalkId(NPC);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		if (st.getPlayer().getLevel() < 20)
			htmltext = npc.getNpcId() + ".htm";
		else
			htmltext = npc.getNpcId() + "-no.htm";

		return htmltext;
	}

	public static void main(String[] args)
	{
		new StrongholdsTeleports(1105, QN, "Strongholds Teleports", "teleports");
	}
}
