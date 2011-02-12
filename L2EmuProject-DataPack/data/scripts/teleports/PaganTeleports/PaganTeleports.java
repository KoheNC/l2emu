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
package teleports.PaganTeleports;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.datatables.DoorTable;
import net.l2emuproject.gameserver.model.Location;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * @author L0ngh0rn
 */
public final class PaganTeleports extends QuestJython
{
	public static final String						QN			= "PaganTeleports";

	// NPCs
	private static final FastMap<Integer, Location>	NPC_FIRST	= new FastMap<Integer, Location>();
	private static final int[]						NPCS		=
																{ 32034, 32035, 32036, 32037, 32039, 32040 };

	// Other
	private static final int						DOOR		= 19160001;
	private static final int						DOOR_A		= 19160010;
	private static final int						DOOR_B		= 19160011;

	static
	{
		NPC_FIRST.put(32039, new Location(-12766, -35840, -10856));
		NPC_FIRST.put(32040, new Location(36640, -51218, 718));
	}

	public PaganTeleports(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (int npc : NPCS)
		{
			addStartNpc(npc);
			addTalkId(npc);
		}

		for (int npc : NPC_FIRST.keySet())
			addFirstTalkId(npc);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("Close_Door1"))
			DoorTable.getInstance().getDoor(DOOR).closeMe();
		else if (event.equalsIgnoreCase("Close_Door2"))
		{
			DoorTable.getInstance().getDoor(DOOR_A).closeMe();
			DoorTable.getInstance().getDoor(DOOR_B).closeMe();
		}
		return null;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		htmltext = "FadedMark.htm";
		switch (npc.getNpcId())
		{
			case 32034:
				if (st.getQuestItemsCount(8064) == 0 && st.getQuestItemsCount(8065) == 0 && st.getQuestItemsCount(8067) == 0)
					htmltext = "32034-no.htm";
				DoorTable.getInstance().getDoor(DOOR).openMe();
				startQuestTimer("Close_Door1", 10000, npc, null);
				break;
			case 32035:
				DoorTable.getInstance().getDoor(DOOR).openMe();
				startQuestTimer("Close_Door1", 10000, npc, null);
				break;
			case 32036:
				if (st.getQuestItemsCount(8067) == 0)
					htmltext = "32036-no.htm";
				else
				{
					startQuestTimer("Close_Door2", 10000, npc, null);
					DoorTable.getInstance().getDoor(DOOR_A).openMe();
					DoorTable.getInstance().getDoor(DOOR_B).openMe();
					htmltext = "32036.htm";
				}
				break;
			case 32037:
				DoorTable.getInstance().getDoor(DOOR_A).openMe();
				DoorTable.getInstance().getDoor(DOOR_B).openMe();
				startQuestTimer("Close_Door2", 10000, npc, null);
				break;
		}
		st.exitQuest(true);
		return htmltext;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final int npcId = npc.getNpcId();
		if (NPC_FIRST.containsKey(npcId))
			player.teleToLocation(NPC_FIRST.get(npcId));
		return null;
	}

	public static void main(String[] args)
	{
		new PaganTeleports(1630, QN, "Pagan Temple Teleports", "teleports");
	}
}