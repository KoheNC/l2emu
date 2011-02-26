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

import net.l2emuproject.gameserver.manager.FortManager;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.ExShowFortressMapInfo;

/**
 * @author KenM
 */
public class RequestFortressMapInfo extends L2GameClientPacket
{
	private static final String	_C__D0_4B_REQUESTFORTRESSMAPINFO = "[C] D0:4B RequestFortressMapInfo";

    private int _fortressId;

    @Override
    protected void readImpl()
    {
        _fortressId = readD();
    }

    @Override
    protected void runImpl()
    {
    	if (getActiveChar() == null) return;

        sendPacket(new ExShowFortressMapInfo(FortManager.getInstance().getFortById(_fortressId)));
        sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Override
    public String getType()
    {
        return _C__D0_4B_REQUESTFORTRESSMAPINFO;
    }
}
