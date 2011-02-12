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

import net.l2emuproject.L2Config;
import net.l2emuproject.status.GameStatusCommand;

public final class ReloadConfig extends GameStatusCommand
{
	public ReloadConfig()
	{
		super("", "reload_config");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "file";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		try
		{
			println(L2Config.loadConfig(params));
		}
		catch (Exception e)
		{
			println("Usage:  reload_config <" + L2Config.getLoaderNames() + ">");
		}
	}
}
