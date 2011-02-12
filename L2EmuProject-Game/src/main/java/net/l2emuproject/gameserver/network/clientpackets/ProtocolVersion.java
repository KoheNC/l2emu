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
package net.l2emuproject.gameserver.network.clientpackets;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.network.serverpackets.KeyPacket;

/**
 * This class represents the first packet that is sent by the client to the game server.
 */
public class ProtocolVersion extends L2GameClientPacket
{
	private static final String	_C__PROTOCOLVERSION	= "[C] 0E ProtocolVersion c[unk] (changes often)";

	private int					_version;

	@Override
	protected void readImpl()
	{
		_version = readD();
		/* A block of bytes
		byte[] b = new byte[260];
		readB(b);
		_log.info(HexUtil.printData(b));
		*/
		skipAll();
	}

	@Override
	protected void runImpl()
	{
		// this packet is never encrypted
		if (_version == -2)
		{
			if (_log.isDebugEnabled())
				_log.debug("Ping received.");
			// this is just a ping attempt from the new C2 client
			getClient().closeNow();
		}
		else if (!Config.PROTOCOL_LIST.contains(_version))
		{
			_log.warn("Client " + getClient() + " have wrong protocol.");
			KeyPacket pk = new KeyPacket(getClient().enableCrypt(), 0);
			getClient().sendPacket(pk);
			getClient().setProtocolOk(false);
		}
		else
		{
			if (_log.isDebugEnabled())
				_log.debug("Client Protocol Revision is ok: " + _version);

			KeyPacket pk = new KeyPacket(getClient().enableCrypt(), 1);
			getClient().sendPacket(pk);
			getClient().setProtocolOk(true);
		}
	}

	@Override
	public String getType()
	{
		return _C__PROTOCOLVERSION;
	}
}
