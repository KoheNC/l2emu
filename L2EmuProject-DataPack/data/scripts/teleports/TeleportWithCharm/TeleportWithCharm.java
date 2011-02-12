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
package teleports.TeleportWithCharm;

import net.l2emuproject.gameserver.model.Location;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * @author L0ngh0rn
 */
public final class TeleportWithCharm extends QuestJython
{
	private static final String		QN						= "TeleportWithCharm";

	// NPCs
	private static final int		WHIRPY					= 30540;
	private static final int		TAMIL					= 30576;
	private static final int[]		NPCS					=
															{ WHIRPY, TAMIL };

	// Quest Item
	private static final int		ORC_GATEKEEPER_CHARM	= 1658;
	private static final int		DWARF_GATEKEEPER_TOKEN	= 1659;

	// Other
	private static final Location	LOC						= new Location(-80826, 149775, -3043);

	public TeleportWithCharm(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (int i : NPCS)
		{
			addStartNpc(i);
			addTalkId(i);
		}
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		switch (npc.getNpcId())
		{
			case WHIRPY:
				if (st.getQuestItemsCount(DWARF_GATEKEEPER_TOKEN) >= 1)
				{
					st.takeItems(DWARF_GATEKEEPER_TOKEN, 1);
					player.teleToLocation(LOC.getX(), LOC.getY(), LOC.getZ());
					st.exitQuest(true);
					return null;
				}
				else
				{
					st.exitQuest(true);
					return "30540-01.htm";
				}
			case TAMIL:
				if (st.getQuestItemsCount(ORC_GATEKEEPER_CHARM) >= 1)
				{
					st.takeItems(ORC_GATEKEEPER_CHARM, 1);
					player.teleToLocation(LOC.getX(), LOC.getY(), LOC.getZ());
					st.exitQuest(true);
					return null;
				}
				else
				{
					st.exitQuest(true);
					return "30576-01.htm";
				}
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new TeleportWithCharm(1100, QN, "Teleport With Charm", "teleports");
	}
}
