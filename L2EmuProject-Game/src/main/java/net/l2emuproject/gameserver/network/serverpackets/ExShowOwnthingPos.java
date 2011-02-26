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

import javolution.util.FastList;
import net.l2emuproject.gameserver.manager.TerritoryWarManager;
import net.l2emuproject.gameserver.model.TerritoryWard;

/**
 * Format: (ch) d[dddd]
 *
 * @author  -Gigiikun-
 */
public class ExShowOwnthingPos extends L2GameServerPacket
{
	private static final String	_S__FE_93_EXSHOWOWNTHINGPOS	= "[S] FE:93 ExShowOwnthingPos";

	public ExShowOwnthingPos()
	{
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x93);

		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			FastList<TerritoryWard> territoryWardList = TerritoryWarManager.getInstance().getAllTerritoryWards();
			writeD(territoryWardList.size());
			for (TerritoryWard ward : territoryWardList)
			{
				writeD(ward.getTerritoryId());

				if (ward.getNpc() != null)
				{
					writeD(ward.getNpc().getX());
					writeD(ward.getNpc().getY());
					writeD(ward.getNpc().getZ());
				}
				else if (ward.getPlayer() != null)
				{
					writeD(ward.getPlayer().getX());
					writeD(ward.getPlayer().getY());
					writeD(ward.getPlayer().getZ());
				}
				else
				{
					writeD(0);
					writeD(0);
					writeD(0);
				}
			}
		}
		else
		{
			writeD(0);
			writeD(0);
		}
	}

	@Override
	public String getType()
	{
		return _S__FE_93_EXSHOWOWNTHINGPOS;
	}
}
