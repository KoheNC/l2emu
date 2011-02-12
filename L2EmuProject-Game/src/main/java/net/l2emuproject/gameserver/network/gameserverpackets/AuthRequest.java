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
package net.l2emuproject.gameserver.network.gameserverpackets;

public final class AuthRequest extends GameServerBasePacket
{
	public AuthRequest(int id, boolean acceptAlternate, byte[] hexid, String _gsNetConfig1, String _gsNetConfig2,
			int port, boolean reserveHost, int maxplayer)
	{
		super(0x01);
		writeC(id);
		writeC(acceptAlternate ? 0x01 : 0x00);
		writeC(reserveHost ? 0x01 : 0x00);
		writeS(_gsNetConfig1);
		writeS(_gsNetConfig2);
		writeH(port);
		writeD(maxplayer);
		writeD(hexid.length);
		writeB(hexid);
	}
}
