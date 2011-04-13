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
package net.l2emuproject.gameserver.handler.admincommandhandlers;

import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Intrepid
 */
public class AdminBBS implements IAdminCommandHandler
{
	private static final String[] ADMIN_BBS_PAGES =
	{
		"admin_adminmenu"
	};

	@Override
	public boolean useAdminCommand(String command, L2Player activeChar)
	{
		CommunityService.getInstance().parseCommand(activeChar, "_bbsadmin_main");
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_BBS_PAGES;
	}
}
