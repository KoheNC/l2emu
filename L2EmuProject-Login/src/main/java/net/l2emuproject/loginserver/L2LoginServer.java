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
package net.l2emuproject.loginserver;

import java.net.InetAddress;

import net.l2emuproject.Config;
import net.l2emuproject.L2Registry;
import net.l2emuproject.loginserver.manager.BanManager;
import net.l2emuproject.loginserver.manager.GameServerManager;
import net.l2emuproject.loginserver.manager.LoginManager;
import net.l2emuproject.loginserver.network.L2LoginSelectorThread;
import net.l2emuproject.loginserver.status.LoginStatusServer;
import net.l2emuproject.loginserver.system.util.Util;
import net.l2emuproject.loginserver.thread.GameServerListener;

public final class L2LoginServer extends Config
{
	public static void main(String[] args) throws Throwable
	{
		long serverLoadStart = System.currentTimeMillis();
		
		// Initialize config
		// ------------------
		Config.load();
		
		System.setProperty("net.l2emuproject.db.driverclass", Config.DATABASE_DRIVER);
		System.setProperty("net.l2emuproject.db.urldb", Config.DATABASE_URL);
		System.setProperty("net.l2emuproject.db.user", Config.DATABASE_LOGIN);
		System.setProperty("net.l2emuproject.db.password", Config.DATABASE_PASSWORD);
		
		// Initialize Application context (registry of beans)
		// ---------------------------------------------------
		Util.printSection("Registry");
		L2Registry.loadRegistry(new String[] { "spring.xml" });
		
		// o Initialize LoginManager
		// -------------------------
		Util.printSection("LoginManager");
		LoginManager.getInstance();
		
		// o Initialize GameServer Manager
		// ------------------------------
		Util.printSection("GameServerManager");
		GameServerManager.getInstance();
		
		// o Initialize ban list
		// ----------------------
		Util.printSection("BanManager");
		BanManager.getInstance();
		
		// o Initialize SelectorThread
		// ----------------------------
		Util.printSection("ServerThreads");
		L2LoginSelectorThread.getInstance();
		
		// o Initialize GS listener
		// ----------------------------
		GameServerListener.getInstance();
		
		_log.info("Listening for GameServers on " + Config.LOGIN_HOSTNAME + ":" + Config.LOGIN_PORT);
		
		// o Start status telnet server
		// --------------------------
		Util.printSection("Telnet");
		if (Config.IS_TELNET_ENABLED)
			LoginStatusServer.initInstance();
		else
			_log.info("Telnet server is currently disabled.");
		
		System.gc();
		System.runFinalization();
		
		// o Start the server
		// ------------------
		
		L2LoginSelectorThread.getInstance().openServerSocket(InetAddress.getByName(Config.LOGIN_SERVER_HOSTNAME), Config.LOGIN_SERVER_PORT);
		L2LoginSelectorThread.getInstance().start();
		
		Util.printSection("LoginServerLog");
		_log.info("Total Boot Time: " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");
		_log.info("Login Server ready on " + Config.LOGIN_SERVER_HOSTNAME + ":" + Config.LOGIN_SERVER_PORT);
	}
}
