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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import net.l2emuproject.L2DatabaseFactory;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.status.GameStatusCommand;

public final class Unjail extends GameStatusCommand
{
	public Unjail()
	{
		super("", "unjail");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "player";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		try
		{
			String name = params;
			L2PcInstance playerObj = L2World.getInstance().getPlayer(name);
			
			if (playerObj != null)
			{
				playerObj.stopJailTask(false);
				playerObj.setInJail(false, 0);
				println("Character " + name + " removed from jail");
			}
			else
				unjailOfflinePlayer(name);
		}
		catch (NoSuchElementException nsee)
		{
			println("Specify a character name.");
		}
		catch (Exception e)
		{
			if (_log.isDebugEnabled())
				_log.debug(e.getMessage(), e);
		}
	}
	
	private void unjailOfflinePlayer(String name)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			
			PreparedStatement statement = con
					.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?");
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, name);
			
			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();
			
			if (count == 0)
				println("Character not found!");
			else
				println("Character " + name + " set free.");
		}
		catch (SQLException se)
		{
			println("SQLException while jailing player");
			if (_log.isDebugEnabled())
				_log.warn("SQLException while jailing player", se);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}