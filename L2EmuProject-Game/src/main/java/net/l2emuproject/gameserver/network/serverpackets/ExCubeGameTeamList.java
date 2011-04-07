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

import java.util.List;

import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Format: (chd) ddd[dS]d[dS]
 * d: unknown
 * d: always -1
 * d: blue players number
 * [
 * 		d: player object id
 * 		S: player name
 * ]
 * d: blue players number
 * [
 * 		d: player object id
 * 		S: player name
 * ]
 * 
 * @author mrTJO
 */
public final class ExCubeGameTeamList extends L2GameServerPacket
{
	private static final String			_S__FE_97_00_EXCUBEGAMETEAMLIST	= "[S] FE:97:00 ExCubeGameTeamList";

	// Players Lists
	private final List<L2Player>	_bluePlayers;
	private final List<L2Player>	_redPlayers;

	// Common Values
	private final int					_roomNumber;

	/**
	 * 
	 * Show Minigame Waiting List to Player
	 * 
	 * @param redPlayers Red Players List
	 * @param bluePlayers Blue Players List
	 * @param roomNumber Arena/Room ID
	 */
	public ExCubeGameTeamList(List<L2Player> redPlayers, List<L2Player> bluePlayers, int roomNumber)
	{
		_redPlayers = redPlayers;
		_bluePlayers = bluePlayers;
		_roomNumber = roomNumber - 1;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x97);
		writeD(0x00);

		writeD(_roomNumber);
		writeD(0xffffffff);

		writeD(_bluePlayers.size());
		for (L2Player player : _bluePlayers)
		{
			writeD(player.getObjectId());
			writeS(player.getName());
		}
		writeD(_redPlayers.size());
		for (L2Player player : _redPlayers)
		{
			writeD(player.getObjectId());
			writeS(player.getName());
		}
	}

	@Override
	public String getType()
	{
		return _S__FE_97_00_EXCUBEGAMETEAMLIST;
	}
}
