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
package net.l2emuproject.gameserver;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.L2Config;
import net.l2emuproject.gameserver.network.Disconnection;
import net.l2emuproject.gameserver.network.L2GameClient;
import net.l2emuproject.gameserver.network.L2GameClient.GameClientState;
import net.l2emuproject.gameserver.network.L2GameSelectorThread;
import net.l2emuproject.gameserver.network.gameserverpackets.AuthRequest;
import net.l2emuproject.gameserver.network.gameserverpackets.BlowFishKey;
import net.l2emuproject.gameserver.network.gameserverpackets.ChangeAccessLevel;
import net.l2emuproject.gameserver.network.gameserverpackets.CompatibleProtocol;
import net.l2emuproject.gameserver.network.gameserverpackets.PlayerAuthRequest;
import net.l2emuproject.gameserver.network.gameserverpackets.PlayerInGame;
import net.l2emuproject.gameserver.network.gameserverpackets.PlayerLogout;
import net.l2emuproject.gameserver.network.gameserverpackets.PlayerTracert;
import net.l2emuproject.gameserver.network.gameserverpackets.ServerStatusPacket;
import net.l2emuproject.gameserver.network.loginserverpackets.AuthResponse;
import net.l2emuproject.gameserver.network.loginserverpackets.InitLS;
import net.l2emuproject.gameserver.network.loginserverpackets.KickPlayer;
import net.l2emuproject.gameserver.network.loginserverpackets.LoginServerFail;
import net.l2emuproject.gameserver.network.loginserverpackets.PlayerAuthResponse;
import net.l2emuproject.gameserver.network.loginserverpackets.PlayerLoginAttempt;
import net.l2emuproject.gameserver.network.serverpackets.CharSelectionInfo;
import net.l2emuproject.gameserver.network.serverpackets.LoginFail;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.network.NetworkThread;
import net.l2emuproject.network.ServerStatus;
import net.l2emuproject.network.ServerStatusAttributes;
import net.l2emuproject.tools.random.Rnd;
import net.l2emuproject.tools.security.NewCrypt;


public final class LoginServerThread extends NetworkThread
{
	private static final class SingletonHolder
	{
		private static final LoginServerThread INSTANCE = new LoginServerThread();
	}
	
	public static LoginServerThread getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private final String _hostname;
	private final int _port;
	private final int _gamePort;
	private final byte[] _hexID;
	private final boolean _acceptAlternate;
	private final int _requestID;
	private int _serverID;
	private final boolean _reserveHost;
	private final Map<String, WaitingClient> _waitingClients;
	private final Map<String, L2GameClient> _accountsInGameServer;
	private ServerStatus _status = ServerStatus.STATUS_AUTO;
	private String _gameExternalHost; // External host for old login server
	private String _gameInternalHost; // Internal host for old login server
	
	private boolean _supportsNewLoginProtocol = false;
	
	public LoginServerThread()
	{
		super("LoginServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		if (Config.HEX_ID == null)
		{
			_requestID = Config.REQUEST_ID;
			_hexID = generateHex(16);
		}
		else
		{
			_requestID = Config.SERVER_ID;
			_hexID = Config.HEX_ID;
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_gameExternalHost = Config.EXTERNAL_HOSTNAME;
		_gameInternalHost = Config.INTERNAL_HOSTNAME;
		_waitingClients = new FastMap<String, WaitingClient>().shared();
		_accountsInGameServer = new FastMap<String, L2GameClient>().shared();
/*		
		if (Config.SUBNETWORKS != null && Config.SUBNETWORKS.length() > 0)
		{
			_gameExternalHost = Config.SUBNETWORKS;
			_gameInternalHost = "";
		}
*/
	}
	
	public void addWaitingClientAndSendRequest(String acc, L2GameClient client, SessionKey key)
	{
		_waitingClients.put(acc, new WaitingClient(client, key));
		
		sendPacketQuietly(new PlayerAuthRequest(acc, key));
	}
	
	public void sendLogout(String account)
	{
		if (account == null || account.isEmpty())
			return;
		
		_waitingClients.remove(account);
		_accountsInGameServer.remove(account);
		
		sendPacketQuietly(new PlayerLogout(account));
	}
	
	public void sendAccessLevel(String account, int level)
	{
		sendPacketQuietly(new ChangeAccessLevel(account, level));
	}
	
	public void sendClientTracert(String account, String[] adress)
	{
		sendPacketQuietly(new PlayerTracert(account, adress[0], adress[1], adress[2], adress[3], adress[4]));
	}
	
	private static String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}
	
	private static byte[] generateHex(int size)
	{
		return Rnd.nextBytes(new byte[size]);
	}
	
	public static final class SessionKey
	{
		public final int playOkID1;
		public final int playOkID2;
		public final int loginOkID1;
		public final int loginOkID2;
		
		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}
		
		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}
	
	private static final class WaitingClient
	{
		public final L2GameClient gameClient;
		public final SessionKey session;
		
		public WaitingClient(L2GameClient client, SessionKey key)
		{
			gameClient = client;
			session = key;
		}
	}
	
