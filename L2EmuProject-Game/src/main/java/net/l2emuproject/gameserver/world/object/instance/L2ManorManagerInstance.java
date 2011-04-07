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

import net.l2emuproject.gameserver.handler.BypassHandler;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.services.manor.CastleManorService;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Player;

public class L2ManorManagerInstance extends L2MerchantInstance
{
	public L2ManorManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		// BypassValidation Exploit plug.
		if (player.getLastFolkNPC() == null || player.getLastFolkNPC().getObjectId() != getObjectId())
			return;

		if (command.startsWith("manor_menu_select"))
		{
			IBypassHandler handler = BypassHandler.getInstance().getBypassHandler("manor_menu_select");

			if (handler != null)
				handler.useBypass(command, player, this);
			else
				_log.warn(getClass().getSimpleName() + ": Unknown NPC bypass: \"" + command + "\" NpcId: " + getNpcId());
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		// Used only in parent method to return from "Territory status"  to initial screen.
		return "data/npc_data/html/manormanager/manager.htm";
	}

	@Override
	public void showChatWindow(L2Player player)
	{
		if (CastleManorService.getInstance().isDisabled())
		{
			showChatWindow(player, "data/npc_data/html/npcdefault.htm");
			return;
		}

		if (!player.isGM() && getCastle() != null && getCastle().getCastleId() > 0 && player.getClan() != null
				&& getCastle().getOwnerId() == player.getClanId() && player.isClanLeader())
			showChatWindow(player, "data/npc_data/html/manormanager/manager-lord.htm");
		else
			showChatWindow(player, "data/npc_data/html/manormanager/manager.htm");
	}
}
