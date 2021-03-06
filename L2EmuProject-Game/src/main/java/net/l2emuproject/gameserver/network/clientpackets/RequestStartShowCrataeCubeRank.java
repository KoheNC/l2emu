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

import net.l2emuproject.gameserver.network.serverpackets.ExPVPMatchCCRecord;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Client requests ExPVPMatchCCRecord to be sent.
 * @author savormix
 */
public final class RequestStartShowCrataeCubeRank extends L2GameClientPacket
{
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

		sendPacket(new ExPVPMatchCCRecord(2, ExPVPMatchCCRecord.EMPTY_ARRAY));

		sendAF();
	}
}
