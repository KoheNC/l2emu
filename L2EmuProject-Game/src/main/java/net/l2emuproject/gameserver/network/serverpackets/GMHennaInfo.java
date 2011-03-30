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
package net.l2emuproject.gameserver.network.serverpackets;

import net.l2emuproject.gameserver.templates.item.L2Henna;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 *
 * @author  KenM
 */
public class GMHennaInfo extends L2GameServerPacket
{
    private final static String S_F0_GMHENNAINFO = "[S] F0 GMHennaInfo";
    private final L2Player _activeChar;
    private final L2Henna[] _hennas = new L2Henna[3];
    private int _count = 0;
    
    public GMHennaInfo(L2Player activeChar)
    {
        _activeChar = activeChar;
        
        for (int i = 0; i < 3; i++)
        {
            L2Henna h = _activeChar.getPlayerHenna().getHenna(i+1);
            if (h != null)
                _hennas[_count++] = h;
        }
    }
    
    /**
     * @see net.l2emuproject.gameserver.serverpackets.L2GameServerPacket#getPostType()
     */
    @Override
    public String getType()
    {
        return S_F0_GMHENNAINFO;
    }

    /**
     * @see net.l2emuproject.gameserver.serverpackets.L2GameServerPacket#writeImpl()
     */
    @Override
    protected void writeImpl()
    {
        writeC(0xf0);
        
        writeC(_activeChar.getPlayerHenna().getHennaStatINT());
        writeC(_activeChar.getPlayerHenna().getHennaStatSTR());
        writeC(_activeChar.getPlayerHenna().getHennaStatCON());
        writeC(_activeChar.getPlayerHenna().getHennaStatMEN());
        writeC(_activeChar.getPlayerHenna().getHennaStatDEX());
        writeC(_activeChar.getPlayerHenna().getHennaStatWIT());
        writeD(3); // slots?
        writeD(_count); //size
        for (int i = 0; i < _count; i++)
        {
            writeD(_hennas[i].getSymbolId());
            writeD(_hennas[i].getSymbolId());
        }
    }
}
