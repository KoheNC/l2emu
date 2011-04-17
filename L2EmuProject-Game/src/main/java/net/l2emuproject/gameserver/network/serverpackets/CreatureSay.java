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

import net.l2emuproject.gameserver.network.L2GameClient;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class CreatureSay extends L2GameServerPacket
{
	private static final String			_S__4A_CREATURESAY	= "[S] 4A CreatureSay [ddss]";

	private final int					_objectId;
	private final SystemChatChannelId	_channel;
	private String						_charName;
	private int							_charId				= 0;
	private String						_text;
	private int							_textId				= 0;

	/**
	 * @param _characters
	 */
	public CreatureSay(int objectId, SystemChatChannelId channel, String charName, String text)
	{
		_objectId = objectId;
		_channel = channel;
		_charName = charName;
		_text = text;
	}

	public CreatureSay(int objectId, SystemChatChannelId channel, int charId, int textId)
	{
		_objectId = objectId;
		_channel = channel;
		_charId = charId;
		_textId = textId;
	}

	@Override
	public final void packetSent(L2GameClient client, L2Player activeChar)
	{
		if (activeChar != null)
			activeChar.broadcastSnoop(_channel, _charName, _text);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x4a);
		writeD(_objectId);
		writeD(_channel.getId());
		if (_charName != null)
			writeS(_charName);
		else
			writeD(_charId);
		writeD(-1); // High Five NPCString ID
		if (_text != null)
			writeS(_text);
		else
			writeD(_textId);
	}

	@Override
	public final String getType()
	{
		return _S__4A_CREATURESAY;
	}
}
