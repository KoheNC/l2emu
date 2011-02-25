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
package teleports.TeleportToFantasyIsle;

import java.util.ArrayList;
import java.util.List;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 */
public final class TeleportToFantasyIsle extends QuestJython
{
	public static final String						QN			= "TeleportToFantasyIsle";

	// NPC
	private static final int						PADDIES		= 32378;

	// Others
	private static final Location					RUNE		= new Location(43835, -47749, -792);
	private static final FastMap<Integer, Integer>	TELEPORTERS	= new FastMap<Integer, Integer>();
	private static final List<Location>				RETURN_LOCS	= new ArrayList<Location>();
	private static final List<Location>				ISLE_LOCS	= new ArrayList<Location>();

	static
	{
		TELEPORTERS.put(30320, 1); // RICHLIN
		TELEPORTERS.put(30256, 2); // BELLA
		TELEPORTERS.put(30059, 3); // TRISHA
		TELEPORTERS.put(30080, 4); // CLARISSA
		TELEPORTERS.put(30899, 5); // FLAUEN
		TELEPORTERS.put(30177, 6); // VALENTIA
		TELEPORTERS.put(30848, 7); // ELISA
		TELEPORTERS.put(30233, 8); // ESMERALDA		
		TELEPORTERS.put(31320, 9); // ILYANA
		TELEPORTERS.put(31275, 10); // TATIANA
		TELEPORTERS.put(31964, 11); // BILIA

		RETURN_LOCS.add(new Location(-80826, 149775, -3043));
		RETURN_LOCS.add(new Location(-12672, 122776, -3116));
		RETURN_LOCS.add(new Location(15670, 142983, -2705));
		RETURN_LOCS.add(new Location(83400, 147943, -3404));
		RETURN_LOCS.add(new Location(111409, 219364, -3545));
		RETURN_LOCS.add(new Location(82956, 53162, -1495));
		RETURN_LOCS.add(new Location(146331, 25762, -2018));
		RETURN_LOCS.add(new Location(116819, 76994, -2714));
		RETURN_LOCS.add(new Location(43835, -47749, -792));
		RETURN_LOCS.add(new Location(147930, -55281, -2728));
		RETURN_LOCS.add(new Location(87386, -143246, -1293));

		ISLE_LOCS.add(new Location(-58752, -56898, -2032));
		ISLE_LOCS.add(new Location(-59716, -57868, -2032));
		ISLE_LOCS.add(new Location(-60691, -56893, -2032));
		ISLE_LOCS.add(new Location(-59720, -55921, -2032));
	}

	public TeleportToFantasyIsle(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addStartNpc(PADDIES);
		addTalkId(PADDIES);

		for (int NPC : TELEPORTERS.keySet())
		{
			addStartNpc(NPC);
			addTalkId(NPC);
		}
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		int npcId = npc.getNpcId();
		if (TELEPORTERS.containsKey(npcId))
		{
			final int rnd = st.getRandom(ISLE_LOCS.size());
			st.getPlayer().teleToLocation(ISLE_LOCS.get(rnd));
			st.setState(State.STARTED);
			st.set(ID, TELEPORTERS.get(npcId));
		}
		else if (npcId == PADDIES)
		{
			if (st.getState() == State.STARTED && st.getInt(ID) != 0)
			{
				st.getPlayer().teleToLocation(RETURN_LOCS.get(st.getInt(ID) - 1));
				st.unset(ID);
			}
			else
			{
				player.sendPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(),
						"You've arrived here from a different way. I'll send you to Rune Township which is the nearest town."));
				st.getPlayer().teleToLocation(RUNE);
			}
			st.exitQuest(true);
		}
		return null;
	}

	public static void main(String[] args)
	{
		new TeleportToFantasyIsle(1106, QN, "Teleport To Fantasy Isle", "teleports");
	}
}
