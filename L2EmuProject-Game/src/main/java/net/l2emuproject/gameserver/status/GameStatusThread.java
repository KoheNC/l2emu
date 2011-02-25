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
package net.l2emuproject.gameserver.status;

import java.io.IOException;
import java.net.Socket;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.datatables.CharNameTable.ICharacterInfo;
import net.l2emuproject.gameserver.status.commands.Abort;
import net.l2emuproject.gameserver.status.commands.Announce;
import net.l2emuproject.gameserver.status.commands.Clean;
import net.l2emuproject.gameserver.status.commands.Clear;
import net.l2emuproject.gameserver.status.commands.Decay;
import net.l2emuproject.gameserver.status.commands.Enchant;
import net.l2emuproject.gameserver.status.commands.GMChat;
import net.l2emuproject.gameserver.status.commands.GMList;
import net.l2emuproject.gameserver.status.commands.GameStat;
import net.l2emuproject.gameserver.status.commands.Give;
import net.l2emuproject.gameserver.status.commands.Halt;
import net.l2emuproject.gameserver.status.commands.IP;
import net.l2emuproject.gameserver.status.commands.Jail;
import net.l2emuproject.gameserver.status.commands.Kick;
import net.l2emuproject.gameserver.status.commands.Msg;
import net.l2emuproject.gameserver.status.commands.Performance;
import net.l2emuproject.gameserver.status.commands.Purge;
import net.l2emuproject.gameserver.status.commands.Reload;
import net.l2emuproject.gameserver.status.commands.ReloadConfig;
import net.l2emuproject.gameserver.status.commands.Restart;
import net.l2emuproject.gameserver.status.commands.ShutdownCommand;
import net.l2emuproject.gameserver.status.commands.Statistics;
import net.l2emuproject.gameserver.status.commands.Unjail;
import net.l2emuproject.status.StatusThread;

//TODO: DynamicExtension related commands were dropped, add if necessary
public final class GameStatusThread extends StatusThread
{
	private final String _password;
	private String _gm;
	
	public GameStatusThread(GameStatusServer server, Socket socket, String password) throws IOException
	{
		super(server, socket);
		
		_password = password;
		
		register(new Abort());
		register(new Announce());
		register(new Clean());
		register(new Clear());
		register(new Decay());
		register(new Enchant());
		register(new GMChat());
		register(new GMList());
		register(new GameStat());
		register(new Give());
		register(new Halt());
		register(new IP());
		register(new Jail());
		register(new Kick());
		register(new Msg());
		register(new Performance());
		register(new Purge());
		register(new Reload());
		register(new ReloadConfig());
		register(new Restart());
		register(new ShutdownCommand());
		register(new Statistics());
		register(new Unjail());
	}
	
	public String getGM()
	{
		return _gm;
	}
	
	@Override
	protected boolean login() throws IOException
	{
		print("Password: ");
		final String password = readLine();
		
		if (password == null || !password.equals(_password))
		{
			println("Incorrect password!");
			return false;
		}
		
		if (Config.ALT_TELNET)
		{
			print("GM name: ");
			_gm = readLine();
			
			final ICharacterInfo info = CharNameTable.getInstance().getICharacterInfoByName(_gm);
			
			if (info == null || info.getAccessLevel() < 100)
			{
				println("Incorrect GM name!");
				return false;
			}
			else
			{
				println("Welcome, " + _gm + "!");
			}
		}
		else
		{
			println("Welcome!");
		}
		
		return true;
	}
}
