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

import net.l2emuproject.gameserver.network.serverpackets.ExShowSeedMapInfo;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * This packet is sent by the client every time the world map is
 * opened. Server replies only if client is in gracia continent.
 * @author savormix
 */
public final class RequestSeedPhase extends L2GameClientPacket
{
	private static final String	_C__REQUESTSEEDPHASE	= "[C] D0:63 RequestSeedPhase ch";

	@Override
	protected void readImpl()
	{
		// trigger packet
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getActiveChar();
		if (player == null)
			return;

		// should be sent only in Gracia
		sendPacket(ExShowSeedMapInfo.PACKET);
	}

	@Override
	public String getType()
	{
		return _C__REQUESTSEEDPHASE;
	}
}
