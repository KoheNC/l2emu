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

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.serverpackets.HennaRemoveList;

public final class RequestHennaRemoveList extends L2GameClientPacket
{
	private static final String _C__BA_RequestHennaRemoveList = "[C] ba RequestHennaRemoveList";

	//private int _unknown;

	@Override
	protected void readImpl()
	{
		/*_unknown = */readD(); // ?? just a trigger packet
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		sendPacket(new HennaRemoveList(activeChar));

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__BA_RequestHennaRemoveList;
	}
}