	public ServerStatus getServerStatus()
	{
		return _status;
	}
	
	public void setServerStatus(int status)
	{
		changeAttribute(ServerStatusAttributes.SERVER_LIST_STATUS, status);
	}
	
	public void setServerStatusDown()
	{
		setServerStatus(ServerStatus.STATUS_DOWN.ordinal());
	}
	
	public int getMaxPlayer()
	{
		return Config.MAXIMUM_ONLINE_USERS;
	}
	
	public void setMaxPlayers(int maxPlayer)
	{
		changeAttribute(ServerStatusAttributes.SERVER_LIST_MAX_PLAYERS, maxPlayer);
	}
	
	public void changeAttribute(int attr, int value)
	{
		changeAttribute(ServerStatusAttributes.valueOf(attr), value);
	}
	
	public void changeAttribute(ServerStatusAttributes attr, int value)
	{
		switch (attr)
		{
			case SERVER_LIST_STATUS:
				_status = ServerStatus.valueOf(value);
				switch (_status)
				{
					case STATUS_DOWN:
						if (!Shutdown.isInProgress())
							kickPlayers();
						break;
				}
				break;
			case SERVER_LIST_UNK:
				Config.SERVER_BIT_1 = (value > 0);
				break;
			case SERVER_LIST_CLOCK:
				Config.SERVER_LIST_CLOCK = (value > 0);
				break;
			case SERVER_LIST_HIDE_NAME:
				Config.SERVER_BIT_3 = (value > 0);
				break;
			case TEST_SERVER:
				Config.SERVER_LIST_TESTSERVER = (value > 0);
				break;
			case SERVER_LIST_BRACKETS:
				Config.SERVER_LIST_BRACKET = (value > 0);
				break;
			case SERVER_LIST_MAX_PLAYERS:
				Config.MAXIMUM_ONLINE_USERS = value;
				break;
			case SERVER_LIST_PVP:
				Config.SERVER_PVP = (value > 0);
				break;
			case SERVER_AGE_LIMITATION:
				Config.SERVER_AGE_LIM = value;
				break;
			case NONE:
			default:
				return;
		}
		
		ServerStatusPacket ss = new ServerStatusPacket();
		ss.addAttribute(attr, value);
		
		sendPacketQuietly(ss);
	}
	
	private void kickPlayers()
	{
		int counter = 0;
		for (L2Player player : L2World.getInstance().getAllPlayers())
		{
			if (player.isGM())
				continue;
			
			try
			{
				new Disconnection(player).defaultSequence(true);
				counter++;
			}
			catch (RuntimeException e)
			{
				_log.warn("", e);
			}
		}
		
		_log.info(counter + " players were auto-kicked.");
	}
	
