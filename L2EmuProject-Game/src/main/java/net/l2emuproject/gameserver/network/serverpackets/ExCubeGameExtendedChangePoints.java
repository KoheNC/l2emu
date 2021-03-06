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
 * Format: (chd) dddddd
 * d: Time Left
 * d: Blue Points
 * d: Red Points
 * d: Player Team
 * d: Player Object ID
 * d: Player Points
 * 
 * @author mrTJO
 */
public final class ExCubeGameExtendedChangePoints extends L2GameServerPacket
{
	private static final String	_S__FE_98_00_EXCUBEGAMEEXTENDEDCHANGEPOINTS	= "[S] FE:98:00 ExCubeGameExtendedChangePoints";

	private final int			_timeLeft;
	private final int			_bluePoints;
	private final int			_redPoints;
	private final boolean		_isRedTeam;
	private final L2Player	_player;
	private final int			_playerPoints;

	/**
	 * Update a Secret Point Counter (used by client when receive ExCubeGameEnd)
	 * 
	 * @param timeLeft Time Left before Minigame's End
	 * @param bluePoints Current Blue Team Points
	 * @param redPoints Current Blue Team points
	 * @param isRedTeam Is Player from Red Team?
	 * @param player Player Instance
	 * @param playerPoints Current Player Points
	 */
	public ExCubeGameExtendedChangePoints(int timeLeft, int bluePoints, int redPoints, boolean isRedTeam, L2Player player, int playerPoints)
	{
		_timeLeft = timeLeft;
		_bluePoints = bluePoints;
		_redPoints = redPoints;
		_isRedTeam = isRedTeam;
		_player = player;
		_playerPoints = playerPoints;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x98);
		writeD(0x00);

		writeD(_timeLeft);
		writeD(_bluePoints);
		writeD(_redPoints);

		writeD(_isRedTeam ? 0x01 : 0x00);
		writeD(_player.getObjectId());
		writeD(_playerPoints);
	}

	@Override
	public String getType()
	{
		return _S__FE_98_00_EXCUBEGAMEEXTENDEDCHANGEPOINTS;
	}
}
