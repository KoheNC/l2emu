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
import net.l2emuproject.gameserver.model.L2ItemInstance;
import net.l2emuproject.gameserver.model.L2Object;
import net.l2emuproject.gameserver.model.L2World;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.serverpackets.ExRpItemLink;
import net.l2emuproject.gameserver.util.IllegalPlayerAction;
import net.l2emuproject.gameserver.util.Util;

/**
 *
 * @author KenM
 */
public class RequestExRqItemLink extends L2GameClientPacket
{
	private static final String _C__D0_1E_REQUESTEXRQITEMLINK = "[C] DO:1E RequestExRqItemLink";

    private int _objectId;

    @Override
    protected void readImpl()
    {
        _objectId = readD();
    }

    @Override
    protected void runImpl()
    {
    	L2PcInstance player = getClient().getActiveChar();
    	if (player == null)
    		return;

        L2Object object = L2World.getInstance().findObject(_objectId);
        if (object instanceof L2ItemInstance)
        	sendPacket(new ExRpItemLink((L2ItemInstance) object));
        else if (object != null && Config.BAN_CLIENT_EMULATORS)
        	Util.handleIllegalPlayerAction(player, "Fake item link packet! " + player,
					IllegalPlayerAction.PUNISH_KICKBAN);

        sendAF();
    }

    @Override
    public String getType()
    {
        return _C__D0_1E_REQUESTEXRQITEMLINK;
    }
}
