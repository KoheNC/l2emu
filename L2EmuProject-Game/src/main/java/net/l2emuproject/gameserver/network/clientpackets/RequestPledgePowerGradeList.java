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


import net.l2emuproject.gameserver.network.serverpackets.PledgePowerGradeList;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.clan.L2Clan.RankPrivs;
import net.l2emuproject.gameserver.world.object.L2Player;

public class RequestPledgePowerGradeList extends L2GameClientPacket
{
    private static final String _C__C0_REQUESTPLEDGEPOWER = "[C] C0 RequestPledgePowerGradeList";
    
    @Override
    protected void readImpl()
    {
        // trigger
    }

    @Override
    protected void runImpl()
    {
        L2Player player = getClient().getActiveChar();
        L2Clan clan = player.getClan();
        if (clan != null)
        {
            RankPrivs[] privs = clan.getAllRankPrivs();
            player.sendPacket(new PledgePowerGradeList(privs));
            //_log.warn("plegdepowergradelist send, privs length: "+privs.length);
        }
    }

    /* (non-Javadoc)
     * @see net.l2emuproject.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__C0_REQUESTPLEDGEPOWER;
    }
}
