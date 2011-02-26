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
package teleports.DelusionTeleport;

import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author d0S
 */
public final class DelusionTeleport extends QuestJython
{
	private static final String	QN				= "DelusionTeleport";

	private final static int	REWARDER_ONE	= 32658;
	private final static int	REWARDER_TWO	= 32659;
	private final static int	REWARDER_THREE	= 32660;
	private final static int	REWARDER_FOUR	= 32661;
	private final static int	REWARDER_FIVE	= 32663;
	private final static int	REWARDER_SIX	= 32662;
	private final static int	START_NPC		= 32484;

	private int					x				= 0;
	private int					y				= 0;
	private int					z				= 0;

	public DelusionTeleport(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(START_NPC);
		addTalkId(START_NPC);
		addTalkId(REWARDER_ONE);
		addTalkId(REWARDER_TWO);
		addTalkId(REWARDER_THREE);
		addTalkId(REWARDER_FOUR);
		addTalkId(REWARDER_FIVE);
		addTalkId(REWARDER_SIX);
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(getName());

		switch (npc.getNpcId())
		{
			case START_NPC:
				x = player.getX();
				y = player.getY();
				z = player.getZ();
				player.teleToLocation(-114592, -152509, -6723);
				if (player.getPet() != null)
					player.getPet().teleToLocation(-114592, -152509, -6723);
				break;
			case REWARDER_ONE:
			case REWARDER_TWO:
			case REWARDER_THREE:
			case REWARDER_FOUR:
			case REWARDER_FIVE:
			case REWARDER_SIX:
				player.teleToLocation(x, y, z);
				if (player.getPet() != null)
					player.getPet().teleToLocation(x, y, z);
				st.exitQuest(true);
				break;
		}
		return "";
	}

	public static void main(String[] args)
	{
		new DelusionTeleport(-1, QN, "teleports");
	}
}
