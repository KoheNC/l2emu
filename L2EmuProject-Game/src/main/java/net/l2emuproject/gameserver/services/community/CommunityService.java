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
package net.l2emuproject.gameserver.services.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.community.boards.*;
import net.l2emuproject.gameserver.services.community.boards.admin.*;
import net.l2emuproject.gameserver.services.community.models.Forum;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class CommunityService
{
	private static final Log	_log	= LogFactory.getLog(CommunityService.class);

	private static final class SingletonHolder
	{
		private static final CommunityService	INSTANCE	= new CommunityService();
	}

	public static CommunityService getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private final FastMap<Integer, Forum>			_forumRoot;
	private int										_lastForumId	= 1;

	private final FastMap<String, CommunityBoard>	_boards;

	private CommunityService()
	{
		if (Config.ALLOW_COMMUNITY_BOARD)
		{
			_forumRoot = new FastMap<Integer, Forum>();
			_boards = new FastMap<String, CommunityBoard>();
			
			// retail boards
			_boards.put("_bbsloc", new RegionBoard(this));
			_boards.put("_bbsfriend", new FriendBoard(this));
			_boards.put("_bbsblock", new BlockBoard(this));
			_boards.put("_bbsclan", new ClanBoard(this));
			_boards.put("_bbscpost", new ClanPostBoard(this));
			_boards.put("_bbsmail", new MailBoard(this));
			_boards.put("_bbsmemo", new MemoBoard(this));
			_boards.put("_bbshome", new TopBoard(this));
			_boards.put("_bbslink", new HomePageBoard(this));
			_boards.put("_bbsgetfav", new FavoriteBoard(this));
			_boards.put("_bbserror", new ErrorBoard(this));
			
			// custom boards
			_boards.put("_bbsadmin_main", new AdminMainBoard(this));

			loadDB();
			_log.info(getClass().getSimpleName() + " : Loaded " + _boards.size() + " board(s).");
		}
		else
		{
			_forumRoot = null;
			_boards = null;
			_log.info(getClass().getSimpleName() + " : Disabled.");
		}
	}

	private void loadDB()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT forum_id, forum_owner_id FROM community_forums");
			final ResultSet result = statement.executeQuery();
			while (result.next())
			{
				final Forum forum = new Forum(Integer.parseInt(result.getString("forum_id")));

				_forumRoot.put(Integer.parseInt(result.getString("forum_owner_id")), forum);

				if (forum.getForumId() > _lastForumId)
					_lastForumId = forum.getForumId();
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Error restoring forums!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT introduction,clanId FROM community_clan_introductions");
			final ResultSet result = statement.executeQuery();
			while (result.next())
			{
				final L2Clan clan = ClanTable.getInstance().getClan(result.getInt("clanId"));
				if (clan != null)
					clan.setIntroduction(result.getString("introduction"));
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Error restoring clan introductions!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private int getNewForumId()
	{
		return ++_lastForumId;
	}

	public Forum getPlayerForum(final L2Player player)
	{
		if (_forumRoot.containsKey(player.getObjectId()))
			return _forumRoot.get(player.getObjectId());

		Forum forum = null;

		if (player != null && player.getLevel() >= Config.MIN_PLAYER_LVL_FOR_FORUM)
		{
			forum = new Forum(getNewForumId(), player.getName(), Forum.PLAYER, player.getObjectId());
			_forumRoot.put(player.getObjectId(), forum);
		}

		return forum;
	}

	public Forum getPlayerForum(final int charId)
	{
		return _forumRoot.get(charId);
	}

	public Forum getClanForum(final int clanId)
	{
		if (_forumRoot.containsKey(clanId))
			return _forumRoot.get(clanId);

		final L2Clan clan = ClanTable.getInstance().getClan(clanId);
		Forum forum = null;

		if (clan != null && clan.getLevel() >= Config.MIN_CLAN_LVL_FOR_FORUM)
		{
			forum = new Forum(getNewForumId(), clan.getName(), Forum.CLAN, clanId);
			_forumRoot.put(clanId, forum);
		}

		return forum;
	}

	public void clanNotice(final int type, final int clanId, final String notice, final boolean noticeEnabled)
	{
		final L2Clan clan = ClanTable.getInstance().getClan(clanId);

		if (clan == null)
			return;

		switch (type)
		{
			case 0:
				clan.setNotice(notice);
				clan.setNoticeEnabled(noticeEnabled);
				break;
			case 1:
				clan.setNotice("");
				clan.setNoticeEnabled(noticeEnabled);
				break;
			case 2:
				break;
		}
	}

	public void storeClanIntroduction(final int clanId, final String introduction)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con
					.prepareStatement("INSERT INTO community_clan_introductions (clanId,introduction) values (?,?) ON DUPLICATE KEY UPDATE introduction = ?");
			statement.setInt(1, clanId);
			statement.setString(2, introduction);
			statement.setString(3, introduction);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("Error while saving new Topic to db " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final void parseCommand(final L2Player player, final String command)
	{
		final String board = command.split(";")[0];
		try
		{
			if (_boards.containsKey(board))
				_boards.get(board).parseCommand(player, command);
			else
				_boards.get("_bbserror").parseCommand(player, "noBoard;" + command);
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
	}

	public final void parseWrite(final L2Player player, final String url, final String arg1, final String arg2, final String arg3, final String arg4,
			final String arg5)
	{
		try
		{
			if (_boards.containsKey(url))
				_boards.get(url).parseWrite(player, arg1, arg2, arg3, arg4, arg5);
			else
				_boards.get("_bbserror").parseCommand(player, "noBoard;" + url);
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
	}
}
