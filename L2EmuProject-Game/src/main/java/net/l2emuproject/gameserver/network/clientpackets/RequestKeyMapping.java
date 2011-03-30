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
import net.l2emuproject.gameserver.network.serverpackets.ExUiSetting;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 *
 * @author  KenM
 */
public class RequestKeyMapping extends L2GameClientPacket
{

    /**
     * @see net.l2emuproject.gameserver.clientpackets.L2GameClientPacket#getPostType()
     */
    @Override
    public String getType()
    {
        return "[C] D0:21 RequestKeyMapping";
    }

    /**
     * @see net.l2emuproject.gameserver.clientpackets.L2GameClientPacket#readImpl()
     */
    @Override
    protected void readImpl()
    {
        // trigger (no data)
    }

    /**
     * @see net.l2emuproject.gameserver.clientpackets.L2GameClientPacket#runImpl()
     */
	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (Config.STORE_UI_SETTINGS)
			activeChar.sendPacket(new ExUiSetting(activeChar));
	}
}
