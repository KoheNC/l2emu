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

package quests.Kamaloka;

import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
* @author lewzer
*/

public final class Kamaloka extends Quest
{
	private static final String	QN			= "Kamaloka";
	//NPC's
	private static final int	BATHIS		= 30332;
	private static final int	LUCAS		= 30071;
	private static final int	GOSTA		= 30916;
	private static final int	MOUEN		= 30196;
	private static final int	VISHOTSKY	= 31981;
	private static final int	MATHIAS		= 31340;
	private static final int	TELEPORT	= 32496;

	public Kamaloka(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addTalkId(BATHIS);
		addTalkId(LUCAS);
		addTalkId(GOSTA);
		addTalkId(MOUEN);
		addTalkId(VISHOTSKY);
		addTalkId(MATHIAS);
		addTalkId(TELEPORT);

	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		int npcID = npc.getNpcId();
		switch (npcID)
		{
			case BATHIS:
				htmltext = "30332.htm";
				break;
			case LUCAS:
				htmltext = "30071.htm";
				break;
			case GOSTA:
				htmltext = "30916.htm";
				break;
			case MOUEN:
				htmltext = "30196.htm";
				break;
			case VISHOTSKY:
				htmltext = "31981.htm";
				break;
			case MATHIAS:
				htmltext = "31340.htm";
				break;
			case TELEPORT:
				htmltext = "32496.htm";
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Kamaloka(-1, QN, "Kamaloka");
	}
}