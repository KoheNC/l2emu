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
package ai.zone.hellbound.Buron;

import net.l2emuproject.gameserver.manager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author DS, based on theOne's work
 */
public final class Buron extends QuestJython
{
	private static final int	BURON			= 32345;
	private static final int	HELMET			= 9669;
	private static final int	TUNIC			= 9670;
	private static final int	PANTS			= 9671;
	private static final int	DARION_BADGE	= 9674;

	public Buron(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(BURON);
		addStartNpc(BURON);
		addTalkId(BURON);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if ("Rumor".equalsIgnoreCase(event))
			return "32345-" + HellboundManager.getInstance().getHellboundLevel() + "r.htm";

		if (HellboundManager.getInstance().getHellboundLevel() > 1)
		{
			if (player.getInventory().getInventoryItemCount(DARION_BADGE, -1, false) >= 10)
			{
				if (player.destroyItemByItemId("Quest", DARION_BADGE, 10, npc, true))
				{
					if ("Tunic".equalsIgnoreCase(event))
						player.addItem("Quest", TUNIC, 1, npc, true);
					else if ("Helmet".equalsIgnoreCase(event))
						player.addItem("Quest", HELMET, 1, npc, true);
					else if ("Pants".equalsIgnoreCase(event))
						player.addItem("Quest", PANTS, 1, npc, true);
					return null;
				}
			}
		}
		return "32345-noitems.htm";
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);

		switch (HellboundManager.getInstance().getHellboundLevel())
		{
			case 1:
				return "32345-01.htm";
			case 2:
			case 3:
			case 4:
				return "32345-02.htm";
			default:
				return "32345-03.htm";
		}
	}

	public static void main(String[] args)
	{
		new Buron(-1, Buron.class.getSimpleName(), "ai/zones/hellbound");
	}
}