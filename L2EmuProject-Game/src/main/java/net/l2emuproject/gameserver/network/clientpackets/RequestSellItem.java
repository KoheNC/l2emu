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

import static net.l2emuproject.gameserver.entity.itemcontainer.PcInventory.MAX_ADENA;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExBuySellListPacket;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.services.transactions.L2TradeList;
import net.l2emuproject.gameserver.world.object.L2Merchant;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2MerchantInstance;

public class RequestSellItem extends L2GameClientPacket
{
	private static final String	_C__1E_REQUESTSELLITEM	= "[C] 1E RequestSellItem";
	
	private static final int	BATCH_LENGTH			= 16;						// length of the one item
																					
	private int					_listId;
	private Item[]				_items					= null;
	
	/**
	 * packet type id 0x1e
	 * sample
	 * 1e
	 * 00 00 00 00 // list id
	 * 02 00 00 00 // number of items
	 * 71 72 00 10 // object id
	 * ea 05 00 00 // item id
	 * 01 00 00 00 // item count
	 * 76 4b 00 10 // object id
	 * 2e 0a 00 00 // item id
	 * 01 00 00 00 // item count
	 * format: cdd (ddd)
	 */
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != getByteBuffer().remaining())
		{
			return;
		}
		
		_items = new Item[count];
		for (int i = 0; i < count; i++)
		{
			int objectId = readD();
			int itemId = readD();
			long cnt = readQ();
			if (objectId < 1 || itemId < 1 || cnt < 1)
			{
				_items = null;
				return;
			}
			_items[i] = new Item(objectId, itemId, cnt);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (_items == null)
		{
			sendAF();
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
				taxRate = ((L2MerchantInstance)merchant).getMpc().getTotalTaxRate();
			else
				taxRate = 50;
		}
		
		long totalPrice = 0;
		// Proceed the sell
		for (Item i : _items)
		{
			L2ItemInstance item = player.checkItemManipulation(i.getObjectId(), i.getCount(), "sell");
			if (item == null || !item.isSellable())
				continue;
			
			long price = item.getReferencePrice() / 2;
			totalPrice += price * i.getCount();
			if ((MAX_ADENA / i.getCount()) < price || totalPrice > MAX_ADENA)
			{
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			
			if (Config.ALLOW_REFUND)
				item = player.getInventory().transferItem("Sell", i.getObjectId(), i.getCount(), player.getRefund(), player, merchant);
			else
				item = player.getInventory().destroyItem("Sell", i.getObjectId(), i.getCount(), player, merchant);
		}
		player.addAdena("Sell", totalPrice, merchant, false);
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		sendPacket(su);
		
		sendPacket(new ExBuySellListPacket(player, list, taxRate, true));
		
		sendAF();
	}
	
	private static class Item
	{
		private final int	_objectId;
		// private final int _itemId;
		private final long	_count;
		
		public Item(int objId, int id, long num)
		{
			_objectId = objId;
			// _itemId = id;
			_count = num;
		}
		
		public int getObjectId()
		{
			return _objectId;
		}
		
		// public int getItemId()
		// {
		// return _itemId;
		// }
		
		public long getCount()
		{
			return _count;
		}
	}
	
	@Override
	public String getType()
	{
		return _C__1E_REQUESTSELLITEM;
	}
}
