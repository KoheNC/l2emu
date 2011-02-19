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
package net.l2emuproject.gameserver.model.actor.instance;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.Shutdown;
import net.l2emuproject.gameserver.Shutdown.DisableType;
import net.l2emuproject.gameserver.datatables.MerchantPriceConfigTable;
import net.l2emuproject.gameserver.datatables.MerchantPriceConfigTable.MerchantPriceConfig;
import net.l2emuproject.gameserver.datatables.TradeListTable;
import net.l2emuproject.gameserver.model.actor.L2Merchant;
import net.l2emuproject.gameserver.model.item.L2TradeList;
import net.l2emuproject.gameserver.model.world.L2Object;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.BuyList;
import net.l2emuproject.gameserver.network.serverpackets.ExBuySellListPacket;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.SellList;
import net.l2emuproject.gameserver.network.serverpackets.SetupGauge;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.util.Util;
import net.l2emuproject.lang.L2TextBuilder;

/**
 * This class ...
 *
 * @version $Revision: 1.10.4.9 $ $Date: 2005/04/11 10:06:08 $
 */
public class L2MerchantInstance extends L2NpcInstance implements L2Merchant
{
	private MerchantPriceConfig	_mpc;

	/**
	 * @param template
	 */
	public L2MerchantInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		String path = "";

		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		path = "data/html/merchant/";

