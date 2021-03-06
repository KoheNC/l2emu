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

import net.l2emuproject.gameserver.datatables.HennaTable;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.HennaItemRemoveInfo;
import net.l2emuproject.gameserver.templates.item.L2Henna;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * RequestHennaItemRemoveInfo
 * Format cd
 */
public final class RequestHennaItemRemoveInfo extends L2GameClientPacket
{
	private static final String _C__BB_RequestHennaItemRemoveInfo = "[C] 71 RequestHennaItemRemoveInfo";

	private int _symbolId;

	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null || _symbolId == 0) // 0 = player closed window
			return;

		L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);
		if (template == null)
		{
			requestFailed(SystemMessageId.SYMBOL_NOT_FOUND);
			return;
		}

		sendPacket(new HennaItemRemoveInfo(template, activeChar));

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__BB_RequestHennaItemRemoveInfo;
	}
}
