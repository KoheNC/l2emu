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
 * @author Kerberos
 */
public class ExBrExtraUserInfo extends L2GameServerPacket
{
	private final int	_charObjId;
	private final int	_val;
	
	public ExBrExtraUserInfo(L2Player player)
	{
		_charObjId = player.getObjectId();
		_val = player.getAfroHaircutId();
	}
	
	/**
	 * This packet should belong to Quest windows, not UserInfo in T3.
	 */
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0xcf);
		writeD(_charObjId); // object id of player
		writeD(_val); // afro hair cut
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.l2emuproject.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] FE:CF ExBrExtraUserInfo";
	}
}
