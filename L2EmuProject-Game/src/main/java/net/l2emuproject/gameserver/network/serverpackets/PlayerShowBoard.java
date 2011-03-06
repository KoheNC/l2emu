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

import net.l2emuproject.gameserver.network.L2GameClient;
import net.l2emuproject.gameserver.system.util.StringUtil;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Unknown
 *	<br> Remade by lord_rex for L2EmuProject
 */
public final class PlayerShowBoard extends L2GameServerPacket
{
	private static final String		_S__6E_SHOWBOARD	= "[S] 6e ShowBoard";

	private final StringBuilder		_htmlCode;

	private static final String[]	HEADER				=
														{
			"bypass _bbshome",
			"bypass _bbsgetfav",
			"bypass _bbsloc",
			"bypass _bbsclan",
			"bypass _bbsmemo",
			"bypass _bbsmail",
			"bypass _bbsfriends",
			"bypass _bbs_add_fav"						};

	public PlayerShowBoard(final String htmlCode, final String id)
	{
		_htmlCode = StringUtil.startAppend(500, id, "\u0008", htmlCode);
	}

	public PlayerShowBoard(final String html)
	{
		this(html, "1001");
	}

	public PlayerShowBoard(final List<String> arg)
	{
		_htmlCode = StringUtil.startAppend(500, "1002\u0008");
		for (String str : arg)
			StringUtil.append(_htmlCode, str, " \u0008");
	}

	@Override
	protected final void writeImpl(L2GameClient client, L2Player player)
	{
		writeC(0x7b);
		writeC(0x01); // c4 1 to show community 00 to hide

		writeS(HEADER[0]);
		writeS(HEADER[1]);
		writeS(HEADER[2]);
		writeS(HEADER[3]);
		writeS(HEADER[4]);
		writeS(HEADER[5]);
		writeS(HEADER[6]);
		writeS(HEADER[7]);

		if (_htmlCode.length() < 8192)
			writeS(_htmlCode.toString());
		else
			writeS("<html><body>Html is too long!</body></html>");
	}

	@Override
	public final String getType()
	{
		return _S__6E_SHOWBOARD;
	}
}