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
package ai.zone.hellbound.Falk;

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * @author DS, based on theOne's work
 */
public final class Falk extends QuestJython
{
	private static final int	FALK			= 32297;
	private static final int	BASIC_CERT		= 9850;
	private static final int	STANDART_CERT	= 9851;
	private static final int	PREMIUM_CERT	= 9852;
	private static final int	DARION_BADGE	= 9674;

	public Falk(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(FALK);
		addStartNpc(FALK);
		addTalkId(FALK);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "32297.htm";
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getInventory().getInventoryItemCount(BASIC_CERT, -1, false) > 0 || player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) > 0
				|| player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0)
			return "32297-certs.htm";

		if (player.getInventory().getInventoryItemCount(DARION_BADGE, -1, false) >= 20)
		{
			if (player.destroyItemByItemId("Quest", DARION_BADGE, 20, npc, true))
			{
				player.addItem("Quest", BASIC_CERT, 1, npc, true);
				return "32297-ok.htm";
			}
		}
		return "32297-nobadges.htm";
	}

	public static void main(String[] args)
	{
		new Falk(-1, Falk.class.getSimpleName(), "ai/zones/hellbound");
	}
}
