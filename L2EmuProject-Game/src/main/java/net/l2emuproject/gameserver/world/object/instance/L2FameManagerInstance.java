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

import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.UserInfo;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Reputation score manager
 * @author Kerberos
 */
public class L2FameManagerInstance extends L2Npc
{
	public L2FameManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		if (actualCommand.equalsIgnoreCase("PK_Count"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			if (player.getFame() >= 5000 && player.getClassId().level() >= 2 && player.getClan() != null && player.getClan().getLevel() >= 5)
			{
				if (player.getPkKills() > 0)
				{
					player.setFame(player.getFame()-5000);
					player.setPkKills(player.getPkKills()-1);
					player.sendPacket(new UserInfo(player));
					html.setFile("data/npc_data/html/famemanager/"+getNpcId()+"-3.htm");
				}
				else
				{
					html.setFile("data/npc_data/html/famemanager/"+getNpcId()+"-4.htm");
				}
			}
			else
			{
				html.setFile("data/npc_data/html/famemanager/"+getNpcId()+"-lowfame.htm");
			}
			sendHtmlMessage(player, html);
		}
		else if (actualCommand.equalsIgnoreCase("CRP"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			if (player.getFame() >= 1000 && player.getClassId().level() >= 2 && player.getClan() != null && player.getClan().getLevel() >= 5)
			{
				player.setFame(player.getFame() - 1000);
				player.getClan().setReputationScore(player.getClan().getReputationScore() + 50, true);
				player.sendPacket(SystemMessageId.ACQUIRED_50_CLAN_FAME_POINTS);
				html.setFile("data/npc_data/html/famemanager/"+getNpcId()+"-5.htm");
			}
			else
			{
				html.setFile("data/npc_data/html/famemanager/"+getNpcId()+"-lowfame.htm");
			}
			sendHtmlMessage(player, html);
		}
		else
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
		String filename = "data/npc_data/html/famemanager/"+getNpcId()+"-lowfame.htm";
		
		if (player.getFame() > 0)
			filename = "data/npc_data/html/famemanager/"+getNpcId()+".htm";
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}
