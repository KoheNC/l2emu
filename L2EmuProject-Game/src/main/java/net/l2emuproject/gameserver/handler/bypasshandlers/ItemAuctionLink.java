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
package net.l2emuproject.gameserver.handler.bypasshandlers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExItemAuctionInfoPacket;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.itemauction.ItemAuction;
import net.l2emuproject.gameserver.services.itemauction.ItemAuctionInstance;
import net.l2emuproject.gameserver.services.itemauction.ItemAuctionService;
import net.l2emuproject.gameserver.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public class ItemAuctionLink implements IBypassHandler
{
	private static final SimpleDateFormat	fmt			= new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

	private static final String[]			COMMANDS	=
														{ "ItemAuction" };

	@Override
	public boolean useBypass(String command, L2Player activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		if (!Config.ALT_ITEM_AUCTION_ENABLED)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.NO_AUCTION_PERIOD));
			return true;
		}

		final ItemAuctionInstance au = ItemAuctionService.getInstance().getManagerInstance(((L2Npc) target).getNpcId());
		if (au == null)
			return false;

		try
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken(); // bypass "ItemAuction"
			if (!st.hasMoreTokens())
				return false;

			String cmd = st.nextToken();
			if ("show".equalsIgnoreCase(cmd))
			{
				if (!activeChar.getFloodProtector().tryPerformAction(Protected.TRANSACTION))
					return false;

				if (activeChar.isItemAuctionPolling())
					return false;

				final ItemAuction currentAuction = au.getCurrentAuction();
				final ItemAuction nextAuction = au.getNextAuction();

				if (currentAuction == null)
				{
					activeChar.sendPacket(SystemMessageId.NO_AUCTION_PERIOD);

					if (nextAuction != null) // used only once when database is empty
						activeChar.sendMessage("The next auction will begin on the " + fmt.format(new Date(nextAuction.getStartingTime())) + ".");
					return true;
				}

				activeChar.sendPacket(new ExItemAuctionInfoPacket(false, currentAuction, nextAuction));
			}
			else if ("cancel".equalsIgnoreCase(cmd))
			{
				final ItemAuction[] auctions = au.getAuctionsByBidder(activeChar.getObjectId());
				boolean returned = false;
				for (final ItemAuction auction : auctions)
				{
					if (auction.cancelBid(activeChar))
						returned = true;
				}
				if (!returned)
					activeChar.sendPacket(SystemMessageId.NO_OFFERINGS_OWN_OR_MADE_BID_FOR);
			}
			else
				return false;
		}
		catch (Exception e)
		{
			_log.error("Exception in: " + getClass().getSimpleName() + ":" + e.getMessage());
		}

		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}