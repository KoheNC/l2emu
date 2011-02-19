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
import net.l2emuproject.gameserver.model.L2Object;
import net.l2emuproject.gameserver.model.actor.L2Merchant;
import net.l2emuproject.gameserver.model.actor.instance.L2MerchantInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.model.item.L2TradeList;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.ExBuySellListPacket;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.util.Util;

public final class RequestRefundItem extends L2GameClientPacket
{
	private static final String _C__D0_75_REQUESTREFUNDITEM = "[C] D0:75 RequestRefundItem";
	
	private static final int BATCH_LENGTH = 4; // length of the one item
	
	private int _listId;
	private int[] _items;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		final int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != getByteBuffer().remaining())
			return;
		
		_items = new int[count];
		for (int i = 0; i < count; i++)
			_items[i] = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (_items == null)
		{
			sendAF();
			return;
		}
		
		if (!player.hasRefund())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Object merchant = (L2Object)player.getTarget(L2Merchant.class);
		final L2TradeList list = L2MerchantInstance.getTradeList(player, merchant, _listId);
		
		if (list == null)
		{
			sendAF();
			return;
		}
		
		double taxRate = 0;
		
		if (merchant != null)
		{
			if (merchant instanceof L2MerchantInstance)
				taxRate = ((L2MerchantInstance) merchant).getMpc().getTotalTaxRate();
			else
				taxRate = 50;
		}
		
		long weight = 0;
		long adena = 0;
		long slots = 0;
		
		L2ItemInstance[] refund = player.getRefund().getItems();
		int[] objectIds = new int[_items.length];
		
		for (int i = 0; i < _items.length; i++)
		{
			int idx = _items[i];
			if (idx < 0 || idx >= refund.length)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
						+ " sent invalid refund index", Config.DEFAULT_PUNISH);
				return;
			}
			
			// check for duplicates - indexes
			for (int j = i + 1; j < _items.length; j++)
				if (idx == _items[j])
				{
					Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
							+ " sent duplicate refund index", Config.DEFAULT_PUNISH);
					return;
				}
			
			final L2ItemInstance item = refund[idx];
			final L2Item template = item.getItem();
			objectIds[i] = item.getObjectId();
			
			// second check for duplicates - object ids
			for (int j = 0; j < i; j++)
				if (objectIds[i] == objectIds[j])
				{
					Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
							+ " has duplicate items in refund list", Config.DEFAULT_PUNISH);
					return;
				}
			
			long count = item.getCount();
			weight += count * template.getWeight();
			adena += count * template.getReferencePrice() / 2;
			if (!template.isStackable())
				slots += count;
			else if (player.getInventory().getItemByItemId(template.getItemId()) == null)
				slots++;
		}
		
		if (weight > Integer.MAX_VALUE || weight < 0 || !player.getInventory().validateWeight((int) weight))
		{
			sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (slots > Integer.MAX_VALUE || slots < 0 || !player.getInventory().validateCapacity((int) slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((adena < 0) || !player.reduceAdena("Refund", adena, player.getLastFolkNPC(), false))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		for (int i = 0; i < _items.length; i++)
		{
			L2ItemInstance item = player.getRefund().transferItem("Refund", objectIds[i], Long.MAX_VALUE, player.getInventory(), player,
					player.getLastFolkNPC());
			if (item == null)
			{
				_log.warn("Error refunding object for char " + player.getName() + " (newitem == null)");
				continue;
			}
		}
		
		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ExBuySellListPacket(player, list, taxRate, true));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_75_REQUESTREFUNDITEM;
	}
}
