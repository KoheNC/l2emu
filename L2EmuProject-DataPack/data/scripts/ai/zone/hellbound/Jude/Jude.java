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
package ai.zone.hellbound.Jude;

import net.l2emuproject.gameserver.manager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author DS, based on theOne's work
 */
public final class Jude extends QuestJython
{
	private static final int	JUDE		= 32356;
	private static final int	TREASURE	= 9684;

	public Jude(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(JUDE);
		addStartNpc(JUDE);
		addTalkId(JUDE);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if ("TreasureSacks".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getHellboundLevel() == 3)
			{
				if (player.getInventory().getInventoryItemCount(TREASURE, -1, false) >= 40)
				{
					if (player.destroyItemByItemId("Quest", TREASURE, 40, npc, true))
						return "32356-ok.htm";
				}
			}
			return "32356-notreasure.htm";
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
			case 1:
			case 2:
				return "32356-02.htm";
			case 3:
				return "32356-03.htm";
			case 4:
				return "32356-04.htm";
			case 5:
				return "32356-05.htm";
			default:
				return "32356-06.htm";
		}
	}

	public static void main(String[] args)
	{
		new Jude(-1, Jude.class.getSimpleName(), "ai/zones/hellbound");
	}
}