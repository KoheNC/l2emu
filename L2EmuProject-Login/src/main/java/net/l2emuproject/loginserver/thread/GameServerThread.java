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
package net.l2emuproject.loginserver.thread;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastSet;
import net.l2emuproject.Config;
import net.l2emuproject.loginserver.beans.GameServerInfo;
import net.l2emuproject.loginserver.beans.SessionKey;
import net.l2emuproject.loginserver.manager.GameServerManager;
import net.l2emuproject.loginserver.manager.LoginManager;
import net.l2emuproject.loginserver.network.gameserverpackets.BlowFishKey;
import net.l2emuproject.loginserver.network.gameserverpackets.ChangeAccessLevel;
import net.l2emuproject.loginserver.network.gameserverpackets.GameServerAuth;
import net.l2emuproject.loginserver.network.gameserverpackets.PlayerAuthRequest;
import net.l2emuproject.loginserver.network.gameserverpackets.PlayerInGame;
import net.l2emuproject.loginserver.network.gameserverpackets.PlayerLogout;
import net.l2emuproject.loginserver.network.gameserverpackets.PlayerTracert;
import net.l2emuproject.loginserver.network.gameserverpackets.ServerStatus;
import net.l2emuproject.loginserver.network.loginserverpackets.AuthResponse;
import net.l2emuproject.loginserver.network.loginserverpackets.InitLS;
import net.l2emuproject.loginserver.network.loginserverpackets.KickPlayer;
import net.l2emuproject.loginserver.network.loginserverpackets.LoginServerFail;
import net.l2emuproject.loginserver.network.loginserverpackets.PlayerAuthResponse;
import net.l2emuproject.loginserver.network.loginserverpackets.PlayerLoginAttempt;
import net.l2emuproject.loginserver.status.LoginStatusServer;
import net.l2emuproject.network.login.LoginServerFailReason;
import net.l2emuproject.network.thread.NetworkThread;
import net.l2emuproject.tools.network.SubNetHost;
import net.l2emuproject.tools.security.NewCrypt;

/**
 * @author -Wooden-
 */
public class GameServerThread extends NetworkThread
{
	private final RSAPublicKey		_publicKey;
	private final RSAPrivateKey		_privateKey;

	private GameServerInfo			_gsi;
	private final List<SubNetHost>	_gameserverSubnets			= new FastList<SubNetHost>();

	private long					_lastIpUpdate;

	/** Authed Clients on a GameServer */
	private final Set<String>		_accountsOnGameServer		= new FastSet<String>();

	private boolean					_supportsNewLoginProtocol	= false;

	@Override
	public void run()
	{
		try
		{
			sendPacket(new InitLS(_publicKey.getModulus().toByteArray()));

			for (;;)
			{
				final byte[] data = read();

				if (data == null)
					break;

				final int packetType = data[0] & 0xff;

				switch (packetType)
				{
					case 0x00:
						onReceiveBlowfishKey(data);
						break;
					case 0x01:
						onGameServerAuth(data);
						break;
					case 0x02:
						onReceivePlayerInGame(data);
						break;
					case 0x03:
						onReceivePlayerLogOut(data);
						break;
					case 0x04:
						onReceiveChangeAccessLevel(data);
						break;
					case 0x05:
						onReceivePlayerAuthRequest(data);
						break;
					case 0x06:
						onReceiveServerStatus(data);
						break;
					case 0x07:
						onReceivePlayerTracert(data);
						break;
					case 0xAF:
						// trigger packet, shows that Game Server supports the actual protocol
						_supportsNewLoginProtocol = true;
						break;
					default:
						_log.warn("Unknown opcode: " + Integer.toHexString(packetType));
						forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
				}
			}
		}
		catch (final IOException e)
		{
			_log.warn("", e);
		}
		catch (final RuntimeException e)
		{
			_log.warn("", e);
		}
		finally
		{
			if (isAuthed())
				getGameServerInfo().setDown();

			final String msg = "Server " + getServerInfo() + " is now set as disconnected";
			_log.info(msg);
			broadcastToTelnet(msg);

			GameServerListener.getInstance().removeGameServer(this);
			GameServerListener.getInstance().removeFloodProtection(getConnectionIp());

			close();
		}
	}

