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
package ai.zone.hellbound.Budenka;

import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author DS, based on theOne's work
 */
public final class Budenka extends QuestJython
{
	private static final int	BUDENKA			= 32294;
	private static final int	STANDART_CERT	= 9851;
	private static final int	PREMIUM_CERT	= 9852;

	public Budenka(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(BUDENKA);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2Player player)
	{
		if (player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) > 0)
			return "32294-premium.htm";
		if (player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0)
			return "32294-standart.htm";

		npc.showChatWindow(player);
		return null;
	}

	public static void main(String[] args)
	{
		new Budenka(-1, Budenka.class.getSimpleName(), "ai/zones/hellbound");
	}
}
