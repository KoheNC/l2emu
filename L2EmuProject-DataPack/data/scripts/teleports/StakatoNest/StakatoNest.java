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
package teleports.StakatoNest;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.model.world.Location;

public final class StakatoNest extends QuestJython
{
	private static final String						QN			= "StakatoNest";

	private static final FastMap<String, Location>	LOCATION	= new FastMap<String, Location>();

	static
	{
		LOCATION.put("1", new Location(80456, -52322, -5640));
		LOCATION.put("2", new Location(88718, -46214, -4640));
		LOCATION.put("3", new Location(87464, -54221, -5120));
		LOCATION.put("4", new Location(80848, -49426, -5128));
		LOCATION.put("5", new Location(87682, -43291, -4128));
	}

	private static final int						NPC			= 32640;

	public StakatoNest(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(NPC);
		addTalkId(NPC);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);

		if (LOCATION.size() > Integer.parseInt(event))
		{
			final Location loc = LOCATION.get(event);
			if (player.getParty() != null)
			{
				for (L2PcInstance partyMember : player.getParty().getPartyMembers())
				{
					if (partyMember.isInsideRadius(player, 1000, true, true))
						partyMember.teleToLocation(loc);
				}
			}
			player.teleToLocation(loc);
			st.exitQuest(true);
		}

		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState accessQuest = player.getQuestState("240_ImTheOnlyOneYouCanTrust");
		if (accessQuest != null && accessQuest.getState() == State.COMPLETED)
			htmltext = "32640.htm";
		else
			htmltext = "32640-no.htm";

		return htmltext;
	}

	public static void main(String[] args)
	{
		new StakatoNest(-1, QN, "teleports");
	}
}
