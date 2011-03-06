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
package net.l2emuproject.gameserver.network;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastList;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.LoginServerThread;
import net.l2emuproject.gameserver.LoginServerThread.SessionKey;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.network.clientpackets.L2GameClientPacket;
import net.l2emuproject.gameserver.network.serverpackets.L2GameServerPacket;
import net.l2emuproject.gameserver.network.serverpackets.LeaveWorld;
import net.l2emuproject.gameserver.network.serverpackets.ServerClose;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.threadmanager.FIFORunnableQueue;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.system.util.FloodProtector;
import net.l2emuproject.gameserver.system.util.TableOptimizer;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.system.util.TableOptimizer.CharacterRelatedTable;
import net.l2emuproject.gameserver.system.util.TableOptimizer.ItemRelatedTable;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;
import net.l2emuproject.tools.security.BlowFishKeygen;
import net.l2emuproject.tools.security.GameCrypt;
import net.l2emuproject.util.concurrent.RunnableStatsManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.SelectorThread;


/**
 * Represents a client connected on Game Server
 * 
 * @author KenM
 */
public final class L2GameClient extends MMOConnection<L2GameClient, L2GameClientPacket, L2GameServerPacket>
{
	private static final Log _log = LogFactory.getLog(L2GameClient.class);
	
	/**
	 * @author KenM
	 */
	public static enum GameClientState
	{
		CONNECTED, // client has just connected
		AUTHED, // client has authed but doesnt has character attached to it yet
		IN_GAME; // client has selected a char and is in game
	}
	
	private GameClientState _state = GameClientState.CONNECTED;
	private String _accountName;
	private SessionKey _sessionId;
	private L2Player _activeChar;
	private boolean _isAuthedGG;
	private int[] _charSlotMapping;
	private GameCrypt _crypt;
	private volatile boolean _disconnected;
	private boolean _protocol;
	private int[][] _trace;
	
	private final FloodProtector _floodProtector = new FloodProtector(this);
	
	public L2GameClient(SelectorThread<L2GameClient, L2GameClientPacket, L2GameServerPacket> selectorThread,
			SocketChannel socketChannel) throws ClosedChannelException
	{
		super(selectorThread, socketChannel);
	}
	
