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

/**
 * A2 00 FF 6F 7F
 * 
 * Format: (s) cccc
 * 
 * @author  DaDummy
 */
public class TutorialEnableClientEvent extends L2GameServerPacket
{
    private static final String _S__A2_TUTORIALENABLECLIENTEVENT = "[S] a2 TutorialEnableClientEvent";
    private final int _event;
    
    public TutorialEnableClientEvent(int event)
    {
        _event = event;
    }
    
    /**
     * @see net.l2emuproject.gameserver.network.serverpackets.ServerBasePacket#writeImpl()
     */
    @Override
    protected final void writeImpl()
    {
        writeC(0xA8);
        writeD(_event);
    }

    /**
     * @see net.l2emuproject.gameserver.network.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _S__A2_TUTORIALENABLECLIENTEVENT;
    }
}
