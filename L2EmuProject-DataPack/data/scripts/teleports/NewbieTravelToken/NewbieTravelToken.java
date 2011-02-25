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
package teleports.NewbieTravelToken;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.Location;

/**
 * @author L0ngh0rn
 * @since 2011-02-08
 */
public final class NewbieTravelToken extends QuestJython
{
	public static final String						QN		= "NewbieTravelToken";

	// Quest Item
	private static final int						TOKEN	= 8542;

	// NPCs
	private static final FastMap<String, Location>	NPCS	= new FastMap<String, Location>();

	static
	{
		NPCS.put("30600", new Location(12160, 16554, -4583)); // DE
		NPCS.put("30601", new Location(115594, -177993, -912)); // DW
		NPCS.put("30599", new Location(45470, 48328, -3059)); // EV
		NPCS.put("30602", new Location(-45067, -113563, -199)); // OV
		NPCS.put("30598", new Location(-84053, 243343, -3729)); // TI
		NPCS.put("32135", new Location(-119712, 44519, 368)); // SI
	}

	public NewbieTravelToken(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (String npcId : NPCS.keySet())
		{
			addStartNpc(Integer.parseInt(npcId));
			addTalkId(Integer.parseInt(npcId));
		}
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (NPCS.containsKey(event))
		{
			if (st.getQuestItemsCount(TOKEN) > 0)
			{
				st.takeItems(TOKEN, 1);
				player.teleToLocation(NPCS.get(event));
				htmltext = null;
			}
			else
				htmltext = "<html><body>Newbie Guide:<br>You need 1 Newbie Travel Token.</body></html>";
		}
		st.exitQuest(true);
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		if (player.getLevel() >= 20)
		{
			htmltext = "no.htm";
			st.exitQuest(true);
		}
		else
			htmltext = npc.getNpcId() + ".htm";
		return htmltext;
	}

	public static void main(String[] args)
	{
		new NewbieTravelToken(1104, QN, "Newbie Travel Token", "teleports");
	}
}