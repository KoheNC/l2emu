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

import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;

public class RequestDeleteMacro extends L2GameClientPacket
{
	private static final String _C__C2_REQUESTDELETEMACRO = "[C] C2 RequestDeleteMacro";

	private int _id;

	/**
	 * packet type id 0xc2
	 * 
	 * sample
	 * 
	 * c2
	 * d // macro id
	 * 
	 * format:		cd
	 * @param decrypt
	 */
    @Override
    protected void readImpl()
    {
		_id = readD();
	}

    @Override
    protected void runImpl()
	{
		if (getClient().getActiveChar() != null)
			getClient().getActiveChar().getPlayerSettings().deleteMacro(_id);
		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__C2_REQUESTDELETEMACRO;
	}
}
