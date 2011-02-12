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
package net.l2emuproject;

import net.l2emuproject.L2Config;
import net.l2emuproject.config.L2Properties;
import net.l2emuproject.loginserver.util.Util;

/**
 * This class containce global server configuration.<br>
 * It has static final fields initialized from configuration files.<br>
 * It's initialized at the very begin of startup, and later JIT will optimize
 * away debug/unused code.
 * 
 * @author mkizub
 */
public class Config extends L2Config
{
	static
	{
		registerConfig(new LSConfig());
		registerConfig(new NetworkConfig());
	}

	protected Config()
	{
	}

	public static void load() throws Exception
	{
		Util.printSection("Configuration");

		L2Config.loadConfigs();

		registerConfig(new AllConfig());
	}

	private static final class AllConfig extends ConfigLoader
	{
		@Override
		protected String getName()
		{
			return "all";
		}

		private boolean	_reloading	= false;

		@Override
		protected void load() throws Exception
		{
			if (_reloading)
				return;

			_reloading = true;
			try
			{
				Config.load();
			}
			finally
			{
				_reloading = false;
			}
		}
	}

	// *******************************************************************************************
	public static final String	NETWORK_FILE	= "./config/network/network.properties";
	// *******************************************************************************************
	public static String		DATABASE_DRIVER;
	public static String		DATABASE_URL;
	public static String		DATABASE_LOGIN;
	public static String		DATABASE_PASSWORD;
	public static String		LOGIN_SERVER_HOSTNAME;
	public static int			LOGIN_SERVER_PORT;
	public static String		LOGIN_HOSTNAME;
	public static int			LOGIN_PORT;
	public static boolean		IS_TELNET_ENABLED;

	// *******************************************************************************************
	private static final class NetworkConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "network/network";
		}

		@Override
		protected void loadImpl(L2Properties networkSettings)
		{
			LOGIN_SERVER_HOSTNAME = networkSettings.getProperty("LoginServerHostname", "0.0.0.0");
			LOGIN_SERVER_PORT = Integer.parseInt(networkSettings.getProperty("LoginServerPort", "2106"));
			LOGIN_HOSTNAME = networkSettings.getProperty("LoginHostname", "127.0.0.1");
			LOGIN_PORT = Integer.parseInt(networkSettings.getProperty("LoginPort", "9014"));

			DATABASE_DRIVER = networkSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = networkSettings.getProperty("URL", "jdbc:mysql://localhost/L2Emu_DB");
			DATABASE_LOGIN = networkSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = networkSettings.getProperty("Password", "");

			IS_TELNET_ENABLED = Boolean.valueOf(networkSettings.getProperty("EnableTelnet", "false"));
		}
	}

	// *******************************************************************************************
	public static final String	LOGIN_CONFIGURATIONS	= "./config/main/loginserver.properties";
	// *******************************************************************************************
	public static int			LOGIN_TRY_BEFORE_BAN;
	public static int			LOGIN_BLOCK_AFTER_BAN;
	public static boolean		SHOW_LICENCE;
	public static boolean		ACCEPT_NEW_GAMESERVER;
	public static boolean		AUTO_CREATE_ACCOUNTS;
	public static int			GM_MIN;
	public static int			IP_UPDATE_TIME;
	public static boolean		FLOOD_PROTECTION;
	public static int			FAST_CONNECTION_LIMIT;
	public static int			NORMAL_CONNECTION_TIME;
	public static int			FAST_CONNECTION_TIME;
	public static int			MAX_CONNECTION_PER_IP;
	public static boolean		SECURITY_CARD_LOGIN;
	public static String		SECURITY_CARD_ID;

	// *******************************************************************************************
	private static final class LSConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/loginserver";
		}

		@Override
		protected void loadImpl(L2Properties loginSettings)
		{
			ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(loginSettings.getProperty("AcceptNewGameServer", "True"));

			SHOW_LICENCE = Boolean.parseBoolean(loginSettings.getProperty("ShowLicence", "true"));

			AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(loginSettings.getProperty("AutoCreateAccounts", "True"));

			LOGIN_TRY_BEFORE_BAN = Integer.parseInt(loginSettings.getProperty("LoginTryBeforeBan", "10"));
			LOGIN_BLOCK_AFTER_BAN = Integer.parseInt(loginSettings.getProperty("LoginBlockAfterBan", "600"));
			GM_MIN = Integer.parseInt(loginSettings.getProperty("GMMinLevel", "100"));

			IP_UPDATE_TIME = Integer.parseInt(loginSettings.getProperty("IpUpdateTime", "0")) * 60 * 1000;

			FLOOD_PROTECTION = Boolean.parseBoolean(loginSettings.getProperty("EnableFloodProtection", "True"));
			FAST_CONNECTION_LIMIT = Integer.parseInt(loginSettings.getProperty("FastConnectionLimit", "15"));
			NORMAL_CONNECTION_TIME = Integer.parseInt(loginSettings.getProperty("NormalConnectionTime", "700"));
			FAST_CONNECTION_TIME = Integer.parseInt(loginSettings.getProperty("FastConnectionTime", "350"));
			MAX_CONNECTION_PER_IP = Integer.parseInt(loginSettings.getProperty("MaxConnectionPerIP", "50"));

			SECURITY_CARD_LOGIN = Boolean.parseBoolean(loginSettings.getProperty("UseSecurityCardToLogin", "False"));
			SECURITY_CARD_ID = loginSettings.getProperty("SecurityCardID", "l2emuproject");
		}
	}
}
