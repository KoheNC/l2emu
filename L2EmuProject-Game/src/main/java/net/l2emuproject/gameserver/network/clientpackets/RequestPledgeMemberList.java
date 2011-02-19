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
import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.network.serverpackets.PledgeShowMemberListAll;

public class RequestPledgeMemberList extends L2GameClientPacket
{
	private static final String _C__REQUESTPLEDGEMEMBERLIST = "[C] 4D RequestPledgeMemberList c";
	
    @Override
    protected void readImpl()
    {
        // trigger
    }
    
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
			return;
		
		L2Clan clan = activeChar.getClan();
		if (clan != null)
			activeChar.sendPacket(new PledgeShowMemberListAll(clan));
	}
	
	@Override
	public String getType()
	{
		return _C__REQUESTPLEDGEMEMBERLIST;
	}
}
