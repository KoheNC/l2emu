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
package ai.zone.hellbound.Bernarde;

import net.l2emuproject.gameserver.manager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author DS, based on theOne's work
 */
public final class Bernarde extends QuestJython
{
	private static final int	BERNARDE			= 32300;
	private static final int	NATIVE_TRANSFORM	= 101;
	private static final int	HOLY_WATER			= 9673;
	private static final int	DARION_BADGE		= 9674;
	private static final int	TREASURE			= 9684;

	private boolean				_treasure			= false;

	public Bernarde(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(BERNARDE);
		addStartNpc(BERNARDE);
		addTalkId(BERNARDE);
	}

	private static final boolean isTransformed(L2Player player)
	{
		return player.getPlayerTransformation().isTransformed() && player.getPlayerTransformation().getTransformation().getId() == NATIVE_TRANSFORM;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if ("HolyWater".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getHellboundLevel() == 2)
			{
				if (player.getInventory().getInventoryItemCount(DARION_BADGE, -1, false) >= 5)
				{
					if (player.destroyItemByItemId("Quest", DARION_BADGE, 5, npc, true))
					{
						player.addItem("Quest", HOLY_WATER, 1, npc, true);
						return "32300-water.htm";
					}
				}
			}
			return "32300-nobadges.htm";
		}
		if ("Treasure".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getHellboundLevel() == 3)
			{
				if (_treasure)
					return "32300-already.htm";

				if (player.getInventory().getInventoryItemCount(TREASURE, -1, false) > 0)
				{
					if (player.destroyItemByItemId("Quest", TREASURE, 1, npc, true))
					{
						_treasure = true;
						return "32300-treasure.htm";
					}
				}
			}
			return "32300-notreasure.htm";
		}

		return event;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);

		switch (HellboundManager.getInstance().getHellboundLevel())
		{
			case 2:
				return isTransformed(player) ? "32300-02n.htm" : "32300-02.htm";
			case 3:
				return isTransformed(player) ? "32300-03n.htm" : "32300-03.htm";
			case 4:
				return isTransformed(player) ? "32300-04n.htm" : "32300-04.htm";
			case 5:
				return isTransformed(player) ? "32300-05n.htm" : "32300-05.htm";
			case 6:
				return isTransformed(player) ? "32300-06n.htm" : "32300-06.htm";
			case 7:
				return isTransformed(player) ? "32300-07n.htm" : "32300-07.htm";
			case 8:
				return isTransformed(player) ? "32300-08n.htm" : "32300-08.htm";
			case 9:
				return "32300-09.htm";
			case 10:
				return "32300-10.htm";
			case 11:
				return "32300-11.htm";
			default:
				return "32300-02.htm";
		}
	}

	public static void main(String[] args)
	{
		new Bernarde(-1, Bernarde.class.getSimpleName(), "ai/zones/hellbound");
	}
}
