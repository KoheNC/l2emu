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
package net.l2emuproject.gameserver.manager;

import net.l2emuproject.gameserver.events.global.clanhallsiege.ClanHallManager;
import net.l2emuproject.gameserver.events.global.siege.CastleManager;
import net.l2emuproject.gameserver.services.auction.AuctionService;
import net.l2emuproject.gameserver.world.mapregion.MapRegionManager;
import net.l2emuproject.gameserver.world.zone.ZoneManager;

public class Manager
{
	public static void reloadAll()
	{
		MapRegionManager.getInstance().reload();
		ZoneManager.getInstance().reload();
		AuctionService.getInstance().reload();
		CastleManager.getInstance().reload();
		ClanHallManager.getInstance().reload();
	}
}
