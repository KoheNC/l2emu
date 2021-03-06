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
package net.l2emuproject.loginserver.manager;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import javax.crypto.Cipher;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.L2Registry;
import net.l2emuproject.loginserver.beans.Accounts;
import net.l2emuproject.loginserver.beans.FailedLoginAttempt;
import net.l2emuproject.loginserver.beans.GameServerInfo;
import net.l2emuproject.loginserver.beans.SessionKey;
import net.l2emuproject.loginserver.network.L2LoginClient;
import net.l2emuproject.loginserver.services.AccountsServices;
import net.l2emuproject.loginserver.services.exception.AccountBannedException;
import net.l2emuproject.loginserver.services.exception.AccountModificationException;
import net.l2emuproject.loginserver.services.exception.AccountWrongPasswordException;
import net.l2emuproject.loginserver.services.exception.HackingException;
import net.l2emuproject.loginserver.services.exception.IPRestrictedException;
import net.l2emuproject.loginserver.services.exception.MaintenanceException;
import net.l2emuproject.loginserver.services.exception.MaturityException;
import net.l2emuproject.loginserver.status.LoginStatusServer;
import net.l2emuproject.loginserver.thread.GameServerThread;
import net.l2emuproject.network.status.ServerStatus;
import net.l2emuproject.tools.codec.Base64;
import net.l2emuproject.tools.math.ScrambledKeyPair;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles login on loginserver.
 * It store connection for each account.
 * 
 * The ClientThread use LoginManager to :
 *  - store his connection identifier
 *  - retrieve basic information
 *  - delog an account
 */
public class LoginManager
{
	private static final Log	_log			= LogFactory.getLog(LoginManager.class);
	private static final Log	_logLogin		= LogFactory.getLog("login");
	private static final Log	_logLoginTries	= LogFactory.getLog("login.try");
	private static final Log	_logLoginFailed	= LogFactory.getLog("login.failed");

	private static final class SingletonHolder
	{
		private static final LoginManager	INSTANCE	= new LoginManager();
	}

	public static LoginManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	/** Authed Clients on LoginServer*/
	protected Map<String, L2LoginClient>			_loginServerClients	= new FastMap<String, L2LoginClient>().shared();

	/** Keep trace of login attempt for an inetadress*/
	private Map<InetAddress, FailedLoginAttempt>	_hackProtection;

	private ScrambledKeyPair[]						_keyPairs;

	protected byte[][]								_blowfishKeys;

	private static final int						BLOWFISH_KEYS		= 20;

	private AccountsServices						_service			= null;

	public static enum AuthLoginResult
	{
		INVALID_PASSWORD, ACCOUNT_BANNED, ALREADY_ON_LS, ALREADY_ON_GS, AUTH_SUCCESS, SYSTEM_ERROR
	}

	private FastList<L2LoginClient>	_connections;

	/**
	 * Private constructor to avoid direct instantiation.
	 * Initialize a key generator.
	 */
	private LoginManager()
	{
		try
		{
			_log.info("LoginManager: initializing.");

			_hackProtection = new FastMap<InetAddress, FailedLoginAttempt>();

			_keyPairs = new ScrambledKeyPair[10];

			_service = (AccountsServices) L2Registry.getBean("AccountsServices");

			_connections = new FastList<L2LoginClient>();

			KeyPairGenerator keygen = null;

			try
			{
				keygen = KeyPairGenerator.getInstance("RSA");
				final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
				keygen.initialize(spec);
			}
			catch (final GeneralSecurityException e)
			{
				_log.fatal("Error in RSA setup:", e);
				_log.info("Server shutting down now");
				System.exit(1);
				return;
			}

			//generate the initial set of keys
			for (int i = 0; i < 10; i++)
				_keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
			_log.info("LoginManager: Cached 10 KeyPairs for RSA communication");

			testCipher((RSAPrivateKey) _keyPairs[0].getPair().getPrivate());

			// Store keys for blowfish communication
			generateBlowFishKeys();
		}
		catch (final GeneralSecurityException e)
		{
			_log.fatal("FATAL: Failed initializing LoginManager. Reason: " + e.getMessage(), e);
			System.exit(1);
		}

	}

