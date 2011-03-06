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
package net.l2emuproject.gameserver.handler.bypasshandlers;

import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public class TerritoryStatus implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "TerritoryStatus" };

	@Override
	public boolean useBypass(String command, L2Player activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		final L2Npc npc = (L2Npc) target;
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		{
			if (npc.getCastle().getOwnerId() > 0)
			{
				html.setFile("data/html/territorystatus.htm");
				L2Clan clan = ClanTable.getInstance().getClan(npc.getCastle().getOwnerId());
				html.replace("%clanname%", clan.getName());
				html.replace("%clanleadername%", clan.getLeaderName());
			}
			else
			{
				html.setFile("data/html/territorynoclan.htm");
			}
		}
		html.replace("%castlename%", npc.getCastle().getName());
		html.replace("%taxpercent%", "" + npc.getCastle().getTaxPercent());
		html.replace("%objectId%", String.valueOf(npc.getObjectId()));
		{
			if (npc.getCastle().getCastleId() > 6)
			{
				html.replace("%territory%", "The Kingdom of Elmore");
			}
			else
			{
				html.replace("%territory%", "The Kingdom of Aden");
			}
		}
		activeChar.sendPacket(html);
		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}