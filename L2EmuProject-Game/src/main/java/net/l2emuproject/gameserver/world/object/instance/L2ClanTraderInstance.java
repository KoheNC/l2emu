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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class L2ClanTraderInstance extends L2Npc
{
	public L2ClanTraderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);

		if (command.equalsIgnoreCase("crp"))
		{
			if (player.getClan().getLevel() > 4)
				html.setFile("data/npc_data/html/clantrader/" + getNpcId() + "-2.htm");
			else
				html.setFile("data/npc_data/html/clantrader/" + getNpcId() + "-1.htm");

			sendHtmlMessage(player, html);
		}
		else if (command.startsWith("exchange"))
		{
			int itemId = Integer.parseInt(command.substring(9).trim());

			int reputation = 0;
			int itemCount = 0;

			L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
			long playerItemCount = item == null ? 0 : item.getCount();

			switch (itemId)
			{
				case 9911:
					reputation = Config.BLOODALLIANCE_POINTS;
					itemCount = 1;
					break;
				case 9910:
					reputation = Config.BLOODOATH_POINTS;
					itemCount = 10;
					break;
				case 9912:
					reputation = Config.KNIGHTSEPAULETTE_POINTS;
					itemCount = 100;
					break;
			}

			if (playerItemCount >= itemCount)
			{
				player.destroyItemByItemId("exchange", itemId, itemCount, player, true);

				player.getClan().setReputationScore(player.getClan().getReputationScore() + reputation, true);
				player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));

				player.sendMessage("Your clan has added " + reputation + " points to its clan reputation score.");
				/* TODO: fix the system message, I cant add the number to the system message
				SystemMessage sm =  new SystemMessage(SystemMessageId.CLAN_ADDED_S1S_POINTS_TO_REPUTATION_SCORE);
				sm.addItemNumber(reputation);
				player.sendPacket(sm);*/

				html.setFile("data/npc_data/html/clantrader/" + getNpcId() + "-ExchangeSuccess.htm");
			}
			else
				html.setFile("data/npc_data/html/clantrader/" + getNpcId() + "-ExchangeFailed.htm");

			sendHtmlMessage(player, html);
		}

		super.onBypassFeedback(player, command);
	}

	private void sendHtmlMessage(L2Player player, NpcHtmlMessage html)
	{
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	@Override
	public final void showChatWindow(L2Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/npc_data/html/clantrader/" + getNpcId() + "-no.htm";

		if (player.isClanLeader())
			filename = "data/npc_data/html/clantrader/" + getNpcId() + ".htm";

		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if (val == 0) pom = "" + npcId;
		else pom = npcId + "-" + val;

		return "data/npc_data/html/clantrader/" + pom + ".htm";
	}
}
