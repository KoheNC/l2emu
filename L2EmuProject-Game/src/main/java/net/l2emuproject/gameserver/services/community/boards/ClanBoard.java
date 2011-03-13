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
package net.l2emuproject.gameserver.services.community.boards;

import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.community.CommunityBoard;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;

public final class ClanBoard extends CommunityBoard
{
	public ClanBoard(CommunityService service)
	{
		super(service);
	}

	@Override
	public final void parseCommand(final L2Player player, final String command)
	{
		if (command.equals("_bbsclan") || command.equals("_bbsclan;clan;0"))
			showMainPage(player, 1);
		else if (command.split(";")[1].equalsIgnoreCase("list"))
			showMainPage(player, Integer.valueOf(command.split(";")[2]));
		else if (command.split(";")[1].equalsIgnoreCase("clan"))
			notImplementedYet(player, command);
		else if (command.split(";")[1].equalsIgnoreCase("notice"))
			notImplementedYet(player, command);
		else if (command.split(";")[1].equalsIgnoreCase("management"))
			notImplementedYet(player, command);
		else if (command.split(";")[1].equalsIgnoreCase("mail"))
			notImplementedYet(player, command);
		else if (command.split(";")[1].equalsIgnoreCase("permission"))
			notImplementedYet(player, command);
	}

	@Override
	public final void parseWrite(final L2Player player, final String ar1, final String ar2, final String ar3, final String ar4, final String ar5)
	{
	}

	private final void showMainPage(final L2Player player, final int index)
	{
		final L2Clan clan = player.getClan();
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanlist.htm");

		content = content.replaceAll("%clanid%", String.valueOf(clan != null ? clan.getClanId() : 0));
		content = content.replaceAll("%clanhomename%", clan != null ? clan.getName() : "");

		final L2TextBuilder tb = L2TextBuilder.newInstance();
		int i = 0;
		for (L2Clan c : ClanTable.getInstance().getClans())
		{
			if (c == null)
				continue;
			if (i > ((index - 1) * 10 + 9))
				break;
			if (i++ >= ((index - 1) * 10))
			{
				tb.append("<img src=\"L2UI.SquareBlank\" width=\"750\" height=\"3\">");
				tb.append("<table border=0 cellspacing=0 cellpadding=0 width=755>");
				tb.append("<tr>");
				tb.append("<td FIXWIDTH=5></td>");
				tb.append("<td FIXWIDTH=200 align=center><a action=\"bypass _bbsclan;clan;").append(c.getClanId()).append("\">").append(c.getName())
						.append("</a></td>");
				tb.append("<td FIXWIDTH=200 align=center>").append(c.getLeaderName()).append("</td>");
				tb.append("<td FIXWIDTH=100 align=center>").append(c.getLevel()).append("</td>");
				tb.append("<td FIXWIDTH=200 align=center>").append(c.getMembersCount()).append("</td>");
				tb.append("<td FIXWIDTH=5></td>");
				tb.append("</tr>");
				tb.append("<tr><td height=5></td></tr>");
				tb.append("</table>");
				tb.append("<img src=\"L2UI.SquareBlank\" width=\"750\" height=\"3\">");
				tb.append("<img src=\"L2UI.SquareGray\" width=\"750\" height=\"1\">");
			}
		}
		content = content.replaceAll("%clanlist%", tb.moveToString());
		tb.clear();
		if (index == 1)
			tb.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		else
			tb.append("<td><button action=\"bypass _bbsclan;list;").append((index - 1))
					.append("\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");

		int nbp;
		nbp = ClanTable.getInstance().getClans().length / 10;
		if (nbp * 10 != ClanTable.getInstance().getClans().length)
			nbp++;
		for (i = 1; i <= nbp; i++)
		{
			if (i == index)
				tb.append("<td> ").append(i).append(" </td>");
			else
				tb.append("<td><a action=\"bypass _bbsclan;list;").append(i).append("\"> ").append(i).append(" </a></td>");
		}
		if (index == nbp)
			tb.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		else
			tb.append("<td><button action=\"bypass _bbsclan;list;").append((index + 1))
					.append("\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");

		content = content.replaceAll("%clanlistlength%", tb.moveToString());

		showHTML(player, content);
	}
}
