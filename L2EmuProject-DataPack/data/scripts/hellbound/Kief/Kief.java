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
package hellbound.Kief;

import net.l2emuproject.gameserver.instancemanager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;

/**
 * @author DS, based on theOne's work
 */
public final class Kief extends QuestJython
{
	private static final int	KIEF					= 32354;

	private static final int	BOTTLE					= 9672;
	private static final int	DARION_BADGE			= 9674;
	private static final int	DIM_LIFE_FORCE			= 9680;
	private static final int	LIFE_FORCE				= 9681;
	private static final int	CONTAINED_LIFE_FORCE	= 9682;
	private static final int	STINGER					= 10012;

	public Kief(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(KIEF);
		addStartNpc(KIEF);
		addTalkId(KIEF);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("Badges".equalsIgnoreCase(event))
		{
			switch (HellboundManager.getInstance().getHellboundLevel())
			{
				case 2:
				case 3:
					final long num = player.getInventory().getInventoryItemCount(DARION_BADGE, -1, false);
					if (num > 0)
					{
						if (player.destroyItemByItemId("Quest", DARION_BADGE, num, npc, true))
						{
							HellboundManager.getInstance().updateTrustPoints((int) num * 1, false);
							return "32354-ok.htm";
						}
					}
			}
			return "32354-nobadges.htm";
		}
		else if ("Bottle".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getHellboundLevel() >= 7)
			{
				if (player.getInventory().getInventoryItemCount(STINGER, -1, false) >= 20)
				{
					if (player.destroyItemByItemId("Quest", STINGER, 20, npc, true))
					{
						player.addItem("Quest", BOTTLE, 1, npc, true);
						return "32354-bottle.htm";
					}
				}
				return "32354-nostingers.htm";
			}
		}
		else if ("dlf".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getHellboundLevel() == 7)
			{
				final long num = player.getInventory().getInventoryItemCount(DIM_LIFE_FORCE, -1, false);
				if (num > 0)
				{
					if (player.destroyItemByItemId("Quest", DIM_LIFE_FORCE, num, npc, true))
					{
						HellboundManager.getInstance().updateTrustPoints((int) num * 1, false);
						return "32354-ok.htm";
					}
				}
				return "32354-nolifeforce.htm";
			}
		}
		else if ("lf".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getHellboundLevel() == 7)
			{
				final long num = player.getInventory().getInventoryItemCount(LIFE_FORCE, -1, false);
				if (num > 0)
				{
					if (player.destroyItemByItemId("Quest", LIFE_FORCE, num, npc, true))
					{
						HellboundManager.getInstance().updateTrustPoints((int) num * 2, false);
						return "32354-ok.htm";
					}
				}
				return "32354-nolifeforce.htm";
			}
		}
		else if ("clf".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getHellboundLevel() == 7)
			{
				final long num = player.getInventory().getInventoryItemCount(CONTAINED_LIFE_FORCE, -1, false);
				if (num > 0)
				{
					if (player.destroyItemByItemId("Quest", CONTAINED_LIFE_FORCE, num, npc, true))
					{
						HellboundManager.getInstance().updateTrustPoints((int) num * 5, false);
						return "32354-ok.htm";
					}
				}
				return "32354-nolifeforce.htm";
			}
		}

		return event;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);

		switch (HellboundManager.getInstance().getHellboundLevel())
		{
			case 1:
				return "32354-01.htm";
			case 2:
			case 3:
				return "32354-03.htm";
			case 4:
				return "32354-04.htm";
			case 5:
				return "32354-05.htm";
			case 6:
				return "32354-06.htm";
			case 7:
				return "32354-07.htm";
			default:
				return "32354-08.htm";
		}
	}

	public static void main(String[] args)
	{
		new Kief(-1, Kief.class.getSimpleName(), "hellbound");
	}
}