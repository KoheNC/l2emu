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
package teleports.CrumaTower;

import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author K4N4BS
 * @since L0ngh0rn - Adapted for L2EmuProject.
 */
public final class CrumaTower extends QuestJython
{
	public static final String		QN	= "CrumaTower";

	// NPC
	private static final int		NPC	= 30483;

	// Other
	private static final Location	LOC	= new Location(17724, 114004, -11672);

	public CrumaTower(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addStartNpc(NPC);
		addTalkId(NPC);
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = null;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		if (player.getLevel() > 55)
			htmltext = "30483.htm";
		else
			player.teleToLocation(LOC);
		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new CrumaTower(1107, QN, "Cruma Tower Teleport", "teleports");
	}
}