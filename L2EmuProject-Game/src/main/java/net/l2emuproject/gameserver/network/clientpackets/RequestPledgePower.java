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

import net.l2emuproject.gameserver.entity.clan.L2Clan;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ManagePledgePower;
import net.l2emuproject.gameserver.world.object.L2Player;

public class RequestPledgePower extends L2GameClientPacket
{
    private static final String _C__REQUESTPLEDGEPOWER = "[C] CC RequestPledgePower c[ddd]";
    
    private int _rank;
    private int _action;
    private int _privs;
    
    @Override
    protected void readImpl()
    {
        _rank = readD();
        _action = readD();
        if (_action == 2)
            _privs = readD();
        else
        	_privs = 0;
    }
    
    @Override
    protected void runImpl()
    {
        L2Player player = getActiveChar();
        if (player == null)
        	return;
        L2Clan clan = player.getClan();
        if (_action == 2)
        {
        	if (L2Clan.checkPrivileges(player, L2Clan.CP_CL_MANAGE_RANKS))
        		clan.setRankPrivs(_rank, _privs);
        	else
        		sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
        }
        else
            sendPacket(new ManagePledgePower(clan, _action, _rank));
    }
    
    @Override
    public String getType()
    {
        return _C__REQUESTPLEDGEPOWER;
    }
}
