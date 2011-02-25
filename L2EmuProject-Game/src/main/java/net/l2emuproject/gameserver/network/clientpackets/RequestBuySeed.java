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

import static net.l2emuproject.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;
import static net.l2emuproject.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.instancemanager.CastleManager;
import net.l2emuproject.gameserver.model.actor.instance.L2ManorManagerInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.entity.Castle;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.manor.CastleManorService;
import net.l2emuproject.gameserver.services.manor.CastleManorService.SeedProduction;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.world.L2Object;


/**
 * Format: cdd[dd] c // id (0xC4)
 * 
 * d // manor id d // seeds to buy [ d // seed id d // count ]
 * 
 * @author l3x
 */
public class RequestBuySeed extends L2GameClientPacket
{
	private static final String	_C__C4_REQUESTBUYSEED	= "[C] C4 RequestBuySeed";

	private static final int BATCH_LENGTH = 12; // length of the one item

	private int					_manorId;
	private Seed[]				_seeds = null;

	@Override
	protected void readImpl()
	{
		_manorId = readD();

		int count = readD();
		if (count <= 0
				|| count > Config.MAX_ITEM_IN_PACKET
				|| count * BATCH_LENGTH != getByteBuffer().remaining())
		{
			return;
		}

		_seeds = new Seed[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			long cnt = readQ();
			if (cnt < 1)
			{
				_seeds = null;
				return;
			}
			_seeds[i] = new Seed(itemId, cnt);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_seeds == null)
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

		Castle castle = CastleManager.getInstance().getCastleById(_manorId);

		long totalPrice = 0;
		int slots = 0;
		int totalWeight = 0;

		for (Seed i : _seeds)
		{
			if (!i.setProduction(castle))
				return;

			totalPrice += i.getPrice();

			if (totalPrice > MAX_ADENA)
			{
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}

			L2Item template = ItemTable.getInstance().getTemplate(i.getSeedId());
			totalWeight += i.getCount() * template.getWeight();
			if (!template.isStackable())
				slots += i.getCount();
			else if (player.getInventory().getItemByItemId(i.getSeedId()) == null)
				slots++;
		}

		if (totalPrice >= MAX_ADENA)
		{
			requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		if (!player.getInventory().validateWeight(totalWeight))
		{
			requestFailed(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}
		if (!player.getInventory().validateCapacity(slots))
		{
			requestFailed(SystemMessageId.SLOTS_FULL);
			return;
		}

		// Charge buyer
		if ((totalPrice < 0) || !player.reduceAdena("Buy", totalPrice, manager, false))
		{
			requestFailed(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}

		// Adding to treasury for Manor Castle
		castle.addToTreasuryNoTax(totalPrice);

		// Proceed the purchase
		InventoryUpdate playerIU = new InventoryUpdate();
		for (Seed i : _seeds)
		{
			i.updateProduction(castle);

			// Add item to Inventory and adjust update packet
			L2ItemInstance item = player.getInventory().addItem("Buy", i.getSeedId(), i.getCount(), player, manager);

			if (item.getCount() > i.getCount())
				playerIU.addModifiedItem(item);
			else
				playerIU.addNewItem(item);

			// Send Char Buy Messages
			SystemMessage sm = null;
			sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(item);
			sm.addItemNumber(i.getCount());
			player.sendPacket(sm);
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
		return _C__C4_REQUESTBUYSEED;
	}

	private class Seed
	{
		private final int _seedId;
		private final long _count;
		SeedProduction _seed;

		public Seed(int id, long num)
		{
			_seedId = id;
			_count = num;
		}

		public int getSeedId()
		{
			return _seedId;
		}

		public long getCount()
		{
			return _count;
		}

		public long getPrice()
		{
			return _seed.getPrice() * _count;
		}

		public boolean setProduction(Castle c)
		{
			_seed = c.getSeed(_seedId, CastleManorService.PERIOD_CURRENT);
			// invalid price - seed disabled
			if (_seed.getPrice() <= 0)
				return false;
			// try to buy more than castle can produce
			if (_seed.getCanProduce() < _count)
				return false;
			// check for overflow
			if ((MAX_ADENA / _count) < _seed.getPrice())
				return false;

			return true;
		}

		public void updateProduction(Castle c)
		{
			_seed.setCanProduce(_seed.getCanProduce() - _count);
			// Update Castle Seeds Amount
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				c.updateSeed(_seedId, _seed.getCanProduce(), CastleManorService.PERIOD_CURRENT);
		}
	}
}
