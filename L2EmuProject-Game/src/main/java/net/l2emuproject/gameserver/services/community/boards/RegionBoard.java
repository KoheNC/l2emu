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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.L2GameServer;
import net.l2emuproject.gameserver.datatables.RecordTable;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.services.blocklist.BlockList;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.community.CommunityBoard;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.system.time.GameTimeController;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Jdk14Logger;

public final class RegionBoard extends CommunityBoard
{
	private static final Logger	_logChat	= ((Jdk14Logger) LogFactory.getLog("chat")).getLogger();

	private static final byte	PMSEND		= 0;
	private static final byte	PMSENT		= 1;
	private static final byte	PMCANTSEND	= 2;

	public RegionBoard(CommunityService service)
	{
		super(service);
	}

	@Override
	public final void parseCommand(final L2Player player, final String command)
	{
		if (command.equalsIgnoreCase("_bbsloc"))
			showMainPage(player);
		else if (command.startsWith("_bbsloc;playerinfo;"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			final String name = st.nextToken();

			showPlayerInfo(player, name, PMSEND);
		}
	}

	@Override
	public final void parseWrite(final L2Player player, final String ar1, final String ar2, final String ar3, final String ar4, final String ar5)
	{
		if (ar1.equals("PM"))
		{
			final L2Player target = L2World.getInstance().getPlayer(ar2);

			if (canSendMessage(player, target))
			{
				if (Config.LOG_CHAT)
				{
					final LogRecord record = new LogRecord(Level.INFO, ar3);
					record.setLoggerName("chat");
					record.setParameters(new Object[]
					{ "TELL", "[" + player.getName() + " to " + target.getName() + "]" });
					_logChat.log(record);
				}
				player.sendPacket(new CreatureSay(player.getObjectId(), SystemChatChannelId.Chat_Tell, "->" + target.getName(), ar3));
				showPlayerInfo(player, ar2, PMSENT);
			}
			else
				showPlayerInfo(player, ar2, PMCANTSEND);
		}
	}

	private final boolean canSendMessage(final L2Player player, final L2Player target)
	{
		if (player == null || target == null)
			return false;

		if (Config.JAIL_DISABLE_CHAT && target.isInJail())
		{
			player.sendMessage("Player is in jail.");
			return false;
		}
		if (target.isChatBanned())
		{
			player.sendMessage("Player is chat banned.");
			return false;
		}
		if (player.isInJail() && Config.JAIL_DISABLE_CHAT)
		{
			player.sendMessage("You cannot chat while in jail.");
			return false;
		}
		if (BlockList.isBlocked(target, player))
		{
			player.sendMessage("Your target is blocked you, so you can't send message.");
			return false;
		}

		return true;
	}

	private final void showMainPage(final L2Player player)
	{
		final L2TextBuilder tb = L2TextBuilder.newInstance();

		tb.append("<html><body><br><br>");
		showServerStartTime(tb);
		showOnlineStatus(tb);
		showOnlinePlayers(tb);
		tb.append("</body></html>");

		showHTML(player, tb.moveToString());
	}

	private final void showPlayerInfo(final L2Player player, final String name, final byte pageId)
	{
		final L2TextBuilder tb = L2TextBuilder.newInstance();
		final L2Player target = L2World.getInstance().getPlayer(name);
		final String sex = target.getAppearance().getSex() ? "Female" : "Male";
		final L2Clan clan = target.getClan();

		if (player == null || target == null)
			return;

		tb.append("<html><body><br><br>");

		tb.append("<table width=800><tr><td>Player Name: ").append(target.getName()).append("</td></tr>");
		tb.append("<tr><td>Player Level: ").append(target.getLevel()).append("</td></tr>");
		tb.append("<tr><td>Player Race: ").append(target.getRace()).append("</td></tr>");
		tb.append("<tr><td>Player Sex: ").append(sex).append("</td></tr>");
		if (clan != null)
			tb.append("<tr><td>Player Clan: ").append(clan.getName()).append("</td></tr>");
		else
			tb.append("<tr><td>Player Clan: ").append("No Clan").append("</td></tr></table>");

		switch (pageId)
		{
			case PMSEND:
				tb.append("<table width=800><tr><td><multiedit var=\"pm\" width=240 height=40><br><button value=\"Send PM\" action=\"Write _bbsloc PM ")
						.append(player.getName())
						.append(" pm pm pm\" width=110 height=15 back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\"></td></tr><tr><td><br><button value=\"Back\" action=\"bypass _bbsloc\" width=40 height=15 back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\"></td></tr></table>");
				break;
			case PMSENT:
				tb.append("<table width=800><tr><td><br>Your message has been sent to " + name + ".</td></tr></table>");
				tb.append("<table width=800><tr><td><br><button value=\"Back\" action=\"bypass _bbsloc;playerinfo;" + name
						+ "\" width=40 height=15 back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\"></td></tr></table>");
				break;
			case PMCANTSEND:
				tb.append("<table width=800><tr><td><br>You can't send message to " + name + ".</td></tr></table>");
				tb.append("<table width=800><tr><td><br><button value=\"Back\" action=\"bypass _bbsloc;playerinfo;" + name
						+ "\" width=40 height=15 back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\"></td></tr></table>");
				break;
		}

		tb.append("</body></html>");

		showHTML(player, tb.moveToString());
	}

	private final void showServerStartTime(final L2TextBuilder tb)
	{
		final SimpleDateFormat format = new SimpleDateFormat("H:mm");
		final String gameTime = GameTimeController.getInstance().getFormattedGameTime();

		tb.append("<table width=800>");
		tb.append("<tr><td>").append("Server Time: ").append(format.format(new Date())).append("</td></tr>");
		tb.append("<tr><td>").append("Game Time: ").append(gameTime).append("</td></tr>");
		tb.append("<tr><td>").append("Server Restarted: ").append(L2GameServer.getStartedTime().getTime()).append("</td></tr>");
		tb.append("</table>");
	}

	private final void showOnlineStatus(final L2TextBuilder tb)
	{
		final int record = RecordTable.getInstance().getRecord();
		final String recordDate = RecordTable.getInstance().getDate();
		final int onlinePlayers = L2World.getInstance().getAllPlayersCount();

		tb.append("<br><table width=800>");
		tb.append("<tr><td>").append("Record of Player(s) Online: ").append(record).append("</td></tr>");
		tb.append("<tr><td>").append("On Date: ").append(recordDate).append("</td></tr>");
		tb.append("<tr><td>").append("Online Player(s): ").append(onlinePlayers).append("</td></tr>");
		tb.append("</table>");
	}

	private final void showOnlinePlayers(final L2TextBuilder tb)
	{
		tb.append("<br><br><table width=750><tr><td>");

		for (L2Player player : L2World.getInstance().getAllPlayers())
		{
			if (player == null)
				continue;

			tb.append("<a action=\"bypass _bbsloc;playerinfo;").append(player.getName()).append("\">");
			tb.append(player.getName());
			tb.append("</a>&nbsp;&nbsp;");
		}
		tb.append("</td></tr></table>");
	}
}
