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
package net.l2emuproject.gameserver.system.announcements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.L2GameServerPacket;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.system.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Announcements
{
	private static final Log			_log			= LogFactory.getLog(Announcements.class);

	private static final String			LOAD_QUERRY		= "SELECT announceId, announcement, dateStart, dateEnd FROM announcements";
	private static final String			SAVE_QUERRY		= "INSERT INTO announcements (announceId, announcement, dateStart, dateEnd) VALUES (?, ?, ?, ?)";
	private static final String			DELETE_QUERRY	= "DELETE FROM announcements WHERE announceId = ?";

	private final Map<Integer, String>	_announcements	= new FastMap<Integer, String>();
	private final List<AnnounceDates>	_dates			= new FastList<AnnounceDates>();
	private int							_nextId			= 0;

	private static final class SingletonHolder
	{
		private static final Announcements	INSTANCE	= new Announcements();
	}

	public static Announcements getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private Announcements()
	{
		loadAnnouncements();
		_log.info(getClass().getSimpleName() + " : Loaded " + _announcements.size() + " announcement(s).");
	}

	private final class AnnounceDates
	{
		private final Date	_startDate;
		private final Date	_endDate;

		private AnnounceDates(Date startDate, Date endDate)
		{
			_startDate = startDate;
			_endDate = endDate;
		}

		public Date getStartDate()
		{
			return _startDate;
		}

		public Date getEndDate()
		{
			return _endDate;
		}
	}

	private final boolean dateIsOk()
	{
		for (AnnounceDates dates : _dates)
		{
			final Date startDate = dates.getStartDate();
			final Date endDate = dates.getEndDate();
			final long current = System.currentTimeMillis();

			if (startDate == null && endDate == null)
				return true;

			if (current >= startDate.getTime() && current <= endDate.getTime())
				return true;
		}

		return false;
	}

	public final void showAnnouncements(final L2Player player)
	{
		if (!dateIsOk())
			return;

		for (String line : _announcements.values())
			announce(player, line.replace("%name%", player.getName()));
	}

	public final void announce(final L2Player player, final String text)
	{
		player.sendCreatureMessage(SystemChatChannelId.Chat_Announce, player.getName(), text);
	}

	public final void listAnnouncements(final L2Player player)
	{
		final String content = HtmCache.getInstance().getHtmForce("data/html/admin/announce.htm");
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(content);

		final L2TextBuilder replyMSG = L2TextBuilder.newInstance();
		replyMSG.append("<br>");
		for (int i = 0; i < _announcements.size(); i++)
		{
			replyMSG.append("<table width=260><tr><td width=220>");
			replyMSG.append(_announcements.get(i));
			replyMSG.append("</td><td width=40><button value=\"Delete\" action=\"bypass -h admin_del_announcement ");
			replyMSG.append(i);
			replyMSG.append("\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
		}
		adminReply.replace("%announces%", replyMSG.moveToString());
		player.sendPacket(adminReply);
	}

	public final void loadAnnouncements()
	{
		_announcements.clear();

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement(LOAD_QUERRY);
			final ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				final int announceId = rset.getInt("announceId");
				final String announcement = rset.getString("announcement");
				final Date dateStart = rset.getDate("dateStart");
				final Date dateEnd = rset.getDate("dateEnd");

				_announcements.put(announceId, announcement);
				_dates.add(new AnnounceDates(dateStart, dateEnd));
				_nextId = Math.max(_nextId, announceId + 1);
			}

			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final void addAnnouncement(final String announcement, final Date dateStart, final Date dateEnd)
	{
		final int announceId = _nextId++;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement(SAVE_QUERRY);

			statement.setInt(1, announceId);
			statement.setString(2, announcement);
			statement.setDate(3, (java.sql.Date) dateStart);
			statement.setDate(4, (java.sql.Date) dateEnd);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		_announcements.put(announceId, announcement);
		_dates.add(new AnnounceDates(dateStart, dateEnd));
	}

	public final void deleteAnnouncement(final int announceId)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement(DELETE_QUERRY);

			statement.setInt(1, announceId);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		_announcements.remove(announceId);
	}

	public final void announceToAll(final String text)
	{
		for (L2Player player : L2World.getInstance().getAllPlayers())
			announce(player, text);
	}

	public final void announceToAll(final L2GameServerPacket gsp)
	{
		for (L2Player player : L2World.getInstance().getAllPlayers())
			player.sendPacket(gsp);
	}

	public final void announceToAll(final SystemMessageId sm)
	{
		for (L2Player player : L2World.getInstance().getAllPlayers())
			player.sendPacket(sm);
	}

	public final void announceToInstance(final L2GameServerPacket gsp, final int instanceId)
	{
		for (L2Player player : L2World.getInstance().getAllPlayers())
			if (player.isSameInstance(instanceId))
				player.sendPacket(gsp);
	}

	// Method fo handling announcements from admin
	public final void handleAnnounce(final String command, final int lengthToTrim)
	{
		try
		{
			// Announce string to everyone on server
			String text = command.substring(lengthToTrim);
			announceToAll(text);
		}

		// No body cares!
		catch (StringIndexOutOfBoundsException e)
		{
			// empty message.. ignore
		}
	}

	/**
	 * Announce to players.<BR>
	 * <BR>
	 * 
	 * @param message
	 *            The String of the message to send to player
	 */
	public final void announceToPlayers(final String message)
	{
		// Get all players
		for (L2Player player : L2World.getInstance().getAllPlayers())
			player.sendMessage(message);
	}
}