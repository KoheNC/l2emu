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

import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author godson
 */
public final class ExOlympiadUserInfo extends L2GameServerPacket
{
	// chcdSddddd
	private static final String _S__FE_29_OLYMPIADUSERINFO = "[S] FE:7A ExOlympiadUserInfo";
	
	private final int _side; // 1 = right, 2 = left
	private final L2Player _player;
	
	public ExOlympiadUserInfo(L2Player player)
	{
		this(player, player.getPlayerOlympiad().getOlympiadSide());
	}
	
	public ExOlympiadUserInfo(L2Player player, int side)
	{
		_player = player;
		_side = side;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x7a);
		writeC(_side);
		writeD(_player.getObjectId());
		writeS(_player.getName());
		writeD(_player.getClassId().getId());
		writeD((int)_player.getStatus().getCurrentHp());
		writeD(_player.getMaxHp());
		writeD((int)_player.getStatus().getCurrentCp());
		writeD(_player.getMaxCp());
	}
	
	@Override
	public String getType()
	{
		return _S__FE_29_OLYMPIADUSERINFO;
	}
}
