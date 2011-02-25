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
package teleports.ToIVortex;

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * @author Intrepid
 */
public final class ToIVortex extends QuestJython
{
	private static final String	QN						= "ToiVortex";

	private static final int	GREEN_DIMENSION_STONE	= 4401;
	private static final int	BLUE_DIMENSION_STONE	= 4402;
	private static final int	RED_DIMENSION_STONE		= 4403;

	private static final int	DIMENSION_VORTEX_1		= 30952;
	private static final int	DIMENSION_VORTEX_2		= 30953;
	private static final int	DIMENSION_VORTEX_3		= 30954;

	private static final int	VORTEX_EXIT				= 29055;

	private int					x, y, z = 0;

	public ToIVortex(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(DIMENSION_VORTEX_1);
		addStartNpc(DIMENSION_VORTEX_2);
		addStartNpc(DIMENSION_VORTEX_3);
		addStartNpc(VORTEX_EXIT);
		addTalkId(DIMENSION_VORTEX_1);
		addTalkId(DIMENSION_VORTEX_2);
		addTalkId(DIMENSION_VORTEX_3);
		addTalkId(VORTEX_EXIT);
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());

		switch (npc.getNpcId())
		{
			case DIMENSION_VORTEX_1:
				if (st.getQuestItemsCount(BLUE_DIMENSION_STONE) >= 1)
				{
					st.takeItems(BLUE_DIMENSION_STONE, 1);
					player.teleToLocation(114097, 19935, 935);
				}
				else if (st.getQuestItemsCount(RED_DIMENSION_STONE) >= 1)
				{
					st.takeItems(RED_DIMENSION_STONE, 1);
					player.teleToLocation(118558, 16659, 5987);
				}
				else { htmltext = "1.htm"; }
				break;
			case DIMENSION_VORTEX_2:
				if (st.getQuestItemsCount(GREEN_DIMENSION_STONE) >= 1)
				{
					st.takeItems(GREEN_DIMENSION_STONE, 1);
					player.teleToLocation(110930, 15963, -4378);
				}
				else if (st.getQuestItemsCount(RED_DIMENSION_STONE) >= 1)
				{
					st.takeItems(RED_DIMENSION_STONE, 1);
					player.teleToLocation(118558, 16659, 5987);
				}
				else { htmltext = "1.htm"; }
				break;
			case DIMENSION_VORTEX_3:
				if (st.getQuestItemsCount(GREEN_DIMENSION_STONE) >= 1)
				{
					st.takeItems(GREEN_DIMENSION_STONE, 1);
					player.teleToLocation(110930, 15963, -4378);
				}
				else if (st.getQuestItemsCount(BLUE_DIMENSION_STONE) >= 1)
				{
					st.takeItems(BLUE_DIMENSION_STONE, 1);
					player.teleToLocation(114097, 19935, 935);
				}
				else { htmltext = "1.htm"; }
				break;
			case VORTEX_EXIT:
				final int chance = st.getRandom(3);

				if (chance == 0)
				{
					x = 108784 + st.getRandom(100);
					y = 16000 + st.getRandom(100);
					z = -4928;
				}
				else if (chance == 1)
				{
					x = 113824 + st.getRandom(100);
					y = 10448 + st.getRandom(100);
					z = -5164;
				}
				else
				{
					x = 115488 + st.getRandom(100);
					y = 22096 + st.getRandom(100);
					z = -5168;
				}
				player.teleToLocation(x, y, z);
				break;
		}
		st.exitQuest(true);

		return htmltext;
	}

	public static void main(String[] args)
	{
		new ToIVortex(-1, QN, "teleports");
	}
}
