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
import net.l2emuproject.gameserver.services.community.CommunityBoard;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Unknown
 *	<br> Remade by lord_rex for L2EmuProject
 */
public final class PlayerShowBoard extends L2GameServerPacket
{
	private static final String	_S__6E_SHOWBOARD	= "[S] 6e ShowBoard";

	private final String		_html;

	public PlayerShowBoard(final String html)
	{
		_html = html;
	}

	@Override
	public final boolean canBeSentTo(final L2GameClient client, final L2Player player)
	{
		return _html != null;
	}

	@Override
	protected final void writeImpl(final L2GameClient client, final L2Player player)
	{
		writeC(0x7b);
		writeC(0x01); // c4 1 to show community 00 to hide

		for (String line : CommunityBoard.HEADER)
			writeS(line);

		if (_html.length() > 8192)
			writeS("<html><body>Sorry, the HTML is too long!</body></html>");
		else
			writeS(_html);
	}

	@Override
	public final String getType()
	{
		return _S__6E_SHOWBOARD;
	}
}