		return path + pom + ".htm";
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_mpc = MerchantPriceConfigTable.getInstance().getMerchantPriceConfig(this);
	}

	/**
	 * @return Returns the mpc.
	 */
	public MerchantPriceConfig getMpc()
	{
		return _mpc;
	}

	public final void showBuySellRefundWindow(L2PcInstance player, int val)
	{
		double taxRate = 0;
		
		taxRate = getMpc().getTotalTaxRate();
		
		player.tempInventoryDisable();
		
		if (_log.isDebugEnabled())
			_log.debug("Showing buylist.");
		
		L2TradeList list = TradeListTable.getInstance().getBuyList(val);
		
		if (list != null && list.getNpcId() == (getNpcId()))
		{
			player.sendPacket(new BuyList(list, player.getAdena(), taxRate));
			player.sendPacket(new ExBuySellListPacket(player, list, taxRate, false));
		}
		else
		{
			_log.warn("Possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.warn("Buylist id:" + val);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Deprecated
	// FIXME 1.4.0 was removed at l2jserver 3696
	protected final void showSellWindow(L2PcInstance player)
	{
		if (_log.isDebugEnabled())
			_log.debug("Showing selllist.");

		player.sendPacket(new SellList(player));

		if (_log.isDebugEnabled())
			_log.debug("Showing sell window.");

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public final void showRentPetWindow(L2PcInstance player)
	{
		if (!Config.LIST_PET_RENT_NPC.contains(getTemplate().getNpcId()))
			return;

		L2TextBuilder html1 = L2TextBuilder.newInstance("<html><body>Pet Manager:<br>");
		html1.append("You can rent a wyvern or strider for adena.<br>My prices:<br1>");
		html1.append("<table border=0><tr><td>Ride</td></tr>");
		html1.append("<tr><td>Wyvern</td><td>Strider</td></tr>");
		html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 1\">30 sec/1800 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 11\">30 sec/900 adena</a></td></tr>");
		html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 2\">1 min/7200 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 12\">1 min/3600 adena</a></td></tr>");
		html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 3\">10 min/720000 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 13\">10 min/360000 adena</a></td></tr>");
		html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 4\">30 min/6480000 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 14\">30 min/3240000 adena</a></td></tr>");
		html1.append("</table>");
		html1.append("</body></html>");

		insertObjectIdAndShowChatWindow(player, html1.moveToString());
	}

	public void tryRentPet(L2PcInstance player, int val)
	{
		if (player == null || player.getPet() != null || player.isMounted() || player.isRentedPet() || player.getPlayerTransformation().isTransformed()
				|| player.isCursedWeaponEquipped())
			return;

		if (!player.disarmWeapons(true))
			return;

		int petId;
		long cost[] =
		{ 1800, 7200, 720000, 6480000 };
		int ridetime[] =
		{ 30, 60, 600, 1800 };

		if (val < 1 || val > 4)
			return;

		long price;
		if (val > 10)
		{
			petId = 12526;
			val -= 10;
			price = cost[val - 1] / 2;
		}
		else
		{
			petId = 12621;
			price = cost[val - 1];
		}

		int time = ridetime[val - 1];

		if (!player.reduceAdena("Rent", price, player.getLastFolkNPC(), true))
			return;

		player.mount(petId, 0, false);
		SetupGauge sg = new SetupGauge(3, time * 1000);
		player.sendPacket(sg);
		player.startRentPet(time);
	}

	@Override
	public final void onActionShift(L2PcInstance player)
	{
		if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
		{
			player.setTarget(this);

			if (isAutoAttackable(player))
			{
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			L2TextBuilder html1 = L2TextBuilder.newInstance("<html><body><table border=0>");
			html1.append("<tr><td>Current Target:</td></tr>");
			html1.append("<tr><td><br></td></tr>");

			html1.append("<tr><td>Object ID: " + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Template ID: " + getTemplate().getNpcId() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");

			html1.append("<tr><td>HP: " + getStatus().getCurrentHp() + "</td></tr>");
			html1.append("<tr><td>MP: " + getStatus().getCurrentMp() + "</td></tr>");
			html1.append("<tr><td>Level: " + getLevel() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");

			String className = getClass().getName().substring(44);
			html1.append("<tr><td>Class: " + className + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");

			//changed by terry 2005-02-22 21:45
			html1.append("</table><table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().getNpcId()
					+ "\" width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().getNpcId()
					+ "\" width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			html1.append("</table>");

			if (player.isGM())
			{
				html1.append("<button value=\"View Shop\" action=\"bypass -h admin_showShop " + getTemplate().getNpcId()
						+ "\" width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></br>");
				html1.append("<button value=\"View Custom Shop\" action=\"bypass -h admin_showCustomShop " + getTemplate().getNpcId()
						+ "\" width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></br>");
				html1.append("<button value=\"Lease next week\" action=\"bypass -h npc_" + getObjectId()
						+ "_Lease\" width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html1.append("<button value=\"Abort current leasing\" action=\"bypass -h npc_" + getObjectId()
						+ "_Lease next\" width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html1.append("<button value=\"Manage items\" action=\"bypass -h npc_" + getObjectId()
						+ "_Lease manage\" width=100 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			}

			html1.append("</body></html>");

			html.setHtml(html1.moveToString());
			player.sendPacket(html);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public static L2TradeList getTradeList(L2PcInstance player, L2Object target, int listId)
	{
		final L2Merchant merchant = L2Object.getActingMerchant(target);

		if (!L2MerchantInstance.canShop(player, target))
			return null;

		L2TradeList list = null;

		if (player.isGM())
			list = TradeListTable.getInstance().getBuyList(listId);
		else if (merchant != null)
			list = TradeListTable.getInstance().getBuyListByNpcId(listId, merchant.getNpcId());

		if (list == null)
		{
			if (!player.isGM())
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
						+ " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
			}
			else
				player.sendMessage("Buylist " + listId + " empty or not exists.");
			return null;
		}

		if (list.isGm() && !player.isGM())
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
					+ " sent a modified packet to buy from gmshop.", Config.DEFAULT_PUNISH);
			return null;
		}

		return list;
	}

	public static boolean canShop(L2PcInstance player, L2Object target)
	{
		if (!player.getFloodProtector().tryPerformAction(Protected.TRANSACTION))
		{
			player.sendMessage("You are acting too fast.");
			return false;
		}

		if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
		{
			player.cancelActiveTrade();
			player.sendPacket(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			return false;
		}

		if (player.isGM())
			return true;

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
			return false;

		if (target == null)
			return false;

		if (!player.isSameInstance(target))
			return false;

		if (!player.isInsideRadius(target, INTERACTION_DISTANCE, false, false))
		{
			player.sendPacket(SystemMessageId.TOO_FAR_FROM_NPC);
			return false;
		}

		return true;
	}
}
