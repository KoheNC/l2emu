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
import net.l2emuproject.gameserver.model.zone.L2Zone;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExShowReceivedPostList;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Migi, DS
 */
public final class RequestReceivedPostList extends L2GameClientPacket
{
	private static final String	_C__D0_67_REQUESTRECEIVEDPOSTLIST	= "[C] D0:67 RequestReceivedPostList";
	
	@Override
	protected void readImpl()
	{
		// trigger packet
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || !Config.ALLOW_MAIL)
			return;
		
		if (!activeChar.isInsideZone(L2Zone.FLAG_PEACE))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE));
			return;
		}
		
		activeChar.sendPacket(new ExShowReceivedPostList(activeChar.getObjectId()));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_67_REQUESTRECEIVEDPOSTLIST;
	}
}
