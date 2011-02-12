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

public final class ExBrPremiumState extends L2GameServerPacket
{
	private static final String	_S__EXBRPREMIUMSTATE	= "[S] FE:CD ExBR_PremiumState ch[dc]";

	private final int			_objectId;
	private final int			_state;

	public ExBrPremiumState(int objectId, int state)
	{
		_objectId = objectId;
		_state = state;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0xcd);

		writeD(_objectId);
		writeC(_state);
	}

	@Override
	public final String getType()
	{
		return _S__EXBRPREMIUMSTATE;
	}
}
