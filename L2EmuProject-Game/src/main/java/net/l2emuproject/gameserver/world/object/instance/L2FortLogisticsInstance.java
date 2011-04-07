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

import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Vice
 */
public class L2FortLogisticsInstance extends L2MerchantInstance
{
	private static final int BLOOD_OATH = 9910;
	
	public L2FortLogisticsInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		// BypassValidation Exploit plug.
		if (player.getLastFolkNPC().getObjectId() != getObjectId())
			return;
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		String par = "";
		if (st.countTokens() >= 1)
		{
			par = st.nextToken();
		}
		
		if (actualCommand.equalsIgnoreCase("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(par);
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
			showChatWindow(player, val);
		}
		else if (actualCommand.equalsIgnoreCase("rewards"))
		{
			if (player.getClan() != null && getFort().getOwnerClan() != null
				&& player.getClan() == getFort().getOwnerClan() && player.isClanLeader())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/npc_data/html/fortress/logistics-rewards.htm");
				int blood = getFort().getBloodOathReward();
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%bloodoath%", String.valueOf(blood));
				player.sendPacket(html);
				return;
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/npc_data/html/fortress/logistics-noprivs.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
		}
		else if (actualCommand.equalsIgnoreCase("blood"))
		{
			if (player.getClan() != null && getFort().getOwnerClan() != null
				&& player.getClan() == getFort().getOwnerClan() && player.isClanLeader())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				int blood = getFort().getBloodOathReward();
				if (blood > 0)
				{
					html.setFile("data/npc_data/html/fortress/logistics-blood.htm");
					player.addItem("Quest", BLOOD_OATH, blood, this, true);
					getFort().setBloodOathReward(0);
				}
				else
					html.setFile("data/npc_data/html/fortress/logistics-noblood.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				
				player.sendPacket(html);
				return;
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/npc_data/html/fortress/logistics-noprivs.htm");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public final void showChatWindow(L2Player player, int val)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		String filename;
		
		if (val == 0)
			filename = "data/npc_data/html/fortress/logistics.htm";
		else
			filename = "data/npc_data/html/fortress/logistics-" + val + ".htm";
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		if (getFort().getOwnerClan() != null)
			html.replace("%clanname%", getFort().getOwnerClan().getName());
		else
			html.replace("%clanname%", "NPC");
		
		player.sendPacket(html);
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}
