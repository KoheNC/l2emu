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

import net.l2emuproject.gameserver.network.serverpackets.ShortCutRegister;
import net.l2emuproject.gameserver.services.shortcuts.L2ShortCut;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestShortCutReg extends L2GameClientPacket
{
	private static final String	_C__33_REQUESTSHORTCUTREG	= "[C] 33 RequestShortCutReg";

	private int					_type;
	private int					_id;
	private int					_slot;
	private int					_page;
	private int					_lvl;
	private int					_characterType;											// 1 - player, 2 - pet

	/**
	 * packet type id 0x33
	 * format:		cdddd
	 * @param rawPacket
	 */
	@Override
	protected void readImpl()
	{
		_type = readD();
		int slot = readD();
		_id = readD();
		_lvl = readD();
		_characterType = readD();

		_slot = slot % 12;
		_page = slot / 12;
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (_page > 10 || _page < 0)
		{
			sendAF();
			return;
		}

		switch (_type)
		{
		case 0x01: // item
		case 0x02: // skill
		case 0x03: // action
		case 0x04: // macro
		case 0x05: // recipe
		case 0x06: // Teleport Bookmark
		{
			L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, _lvl, _characterType);
			sendPacket(new ShortCutRegister(sc));
			activeChar.getPlayerSettings().registerShortCut(sc);
			break;
		}
		}

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__33_REQUESTSHORTCUTREG;
	}
}
