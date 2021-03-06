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

import net.l2emuproject.gameserver.services.shortcuts.L2ShortCut;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 *
 * ShortCutInit
 * format   d *(1dddd)/(2ddddd)/(3dddd)
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShortCutInit extends L2GameServerPacket
{
	private static final String _S__57_SHORTCUTINIT = "[S] 45 ShortCutInit";

	private final L2ShortCut[] _shortCuts;

	public ShortCutInit(L2Player activeChar)
	{
		_shortCuts = activeChar.getPlayerSettings().getAllShortCuts();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x45);
		writeD(_shortCuts.length);

		for (L2ShortCut sc : _shortCuts)
		{
			writeD(sc.getType());
			writeD(sc.getSlot() + sc.getPage() * 12);
			
			switch(sc.getType())
			{
			case L2ShortCut.TYPE_ITEM: //1
				writeD(sc.getId());
				writeD(0x01);
				writeD(-1);
				writeD(0x00);
				writeD(0x00);
				writeH(0x00);
				writeH(0x00);
				break;
			case L2ShortCut.TYPE_SKILL: //2
				writeD(sc.getId());
				writeD(sc.getLevel());
				writeC(0x00); // C5
				writeD(0x01); // C6
				break;
			case L2ShortCut.TYPE_ACTION: //3
				writeD(sc.getId());
				writeD(0x01); // C6
				break;
			case L2ShortCut.TYPE_MACRO: //4
				writeD(sc.getId());
				writeD(0x01); // C6
				break;
			case L2ShortCut.TYPE_RECIPE: //5
				writeD(sc.getId());
				writeD(0x01); // C6
				break;
			default:
				writeD(sc.getId());
				writeD(0x01); // C6
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__57_SHORTCUTINIT;
	}
}
