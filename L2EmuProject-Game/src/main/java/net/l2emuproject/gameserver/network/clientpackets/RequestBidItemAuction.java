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

import net.l2emuproject.gameserver.model.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.services.itemauction.ItemAuction;
import net.l2emuproject.gameserver.services.itemauction.ItemAuctionInstance;
import net.l2emuproject.gameserver.services.itemauction.ItemAuctionService;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Forsaiken
 */
public final class RequestBidItemAuction extends L2GameClientPacket
{
	private int		_instanceId;
	private long	_bid;

	@Override
	protected final void readImpl()
	{
		_instanceId = readD();
		_bid = readQ();
	}

	@Override
	protected final void runImpl()
	{
		final L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (_bid < 0 || _bid > PcInventory.MAX_ADENA)
			return;

		final ItemAuctionInstance instance = ItemAuctionService.getInstance().getManagerInstance(_instanceId);
		if (instance != null)
		{
			final ItemAuction auction = instance.getCurrentAuction();
			if (auction != null)
				auction.registerBid(activeChar, _bid);
		}
	}

	@Override
	public final String getType()
	{
		return "[C] D0:39 RequestBidItemAuction";
	}
}
