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
 * Format: (chd) ddd
 * d: Always -1
 * d: Player Team
 * d: Player Object ID
 * 
 * @author mrTJO
 */
public final class ExCubeGameRemovePlayer extends L2GameServerPacket
{
	private static final String	_S__FE_97_02_EXCUBEGAMEREMOVEPLAYER	= "[S] FE:97:02 ExCubeGameRemovePlayer";

	private final L2Player	_player;
	private final boolean		_isRedTeam;

	/**
	 * Remove Player from Minigame Waiting List
	 * 
	 * @param player: Player to Remove
	 * @param isRedTeam: Is Player from Red Team?
	 */
	public ExCubeGameRemovePlayer(L2Player player, boolean isRedTeam)
	{
		_player = player;
		_isRedTeam = isRedTeam;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x97);
		writeD(0x02);

		writeD(0xffffffff);

		writeD(_isRedTeam ? 0x01 : 0x00);
		writeD(_player.getObjectId());
	}

	@Override
	public String getType()
	{
		return _S__FE_97_02_EXCUBEGAMEREMOVEPLAYER;
	}
}
