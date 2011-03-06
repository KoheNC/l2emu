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
package net.l2emuproject.loginserver.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.security.interfaces.RSAPrivateKey;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.loginserver.beans.SessionKey;
import net.l2emuproject.loginserver.manager.LoginManager;
import net.l2emuproject.loginserver.network.clientpackets.L2LoginClientPacket;
import net.l2emuproject.loginserver.network.serverpackets.L2LoginServerPacket;
import net.l2emuproject.loginserver.network.serverpackets.LoginFail;
import net.l2emuproject.loginserver.network.serverpackets.PlayFail;
import net.l2emuproject.tools.math.ScrambledKeyPair;
import net.l2emuproject.tools.random.Rnd;
import net.l2emuproject.tools.security.LoginCrypt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.SelectorThread;


/**
 * Represents a client connected into the LoginServer
 * 
 * @author KenM
 */
public final class L2LoginClient extends MMOConnection<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket>
{
	private static final Log _log = LogFactory.getLog(L2LoginClient.class);
	
	public static enum LoginClientState
	{
		CONNECTED,
		AUTHED_GG,
		AUTHED_LOGIN;
	}
	
	private LoginClientState _state = LoginClientState.CONNECTED;
	
	// Crypt
	private LoginCrypt _loginCrypt;
	private final ScrambledKeyPair _scrambledPair;
	private final byte[] _blowfishKey;
	
	private String _account;
	private int _accessLevel;
	private int _lastServerId;
	private int _age;
	private SessionKey _sessionKey;
	private final int _sessionId = Rnd.nextInt(Integer.MAX_VALUE);
	private boolean _joinedGS;
	
	private boolean _card;
	
	public L2LoginClient(SelectorThread<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket> selectorThread,
			SocketChannel socketChannel) throws ClosedChannelException
	{
		super(selectorThread, socketChannel);
		
		_scrambledPair = LoginManager.getInstance().getScrambledRSAKeyPair();
		_blowfishKey = LoginManager.getInstance().getBlowfishKey();
	}
	
	private LoginCrypt getLoginCrypt()
	{
		if (_loginCrypt == null)
		{
			_loginCrypt = new LoginCrypt();
			_loginCrypt.setKey(_blowfishKey);
		}
		
		return _loginCrypt;
	}
	
	public String getIp()
	{
		return getHostAddress();
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean ret = false;
		try
		{
			ret = getLoginCrypt().decrypt(buf.array(), buf.position(), size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			closeNow();
			return false;
		}
		
		if (!ret)
		{
			byte[] dump = new byte[size];
			System.arraycopy(buf.array(), buf.position(), dump, 0, size);
			_log.warn("Wrong checksum from client: " + toString());
			closeNow();
		}
		
		return ret;
	}
	
	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		final int offset = buf.position();
		try
		{
			size = getLoginCrypt().encrypt(buf.array(), offset, size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		buf.position(offset + size);
		return true;
	}
	
	public LoginClientState getState()
	{
		return _state;
	}
	
	public void setState(LoginClientState state)
	{
		_state = state;
	}
	
	public byte[] getBlowfishKey()
	{
		return _blowfishKey;
	}
	
	public byte[] getScrambledModulus()
	{
		return _scrambledPair.getScrambledModulus();
	}
	
	public RSAPrivateKey getRSAPrivateKey()
	{
		return (RSAPrivateKey)_scrambledPair.getPair().getPrivate();
	}
	
	public String getAccount()
	{
		return _account;
	}
	
	public void setAccount(String account)
	{
		_account = account;
	}
	
	public void setAccessLevel(int accessLevel)
	{
		_accessLevel = accessLevel;
	}
	
	public int getAccessLevel()
	{
		return _accessLevel;
	}
	
	public void setLastServerId(int lastServerId)
	{
		_lastServerId = lastServerId;
	}
	
	public int getLastServerId()
	{
		return _lastServerId;
	}
	
	public void setAge(int year, int month, int day)
	{
		Calendar dateOfBirth = new GregorianCalendar(year, month - 1, day);
		Calendar today = Calendar.getInstance();
		int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
		dateOfBirth.add(Calendar.YEAR, age);
		if (today.before(dateOfBirth))
			age--;
		
		_age = age;
	}
	
	public int getAge()
	{
		return _age;
	}
	
	public int getSessionId()
	{
		return _sessionId;
	}
	
	public void setSessionKey(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}
	
	public boolean hasJoinedGS()
	{
		return _joinedGS;
	}
	
	public void setJoinedGS(boolean val)
	{
		_joinedGS = val;
	}
	
	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}
	
	public void closeLogin(int reason)
	{
		close(new LoginFail(reason));
	}
	
	public void closeLoginGame(int reason)
	{
		close(new PlayFail(reason));
	}
	
	public void closeBanned()
	{
		close(new LoginFail(getAccessLevel(), true));
	}
	
	public void closeBanned(int timeLeft)
	{
		closeLogin(LoginFail.REASON_IP_RESTRICTED);
	}
	
	public boolean isCardAuthed()
	{
		return _card;
	}
	
	public void setCardAuthed(boolean card)
	{
		_card = card;
	}
	
	@Override
	protected L2LoginServerPacket getDefaultClosePacket()
	{
		return new LoginFail(LoginFail.REASON_ACCESS_FAILED);
	}
	
	@Override
	public void onDisconnection()
	{
		if (_log.isDebugEnabled())
			_log.info("onDisconnection: " + this);
		
		LoginManager.getInstance().remConnection(this);
		
		// If player was not on GS, don't forget to remove it from authed login on LS
		if (getState() == LoginClientState.AUTHED_LOGIN && !hasJoinedGS())
		{
			LoginManager.getInstance().removeAuthedLoginClient(getAccount());
		}
	}
	
	@Override
	protected void onForcedDisconnection()
	{
		if (_log.isDebugEnabled())
			_log.info("onForcedDisconnection: " + this);
	}
	
	@Override
	public String toString()
	{
		L2TextBuilder tb = L2TextBuilder.newInstance();
		
		tb.append("[State: ").append(getState());
		
		String ip = getIp();
		if (ip != null)
			tb.append(" | IP: ").append(String.format("%-15s", ip));
		
		String account = getAccount();
		if (account != null)
			tb.append(" | Account: ").append(String.format("%-15s", account));
		
		tb.append("]");
		
		return tb.moveToString();
	}
	
	@Override
	protected String getUID()
	{
		return getAccount();
	}
}
