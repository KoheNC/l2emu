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

import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.clan.L2ClanMember;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Format: (ch) dSdS
 * @author  -Wooden-
 */
public class RequestPledgeReorganizeMember extends L2GameClientPacket
{
	private static final String _C__D0_24_REQUESTPLEDGEREORGANIZEMEMBER = "[C] D0:24 RequestPledgeReorganizeMember";

	private int _isMemberSelected;
	private String _memberName;
	private int _newPledgeType;
	private String _selectedMember;

	@Override
	protected void readImpl()
	{
		_isMemberSelected = readD();
		_memberName = readS();
		_newPledgeType = readD();
		_selectedMember = readS();
	}

	/**
	 * @see net.l2emuproject.gameserver.network.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (_isMemberSelected == 0)
			return;

		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		//do we need powers to do that??
		L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		L2ClanMember member1 = clan.getClanMember(_memberName);
		L2ClanMember member2 = clan.getClanMember(_selectedMember);
		if (member1 == null || member2 == null)
			return;
		int oldPledgeType = member1.getSubPledgeType();
		if (oldPledgeType == _newPledgeType)
			return;
		member1.setSubPledgeType(_newPledgeType);
		member2.setSubPledgeType(oldPledgeType);
		clan.broadcastClanStatus();
	}
	
	/**
	 * @see net.l2emuproject.gameserver.network.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_24_REQUESTPLEDGEREORGANIZEMEMBER;
	}
}
