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

import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.model.clan.L2ClanMember;
import net.l2emuproject.gameserver.network.serverpackets.PledgeReceiveMemberInfo;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class RequestPledgeMemberInfo extends L2GameClientPacket
{
	private static final String _C__24_REQUESTJOINPLEDGE = "[C] 24 RequestPledgeMemberInfo";
	
	//private int _pledgeType;
	private String _target;
	
	@Override
	protected void readImpl()
	{
		/*_pledgeType = */readD();
		_target = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2Player activeChar = getActiveChar();
		if (activeChar == null)
			return;
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		
		final L2ClanMember cm = clan.getClanMember(_target);
		if (cm == null)
			return;
		
		sendPacket(new PledgeReceiveMemberInfo(cm));
	}
	
	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__24_REQUESTJOINPLEDGE;
	}
}