	private void broadcastToTelnet(String msg)
	{
		LoginStatusServer.tryBroadcast(msg);
	}

	private void onReceiveBlowfishKey(byte[] data)
	{
		final BlowFishKey bfk = new BlowFishKey(data, _privateKey);
		final byte[] blowfishKey = bfk.getKey();
		setBlowfish(new NewCrypt(blowfishKey));
		if (_log.isDebugEnabled())
			_log.info("New BlowFish key received, Blowfish Engine initialized.");
	}

	private void onGameServerAuth(byte[] data) throws IOException
	{
		final GameServerAuth gsa = new GameServerAuth(data);
		if (_log.isDebugEnabled())
			_log.info("Auth request received");
		handleRegProcess(gsa);
		if (isAuthed())
		{
			sendPacket(new AuthResponse(getGameServerInfo().getId()));
			if (_log.isDebugEnabled())
				_log.info("Authed: id: " + getGameServerInfo().getId());
			broadcastToTelnet("GameServer " + getServerInfo() + " is connected");
		}
	}

	private void onReceivePlayerInGame(byte[] data)
	{
		if (isAuthed())
		{
			final PlayerInGame pig = new PlayerInGame(data);
			for (final String account : pig.getAccounts())
			{
				_accountsOnGameServer.add(account);
				if (_log.isDebugEnabled())
					_log.info("Account " + account + " logged in GameServer: " + getServerInfo());

				broadcastToTelnet("Account " + account + " logged in GameServer " + getServerInfo());
			}
		}
		else
			forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
	}

	private void onReceivePlayerLogOut(byte[] data)
	{
		if (isAuthed())
		{
			final PlayerLogout plo = new PlayerLogout(data);
			_accountsOnGameServer.remove(plo.getAccount());
			if (_log.isDebugEnabled())
				_log.info("Player " + plo.getAccount() + " logged out from gameserver " + getServerInfo());

			broadcastToTelnet("Player " + plo.getAccount() + " disconnected from GameServer " + getServerInfo());
		}
		else
			forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
	}

	private void onReceiveChangeAccessLevel(byte[] data)
	{
		if (isAuthed())
		{
			final ChangeAccessLevel cal = new ChangeAccessLevel(data);
			try
			{
				LoginManager.getInstance().setAccountAccessLevel(cal.getAccount(), cal.getLevel());
				_log.info("Changed " + cal.getAccount() + " access level to " + cal.getLevel());
			}
			catch (final Exception e)
			{
				_log.warn("Access level could not be changed. Reason: ", e);
			}
		}
		else
			forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
	}

	private void onReceivePlayerAuthRequest(byte[] data) throws IOException
	{
		if (isAuthed())
		{
			final PlayerAuthRequest par = new PlayerAuthRequest(data);
			PlayerAuthResponse authResponse;
			if (_log.isDebugEnabled())
				_log.info("auth request received for Player " + par.getAccount());
			final SessionKey key = LoginManager.getInstance().getKeyForAccount(par.getAccount());
			final String host = LoginManager.getInstance().getHostForAccount(par.getAccount());
			if (key != null && key.equals(par.getKey()))
			{
				if (_log.isDebugEnabled())
					_log.info("auth request: OK");
				LoginManager.getInstance().removeAuthedLoginClient(par.getAccount());
				authResponse = new PlayerAuthResponse(par.getAccount(), true, host);
			}
			else
			{
				if (_log.isDebugEnabled())
				{
					_log.info("auth request: NO");
					_log.info("session key from self: " + key);
					_log.info("session key sent: " + par.getKey());
				}
				authResponse = new PlayerAuthResponse(par.getAccount(), false, host);
			}
			sendPacket(authResponse);
		}
		else
			forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
	}

