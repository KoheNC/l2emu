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

import net.l2emuproject.gameserver.network.serverpackets.PlayerShowBoard;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class CommunityBoard
{
	protected static final Log		_log				= LogFactory.getLog(CommunityBoard.class);

	private final CommunityService	_service;

	protected static final String	TOP_PATH			= "data/html/CommunityBoard/top/";
	protected static final String	STATICFILES_PATH	= "data/html/CommunityBoard/staticfiles/";

	public static final String[]	HEADER				=
														{
			"bypass _bbshome",
			"bypass _bbsgetfav",
			"bypass _bbsloc",
			"bypass _bbsclan",
			"bypass _bbsmemo",
			"bypass _bbsmail",
			"bypass _bbsfriends",
			"bypass _bbs_add_fav"						};

	public CommunityBoard(CommunityService service)
	{
		_service = service;
	}

	public final CommunityService getCommunityService()
	{
		return _service;
	}

	protected final void showHTML(final L2Player player, final String html)
	{
		player.sendPacket(new PlayerShowBoard(html));
	}

	protected final void notImplementedYet(final L2Player player, final String command)
	{
		if (player == null || command == null)
			return;

		showHTML(player, "<html><body><br><br><center>The command: [" + command + "] isn't implemented yet!</center><br><br></body></html>");
	}

	public abstract void parseCommand(final L2Player player, final String command);

	public abstract void parseWrite(final L2Player player, String ar1, String ar2, String ar3, String ar4, String ar5);
}
