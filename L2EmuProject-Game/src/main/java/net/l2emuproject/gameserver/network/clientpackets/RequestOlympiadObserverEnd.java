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

/**
 * format ch
 * c: (id) 0xD0
 * h: (subid) 0x12
 * @author -Wooden-
 *
 */
public class RequestOlympiadObserverEnd extends L2GameClientPacket
{
	private static final String _C__D0_12_REQUESTOLYMPIADOBSERVEREND = "[C] D0:12 RequestOlympiadObserverEnd";
	/**
	 * @param buf
	 * @param client
	 */
	@Override
	protected void readImpl()
	{
		// trigger
	}


	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (activeChar.getPlayerObserver().inObserverMode()) activeChar.getPlayerOlympiad().leaveOlympiadObserverMode();
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.BasePacket#getType()
	 */
	@Override

	public String getType()
	{
		return _C__D0_12_REQUESTOLYMPIADOBSERVEREND;
	}
}
