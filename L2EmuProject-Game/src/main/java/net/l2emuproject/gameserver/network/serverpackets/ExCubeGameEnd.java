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

/**
 * Format: (chd) ddd
 * d: Winner Team
 * 
 * @author mrTJO
 */
public final class ExCubeGameEnd extends L2GameServerPacket
{
	private static final String	_S__FE_98_01_EXCUBEGAMEEND	= "[S] FE:98:01 ExCubeGameEnd";

	private final boolean		_isRedTeamWin;

	/**
	 * Show Minigame Results
	 * 
	 * @param isRedTeamWin: Is Red Team Winner?
	 */
	public ExCubeGameEnd(boolean isRedTeamWin)
	{
		_isRedTeamWin = isRedTeamWin;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x98);
		writeD(0x01);

		writeD(_isRedTeamWin ? 0x01 : 0x00);
	}

	@Override
	public String getType()
	{
		return _S__FE_98_01_EXCUBEGAMEEND;
	}
}
