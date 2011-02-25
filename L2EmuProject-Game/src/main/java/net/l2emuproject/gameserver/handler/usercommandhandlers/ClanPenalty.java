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
package net.l2emuproject.gameserver.handler.usercommandhandlers;

import java.text.SimpleDateFormat;

import net.l2emuproject.gameserver.handler.IUserCommandHandler;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;


/**
 * Support for clan penalty user command.
 * @author Tempy
 */
public class ClanPenalty implements IUserCommandHandler
{
	private static final int[]	COMMAND_IDS	=
											{ 100 };

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.l2emuproject.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useUserCommand(int id, L2Player activeChar)
	{
		boolean penalty = false;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		L2TextBuilder htmlContent = L2TextBuilder.newInstance("<html><body>");
		htmlContent.append("<center><table width=270 border=0 bgcolor=111111>");
		htmlContent.append("<tr><td width=170>Penalty</td>");
		htmlContent.append("<td width=100 align=center>Expiration Date</td></tr>");
		htmlContent.append("</table><table width=270 border=0><tr>");

		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			htmlContent.append("<td width=170>Unable to join a clan.</td>");
			htmlContent.append("<td width=100 align=center>"+format.format(activeChar.getClanJoinExpiryTime())+"</td>");
			penalty = true;
		}
		if (activeChar.getClanCreateExpiryTime() > System.currentTimeMillis())
		{
			htmlContent.append("<td width=170>Unable to create a clan.</td>");
			htmlContent.append("<td width=100 align=center>"+format.format(activeChar.getClanCreateExpiryTime())+"</td>");
			penalty = true;
		}
		if (activeChar.getClan() != null && activeChar.getClan().getCharPenaltyExpiryTime() > System.currentTimeMillis())
		{
			htmlContent.append("<td width=170>Unable to invite a clan member.</td>");
			htmlContent.append("<td width=100 align=center>");
			htmlContent.append(format.format(activeChar.getClan().getCharPenaltyExpiryTime()));
			htmlContent.append("</td>");
			penalty = true;
		}
	 	 		 
		if (!penalty)
		{
			htmlContent.append("<td width=170>No penalty is imposed.</td>");
			htmlContent.append("<td width=100 align=center> </td>");
		}
		
		htmlContent.append("</tr></table><img src=\"L2UI.SquareWhite\" width=270 height=1>");
		htmlContent.append("</center></body></html>");

		NpcHtmlMessage penaltyHtml = new NpcHtmlMessage(0);
		penaltyHtml.setHtml(htmlContent.moveToString());
		activeChar.sendPacket(penaltyHtml);

		return true;
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
