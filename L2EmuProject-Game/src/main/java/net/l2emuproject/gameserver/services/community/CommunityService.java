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

import javolution.util.FastMap;

import net.l2emuproject.gameserver.services.community.boards.BlockBoard;
import net.l2emuproject.gameserver.services.community.boards.ClanBoard;
import net.l2emuproject.gameserver.services.community.boards.ClanPostBoard;
import net.l2emuproject.gameserver.services.community.boards.ErrorBoard;
import net.l2emuproject.gameserver.services.community.boards.FriendBoard;
import net.l2emuproject.gameserver.services.community.boards.MailBoard;
import net.l2emuproject.gameserver.services.community.boards.MemoBoard;
import net.l2emuproject.gameserver.services.community.boards.RegionBoard;
import net.l2emuproject.gameserver.services.community.boards.TopBoard;
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

	private final FastMap<String, CommunityBoard>	_boards;

	private CommunityService()
	{
		_boards = new FastMap<String, CommunityBoard>();

		_boards.put("_bbsloc", new RegionBoard(this));
		_boards.put("_bbsfriend", new FriendBoard(this));
		_boards.put("_bbsblock", new BlockBoard(this));
		_boards.put("_bbsclan", new ClanBoard(this));
		_boards.put("_bbscpost", new ClanPostBoard(this));
		_boards.put("_bbsmail", new MailBoard(this));
		_boards.put("_bbsmemo", new MemoBoard(this));
		_boards.put("_bbshome", new TopBoard(this));
		_boards.put("_bbserror", new ErrorBoard(this));

		_log.info(getClass().getSimpleName() + " : Loaded " + _boards.size() + " board(s).");
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