	private GameCrypt getCrypt()
	{
		if (_crypt == null)
			_crypt = new GameCrypt();
		
		return _crypt;
	}
	
	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		getCrypt().setKey(key);
		return key;
	}
	
	public GameClientState getState()
	{
		return _state;
	}
	
	public void setState(GameClientState pState)
	{
		_state = pState;
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		getCrypt().decrypt(buf.array(), buf.position(), size);
		return true;
	}
	
	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		getCrypt().encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}
	
	public L2Player getActiveChar()
	{
		return _activeChar;
	}
	
	public void setActiveChar(L2Player pActiveChar)
	{
		_activeChar = pActiveChar;
	}
	
	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}
	
	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}
	
	public void setAccountName(String pAccountName)
	{
		_accountName = pAccountName;
	}
	
	public String getAccountName()
	{
		return _accountName;
	}
	
	public void setSessionId(SessionKey sk)
	{
		_sessionId = sk;
	}
	
	public SessionKey getSessionId()
	{
		return _sessionId;
	}
	
	/**
	 * Method to handle character deletion
	 * 
	 * @return a byte:
	 *         <li>-1: Error: No char was found for such charslot, caught exception, etc...
	 *         <li>0: character is not member of any clan, proceed with deletion
	 *         <li>1: character is member of a clan, but not clan leader
	 *         <li>2: character is clan leader
	 */
	public byte markToDeleteChar(int charslot)
	{
		int objid = getObjectIdForSlot(charslot);
		
		if (objid < 0)
			return -1;
		
		byte result = -1;
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("SELECT clanId from characters WHERE charId=?");
			statement.setInt(1, objid);
			ResultSet rs = statement.executeQuery();
			byte answer = -1;
			if (rs.next())
			{
				int clanId = rs.getInt(1);
				if (clanId != 0)
				{
					L2Clan clan = ClanTable.getInstance().getClan(clanId);
					
					if (clan == null)
						answer = 0; // jeezes!
					else if (clan.getLeaderId() == objid)
						answer = 2;
					else
						answer = 1;
				}
				else
					answer = 0;
				
				// Setting delete time
				if (answer == 0)
				{
					if (Config.DELETE_DAYS == 0)
					{
						deleteCharByObjId(objid);
					}
					else
					{
						statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE charId=?");
						statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L); // 24*60*60*1000 = 86400000
						statement.setInt(2, objid);
						statement.execute();
						statement.close();
					}
				}
			}
			result = answer;
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error updating delete time of character.", e);
			result = -1;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		return result;
	}
	
	public void markRestoredChar(int charslot) throws Exception
	{
		// have to make sure active character must be nulled
		/*
		 * if (getActiveChar() != null) { saveCharToDisk (getActiveChar()); if
		 * (_log.isDebugEnabled()) _log.debug("active Char saved"); _activeChar
		 * = null; }
		 */

		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
			return;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error restoring character.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public static void deleteCharByObjId(int objid)
	{
		if (objid < 0)
			return;
		
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			
			for (ItemRelatedTable table : TableOptimizer.getItemRelatedTables())
			{
				statement = con.prepareStatement(table.getDeleteQuery());
				statement.setInt(1, objid);
				statement.execute();
				statement.close();
			}
			
			for (CharacterRelatedTable table : TableOptimizer.getCharacterRelatedTables())
			{
				statement = con.prepareStatement(table.getDeleteQuery());
				statement.setInt(1, objid);
				statement.execute();
				statement.close();
			}
			
			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM characters WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error deleting character.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	public L2Player loadCharFromDisk(int charslot)
	{
		return L2Player.load(getObjectIdForSlot(charslot));
	}
	
	/**
	 * @param chars
	 */
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping = new int[chars.length];
		
		int i = 0;
		for (CharSelectInfoPackage element : chars)
			_charSlotMapping[i++] = element.getObjectId();
	}
	
	/**
	 * @param charslot
	 * @return
	 */
	private int getObjectIdForSlot(int charslot)
	{
		if (_charSlotMapping == null || charslot < 0 || charslot >= _charSlotMapping.length)
		{
			_log.warn(toString() + " tried to delete Character in slot " + charslot
				+ " but no characters exits at that slot.");
			return -1;
		}
		
		return _charSlotMapping[charslot];
	}
	
	@Override
	public String toString()
	{
		L2TextBuilder tb = L2TextBuilder.newInstance();
		
		tb.append("[State: ").append(getState());
		
		String host = getHostAddress();
		if (host != null)
			tb.append(" | IP: ").append(String.format("%-15s", host));
		
		String account = getAccountName();
		if (account != null)
			tb.append(" | Account: ").append(String.format("%-15s", account));
		
		L2Player player = getActiveChar();
		if (player != null)
			tb.append(" | Character: ").append(String.format("%-15s", player.getName()));
		
		tb.append("]");
		
		return tb.moveToString();
	}
	
	public boolean isProtocolOk()
	{
		return _protocol;
	}
	
	public void setProtocolOk(boolean b)
	{
		_protocol = b;
	}
	
	boolean isDisconnected()
	{
		return _disconnected;
	}
	
	void setDisconnected()
	{
		LoginServerThread.getInstance().sendLogout(getAccountName());
		
		_disconnected = true;
	}
	
	void execute(L2GameClientPacket rp)
	{
		getPacketQueue().execute(rp);
	}
	
	private FIFORunnableQueue<Runnable> _packetQueue;
	
	public FIFORunnableQueue<Runnable> getPacketQueue()
	{
		if (_packetQueue == null)
			_packetQueue = new FIFORunnableQueue<Runnable>() {};
		
		return _packetQueue;
	}
	
	private final class ServerPacketQueue extends FastList<L2GameServerPacket> implements Runnable
	{
		private static final long serialVersionUID = 6715576112277597425L;
		
		public ServerPacketQueue()
		{
			ThreadPoolManager.getInstance().schedule(this, Config.ENTERWORLD_TICK);
		}
		
		@Override
		public void run()
		{
			synchronized (L2GameClient.this)
			{
				if (_serverPacketQueue != this)
					return;
				
				if (isEmpty())
				{
					_serverPacketQueue = null;
					return;
				}
				
				int pn = size();
				if (pn > Config.ENTERWORLD_PPT)
					pn = Config.ENTERWORLD_PPT;
				
				for (int i = 0; i < pn; i++)
				{
					try
					{
						sendPacketImpl(removeFirst());
					}
					catch (Exception e)
					{
						_log.error("GDQ noob error (report in forum!) i=" + i + ", pn=" + pn, e);
					}
				}
				
				ThreadPoolManager.getInstance().schedule(this, Config.ENTERWORLD_TICK);
			}
		}
	}
	
	private ServerPacketQueue _serverPacketQueue;
	
	@Override
	public void sendPacket(L2GameServerPacket sp)
	{
		if (_serverPacketQueue != null)
		{
			synchronized (this)
			{
				if (_serverPacketQueue != null)
				{
					_serverPacketQueue.add(sp);
					return;
				}
			}
		}
		
		sendPacketImpl(sp);
	}
	
	public void initServerPacketQueue()
	{
		if (!Config.ENTERWORLD_QUEUING)
			return;
		
		if (_serverPacketQueue == null)
		{
			synchronized (this)
			{
				if (_serverPacketQueue == null)
				{
					_serverPacketQueue = new ServerPacketQueue();
				}
			}
		}
	}
	
	/**
	 * {@link RunnableStatsManager} used here mostly for counting, since constructors - usually the longest parts - are
	 * excluded.
	 */
	private void sendPacketImpl(L2GameServerPacket sp)
	{
		final long begin = System.nanoTime();
		final L2Player activeChar = getActiveChar();
		
		try
		{
			if (isDisconnected())
				return;
			
			if (!sp.canBeSentTo(this, activeChar))
				return;
			
			sp.prepareToSend(this, activeChar);
			
			super.sendPacket(sp);
			
			sp.packetSent(this, activeChar);
		}
		finally
		{
			RunnableStatsManager.handleStats(sp.getClass(), "runImpl()", System.nanoTime() - begin);
		}
	}
	
	void close(boolean toLoginScreen)
	{
		super.close(toLoginScreen ? ServerClose.STATIC_PACKET : LeaveWorld.STATIC_PACKET);
		
		setDisconnected();
	}
	
	@Override
	public void close(L2GameServerPacket sp)
	{
		new Disconnection(this).defaultSequence(false);
	}
	
	@Override
	public void closeNow()
	{
		new Disconnection(this).defaultSequence(false);
	}
	
	@Override
	protected L2GameServerPacket getDefaultClosePacket()
	{
		return LeaveWorld.STATIC_PACKET;
	}
	
	@Override
	protected void onDisconnection()
	{
		new Disconnection(this).onDisconnection();
		
		setDisconnected();
	}
	
	@Override
	protected void onForcedDisconnection()
	{
		if (_log.isDebugEnabled())
			_log.info("Client " + toString() + " disconnected abnormally.");
	}

	@Override
	protected String getUID()
	{
		return getAccountName();
	}
	
	public void setClientTracert(int[][] tracert)
	{
		_trace = tracert;
	}

	public int[][] getTrace()
	{
		return _trace;
	}
	
	public final FloodProtector getFloodProtector()
	{
		return _floodProtector;
	}

	public final boolean handleCheat(String punishment)
	{
		if (_activeChar != null)
		{
			Util.handleIllegalPlayerAction(_activeChar, toString() + ": " + punishment, Config.DEFAULT_PUNISH);
			return true;
		}

		Log logAudit = LogFactory.getLog("audit");
		
		logAudit.warn("AUDIT: Client " + toString() + " kicked for reason: " + punishment);
		closeNow();
		
		return false;
	}
}
