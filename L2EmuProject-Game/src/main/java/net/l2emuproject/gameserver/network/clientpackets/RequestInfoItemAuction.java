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

import net.l2emuproject.gameserver.network.serverpackets.ExItemAuctionInfoPacket;
import net.l2emuproject.gameserver.services.itemauction.ItemAuction;
import net.l2emuproject.gameserver.services.itemauction.ItemAuctionInstance;
import net.l2emuproject.gameserver.services.itemauction.ItemAuctionService;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Forsaiken
 */
public final class RequestInfoItemAuction extends L2GameClientPacket
{
	private int	_instanceId;

	@Override
	protected final void readImpl()
	{
		_instanceId = readD();
	}

	@Override
	protected final void runImpl()
	{
		final L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		final ItemAuctionInstance instance = ItemAuctionService.getInstance().getManagerInstance(_instanceId);
		if (instance == null)
			return;

		final ItemAuction auction = instance.getCurrentAuction();
		if (auction == null)
			return;

		activeChar.updateLastItemAuctionRequest();
		activeChar.sendPacket(new ExItemAuctionInfoPacket(true, auction, instance.getNextAuction()));
	}

	@Override
	public final String getType()
	{
		return "[C] D0:3A RequestBidItemAuction";
	}
}
