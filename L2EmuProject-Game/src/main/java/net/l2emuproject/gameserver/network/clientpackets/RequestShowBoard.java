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
package net.l2emuproject.gameserver.network.clientpackets;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class RequestShowBoard extends L2GameClientPacket
{
	private static final String	_C__57_REQUESTSHOWBOARD	= "[C] 57 RequestShowBoard";

	@Override
	protected final void readImpl()
	{
		readD();
	}

	@Override
	protected final void runImpl()
	{
		final L2Player player = getActiveChar();
		if (player == null)
			return;
		
		if (!Config.ALLOW_COMMUNITY_BOARD)
		{
			player.sendPacket(SystemMessageId.CB_OFFLINE);
			return;
		}

		CommunityService.getInstance().parseCommand(player, "_bbshome");
	}

	@Override
	public final String getType()
	{
		return _C__57_REQUESTSHOWBOARD;
	}
}
