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

import net.l2emuproject.gameserver.events.global.clanhallsiege.ClanHall;
import net.l2emuproject.gameserver.events.global.clanhallsiege.ClanHallManager;
import net.l2emuproject.gameserver.events.global.siege.Castle;
import net.l2emuproject.gameserver.events.global.siege.CastleManager;
import net.l2emuproject.gameserver.network.serverpackets.SiegeDefenderList;

public class RequestSiegeDefenderList extends L2GameClientPacket
{
	private static final String	_C__a3_RequestSiegeDefenderList	= "[C] a3 RequestSiegeDefenderList";

	private int					_siegeableID;

	@Override
	protected void readImpl()
	{
		_siegeableID = readD();
	}

	@Override
	protected void runImpl()
	{
		SiegeDefenderList sdl = null;
		Castle castle = CastleManager.getInstance().getCastleById(_siegeableID);
		if (castle == null)
		{
			ClanHall hideout = ClanHallManager.getInstance().getClanHallById(_siegeableID);
			if (hideout != null && hideout.getSiege() != null)
				sdl = new SiegeDefenderList(hideout);
		}
		else
			sdl = new SiegeDefenderList(castle);

		if (sdl != null)
			sendPacket(sdl);

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__a3_RequestSiegeDefenderList;
	}
}
