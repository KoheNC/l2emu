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
package teleports.RaceTrackTeleport;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.model.Location;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;

/**
 * @author L0ngh0rn
 * @since 2011-02-09
 */
public final class RaceTrackTeleport extends QuestJython
{
	public static final String						QN				= "RaceTrackTeleport";

	// NPCs
	private static final int						RACE_MANAGER	= 30995;

	// Locs
	private static final FastMap<String, Location>	NPC_LOCS		= new FastMap<String, Location>();

	// Other
	private static final Location					RACE_ARENA		= new Location(12661, 181687, -3560);
	private static final Location					RETURN_DION		= new Location(15670, 142983, -2700);

	static
	{
		NPC_LOCS.put("30320", new Location(-80826, 149775, -3043));
		NPC_LOCS.put("30256", new Location(-12672, 122776, -3116));
		NPC_LOCS.put("30059", new Location(15670, 142983, -2705));
		NPC_LOCS.put("30080", new Location(83400, 147943, -3404));
		NPC_LOCS.put("30899", new Location(111409, 219364, -3545));
		NPC_LOCS.put("30177", new Location(82956, 53162, -1495));
		NPC_LOCS.put("30848", new Location(146331, 25762, -2018));
		NPC_LOCS.put("30233", new Location(116819, 76994, -2714));
		NPC_LOCS.put("31320", new Location(43835, -47749, -792));
		NPC_LOCS.put("31275", new Location(147930, -55281, -2728));
		NPC_LOCS.put("31964", new Location(87386, -143246, -1293));
		NPC_LOCS.put("31210", new Location(12882, 181053, -3560));
	}

	public RaceTrackTeleport(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (String npcId : NPC_LOCS.keySet())
		{
			addStartNpc(Integer.parseInt(npcId));
			addTalkId(Integer.parseInt(npcId));
		}

		addStartNpc(RACE_MANAGER);
		addTalkId(RACE_MANAGER);
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		final int npcId = npc.getNpcId();

		if (NPC_LOCS.containsKey(String.valueOf(npcId)))
		{
			st.getPlayer().teleToLocation(RACE_ARENA);
			st.setState(State.STARTED);
			st.set(ID, String.valueOf(npcId));
		}
		else if (npcId == RACE_MANAGER)
		{
			final String id = String.valueOf(st.get(ID));
			if (st.getState() == State.STARTED && id != null)
			{
				st.getPlayer().teleToLocation(NPC_LOCS.get(id));
				st.unset(ID);
			}
			else
			{
				player.sendPacket(new NpcSay(npc.getObjectId(), 0, npcId,
						"You've arrived here from a different way. I'll send you to Dion Castle Town which is the nearest town."));
				st.getPlayer().teleToLocation(RETURN_DION);
			}
			st.exitQuest(true);
		}
		return null;
	}

	public static void main(String[] args)
	{
		new RaceTrackTeleport(1101, QN, "Race Track Teleport", "teleports");
	}
}