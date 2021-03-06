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

import static net.l2emuproject.gameserver.world.object.L2Npc.INTERACTION_DISTANCE;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.events.global.siege.Castle;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.manor.CastleManorService;
import net.l2emuproject.gameserver.services.manor.L2Manor;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2ManorManagerInstance;


public class RequestBuyProcure extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 16; // length of the one item

	private static final String	_C__C3_REQUESTBUYPROCURE	= "[C] C3 RequestBuyProcure";
	//private int _listId;
	private Procure[]			_items = null;

	@Override
	protected void readImpl()
	{
		/* _listId = */readD();

		int count = readD();
		if (count <= 0
				|| count > Config.MAX_ITEM_IN_PACKET
				|| count * BATCH_LENGTH != getByteBuffer().remaining())
		{
			return;
		}

		_items = new Procure[count];
		for (int i = 0; i < count; i++)
		{
			readD(); //service
			int itemId = readD();
			long cnt = readQ();
			if (itemId < 1 || cnt < 1)
			{
				_items = null;
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			_items[i] = new Procure(itemId, cnt);
		}
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_items == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object manager = player.getTarget();

		if (!(manager instanceof L2ManorManagerInstance))
			manager = player.getLastFolkNPC();

		if (!(manager instanceof L2ManorManagerInstance))
			return;

		if (!player.isInsideRadius(manager, INTERACTION_DISTANCE, true, false))
			return;

		Castle castle = ((L2ManorManagerInstance) manager).getCastle();
		//long subTotal = 0;
		//int tax = 0;
		int slots = 0;
		int weight = 0;

		for (Procure i : _items)
		{
			i.setReward(castle);

			L2Item template = ItemTable.getInstance().getTemplate(i.getReward());
			weight += i.getCount() * template.getWeight();

			if (!template.isStackable())
				slots += i.getCount();
			else if (player.getInventory().getItemByItemId(i.getItemId()) == null)
				slots++;
		}

		if (!player.getInventory().validateWeight(weight))
		{
			requestFailed(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}
		if (!player.getInventory().validateCapacity(slots))
		{
			requestFailed(SystemMessageId.SLOTS_FULL);
			return;
		}

		// Proceed the purchase
		InventoryUpdate playerIU = new InventoryUpdate();

		for (Procure i : _items)
		{
			// check if player have correct items count
			L2ItemInstance item = player.getInventory().getItemByItemId(i.getItemId());
			if (item == null || item.getCount() < i.getCount())
				continue;

			L2ItemInstance iteme = player.getInventory().destroyItemByItemId("Manor", i.getItemId(), i.getCount(), player, manager);
			if (iteme == null)
				continue;

			item = player.getInventory().addItem("Manor",i.getReward(),i.getCount(),player,manager);
			if (item == null)
				continue;

			playerIU.addRemovedItem(iteme);
			if (item.getCount() > i.getCount())
				playerIU.addModifiedItem(item);
			else
				playerIU.addNewItem(item);

			// Send Char Buy Messages
			SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(item);
			sm.addItemNumber(i.getCount());
			sendPacket(sm);
			sm = null;

			//manor.getCastle().setCropAmount(itemId, manor.getCastle().getCrop(itemId, CastleManorService.PERIOD_CURRENT).getAmount() - count);
		}

		// Send update packets
		sendPacket(playerIU);
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		sendPacket(su);
		su = null;

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__C3_REQUESTBUYPROCURE;
	}

	private class Procure
	{
		private final int _itemId;
		private final long _count;
		private int _reward;

		public Procure(int id, long num)
		{
			_itemId = id;
			_count = num;
		}

		public int getItemId()
		{
			return _itemId;
		}

		public long getCount()
		{
			return _count;
		}

		public int getReward()
		{
			return _reward;
		}

		public void setReward(Castle c)
		{
			_reward = L2Manor.getInstance().getRewardItem(_itemId,
					c.getCrop(_itemId,CastleManorService.PERIOD_CURRENT).getReward());
		}
	}
}