	private void onReceiveServerStatus(byte[] data)
	{
		if (isAuthed())
		{
			if (_log.isDebugEnabled())
				_log.info("ServerStatus received");
			new ServerStatus(data, getServerId()); //will do the actions by itself
		}
		else
			forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
	}

	private void onReceivePlayerTracert(byte[] data)
	{
		if (isAuthed())
		{
			final PlayerTracert plt = new PlayerTracert(data);
			try
			{
				LoginManager.getInstance().setAccountLastTracert(plt.getAccount(), plt.getPcIp(), plt.getFirstHop(), plt.getSecondHop(), plt.getThirdHop(),
						plt.getFourthHop());

				if (_log.isDebugEnabled())
					_log.debug("Saved " + plt.getAccount() + " last tracert.");
			}
			catch (final Exception e)
			{
				_log.warn("", e);
			}
		}
		else
			forceClose(LoginServerFailReason.REASON_NOT_AUTHED);
	}

	private void forceClose(LoginServerFailReason reason)
	{
		sendPacketQuietly(new LoginServerFail(reason));
		close();
	}

	private void handleRegProcess(GameServerAuth gameServerAuth)
	{
		final int id = gameServerAuth.getDesiredID();
		final byte[] hexId = gameServerAuth.getHexID();

		GameServerInfo gsi = GameServerManager.getInstance().getRegisteredGameServerById(id);
		// is there a gameserver registered with this id?
		if (gsi != null)
		{
			// does the hex id match?
			if (Arrays.equals(gsi.getHexId(), hexId))
				// check to see if this GS is already connected
				synchronized (gsi)
				{
					if (gsi.isAuthed())
						forceClose(LoginServerFailReason.REASON_ALREADY_LOGGED_IN);
					else
						attachGameServerInfo(gsi, gameServerAuth);
				}
			else // there is already a server registered with the desired id and different hex id
			// try to register this one with an alternative id
			if (Config.ACCEPT_NEW_GAMESERVER && gameServerAuth.acceptAlternateID())
			{
				gsi = new GameServerInfo(id, hexId, this);
				if (GameServerManager.getInstance().registerWithFirstAvailableId(gsi))
				{
					attachGameServerInfo(gsi, gameServerAuth);
					GameServerManager.getInstance().registerServerOnDB(gsi);
				}
				else
					forceClose(LoginServerFailReason.REASON_NO_FREE_ID);
			}
			else
				// server id is already taken, and we cant get a new one for you
				forceClose(LoginServerFailReason.REASON_WRONG_HEXID);
		}
		else if (Config.ACCEPT_NEW_GAMESERVER)// can we register on this id?
		{
			gsi = new GameServerInfo(id, hexId, this);
			if (GameServerManager.getInstance().register(id, gsi))
			{
				attachGameServerInfo(gsi, gameServerAuth);
				GameServerManager.getInstance().registerServerOnDB(gsi);
			}
			else
				// some one took this ID meanwhile
				forceClose(LoginServerFailReason.REASON_ID_RESERVED);
		}
		else
			forceClose(LoginServerFailReason.REASON_WRONG_HEXID);
	}

	/**
	 * Attachs a GameServerInfo to this Thread
	 * <li>Updates the GameServerInfo values based on GameServerAuth packet</li>
	 * <li><b>Sets the GameServerInfo as Authed</b></li>
	 * 
	 * @param gsi The GameServerInfo to be attached.
	 * @param gameServerAuth The server info.
	 */
	private void attachGameServerInfo(GameServerInfo gsi, GameServerAuth gameServerAuth)
	{
		setGameServerInfo(gsi);
		gsi.setGameServerThread(this);
		gsi.setPort(gameServerAuth.getPort());
		setNetConfig(gameServerAuth.getNetConfig());
		gsi.setIp(getConnectionIp());

		gsi.setMaxPlayers(gameServerAuth.getMaxPlayers());
		gsi.setAuthed(true);
	}

