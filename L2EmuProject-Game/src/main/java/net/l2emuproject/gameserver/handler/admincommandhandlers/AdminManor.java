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

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.instancemanager.CastleManager;
import net.l2emuproject.gameserver.model.entity.Castle;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.services.manor.CastleManorService;
import net.l2emuproject.gameserver.services.manor.CastleManorService.CropProcure;
import net.l2emuproject.gameserver.services.manor.CastleManorService.SeedProduction;
import net.l2emuproject.gameserver.world.object.L2Player;


/**
 * Admin comand handler for Manor System
 * This class handles following admin commands:
 * - manor_info = shows info about current manor state
 * - manor_approve = approves settings for the next manor period
 * - manor_setnext = changes manor settings to the next day's
 * - manor_reset castle = resets all manor data for specified castle (or all)
 * - manor_setmaintenance = sets manor system under maintenance mode
 * - manor_save = saves all manor data into database
 * - manor_disable = disables manor system
 * 
 * @author l3x
 */
public class AdminManor implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{
			"admin_manor",
			"admin_manor_approve",
			"admin_manor_setnext",
			"admin_manor_reset",
			"admin_manor_setmaintenance",
			"admin_manor_save",
			"admin_manor_disable"					};

	@Override
	public boolean useAdminCommand(String command, L2Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		command = st.nextToken();

		if (command.equals("admin_manor"))
		{
			showMainPage(activeChar);
		}
		else if (command.equals("admin_manor_setnext"))
		{
			CastleManorService.getInstance().setNextPeriod();
			CastleManorService.getInstance().setNewManorRefresh();
			CastleManorService.getInstance().updateManorRefresh();
			activeChar.sendMessage("Manor System: set to next period");
			showMainPage(activeChar);
		}
		else if (command.equals("admin_manor_approve"))
		{
			CastleManorService.getInstance().approveNextPeriod();
			CastleManorService.getInstance().setNewPeriodApprove();
			CastleManorService.getInstance().updatePeriodApprove();
			activeChar.sendMessage("Manor System: next period approved");
			showMainPage(activeChar);
		}
		else if (command.equals("admin_manor_reset"))
		{
			int castleId = 0;
			try
			{
				castleId = Integer.parseInt(st.nextToken());
			}
			catch (Exception e)
			{
			}

			if (castleId > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(castleId);
				castle.setCropProcure(new FastList<CropProcure>(), CastleManorService.PERIOD_CURRENT);
				castle.setCropProcure(new FastList<CropProcure>(), CastleManorService.PERIOD_NEXT);
				castle.setSeedProduction(new FastList<SeedProduction>(), CastleManorService.PERIOD_CURRENT);
				castle.setSeedProduction(new FastList<SeedProduction>(), CastleManorService.PERIOD_NEXT);
				if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					castle.saveCropData();
					castle.saveSeedData();
				}
				activeChar.sendMessage("Manor data for " + castle.getName() + " was nulled");
			}
			else
			{
				for (Castle castle : CastleManager.getInstance().getCastles().values())
				{
					castle.setCropProcure(new FastList<CropProcure>(), CastleManorService.PERIOD_CURRENT);
					castle.setCropProcure(new FastList<CropProcure>(), CastleManorService.PERIOD_NEXT);
					castle.setSeedProduction(new FastList<SeedProduction>(), CastleManorService.PERIOD_CURRENT);
					castle.setSeedProduction(new FastList<SeedProduction>(), CastleManorService.PERIOD_NEXT);
					if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
					{
						castle.saveCropData();
						castle.saveSeedData();
					}
				}
				activeChar.sendMessage("Manor data was nulled");
			}
			showMainPage(activeChar);
		}
		else if (command.equals("admin_manor_setmaintenance"))
		{
			boolean mode = CastleManorService.getInstance().isUnderMaintenance();
			CastleManorService.getInstance().setUnderMaintenance(!mode);
			if (mode)
				activeChar.sendMessage("Manor System: not under maintenance");
			else
				activeChar.sendMessage("Manor System: under maintenance");
			showMainPage(activeChar);
		}
		else if (command.equals("admin_manor_save"))
		{
			CastleManorService.getInstance().save();
			activeChar.sendMessage("Manor System: all data saved");
			showMainPage(activeChar);
		}
		else if (command.equals("admin_manor_disable"))
		{
			boolean mode = CastleManorService.getInstance().isDisabled();
			CastleManorService.getInstance().setDisabled(!mode);
			if (mode)
				activeChar.sendMessage("Manor System: enabled");
			else
				activeChar.sendMessage("Manor System: disabled");
			showMainPage(activeChar);
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private String formatTime(long millis)
	{
		String s = "";
		int secs = (int) millis / 1000;
		int mins = secs / 60;
		secs -= mins * 60;
		int hours = mins / 60;
		mins -= hours * 60;

		if (hours > 0)
			s += hours + ":";
		s += mins + ":";
		s += secs;
		return s;
	}

	private void showMainPage(L2Player activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		TextBuilder replyMSG = new TextBuilder("<html><body>");

		replyMSG.append("<center><font color=\"LEVEL\"> [Manor System] </font></center><br>");
		replyMSG.append("<table width=\"100%\"><tr><td>");
		replyMSG.append("Disabled: " + (CastleManorService.getInstance().isDisabled() ? "yes" : "no") + "</td><td>");
		replyMSG.append("Under Maintenance: " + (CastleManorService.getInstance().isUnderMaintenance() ? "yes" : "no") + "</td></tr><tr><td>");
		replyMSG.append("Time to refresh: " + formatTime(CastleManorService.getInstance().getMillisToManorRefresh()) + "</td><td>");
		replyMSG.append("Time to approve: " + formatTime(CastleManorService.getInstance().getMillisToNextPeriodApprove()) + "</td></tr>");
		replyMSG.append("</table>");

		replyMSG.append("<center><table><tr><td>");
		replyMSG
				.append("<button value=\"Set Next\" action=\"bypass -h admin_manor_setnext\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td>");
		replyMSG
				.append("<button value=\"Approve Next\" action=\"bypass -h admin_manor_approve\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr><tr><td>");
		replyMSG.append("<button value=\"" + (CastleManorService.getInstance().isUnderMaintenance() ? "Set normal" : "Set mainteance")
				+ "\" action=\"bypass -h admin_manor_setmaintenance\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td>");
		replyMSG.append("<button value=\"" + (CastleManorService.getInstance().isDisabled() ? "Enable" : "Disable")
				+ "\" action=\"bypass -h admin_manor_disable\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr><tr><td>");
		replyMSG
				.append("<button value=\"Refresh\" action=\"bypass -h admin_manor\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td>");
		replyMSG
				.append("<button value=\"Back\" action=\"bypass -h admin_admin\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("</table></center>");

		replyMSG.append("<br><center>Castle Information:<table width=\"100%\">");
		replyMSG.append("<tr><td></td><td>Current Period</td><td>Next Period</td></tr>");

		for (Castle c : CastleManager.getInstance().getCastles().values())
		{
			replyMSG.append("<tr><td>" + c.getName() + "</td>" + "<td>" + c.getManorCost(CastleManorService.PERIOD_CURRENT) + "a</td>" + "<td>"
					+ c.getManorCost(CastleManorService.PERIOD_NEXT) + "a</td>" + "</tr>");
		}

		replyMSG.append("</table><br>");
		replyMSG.append("</body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
}
