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

import net.l2emuproject.gameserver.SevenSigns;
import net.l2emuproject.gameserver.network.serverpackets.SSQStatus;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Seven Signs Record Update Request
 * 
 * packet type id 0xc7
 * format: cc
 * 
 * @author Tempy
 */
public class RequestSSQStatus extends L2GameClientPacket
{
	private static final String	_C__C7_RequestSSQStatus	= "[C] C7 RequestSSQStatus";

	private int					_page;

	@Override
	protected void readImpl()
	{
		_page = readC();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if ((SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod()) && _page == 4)
		{
			sendAF();
			return;
		}

		sendPacket(new SSQStatus(activeChar, _page));
		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__C7_RequestSSQStatus;
	}
}
