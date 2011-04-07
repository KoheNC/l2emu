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
package ai.zone.hellbound.Hude;

import net.l2emuproject.gameserver.manager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author DS, based on theOne's work
 */
public final class Hude extends QuestJython
{
	private static final int	HUDE					= 32298;
	private static final int	BASIC_CERT				= 9850;
	private static final int	STANDART_CERT			= 9851;
	private static final int	PREMIUM_CERT			= 9852;
	private static final int	MARK_OF_BETRAYAL		= 9676;
	private static final int	LIFE_FORCE				= 9681;
	private static final int	CONTAINED_LIFE_FORCE	= 9682;
	private static final int	MAP						= 9994;
	private static final int	STINGER					= 10012;

	public Hude(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(HUDE);
		addStartNpc(HUDE);
		addTalkId(HUDE);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if ("scertif".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getHellboundLevel() > 3)
			{
				if (player.getInventory().getInventoryItemCount(MARK_OF_BETRAYAL, -1, false) >= 40
						&& player.getInventory().getInventoryItemCount(STINGER, -1, false) >= 60
						&& player.getInventory().getInventoryItemCount(BASIC_CERT, -1, false) > 0)
				{
					if (player.destroyItemByItemId("Quest", MARK_OF_BETRAYAL, 40, npc, true) && player.destroyItemByItemId("Quest", STINGER, 60, npc, true)
							&& player.destroyItemByItemId("Quest", BASIC_CERT, 1, npc, true))
					{
						player.addItem("Quest", STANDART_CERT, 1, npc, true);
						return "32298-getstandart.htm";
					}
				}
			}
			return "32298-nostandart.htm";
		}
		if ("pcertif".equalsIgnoreCase(event))
		{
			if (HellboundManager.getInstance().getHellboundLevel() > 6)
			{
				if (player.getInventory().getInventoryItemCount(LIFE_FORCE, -1, false) >= 56
						&& player.getInventory().getInventoryItemCount(CONTAINED_LIFE_FORCE, -1, false) >= 14
						&& player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0)
				{
					if (player.destroyItemByItemId("Quest", LIFE_FORCE, 56, npc, true)
							&& player.destroyItemByItemId("Quest", CONTAINED_LIFE_FORCE, 14, npc, true)
							&& player.destroyItemByItemId("Quest", STANDART_CERT, 1, npc, true))
					{
						player.addItem("Quest", PREMIUM_CERT, 1, npc, true);
						player.addItem("Quest", MAP, 1, npc, true);
						return "32298-getpremium.htm";
					}
				}
			}
			return "32298-nopremium.htm";
		}

		return null;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		if (player.getQuestState(getName()) == null)
			newQuestState(player);

		switch (HellboundManager.getInstance().getHellboundLevel())
		{
			default:
				if (player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) > 0)
					return "32298-premium.htm";
			case 4:
			case 5:
			case 6:
				if (player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0)
					return "32298-standart.htm";
				if (player.getInventory().getInventoryItemCount(BASIC_CERT, -1, false) > 0)
					return "32298-basic.htm";
			case 1:
			case 2:
			case 3:
				return "32298-no.htm";
		}
	}

	public static void main(String[] args)
	{
		new Hude(-1, Hude.class.getSimpleName(), "ai/zones/hellbound");
	}
}
