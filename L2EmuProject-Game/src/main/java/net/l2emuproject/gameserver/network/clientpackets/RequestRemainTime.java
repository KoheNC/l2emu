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
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;

public class RequestRemainTime extends L2GameClientPacket
{
	private static final String _C__B2_REQUESTREMAINTIME = "[C] B2 RequestRemainTime";

	@Override
	protected void readImpl()
	{
		// Trigger packet
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getActiveChar();
		if (player == null)
			return;

		if (Config.SERVER_LIST_CLOCK)
			// we do not support limited time/week servers
			player.sendPacket(SystemMessageId.WEEKS_USAGE_TIME_FINISHED);
		else // verified
			player.sendPacket(SystemMessageId.RELAX_SERVER_ONLY);
	}

	@Override
	public String getType()
	{
		return _C__B2_REQUESTREMAINTIME;
	}
}
