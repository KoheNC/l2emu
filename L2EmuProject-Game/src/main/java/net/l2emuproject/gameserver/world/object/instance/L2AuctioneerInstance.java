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
package net.l2emuproject.gameserver.world.object.instance;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.events.global.clanhallsiege.ClanHallManager;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.services.auction.Auction;
import net.l2emuproject.gameserver.services.auction.Auction.Bidder;
import net.l2emuproject.gameserver.services.auction.AuctionService;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.town.Town;
import net.l2emuproject.gameserver.world.town.TownManager;

public final class L2AuctioneerInstance extends L2Npc
{
	private static final byte COND_ALL_FALSE = 0;
	private static final byte COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final byte COND_REGULAR = 3;

	private final Map<Integer, Auction> _pendingAuctions = new FastMap<Integer, Auction>();

	public L2AuctioneerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2Player player, String command)
	{
		int condition = validateCondition(player);
		if (condition == COND_ALL_FALSE)
		{
			player.sendMessage("Wrong conditions.");
			return;
		}
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			player.sendMessage("Busy because of siege.");
			return;
		}
		else if (condition == COND_REGULAR)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken(); // Get actual command

			String val = "";
			if (st.countTokens() >= 1)
			{
				val = st.nextToken();
			}

			if (actualCommand.equalsIgnoreCase("auction"))
			{
				if (val.isEmpty())
					return;

				try
				{
					int days = Integer.parseInt(val);
					try
					{
						SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
						int bid = 0;
						if (st.countTokens() >= 1) bid = Integer.parseInt(st.nextToken());

						Auction a = new Auction(player.getClan().getHasHideout(), player.getClan(), days*86400000L, bid, ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getName());
						if (_pendingAuctions.get(a.getId()) != null)
							_pendingAuctions.remove(a.getId());

						_pendingAuctions.put(a.getId(), a);

						String filename = "data/npc_data/html/auction/AgitSale3.htm";
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(filename);
						html.replace("%x%", val);
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_MIN%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_sale2");
						html.replace("%objectId%", String.valueOf((getObjectId())));
						player.sendPacket(html);
					}
					catch (Exception e)
					{
						player.sendMessage("Invalid bid!");
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction duration!");
				}
				return;
			}
			if (actualCommand.equalsIgnoreCase("confirmAuction"))
			{
				try
				{
					Auction a = _pendingAuctions.get(player.getClan().getHasHideout());
					a.confirmAuction();
					_pendingAuctions.remove(player.getClan().getHasHideout());
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bidding"))
			{
				if (val.isEmpty())
					return;
				if(_log.isDebugEnabled())
					_log.warn("bidding show successful");

				try
				{
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					int auctionId = Integer.parseInt(val);
					if(_log.isDebugEnabled())
						_log.warn("auction test started");
					String filename = "data/npc_data/html/auction/AgitAuctionInfo.htm";
					Auction a = AuctionService.getInstance().getAuction(auctionId);

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					if(a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", "30 ");
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate()- System.currentTimeMillis())/3600000)+" hours "+String.valueOf((((a.getEndDate() - System.currentTimeMillis()) / 60000) % 60))+" minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_COUNT%", String.valueOf(a.getBidders().size()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_list");
						html.replace("%AGIT_LINK_BIDLIST%", "bypass -h npc_"+getObjectId()+"_bidlist "+a.getId());
						html.replace("%AGIT_LINK_RE%", "bypass -h npc_"+getObjectId()+"_bid1 "+a.getId());
					}
					else
					{
						_log.warn("Auctioneer Auction null for AuctionId : "+auctionId);
					}
					player.sendPacket(html);
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}

				return;
			}
			else if (actualCommand.equalsIgnoreCase("bid"))
			{
				if (val.isEmpty())
					return;

				try
				{
					int auctionId = Integer.parseInt(val);
					try
					{
						int bid = 0;
						if (st.countTokens() >= 1) bid = Integer.parseInt(st.nextToken());

						AuctionService.getInstance().getAuction(auctionId).setBid(player, bid);
					}
					catch (Exception e)
					{
						player.sendMessage("Invalid bid!");
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}

				return;
			}
			else if (actualCommand.equalsIgnoreCase("bid1"))
			{
				if (player.getClan() == null || player.getClan().getLevel() < 2)
				{
					player.sendMessage("Your clan's level needs to be at least 2, before you can bid in an auction");
					return;
				}

				if (val.isEmpty())
					return;
				if ((player.getClan().getAuctionBiddedAt() > 0 && player.getClan().getAuctionBiddedAt() != Integer.parseInt(val)) || player.getClan().getHasHideout() > 0)
				{
					player.sendMessage("You can't bid at more than one auction");
					return;
				}

				try
				{
					String filename = "data/npc_data/html/auction/AgitBid1.htm";

					int minimumBid = AuctionService.getInstance().getAuction(Integer.parseInt(val)).getHighestBidderMaxBid();
					if (minimumBid == 0) minimumBid = AuctionService.getInstance().getAuction(Integer.parseInt(val)).getStartingBid();

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_bidding "+val);
					html.replace("%PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getAdena()));
					html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(minimumBid));
					html.replace("npc_%objectId%_bid", "npc_"+getObjectId()+"_bid "+val);
					player.sendPacket(html);
					return;
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("list"))
			{
				List<Auction> auctions =AuctionService.getInstance().getAuctions();
				SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd");
				/** Limit for make new page, prevent client crash **/
				int limit = 15;
				int start;
				int i = 1;
				double npage = Math.ceil((float)auctions.size() / limit);
				if (val.isEmpty())
				{
					start = 1;
				}
				else
				{
					start = limit * (Integer.parseInt(val) - 1) + 1;
					limit *= Integer.parseInt(val);
				}
				if (_log.isDebugEnabled()) _log.warn("cmd list: auction test started");
				String items = "";
				items += "<table width=280 border=0><tr>";
				for (int j = 1; j <= npage; j++)
					items+= "<td><center><a action=\"bypass -h npc_"+getObjectId()+"_list "+j+"\"> Page "+j+" </a></center></td>";
				items += "</tr></table>" +
						"<table width=280 border=0>";
				for (Auction a : auctions)
				{
					if (i > limit)
					{
						break;
					}
					else if(i < start)
					{
						i++;
						continue;
					}
					else
					{
						i++;
					}
					items+="<tr>" +
								"<td>"+ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation()+"</td>" +
								"<td><a action=\"bypass -h npc_"+getObjectId()+"_bidding "+a.getId()+"\">"+a.getItemName()+"</a></td>" +
								"<td>"+format.format(a.getEndDate())+"</td>" +
								"<td>"+a.getStartingBid()+"</td>" +
							"</tr>";
					
				}
				items+= "</table>";
				String filename = "data/npc_data/html/auction/AgitAuctionList.htm";

				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_start");
				html.replace("%itemsField%", items);
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bidlist"))
			{
				int auctionId = 0;
				if (val.isEmpty())
				{
					if (player.getClan().getAuctionBiddedAt() <= 0)
						return;

					auctionId = player.getClan().getAuctionBiddedAt();
				}
				else
					auctionId = Integer.parseInt(val);
				if (_log.isDebugEnabled())
					_log.warn("cmd bidlist: auction test started");
				String biders = "";
				Map<Integer, Bidder> bidders = AuctionService.getInstance().getAuction(auctionId).getBidders();
				for (Bidder b : bidders.values())
				{
					biders+="<tr>" +
							"<td>"+b.getClanName()+"</td><td>"+b.getName()+"</td><td>"+b.getTimeBid().get(Calendar.YEAR)+"/"+(b.getTimeBid().get(Calendar.MONTH)+1)+"/"+b.getTimeBid().get(Calendar.DATE)+"</td><td>"+b.getBid()+"</td>" +
							"</tr>";
				}
				String filename = "data/npc_data/html/auction/AgitBidderList.htm";

				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_LIST%", biders);
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_selectedItems");
				html.replace("%x%", val);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("selectedItems"))
			{
				if (player.getClan() != null && player.getClan().getHasHideout() == 0 && player.getClan().getAuctionBiddedAt() > 0)
				{
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					String filename = "data/npc_data/html/auction/AgitBidInfo.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					Auction a = AuctionService.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
					if(a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", "30 ");
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate()-System.currentTimeMillis()) / 3600000)+" hours "+String.valueOf((((a.getEndDate()-System.currentTimeMillis()) / 60000) % 60))+" minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_MYBID%", String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_start");
					}
					else
					{
						_log.warn("Auctioneer Auction null for AuctionBiddedAt : "+player.getClan().getAuctionBiddedAt());
					}
					player.sendPacket(html);
					return;
				}
				else if (player.getClan() != null && AuctionService.getInstance().getAuction(player.getClan().getHasHideout()) != null)
				{
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					String filename = "data/npc_data/html/auction/AgitSaleInfo.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					Auction a = AuctionService.getInstance().getAuction(player.getClan().getHasHideout());
					if(a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%AGIT_OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", "30 ");
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate()-System.currentTimeMillis()) / 3600000)+" hours "+String.valueOf((((a.getEndDate()-System.currentTimeMillis()) / 60000) % 60))+" minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_BIDCOUNT%", String.valueOf(a.getBidders().size()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_start");
						html.replace("%id%", String.valueOf(a.getId()));
						html.replace("%objectId%", String.valueOf(getObjectId()));
					}
					else
					{
						_log.warn("Auctioneer Auction null for getHasHideout : "+player.getClan().getHasHideout());
					}
					player.sendPacket(html);
					return;
				}
				else if(player.getClan() != null && player.getClan().getHasHideout() != 0)
				{
					int ItemId = player.getClan().getHasHideout();
					String filename = "data/npc_data/html/auction/AgitInfo.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					if(ClanHallManager.getInstance().getClanHallById(ItemId) != null)
					{
						html.replace("%AGIT_NAME%", ClanHallManager.getInstance().getClanHallById(ItemId).getName());
						html.replace("%AGIT_OWNER_PLEDGE_NAME%", player.getClan().getName());
						html.replace("%OWNER_PLEDGE_MASTER%", player.getClan().getLeaderName());
						html.replace("%AGIT_SIZE%", "30 ");
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(ItemId).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(ItemId).getLocation());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_start");
						html.replace("%objectId%", String.valueOf(getObjectId()));
					}
					else
						_log.warn("Clan Hall ID NULL : "+ItemId+" Can be caused by concurent write in ClanHallManager");
					player.sendPacket(html);
					return;
				}
			}
			else if (actualCommand.equalsIgnoreCase("cancelBid"))
			{
				int bid = AuctionService.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).getBidders().get(player.getClanId()).getBid();
				String filename = "data/npc_data/html/auction/AgitBidCancel.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_BID%", String.valueOf(bid));
				html.replace("%AGIT_BID_REMAIN%", String.valueOf((int)(bid*0.9)));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_selectedItems");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("doCancelBid"))
			{
				if (AuctionService.getInstance().getAuction(player.getClan().getAuctionBiddedAt()) != null)
				{
					AuctionService.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).cancelBid(player.getClanId());
					player.sendMessage("You have succesfully canceled your bidding at the auction");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("cancelAuction"))
			{
				if (!L2Clan.checkPrivileges(player, L2Clan.CP_CH_AUCTION))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					return;
				}
				String filename = "data/npc_data/html/auction/AgitSaleCancel.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_selectedItems");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("doCancelAuction"))
			{
				if (AuctionService.getInstance().getAuction(player.getClan().getHasHideout()) != null)
				{
					AuctionService.getInstance().getAuction(player.getClan().getHasHideout()).cancelAuction();
					player.sendMessage("Your auction has been canceled");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("sale2"))
			{
				String filename = "data/npc_data/html/auction/AgitSale2.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_LAST_PRICE%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_sale");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("sale"))
			{
				if (!L2Clan.checkPrivileges(player, L2Clan.CP_CH_AUCTION))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					return;
				}
				String filename = "data/npc_data/html/auction/AgitSale1.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getAdena()));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_selectedItems");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("rebid"))
			{
				if (!L2Clan.checkPrivileges(player, L2Clan.CP_CH_AUCTION))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					return;
				}
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
				try
				{
				String filename = "data/npc_data/html/auction/AgitBid2.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				Auction a = AuctionService.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
				if (a != null)
				{
					html.replace("%AGIT_AUCTION_BID%", String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
					html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
					html.replace("%AGIT_AUCTION_END%",String.valueOf(format.format(a.getEndDate())));
					html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_selectedItems");
					html.replace("npc_%objectId%_bid1", "npc_"+getObjectId()+"_bid1 "+a.getId());
				}
				else
				{
					_log.warn("Auctioneer Auction null for AuctionBiddedAt : "+player.getClan().getAuctionBiddedAt());
				}
				player.sendPacket(html);
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("location"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/npc_data/html/auction/location.htm");
				html.replace("%location%", TownManager.getInstance().getClosestTownName(player));
				html.replace("%LOCATION%", getPictureName(player));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_start");
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("start"))
			{
				showChatWindow(player);
				return;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(L2Player player)
	{
		String filename = "data/npc_data/html/auction/auction-no.htm";

		int condition = validateCondition(player);
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			filename = "data/npc_data/html/auction/auction-busy.htm"; // Busy because of siege
		else
			filename = "data/npc_data/html/auction/auction.htm";

		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	/**
	 * @param player
	 */
	private int validateCondition(L2Player player)
	{
		if (getCastle() != null && getCastle().getCastleId() > 0)
		{
			if (getCastle().getSiege().getIsInProgress())
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege

			return COND_REGULAR;
		}

		return COND_ALL_FALSE;
	}

	private String getPictureName(L2Player activeChar)
	{
		Town town = TownManager.getInstance().getClosestTown(activeChar);

		int nearestTownId = town.getTownId();
		String nearestTown;

		switch (nearestTownId)
		{
			case 5: nearestTown = "GLUDIO"; break;
			case 6: nearestTown = "GLUDIN"; break;
			case 7: nearestTown = "DION"; break;
			case 8: nearestTown = "GIRAN"; break;
			case 14: nearestTown = "RUNE"; break;
			case 15: nearestTown = "GODARD"; break;
			case 16: nearestTown = "SCHUTTGART"; break;
			default: nearestTown = "ADEN"; break;
		}

		return nearestTown;
	}
}
