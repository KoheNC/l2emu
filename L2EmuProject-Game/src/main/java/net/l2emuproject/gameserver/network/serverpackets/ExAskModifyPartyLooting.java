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
 * @author JIV
 */
public final class ExAskModifyPartyLooting extends L2GameServerPacket
{
	private static final String	TYPE	= "[S] FE:BE ExAskModifyPartyLooting";

	private final String		_requestor;
	private final byte			_mode;

	public ExAskModifyPartyLooting(String name, byte mode)
	{
		_requestor = name;
		_mode = mode;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xBE);
		writeS(_requestor);
		writeD(_mode);
	}

	@Override
	public final String getType()
	{
		return TYPE;
	}
}
