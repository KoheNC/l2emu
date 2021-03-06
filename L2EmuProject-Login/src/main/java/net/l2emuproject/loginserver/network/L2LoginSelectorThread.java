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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.l2emuproject.loginserver.manager.BanManager;
import net.l2emuproject.loginserver.manager.LoginManager;
import net.l2emuproject.loginserver.network.clientpackets.L2LoginClientPacket;
import net.l2emuproject.loginserver.network.serverpackets.Init;
import net.l2emuproject.loginserver.network.serverpackets.L2LoginServerPacket;
import net.l2emuproject.network.mmocore.FloodManager.ErrorMode;
import net.l2emuproject.network.mmocore.IPacketHandler;
import net.l2emuproject.network.mmocore.SelectorConfig;
import net.l2emuproject.network.mmocore.SelectorThread;
import net.l2emuproject.tools.util.HexUtil;
import net.l2emuproject.util.concurrent.ExecuteWrapper;

import org.apache.commons.lang.StringUtils;

public final class L2LoginSelectorThread extends SelectorThread<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket>
{
	private static final class SingletonHolder
	{
		private static final L2LoginSelectorThread	INSTANCE;

		static
		{
			final SelectorConfig sc = new SelectorConfig();

			try
			{
				INSTANCE = new L2LoginSelectorThread(sc, new L2LoginPacketHandler());
			}
			catch (final Exception e)
			{
				throw new Error(e);
			}
		}
	}

	public static L2LoginSelectorThread getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private L2LoginSelectorThread(SelectorConfig sc, IPacketHandler<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket> packetHandler) throws IOException
	{
		super(sc, packetHandler);
	}

	public void printDebug(ByteBuffer buf, L2LoginClient client, int opcode)
	{
		report(ErrorMode.INVALID_OPCODE, client, null, null);

		//if (!Config.PACKET_HANDLER_DEBUG)
		//	return;

		final StringBuilder sb = new StringBuilder("Unknown Packet: ");
		sb.append("0x").append(Integer.toHexString(opcode));
		sb.append(", Client: ").append(client);
		_log.info(sb);

		final byte[] array = new byte[buf.remaining()];
		buf.get(array);
		for (final String line : StringUtils.split(HexUtil.printData(array), "\n"))
			_log.info(line);
	}

	// ==============================================

	@Override
	protected L2LoginClient createClient(SocketChannel socketChannel) throws ClosedChannelException
	{
		final L2LoginClient client = new L2LoginClient(this, socketChannel);
		client.sendPacket(new Init(client));
		LoginManager.getInstance().addConnection(client);
		return client;
	}

	private final ThreadPoolExecutor	_generalPacketsThreadPool	= new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
																			new SynchronousQueue<Runnable>());

	@Override
	protected void executePacket(L2LoginClientPacket packet)
	{
		_generalPacketsThreadPool.execute(new ExecuteWrapper(packet));
	}

	// ==============================================

	@Override
	public boolean acceptConnectionFrom(SocketChannel sc)
	{
		if (!super.acceptConnectionFrom(sc))
			return false;

		// Ignore permabanned IPs
		if (BanManager.getInstance().isRestrictedAddress(sc.socket().getInetAddress()))
			return false;

		return true;
	}
}
