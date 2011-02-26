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
package net.l2emuproject.gameserver.world.object.instance;

import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWarManager;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * NPC that gives information about territory wars
 * @author  GodKratos
 */
public class L2MercenaryManagerInstance extends L2Npc
{
	// private int[] TW_BADGE_IDS = { 13757, 13758, 13759, 13760, 13761, 13762, 13763, 13764, 13765 };
	/**
	 * @param objectId
	 * @param template
	 */
	public L2MercenaryManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(L2Player player)
	{
		if (player.getLevel() < 40 || player.getClassId().level() < 2)
			super.showChatWindow(player, 2);
		else if (TerritoryWarManager.getInstance().isTWInProgress())
			super.showChatWindow(player, 10);
		else
			super.showChatWindow(player, 0);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if (val == 0)
			pom = "data/html/mercmanager/" + npcId + ".htm";
		else
			pom = "data/html/mercmanager/" + npcId + "-" + val + ".htm";

		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return pom;
	}
}