	@Override
	public void run()
	{
		for (;;)
		{
			try
			{
				// Connection
				_log.info("Connecting to login on " + _hostname + ":" + _port);
				
				initConnection(new Socket(_hostname, _port));
				
				while (true)
				{
					byte[] decrypt = read();
					
					if (decrypt == null)
						break;
					
					int packetType = decrypt[0] & 0xff;
					
					switch (packetType)
					{
						case 0x00:
						{
							InitLS init = new InitLS(decrypt);
							if (_log.isDebugEnabled())
								_log.debug("Init received");
							
							if (init.getRevision() != L2Config.LOGIN_PROTOCOL_L2J)
							{
								_log.warn("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							
							if (init.getTrueRevision() == L2Config.LOGIN_PROTOCOL_CURRENT)
							{
								// Fully compatible login
								sendPacket(new CompatibleProtocol());
								
								_supportsNewLoginProtocol = true;
								
								if (!Config.CONNECTION_FILTERING)
								{
									// not supported by login, inform that GS can't do anything
									_log.warn("Connection filtering is disabled, which is not recommended.");
								}
							}
							else
							{
								_supportsNewLoginProtocol = false;
								
								// Default compatibility login
								if (Config.CONNECTION_FILTERING)
								{
									// not supported by login, inform that GS can't do anything
									_log
											.warn("Connection filtering has been disabled, as the login server doesn't support it.");
								}
							}
							
							RSAPublicKey publicKey;
							
							try
							{
								KeyFactory kfac = KeyFactory.getInstance("RSA");
								BigInteger modulus = new BigInteger(init.getRSAKey());
								RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								publicKey = (RSAPublicKey)kfac.generatePublic(kspec1);
								if (_log.isDebugEnabled())
									_log.debug("RSA key set up");
							}
							catch (GeneralSecurityException e)
							{
								_log.warn("Troubles while init the public key send by login");
								break;
							}
							
							byte[] blowfishKey = generateHex(40);
							
							// send the blowfish key through the rsa encryption
							sendPacket(new BlowFishKey(blowfishKey, publicKey));
							if (_log.isDebugEnabled())
								_log.info("Sent new blowfish key");
							// now, only accept paket with the new encryption
							setBlowfish(new NewCrypt(blowfishKey));
							if (_log.isDebugEnabled())
								_log.info("Changed blowfish key");
							sendPacket(new AuthRequest(_requestID, _acceptAlternate, _hexID, _gameExternalHost,
									_gameInternalHost, _gamePort, _reserveHost, Config.MAXIMUM_ONLINE_USERS));
							if (_log.isDebugEnabled())
								_log.debug("Sent AuthRequest to login");
							break;
						}
						case 0x01:
						{
							LoginServerFail lsf = new LoginServerFail(decrypt);
							_log.info("Damn! Registration Failed: " + lsf.getReasonString());
							// login will close the connection here
							break;
						}
						case 0x02:
						{
							AuthResponse aresp = new AuthResponse(decrypt);
							_serverID = aresp.getServerId();
							String serverName = aresp.getServerName();
							Config.saveHexid(_serverID, hexToString(_hexID));
							_log.info("Registered on login as Server " + _serverID + " : " + serverName);
							
							_status = Config.SERVER_GMONLY ? ServerStatus.STATUS_GM_ONLY : ServerStatus.STATUS_AUTO;
							
							ServerStatusPacket st = new ServerStatusPacket();
							st.addAttribute(ServerStatusAttributes.SERVER_LIST_STATUS, _status.ordinal());
							st.addAttribute(ServerStatusAttributes.SERVER_LIST_CLOCK, Config.SERVER_LIST_CLOCK);
							st.addAttribute(ServerStatusAttributes.SERVER_LIST_BRACKETS, Config.SERVER_LIST_BRACKET);
							st.addAttribute(ServerStatusAttributes.TEST_SERVER, Config.SERVER_LIST_TESTSERVER);
							if (_supportsNewLoginProtocol)
							{
								//max players already sent with auth
								st.addAttribute(ServerStatusAttributes.SERVER_LIST_PVP, Config.SERVER_PVP);
								st.addAttribute(ServerStatusAttributes.SERVER_LIST_UNK, Config.SERVER_BIT_1);
								st.addAttribute(ServerStatusAttributes.SERVER_LIST_HIDE_NAME, Config.SERVER_BIT_3);
								st.addAttribute(ServerStatusAttributes.SERVER_AGE_LIMITATION, Config.SERVER_AGE_LIM);
							}
							sendPacket(st);
							
							if (L2World.getInstance().getAllPlayersCount() > 0)
							{
								ArrayList<String> playerList = new ArrayList<String>();
								for (L2Player player : L2World.getInstance().getAllPlayers())
								{
									if (player.getClient() == null)
										continue;
									
									playerList.add(player.getAccountName());
								}
								sendPacket(new PlayerInGame(playerList.toArray(new String[playerList.size()])));
							}
							break;
						}
						case 0x03:
						{
							PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
							WaitingClient wcToRemove = _waitingClients.remove(par.getAccount());
							
							if (wcToRemove != null)
							{
								final L2GameClient client = wcToRemove.gameClient;
								
								if (par.isAuthed())
								{
									sendPacket(new PlayerInGame(par.getAccount()));
									client.setState(GameClientState.AUTHED);
									client.setSessionId(wcToRemove.session);
									client.setHostAddress(par.getHost());
									
									// executing the sql query on the thread pool
									final class AsyncCharSelectionInfo implements Runnable
									{
										@Override
										public void run()
										{
											client.sendPacket(new CharSelectionInfo(client));
										}
									}
									
									client.getPacketQueue().execute(new AsyncCharSelectionInfo());
									
									_accountsInGameServer.put(client.getAccountName(), client);
								}
								else
								{
									_log.warn("session key is not correct. closing connection");
									client.sendPacket(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
									client.closeNow();
								}
							}
							break;
						}
						case 0x04:
						{
							KickPlayer kp = new KickPlayer(decrypt);
							
							L2GameClient client = _accountsInGameServer.get(kp.getAccount());
							
							if (client != null)
								client.closeNow();
							
							WaitingClient wc = _waitingClients.get(kp.getAccount());
							
							if (wc != null)
								wc.gameClient.closeNow();
							
							L2Player.disconnectIfOnline(kp.getAccount());
							break;
						}
						case 0x10:
						{
							if (_supportsNewLoginProtocol)
							{
								PlayerLoginAttempt pla = new PlayerLoginAttempt(decrypt);
								L2GameSelectorThread.getInstance().legalize(pla.getIP());
								break;
							}
						}
							//$FALL-THROUGH$
						default:
						{
							_log.warn("Unknown opcode: " + Integer.toHexString(packetType));
							break;
						}
					}
				}
			}
			catch (ConnectException e)
			{
				_log.info(e);
			}
			catch (IOException e)
			{
				_log.warn("", e);
			}
			catch (RuntimeException e)
			{
				_log.warn("", e);
			}
			finally
			{
				close();
				
				_log.info("Disconnected from login, trying to reconnect!");
			}
			
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				_log.warn("", e);
			}
		}
	}
	
	public boolean supportsNewLoginProtocol()
	{
		return _supportsNewLoginProtocol;
	}
}
