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

import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * CDSDDSS -> (0xd5)(objId)(name)(0x00)(type)(speaker)(name)
 */
public class Snoop extends L2GameServerPacket
{
	private static final String _S__D5_SNOOP = "[S] D5 Snoop";
	
	private final L2Player _snooped;
	private final int _type;
	private final String _speaker;
	private final String _msg;
	
	public Snoop(L2Player snooped, SystemChatChannelId channel, String speaker, String msg)
	{
		_snooped = snooped;
		_type = channel.getId();
		_speaker = speaker;
		_msg = msg;
	}
	
	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xdb);
		writeD(_snooped.getObjectId());
		writeS(_snooped.getName());
		writeD(0);
		writeD(_type);
		writeS(_speaker);
		writeS(_msg);
	}
	
	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__D5_SNOOP;
	}
}
