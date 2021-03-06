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
package net.l2emuproject.loginserver.status;

import java.io.IOException;
import java.net.Socket;

import net.l2emuproject.status.StatusServer;
import net.l2emuproject.status.StatusThread;

public final class LoginStatusServer extends StatusServer
{
	private static LoginStatusServer	_instance;

	public static void initInstance() throws IOException
	{
		if (_instance == null)
			_instance = new LoginStatusServer();
	}

	public static void tryBroadcast(String message)
	{
		if (_instance != null)
			_instance.broadcast(message);
	}

	private LoginStatusServer() throws IOException
	{
	}

	@Override
	protected StatusThread newStatusThread(Socket socket) throws IOException
	{
		return new LoginStatusThread(this, socket);
	}
}
