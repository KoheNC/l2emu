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

import java.util.StringTokenizer;

import net.l2emuproject.gameserver.services.community.CommunityBoard;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class TopBoard extends CommunityBoard
{
	private static final String[]	COMMANDS	=
												{ "_bbshome", "_bbshome;" };

	public TopBoard(CommunityService service)
	{
		super(service);
	}

	@Override
	public final void parseCommand(final L2Player player, final String command)
	{
		String file = "";

		if (command.equalsIgnoreCase(COMMANDS[0]))
			file = "index.htm";
		else if (command.startsWith(COMMANDS[1]))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			file = st.nextToken();
		}

		showHTML(player, HtmCache.getInstance().getHtm(TOP_PATH + file));
	}

	@Override
	public final void parseWrite(final L2Player player, final String ar1, final String ar2, final String ar3, final String ar4, final String ar5)
	{
	}
}
