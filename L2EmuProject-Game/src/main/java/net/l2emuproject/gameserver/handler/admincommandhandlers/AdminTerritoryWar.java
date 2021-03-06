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
package net.l2emuproject.gameserver.handler.admincommandhandlers;

import java.util.Calendar;
import java.util.StringTokenizer;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWarManager;
import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestService;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Admin comand handler for Territory War System
 * This class handles following admin commands:
 *
 * @author Gigiikun
 */
public class AdminTerritoryWar implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{ "admin_territory_war", "admin_territory_war_time", "admin_territory_war_start", "admin_territory_war_end" };

	@Override
	public boolean useAdminCommand(String command, L2Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		command = st.nextToken();

		if (command.equals("admin_territory_war"))
		{
			showMainPage(activeChar);
		}
		else if (command.equalsIgnoreCase("admin_territory_war_time"))
		{
			String val = "";
			if (st.hasMoreTokens())
			{
				val = st.nextToken();
				Calendar newAdminTWDate = Calendar.getInstance();
				newAdminTWDate.setTimeInMillis(TerritoryWarManager.getInstance().getTWStartTimeInMillis());
				if (val.equalsIgnoreCase("day"))
					newAdminTWDate.set(Calendar.DAY_OF_WEEK, Integer.parseInt(st.nextToken()));
				else if (val.equalsIgnoreCase("hour"))
					newAdminTWDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(st.nextToken()));
				else if (val.equalsIgnoreCase("min"))
					newAdminTWDate.set(Calendar.MINUTE, Integer.parseInt(st.nextToken()));

				if (newAdminTWDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
				{
					activeChar.sendMessage("Unable to change TW Date!");
				}
				else if (newAdminTWDate.getTimeInMillis() != TerritoryWarManager.getInstance().getTWStartTimeInMillis())
				{
					Quest twQuest = QuestService.getInstance().getQuest(TerritoryWarManager.qn);
					if (twQuest != null)
						twQuest.onAdvEvent("setTWDate " + newAdminTWDate.getTimeInMillis(), null, null);
					else
						activeChar.sendMessage("Missing Territory War Quest!");
				}
			}
			showSiegeTimePage(activeChar);
		}
		else if (command.equalsIgnoreCase("admin_territory_war_start"))
		{
			Quest twQuest = QuestService.getInstance().getQuest(TerritoryWarManager.qn);
			if (twQuest != null)
				twQuest.onAdvEvent("setTWDate " + Calendar.getInstance().getTimeInMillis(), null, null);
			else
				activeChar.sendMessage("Missing Territory War Quest!");
		}
		else if (command.equalsIgnoreCase("admin_territory_war_end"))
		{
			Quest twQuest = QuestService.getInstance().getQuest(TerritoryWarManager.qn);
			if (twQuest != null)
				twQuest.onAdvEvent("setTWDate " + (Calendar.getInstance().getTimeInMillis() - Config.WARLENGTH), null, null);
			else
				activeChar.sendMessage("Missing Territory War Quest!");
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void showSiegeTimePage(L2Player activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/npc_data/html/admin/territorywartime.htm");
		adminReply.replace("%time%", TerritoryWarManager.getInstance().getTWStart().getTime().toString());
		activeChar.sendPacket(adminReply);
	}

	private void showMainPage(L2Player activeChar)
	{
		activeChar.showHTMLFile(AdminHelpPage.ADMIN_HELP_PAGE + "territorywar.htm");
	}
}
