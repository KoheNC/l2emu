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

import net.l2emuproject.L2Config;
import net.l2emuproject.config.L2Properties;
import net.l2emuproject.status.StatusServer;
import net.l2emuproject.status.StatusThread;

public final class GameStatusServer extends StatusServer
{
	private static GameStatusServer _instance;
	
	public static void initInstance() throws IOException
	{
		if (_instance == null)
			_instance = new GameStatusServer();
	}
	
	public static void tryBroadcast(String message)
	{
		if (_instance != null)
			_instance.broadcast(message);
	}
	
	private final String _password;
	
	private GameStatusServer() throws IOException
	{
		final L2Properties telnetSettings = new L2Properties(L2Config.NETWORK_FILE);
		
		final String password = telnetSettings.getProperty("StatusPW");
		
		if (password != null && !password.isEmpty())
			_password = password;
		else
		{
			_password = generateRandomPassword(10);
			_log.warn("Server's Telnet Function Has No Password Defined!");
			_log.warn("A Password Has Been Automaticly Created!");
			_log.warn("Password Has Been Set To: " + _password);
		}
	}
	
	@Override
	protected StatusThread newStatusThread(Socket socket) throws IOException
	{
		return new GameStatusThread(this, socket, _password);
	}
}
