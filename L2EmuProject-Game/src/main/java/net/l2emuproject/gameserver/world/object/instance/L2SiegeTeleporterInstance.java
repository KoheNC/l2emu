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

import java.util.StringTokenizer;

import net.l2emuproject.gameserver.datatables.TeleportLocationTable;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.system.restriction.AvailableRestriction;
import net.l2emuproject.gameserver.system.restriction.ObjectRestrictions;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.mapregion.L2TeleportLocation;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Represents a hireable mercenary teleporter, that can be manually positioned.
 * @author savormix
 */
public final class L2SiegeTeleporterInstance extends L2Npc
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2SiegeTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		// IDK if needed, must test
		if (ObjectRestrictions.getInstance().checkRestriction(player, AvailableRestriction.PlayerTeleport))
		{
			player.sendMessage("You cannot teleport due to a restriction.");
			return;
		}
		if (getCastle() == null || !getCastle().getSiege().getIsInProgress() || player.getClanId() != getCastle().getOwnerId())
		{
			showChatWindow(player);
			return;
		}
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		if (actualCommand.equals("goto"))
		{
			if (st.countTokens() <= 0)
			{
				showChatWindow(player);
				return;
			}
			int where = -1;
			try
			{
				where = Integer.parseInt(st.nextToken());
			}
			catch (NumberFormatException e)
			{
			}
			if (where == -1 || player.isAlikeDead())
				return;
			L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(where);
			player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
		}
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/siege_teleporter/" + npcId + ".htm";
	}

	@Override
	public void showChatWindow(L2Player player, int val)
	{
		String filename = "data/html/teleporter/castleteleporter-no.htm";
		if (getCastle() != null && getCastle().getSiege().getIsInProgress() && player.getClanId() == getCastle().getOwnerId())
			filename = getHtmlPath(getNpcId(), val);

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}
