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

import java.nio.ByteBuffer;

import net.l2emuproject.Config;
import net.l2emuproject.loginserver.network.L2LoginClient.LoginClientState;
import net.l2emuproject.loginserver.network.clientpackets.AuthGameGuard;
import net.l2emuproject.loginserver.network.clientpackets.L2LoginClientPacket;
import net.l2emuproject.loginserver.network.clientpackets.RequestAuthLogin;
import net.l2emuproject.loginserver.network.clientpackets.RequestServerList;
import net.l2emuproject.loginserver.network.clientpackets.RequestServerLogin;
import net.l2emuproject.loginserver.network.clientpackets.RequestSubmitCardNo;
import net.l2emuproject.loginserver.network.serverpackets.L2LoginServerPacket;

import org.mmocore.network.IPacketHandler;


/**
 * Handler for packets received by Login Server
 * 
 * @author KenM
 */
public final class L2LoginPacketHandler implements
	IPacketHandler<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket>
{
	@Override
	public L2LoginClientPacket handlePacket(ByteBuffer buf, L2LoginClient client, final int opcode)
	{
		final LoginClientState state = client.getState();
		
		switch (state)
		{
			case CONNECTED:
				if (opcode == 0x07)
				{
					return new AuthGameGuard();
				}
				else
				{
					debugOpcode(buf, client, opcode);
				}
				break;
			case AUTHED_GG:
				if (opcode == 0x00)
				{
					return new RequestAuthLogin();
				}
				else
				{
					debugOpcode(buf, client, opcode);
				}
				break;
			case AUTHED_LOGIN:
				if (opcode == 0x05)
				{
					return new RequestServerList();
				}
				else if (opcode == 0x02)
				{
					return new RequestServerLogin();
				}
				else if (opcode == 0x06)
				{
					if (Config.SECURITY_CARD_LOGIN)
						return new RequestSubmitCardNo();
				}
				else
				{
					debugOpcode(buf, client, opcode);
				}
				break;
		}
		
		return null;
	}
	
	private void debugOpcode(ByteBuffer buf, L2LoginClient client, int opcode)
	{
		L2LoginSelectorThread.getInstance().printDebug(buf, client, opcode);
	}
}
