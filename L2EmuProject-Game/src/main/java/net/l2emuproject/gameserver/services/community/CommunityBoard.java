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
	protected static final Log		_log		= LogFactory.getLog(CommunityBoard.class);

	private final CommunityService	_service;

	protected static final String	TOP_PATH	= "data/html/CommunityBoard/top/";

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
		final int length = html.length();
		if (length < 4090)
		{
			player.sendPacket(new PlayerShowBoard(html, "101"));
			player.sendPacket(new PlayerShowBoard(null, "102"));
			player.sendPacket(new PlayerShowBoard(null, "103"));
		}
		else if (length < 8180)
		{
			player.sendPacket(new PlayerShowBoard(html.substring(0, 4090), "101"));
			player.sendPacket(new PlayerShowBoard(html.substring(4090, length), "102"));
			player.sendPacket(new PlayerShowBoard(null, "103"));
		}
		else if (length < 12270)
		{
			player.sendPacket(new PlayerShowBoard(html.substring(0, 4090), "101"));
			player.sendPacket(new PlayerShowBoard(html.substring(4090, 8180), "102"));
			player.sendPacket(new PlayerShowBoard(html.substring(8180, length), "103"));
		}
	}

	protected final void notImplementedYet(L2Player player, String command)
	{
		if (player == null || command == null)
			return;

		showHTML(player, "<html><body><br><br><center>The command: [" + command + "] isn't implemented yet!</center><br><br></body></html>");
	}

	public abstract void parseCommand(final L2Player player, final String command);

	public abstract void parseWrite(final L2Player player, String ar1, String ar2, String ar3, String ar4, String ar5);
}