	/**
	 * This is mostly to force the initialization of the Crypto Implementation, avoiding it being done on runtime when its first needed.<BR>
	 * In short it avoids the worst-case execution time on runtime by doing it on loading.
	 * @param key Any private RSA Key just for testing purposes.
	 * @throws GeneralSecurityException if a underlying exception was thrown by the Cipher
	 */
	private void testCipher(RSAPrivateKey key) throws GeneralSecurityException
	{
		// avoid worst-case execution, KenM
		final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
		rsaCipher.init(Cipher.DECRYPT_MODE, key);
	}

	private void generateBlowFishKeys()
	{
		_blowfishKeys = new byte[BLOWFISH_KEYS][16];

		for (int i = 0; i < BLOWFISH_KEYS; i++)
			for (int j = 0; j < _blowfishKeys[i].length; j++)
				_blowfishKeys[i][j] = (byte) (Rnd.nextInt(255) + 1);
		_log.info("Stored " + _blowfishKeys.length + " keys for Blowfish communication");
	}

	/**
	 * @return Returns a random key
	 */
	public byte[] getBlowfishKey()
	{
		return _blowfishKeys[(int) (Math.random() * BLOWFISH_KEYS)];
	}

	/**
	 * 
	 * @param account
	 * @param client
	 * @return a SessionKey
	 */
	public SessionKey assignSessionKeyToLogin(String account, L2LoginClient client)
	{
		SessionKey key;

		key = new SessionKey(Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE));
		_loginServerClients.put(account, client);
		return key;
	}

	public void removeAuthedLoginClient(String account)
	{
		_loginServerClients.remove(account);
	}

	public boolean isAccountInLoginServer(String account)
	{
		return _loginServerClients.containsKey(account);
	}

	public SessionKey assignSessionKeyToClient(String account, L2LoginClient client)
	{
		SessionKey key;

		key = new SessionKey(Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE), Rnd.nextInt(Integer.MAX_VALUE));
		_loginServerClients.put(account, client);
		return key;
	}

	public GameServerInfo getAccountOnGameServer(String account)
	{
		final Collection<GameServerInfo> serverList = GameServerManager.getInstance().getRegisteredGameServers().values();
		for (final GameServerInfo gsi : serverList)
		{
			final GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
				return gsi;
		}
		return null;
	}

	public boolean isAccountInAnyGameServer(String account)
	{
		final Collection<GameServerInfo> serverList = GameServerManager.getInstance().getRegisteredGameServers().values();
		for (final GameServerInfo gsi : serverList)
		{
			final GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param account
	 * @param password
	 * @param client
	 * @return true if validation succeed or false if we have technical problems
	 * @throws HackingException if we detect a hacking attempt
	 * @throws AccountBannedException if the use was banned
	 * @throws AccountWrongPasswordException if the password was wrong
	 */
	public AuthLoginResult tryAuthLogin(String account, String password, L2LoginClient client) throws AccountBannedException, AccountWrongPasswordException,
			IPRestrictedException
	{
		AuthLoginResult ret = AuthLoginResult.INVALID_PASSWORD;

		try
		{
			// check auth
			if (loginValid(account, password, client))
			{
				// login was successful, verify presence on Gameservers
				ret = AuthLoginResult.ALREADY_ON_GS;
				if (!isAccountInAnyGameServer(account))
				{
					// account isnt on any GS, verify LS itself
					ret = AuthLoginResult.ALREADY_ON_LS;
					// don't allow 2 simultaneous login
					synchronized (_loginServerClients)
					{
						if (!_loginServerClients.containsKey(account))
						{
							_loginServerClients.put(account, client);
							ret = AuthLoginResult.AUTH_SUCCESS;
						}
					}
					final Accounts acc = _service.getAccountById(account);
					// keep access level in the L2LoginClient
					client.setAccessLevel(acc.getAccessLevel());
					// keep last server choice
					client.setLastServerId(acc.getLastServerId());
					client.setAge(acc.getBirthYear(), acc.getBirthMonth(), acc.getBirthDay());
				}
			}
		}
		catch (final NoSuchAlgorithmException e)
		{
			_log.error("could not check password:", e);
			ret = AuthLoginResult.SYSTEM_ERROR;
		}
		catch (final UnsupportedEncodingException e)
		{
			_log.error("could not check password:", e);
			ret = AuthLoginResult.SYSTEM_ERROR;
		}
		catch (final AccountModificationException e)
		{
			_log.warn("could not check password:", e);
			ret = AuthLoginResult.SYSTEM_ERROR;
		}
		return ret;
	}

	public L2LoginClient getAuthedClient(String account)
	{
		return _loginServerClients.get(account);
	}

	public SessionKey getKeyForAccount(String account)
	{
		final L2LoginClient client = _loginServerClients.get(account);
		if (client != null)
			return client.getSessionKey();
		return null;
	}

	public String getHostForAccount(String account)
	{
		final L2LoginClient client = getAuthedClient(account);

		return client != null ? client.getIp() : "-1";
	}

	/**
	 * Login is possible if number of player < max player for this GS
	 * and the status of the GS != STATUS_GM_ONLY
	 * All those conditions are not applied if the player is a GM
	 * @return
	 */
	public boolean isLoginPossible(int age, int access, int serverId) throws MaintenanceException, MaturityException
	{
		final GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServerById(serverId);
		if (gsi != null && gsi.isAuthed())
		{
			if (gsi.getStatus() == ServerStatus.STATUS_GM_ONLY && access < Config.GM_MIN)
				throw MaintenanceException.MAINTENANCE;
			//Some accounts, like GM ones, can always connect
			if (age < gsi.getAgeLimitation())
				throw new MaturityException(age, gsi.getAgeLimitation());
			return gsi.getCurrentPlayerCount() < gsi.getMaxPlayers() || access >= Config.GM_MIN;
		}
		else
			throw MaintenanceException.MAINTENANCE;
	}

	/**
	 * 
	 * @param ServerID
	 * @return online player count for a server
	 */
	public int getOnlinePlayerCount(int serverId)
	{
		final GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServerById(serverId);
		if (gsi != null && gsi.isAuthed())
			return gsi.getCurrentPlayerCount();
		return 0;
	}

	/***
	 * 
	 * @param ServerID
	 * @return max allowed online player for a server
	 */
	public int getMaxAllowedOnlinePlayers(int id)
	{
		final GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServerById(id);
		if (gsi != null)
			return gsi.getMaxPlayers();
		return 0;
	}

	/**
	 * 
	 * @param user
	 * @param banLevel
	 */
	public void setAccountAccessLevel(String account, int banLevel)
	{
		try
		{
			_service.changeAccountLevel(account, banLevel);
		}
		catch (final AccountModificationException e)
		{
			_log.error("Could not set accessLevel for user: " + account, e);
		}
	}

	/**
	 * 
	 * @param user
	 * @param lastServerId
	 */
	public void setAccountLastServerId(String account, int lastServerId)
	{
		try
		{
			final Accounts acc = _service.getAccountById(account);
			acc.setLastServerId(lastServerId);
			_service.addOrUpdateAccount(acc);
		}
		catch (final AccountModificationException e)
		{
			_log.error("Could not set last server for user: " + account, e);
		}
	}

	/**
	 * 
	 * @param user
	 * @return true if a user is a GM account
	 */
	public boolean isGM(Accounts acc)
	{
		if (acc != null)
			return acc.getAccessLevel() >= Config.GM_MIN;
		else
			return false;
	}

	/**
	 * 
	 * @param user
	 * @return account if exist, null if not
	 */
	public Accounts getAccount(String user)
	{
		return _service.getAccountById(user);
	}

	/**
	 * <p>This method returns one of the 10 {@link ScrambledKeyPair}.</p>
	 * <p>One of them the renewed asynchronously using a {@link UpdateKeyPairTask} if necessary.</p>
	 * @return a scrambled keypair
	 */
	public ScrambledKeyPair getScrambledRSAKeyPair()
	{
		return _keyPairs[Rnd.nextInt(10)];
	}

	/**
	 * user name is not case sensitive any more
	 * @param user
	 * @param password
	 * @param address
	 * @return true if all operations succeed
	 * @throws NoSuchAlgorithmException if SHA is not supported
	 * @throws UnsupportedEncodingException if UTF-8 is not supported
	 * @throws AccountModificationException  if we were unable to modify the account
	 * @throws AccountBannedException  if account is banned
	 * @throws AccountWrongPasswordException if the password is wrong
	 */
	public boolean loginValid(String user, String password, L2LoginClient client) throws NoSuchAlgorithmException, UnsupportedEncodingException,
			AccountModificationException, AccountBannedException, AccountWrongPasswordException, IPRestrictedException
	{
		final InetAddress address = client.getInetAddress();
		if (BanManager.getInstance().isRestrictedAddress(address))
			throw new IPRestrictedException();
		else if (BanManager.getInstance().isBannedAddress(address))
			throw new IPRestrictedException(BanManager.getInstance().getBanExpiry(address));
		// player disconnected meanwhile
		if (address == null)
			return false;
		return loginValid(user, password, address);
	}

	/**
	 * user name is not case sensitive any more
	 * @param user
	 * @param password
	 * @param address
	 * @return true if all operations succeed
	 * @throws NoSuchAlgorithmException if SHA is not supported
	 * @throws UnsupportedEncodingException if UTF-8 is not supported
	 * @throws AccountModificationException  if we were unable to modify the account
	 * @throws AccountBannedException  if account is banned
	 * @throws AccountWrongPasswordException if the password is wrong
	 */
	public boolean loginValid(String user, String password, InetAddress address) throws NoSuchAlgorithmException, UnsupportedEncodingException,
			AccountModificationException, AccountBannedException, AccountWrongPasswordException
	{
		_logLoginTries.info("User trying to connect  '" + user + "' " + (address == null ? "null" : address.getHostAddress()));

		// o Convert password in utf8 byte array
		// ----------------------------------
		final MessageDigest md = MessageDigest.getInstance("SHA");
		final byte[] raw = password.getBytes("UTF-8");
		final byte[] hash = md.digest(raw);

		// o find Account
		// -------------
		final Accounts acc = _service.getAccountById(user);

		// If account is not found
		// try to create it if AUTO_CREATE_ACCOUNTS is activated
		// or return false
		// ------------------------------------------------------
		if (acc == null)
		{
			if (handleAccountNotFound(user, address, hash))
			{
				handleGoodLogin(user, address);
				return true;
			}
			else
			{
				handleBadLogin(user, password, address);
				throw new AccountWrongPasswordException(user);
			}
		}
		// If account is found
		// check ban state
		// check password and update last ip/last active
		// ---------------------------------------------
		else
		{
			// check the account is not ban
			if (acc.getAccessLevel() < 0)
			{
				handleBadLogin(user, password, address);
				throw new AccountBannedException(user);
			}
			try
			{
				checkPassword(hash, acc);
				acc.setLastactive(new BigDecimal(System.currentTimeMillis()));
				if (address != null)
					acc.setLastIp(address.getHostAddress());
				_service.addOrUpdateAccount(acc);
			}
			// If password are different
			// -------------------------
			catch (final AccountWrongPasswordException e)
			{
				handleBadLogin(user, password, address);
				throw e;
			}
		}

		handleGoodLogin(user, address);
		return true;
	}

	/**
	 * @param user
	 * @param address
	 */
	private void handleGoodLogin(String user, InetAddress address)
	{
		// for long running servers, this should prevent blocking
		// of users that mistype their passwords once every day :)
		if (address != null)
			_hackProtection.remove(address);
		if (_logLogin.isDebugEnabled())
			_logLogin.debug("login successfull for '" + user + "' " + (address == null ? "null" : address.getHostAddress()));
	}

	/**
	 * 
	 * If login are different, increment hackProtection counter. It's maybe a hacking attempt
	 * 
	 * @param user
	 * @param password
	 * @param address
	 */
	private void handleBadLogin(String user, String password, InetAddress address)
	{
		_logLoginFailed.info("login failed for user : '" + user + "' " + (address == null ? "null" : address.getHostAddress()));

		// In special case, adress is null, so this protection is useless
		if (address != null)
		{
			final FailedLoginAttempt failedAttempt = _hackProtection.get(address);
			int failedCount;
			if (failedAttempt == null)
			{
				_hackProtection.put(address, new FailedLoginAttempt(address, password));
				failedCount = 1;
			}
			else
			{
				failedAttempt.increaseCounter(password);
				failedCount = failedAttempt.getCount();
			}

			if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN)
			{
				_log.info("Temporary auto-ban for " + address.getHostAddress() + " (" + Config.LOGIN_BLOCK_AFTER_BAN + " seconds, " + failedCount
						+ " login tries)");
				BanManager.getInstance().addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
			}
		}
	}

	/**
	 * @param hash
	 * @param acc
	 * @throws AccountWrongPasswordException if password is wrong
	 */
	private void checkPassword(byte[] hash, Accounts acc) throws AccountWrongPasswordException
	{
		if (_log.isDebugEnabled())
			_log.debug("account exists");

		final byte[] expected = Base64.decode(acc.getPassword());

		for (int i = 0; i < expected.length; i++)
			if (hash[i] != expected[i])
				throw new AccountWrongPasswordException(acc.getLogin());
	}

	/**
	 * @param user
	 * @param address
	 * @param hash
	 * @return true if accounts was successfully created or false is AUTO_CREATE_ACCOUNTS = false or creation failed
	 * @throws AccountModificationException
	 */
	private boolean handleAccountNotFound(String user, InetAddress address, byte[] hash) throws AccountModificationException
	{
		Accounts acc;
		if (Config.AUTO_CREATE_ACCOUNTS)
		{
			if (user.length() >= 2 && user.length() <= 14)
			{
				acc = new Accounts(user, Base64.encodeBytes(hash), new BigDecimal(System.currentTimeMillis()), 0, 0, 1900, 1, 1,
						(address == null ? "null" : address.getHostAddress()));
				_service.addOrUpdateAccount(acc);

				_logLogin.info("Account created: " + user);
				_log.info("An account was newly created: " + user);
				LoginStatusServer.tryBroadcast("Account created for player " + user);

				return true;

			}
			_logLogin.warn("Invalid username creation/use attempt: " + user);
			return false;
		}
		_logLogin.warn("No such account exists: " + user);
		return false;
	}

	public void addConnection(L2LoginClient lc)
	{
		_connections.add(lc);
	}

	public void remConnection(L2LoginClient lc)
	{
		_connections.remove(lc);
	}

	public void setAccountLastTracert(String account, String pcIp, String hop1, String hop2, String hop3, String hop4)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2Registry.getConnection(con);

			final String stmt = "UPDATE accounts SET pcIp=?, hop1=?, hop2=?, hop3=?, hop4=? WHERE login=?";
			statement = con.prepareStatement(stmt);
			statement.setString(1, pcIp);
			statement.setString(2, hop1);
			statement.setString(3, hop2);
			statement.setString(4, hop3);
			statement.setString(5, hop4);
			statement.setString(6, account);
			statement.executeUpdate();
			statement.close();
		}
		catch (final Exception e)
		{
			_log.warn("Could not set last tracert: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (final SQLException e)
			{
				_log.warn("", e);
			}
		}
	}
}
