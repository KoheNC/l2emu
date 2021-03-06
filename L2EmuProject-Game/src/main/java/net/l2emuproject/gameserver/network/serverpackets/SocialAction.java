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

import net.l2emuproject.gameserver.world.object.L2Character;

public final class SocialAction extends L2GameServerPacket
{
	private static final String	_S__3D_SOCIALACTION	= "[S] 2D SocialAction";

	private final int			_objectId;
	private final int			_actionId;

	public static final int		LEVEL_UP			= 2122;

	public SocialAction(final L2Character cha, final int actionId)
	{
		_objectId = cha.getObjectId();
		_actionId = actionId;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x27);
		writeD(_objectId);
		writeD(_actionId);
	}

	@Override
	public String getType()
	{
		return _S__3D_SOCIALACTION;
	}
}
