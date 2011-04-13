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
package net.l2emuproject.gameserver.services.community.boards.admin;

import net.l2emuproject.gameserver.services.community.CommunityBoard;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Intrepid
 */
public class AdminMainBoard extends CommunityBoard
{
	public AdminMainBoard(CommunityService service)
	{
		super(service);
	}

	@Override
	public void parseCommand(L2Player player, String command)
	{
		showHTML(player, HtmCache.getInstance().getHtm(ADMINFILES_PATH + "homepage.htm"));
	}

	@Override
	public void parseWrite(L2Player player, String ar1, String ar2, String ar3, String ar4, String ar5)
	{
		
	}
}