	public GameServerThread(Socket con) throws IOException
	{
		initConnection(con);

		final KeyPair pair = GameServerManager.getInstance().getKeyPair();
		_privateKey = (RSAPrivateKey) pair.getPrivate();
		_publicKey = (RSAPublicKey) pair.getPublic();
		start();
	}

	public void kickPlayer(String account)
	{
		sendPacketQuietly(new KickPlayer(account));
	}

	public boolean hasAccountOnGameServer(String account)
	{
		return _accountsOnGameServer.contains(account);
	}

	public int getPlayerCount()
	{
		return _accountsOnGameServer.size();
	}

	private void setNetConfig(String netConfig)
	{
		if (_gameserverSubnets.size() == 0)
		{
			final StringTokenizer hostNets = new StringTokenizer(netConfig.trim(), ";");

			while (hostNets.hasMoreTokens())
			{
				final String hostNet = hostNets.nextToken();

				final StringTokenizer addresses = new StringTokenizer(hostNet.trim(), ",");

				final String _host = addresses.nextToken();

				final SubNetHost _subNetHost = new SubNetHost(_host);

				if (addresses.hasMoreTokens())
					while (addresses.hasMoreTokens())
						try
						{
							final StringTokenizer netmask = new StringTokenizer(addresses.nextToken().trim(), "/");
							final String _net = netmask.nextToken();
							final String _mask = netmask.nextToken();

							_subNetHost.addSubNet(_net, _mask);
						}
						catch (final NoSuchElementException c)
						{
							// Silence of the Lambs =)
						}
				else
					_subNetHost.addSubNet("0.0.0.0", "0");

				_gameserverSubnets.add(_subNetHost);
			}
		}

		updateIPs();
	}

	private void updateIPs()
	{
		_lastIpUpdate = System.currentTimeMillis();

		if (_gameserverSubnets.size() > 0)
		{
			_log.info("Updated Gameserver " + getServerInfo() + " IP's:");

			for (final SubNetHost _netConfig : _gameserverSubnets)
			{
				final String _hostName = _netConfig.getHostname();
				try
				{
					final String _hostAddress = InetAddress.getByName(_hostName).getHostAddress();
					_netConfig.setIp(_hostAddress);
					_log.info(!_hostName.equals(_hostAddress) ? _hostName + " (" + _hostAddress + ")" : _hostAddress);
				}
				catch (final UnknownHostException e)
				{
					_log.warn("Couldn't resolve hostname \"" + _hostName + "\"");
				}
			}
		}
	}

	public String getIp(String ip)
	{
		String _host = null;

		if (Config.IP_UPDATE_TIME > 0 && System.currentTimeMillis() > _lastIpUpdate + Config.IP_UPDATE_TIME)
			updateIPs();

		for (final SubNetHost _netConfig : _gameserverSubnets)
			if (_netConfig.isInSubnet(ip))
			{
				_host = _netConfig.getIp();
				break;
			}
		if (_host == null)
			_host = ip;

		return _host;
	}

	/**
	 * @return Returns the isAuthed.
	 */
	private boolean isAuthed()
	{
		final GameServerInfo gsi = getGameServerInfo();

		return gsi == null ? false : gsi.isAuthed();
	}

	private void setGameServerInfo(GameServerInfo gsi)
	{
		_gsi = gsi;
	}

	private GameServerInfo getGameServerInfo()
	{
		return _gsi;
	}

	public int getServerId()
	{
		final GameServerInfo gsi = getGameServerInfo();

		return gsi == null ? -1 : gsi.getId();
	}

	public String getServerInfo()
	{
		final int serverId = getServerId();

		if (serverId != -1)
			return "[" + serverId + "] " + GameServerManager.getInstance().getServerNameById(serverId);
		else
			return "(" + getConnectionIp() + ")";
	}

	public void playerSelectedServer(String ip)
	{
		if (!_supportsNewLoginProtocol)
			return;

		sendPacketQuietly(new PlayerLoginAttempt(ip));
	}
}
