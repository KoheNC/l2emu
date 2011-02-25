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

public final class HennaInfo extends L2GameServerPacket
{
	private static final String _S__E5_HennaInfo = "[S] E5 HennaInfo";

	private final L2Player _activeChar;
	private final L2Henna[] _hennas = new L2Henna[3];
	private int _count = 0;

	public HennaInfo(L2Player player)
	{
		_activeChar = player;

		for (int i = 1; i <= 3; i++)
		{
			L2Henna h = _activeChar.getPlayerHenna().getHenna(i);
			if (h != null)
				_hennas[_count++] = h;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xe5);
		writeC(_activeChar.getPlayerHenna().getHennaStatINT());	//equip INT
		writeC(_activeChar.getPlayerHenna().getHennaStatSTR());	//equip STR
		writeC(_activeChar.getPlayerHenna().getHennaStatCON());	//equip CON
		writeC(_activeChar.getPlayerHenna().getHennaStatMEN());	//equip MEM
		writeC(_activeChar.getPlayerHenna().getHennaStatDEX());	//equip DEX
		writeC(_activeChar.getPlayerHenna().getHennaStatWIT());	//equip WIT
		writeD(3); // slots?
		writeD(_count); //size
		for (int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].getSymbolId());
		}
	}

	@Override
	public String getType()
	{
		return _S__E5_HennaInfo;
	}
}
