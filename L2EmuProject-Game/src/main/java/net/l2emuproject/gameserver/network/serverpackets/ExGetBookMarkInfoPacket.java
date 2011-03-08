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

import net.l2emuproject.gameserver.entity.player.PlayerTeleportBookmark.TeleportBookmark;
import net.l2emuproject.gameserver.network.L2GameClient;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author ShanSoft
 * @Structure d dd (ddddSdS)
 */
public final class ExGetBookMarkInfoPacket extends L2GameServerPacket
{
	private static final String	_S__FE_84_EXGETBOOKMARKINFOPACKET	= "[S] FE:84 ExGetBookMarkInfoPacket";

	@Override
	protected final void writeImpl(final L2GameClient client, final L2Player player)
	{
		writeC(0xFE);
		writeH(0x84);
		writeD(0x00); // Dummy
		writeD(player.getPlayerBookmark().getBookMarkSlot());
		writeD(player.getPlayerBookmark().getTpbookmark().size());

		for (TeleportBookmark tpbm : player.getPlayerBookmark().getTpbookmark())
		{
			writeD(tpbm.getId());
			writeD(tpbm.getX());
			writeD(tpbm.getY());
			writeD(tpbm.getZ());
			writeS(tpbm.getName());
			writeD(tpbm.getIcon());
			writeS(tpbm.getTag());
		}
	}

	@Override
	public final String getType()
	{
		return _S__FE_84_EXGETBOOKMARKINFOPACKET;
	}
}
