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
package net.l2emuproject.status.commands;

import net.l2emuproject.loginserver.manager.GameServerManager;
import net.l2emuproject.status.LoginStatusCommand;

import org.apache.commons.lang.StringUtils;

/**
 * @author NB4L1
 */
public final class Statistics extends LoginStatusCommand
{
	public Statistics()
	{
		super("displays basic server statistics", "status", "stats");
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		println("Registered server(s):");
		println("\t...count: " + GameServerManager.getInstance().getRegisteredGameServers().size());
		println("\t...ids: "
			+ StringUtils.join(GameServerManager.getInstance().getRegisteredGameServers().keySet().iterator(), ", "));
		
		// TODO add more details
	}
}
