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

import java.util.ArrayList;
import java.util.List;

import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * @author Kerberos
 */
public final class NpcSay extends L2GameServerPacket
{
	// dddS
	private static final String	_S__30_NPCSAY	= "[S] 30 NpcSay";
	private final int			_objectId;
	private final int			_textType;
	private final int			_npcId;
	private String				_text;

	private int					_npcString;
	private List<String>		_parameters;

	public NpcSay(final int objectId, final int messageType, final int npcId, final String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_npcString = -1;
		_text = text;
	}

	public NpcSay(final L2Npc npc, final String text)
	{
		this(npc.getObjectId(), 0, npc.getNpcId(), text);
	}

	public NpcSay(final int objectId, final int messageType, final int npcId, final int npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_npcString = npcString;
	}

	/**
	 * String parameter for argument S1,S2,.. in npcstring-e.dat
	 * @param text
	 */
	public final void addStringParameter(final String text)
	{
		if (_parameters == null)
			_parameters = new ArrayList<String>();
		_parameters.add(text);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x30);
		writeD(_objectId);
		writeD(_textType);
		writeD(_npcId);
		writeD(_npcString);
		if (_npcString == -1)
			writeS(_text);
		else
		{
			if (_parameters != null)
				for (String s : _parameters)
					writeS(s);
		}
	}

	@Override
	public final String getType()
	{
		return _S__30_NPCSAY;
	}
}
