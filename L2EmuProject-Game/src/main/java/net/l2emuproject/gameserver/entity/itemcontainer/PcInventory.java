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
package net.l2emuproject.gameserver.entity.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javolution.util.FastList;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.items.L2ItemInstance.ItemLocation;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.ItemList;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.services.transactions.TradeList;
import net.l2emuproject.gameserver.services.transactions.TradeList.TradeItem;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.templates.item.L2EtcItemType;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.ArrayBunch;


public class PcInventory extends Inventory
{
	public static final int ADENA_ID = 57;
	public static final int ANCIENT_ADENA_ID = 5575;
	public static final long MAX_ADENA = 99000000000L;
	
	private final L2Player _owner;
	private L2ItemInstance _adena;
	private L2ItemInstance _ancientAdena;
	
	private int[] _blockItems = null;
	
	/**
	 * Block modes:
	 * <UL>
	 * <LI>-1 - no block
	 * <LI>0 - block items from _invItems, allow usage of other items
	 * <LI>1 - allow usage of items from _invItems, block other items
	 * </UL>
	 */
	private int _blockMode = -1;

	public PcInventory(L2Player owner)
	{
		_owner = owner;
	}
	
	@Override
	public L2Player getOwner()
	{
		return _owner;
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.INVENTORY;
	}

	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PAPERDOLL;
	}

	public L2ItemInstance getAdenaInstance()
	{
		return _adena;
	}

	@Override
	public long getAdena()
	{
		return _adena != null ? _adena.getCount() : 0;
	}
	
	public L2ItemInstance getAncientAdenaInstance()
	{
		return _ancientAdena;
	}
	
	public long getAncientAdena()
	{
		return (_ancientAdena != null) ? _ancientAdena.getCount() : 0;
	}
	
	/**
	 * Returns the list of items in inventory available for transaction
	 * @return L2ItemInstance : items in inventory
	 */
	public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena)
	{
		return getUniqueItems(allowAdena, allowAncientAdena, true);
	}
	
	public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
	{
		List<L2ItemInstance> list = new ArrayList<L2ItemInstance>();
		for (L2ItemInstance item : _items)
		{
			if ((!allowAdena && item.getItemId() == ADENA_ID))
				continue;
			if ((!allowAncientAdena && item.getItemId() == ANCIENT_ADENA_ID))
				continue;
			boolean isDuplicate = false;
			for (L2ItemInstance litem : list)
			{
				if (litem.getItemId() == item.getItemId())
				{
					isDuplicate = true;
					break;
				}
			}
			if (!isDuplicate && (!onlyAvailable || (item.isSellable() && item.isAvailable(getOwner(), false, false))))
				list.add(item);
		}

		return list.toArray(new L2ItemInstance[list.size()]);
	}

	/**
	* Returns the list of items in inventory available for transaction
	* Allows an item to appear twice if and only if there is a difference in enchantment level.
	* @return L2ItemInstance : items in inventory
	*/
	public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena)
	{
		return getUniqueItemsByEnchantLevel(allowAdena, allowAncientAdena, true);
	}

	public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
	{
		List<L2ItemInstance> list = new ArrayList<L2ItemInstance>();
		for (L2ItemInstance item : _items)
		{
			if ((!allowAdena && item.getItemId() == ADENA_ID))
				continue;
			if ((!allowAncientAdena && item.getItemId() == ANCIENT_ADENA_ID))
				continue;

			boolean isDuplicate = false;
			for (L2ItemInstance litem : list)
			{
				if( (litem.getItemId() == item.getItemId()) && (litem.getEnchantLevel() == item.getEnchantLevel()))
				{
					isDuplicate = true;
					break;
				}
			}
			if (!isDuplicate && (!onlyAvailable || (item.isSellable() && item.isAvailable(getOwner(), false, false))))
				list.add(item);
		}

		return list.toArray(new L2ItemInstance[list.size()]);
	}

	/**
	* Returns the list of all items in inventory that have a given item id.
	* @return L2ItemInstance[] : matching items from inventory
	*/
	public L2ItemInstance[] getAllItemsByItemId(int itemId)
	{
		ArrayBunch<L2ItemInstance> list = new ArrayBunch<L2ItemInstance>();
		for (L2ItemInstance item : _items)
		{
			if (item.getItemId() == itemId)
				list.add(item);
		}

		return list.moveToArray(new L2ItemInstance[list.size()]);
	}

	/**
	* Returns the list of all items in inventory that have a given item id AND a given enchantment level.
	* @return L2ItemInstance[] : matching items from inventory
	*/
	public L2ItemInstance[] getAllItemsByItemId(int itemId, int enchantment)
	{
		ArrayBunch<L2ItemInstance> list = new ArrayBunch<L2ItemInstance>();
		for (L2ItemInstance item : _items)
		{
			if ((item.getItemId() == itemId) && (item.getEnchantLevel() == enchantment))
				list.add(item);
		}

		return list.moveToArray(new L2ItemInstance[list.size()]);
	}

	/**
	 * Returns the list of items in inventory available for transaction
	 * @return L2ItemInstance : items in inventory
	 */
	public List<L2ItemInstance> getAvailableItems(boolean allowAdena, boolean allowNonTradeable)
	{
		FastList<L2ItemInstance> list = new FastList<L2ItemInstance>();
		for (L2ItemInstance item : _items)
			if (item != null && item.isAvailable(getOwner(), allowAdena, allowNonTradeable))
				list.add(item);

		return list;
	}

	/**
	 * Get all augmented items
	 * @return
	 */
	public L2ItemInstance[] getAugmentedItems()
	{
		ArrayBunch<L2ItemInstance> list = new ArrayBunch<L2ItemInstance>();
		for (L2ItemInstance item : _items)
		{
			if (item != null && item.isAugmented())
				list.add(item);
		}
		return list.moveToArray(new L2ItemInstance[list.size()]);
	}

	/**
	 * Get all element items
	 * @return
	 */
	public L2ItemInstance[] getElementItems()
	{
		ArrayBunch<L2ItemInstance> list = new ArrayBunch<L2ItemInstance>();
		for (L2ItemInstance item : _items)
		{
			if (item != null && item.getElementals() != null)
				list.add(item);
		}
		return list.moveToArray(new L2ItemInstance[list.size()]);
	}

	/**
	 * Returns the list of items in inventory available for transaction adjusted by tradeList
	 * @return L2ItemInstance : items in inventory
	 */
	public TradeList.TradeItem[] getAvailableItems(TradeList tradeList)
	{
		ArrayBunch<TradeList.TradeItem> list = new ArrayBunch<TradeList.TradeItem>();
		for (L2ItemInstance item : _items)
		{
			if (item.isAvailable(getOwner(), false, false))
			{
				TradeList.TradeItem adjItem = tradeList.adjustAvailableItem(item);
				if (adjItem != null)
					list.add(adjItem);
			}
		}
		return list.moveToArray(new TradeList.TradeItem[list.size()]);
	}

	/**
	* Adjust TradeItem according his status in inventory
	* @param item : L2ItemInstance to be adjusten
	* @return TradeItem representing adjusted item
	*/
	public void adjustAvailableItem(TradeItem item)
	{
		boolean notAllEquipped = false;
		for (L2ItemInstance adjItem: getItemsByItemId(item.getItem().getItemId()))
		{
			if (adjItem.isEquipable())
			{
				if (!adjItem.isEquipped())
					notAllEquipped |= true;
			}
			else
			{
				notAllEquipped |= true;
				break;
			}
		}

		if (notAllEquipped)
		{
			L2ItemInstance adjItem = getItemByItemId(item.getItem().getItemId());
			item.setObjectId(adjItem.getObjectId());
			item.setEnchant(adjItem.getEnchantLevel());
			
			if (adjItem.getCount() < item.getCount())
				item.setCount(adjItem.getCount());
			
			return;
		}

		item.setCount(0);
	}

	/**
	 * Adds adena to PCInventory
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of adena to be added
	 * @param actor : L2Player Player requesting the item add
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void addAdena(String process, long count, L2Player actor, L2Object reference)
	{
		if (count > 0)
			addItem(process, ADENA_ID, count, actor, reference);
	}

	/**
	 * Removes adena to PCInventory
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of adena to be removed
	 * @param actor : L2Player Player requesting the item add
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void reduceAdena(String process, long count, L2Player actor, L2Object reference)
	{
		if (count > 0)
			destroyItemByItemId(process, ADENA_ID, count, actor, reference);
	}
	
	/**
	 * Adds specified amount of ancient adena to player inventory.
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of adena to be added
	 * @param actor : L2Player Player requesting the item add
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void addAncientAdena(String process, long count, L2Player actor, L2Object reference)
	{
		if (count > 0)
			addItem(process, ANCIENT_ADENA_ID, count, actor, reference);
	}

	/**
	 * Removes specified amount of ancient adena from player inventory.
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of adena to be removed
	 * @param actor : L2Player Player requesting the item add
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void reduceAncientAdena(String process, long count, L2Player actor, L2Object reference)
	{
		if (count > 0)
			destroyItemByItemId(process, ANCIENT_ADENA_ID, count, actor, reference);
	}

	/**
	 * Adds item in inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be added
	 * @param actor : L2Player Player requesting the item add
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance addItem(String process, L2ItemInstance item, L2Player actor, L2Object reference)
	{
		item = super.addItem(process, item, actor, reference);
		
		if (item != null && item.getItemId() == ADENA_ID && !item.equals(_adena))
			_adena = item;
		
		if (item != null && item.getItemId() == ANCIENT_ADENA_ID && !item.equals(_ancientAdena))
			_ancientAdena = item;
		
		return item;
	}

	/**
	 * Adds item in inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be added
	 * @param count : long Quantity of items to be added
	 * @param actor : L2Player Player requesting the item creation
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance addItem(String process, int itemId, long count, L2Player actor, L2Object reference)
	{
		L2ItemInstance item = super.addItem(process, itemId, count, actor, reference);
		
		if (item != null && item.getItemId() == ADENA_ID && !item.equals(_adena))
			_adena = item;
		
		if (item != null && item.getItemId() == ANCIENT_ADENA_ID && !item.equals(_ancientAdena))
			_ancientAdena = item;
		
		return item;
	}

	/**
	 * Transfers item to another inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be transfered
	 * @param count : long Quantity of items to be transfered
	 * @param actor : L2Player Player requesting the item transfer
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance transferItem(String process, int objectId, long count, ItemContainer target, L2Player actor, L2Object reference)
	{
		L2ItemInstance item = super.transferItem(process, objectId, count, target, actor, reference);
		
		if (_adena != null && (_adena.getCount() <= 0 || _adena.getOwnerId() != getOwnerId()))
			_adena = null;
		
		if (_ancientAdena != null && (_ancientAdena.getCount() <= 0 || _ancientAdena.getOwnerId() != getOwnerId()))
			_ancientAdena = null;
		
		return item;
	}

	/**
	 * Destroy item from inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param actor : L2Player Player requesting the item destroy
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, L2Player actor, L2Object reference)
	{
		return this.destroyItem(process, item, item.getCount(), actor, reference);
	}

	/**
	 * Destroy item from inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param actor : L2Player Player requesting the item destroy
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, long count, L2Player actor, L2Object reference)
	{
		item = super.destroyItem(process, item, count, actor, reference);
		
		if (_adena != null && _adena.getCount() <= 0)
			_adena = null;
		
		if (_ancientAdena != null && _ancientAdena.getCount() <= 0)
			_ancientAdena = null;
		
		return item;
	}

	/**
	 * Destroys item from inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : long Quantity of items to be destroyed
	 * @param actor : L2Player Player requesting the item destroy
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance destroyItem(String process, int objectId, long count, L2Player actor, L2Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}
		return this.destroyItem(process, item, count, actor, reference);
	}

	/**
	 * Destroy item from inventory by using its <B>itemId</B> and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : long Quantity of items to be destroyed
	 * @param actor : L2Player Player requesting the item destroy
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance destroyItemByItemId(String process, int itemId, long count, L2Player actor, L2Object reference)
	{
		L2ItemInstance item = getItemByItemId(itemId);
		if (item == null)
		{
			return null;
		}
		return this.destroyItem(process, item, count, actor, reference);
	}

	/**
	 * Drop item from inventory and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be dropped
	 * @param actor : L2Player Player requesting the item drop
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance dropItem(String process, L2ItemInstance item, L2Player actor, L2Object reference)
	{
		item = super.dropItem(process, item, actor, reference);
		
		if (_adena != null && (_adena.getCount() <= 0 || _adena.getOwnerId() != getOwnerId()))
			_adena = null;
		
		if (_ancientAdena != null && (_ancientAdena.getCount() <= 0 || _ancientAdena.getOwnerId() != getOwnerId()))
			_ancientAdena = null;
		
		return item;
	}
	
	/**
	 * Drop item from inventory by using its <B>objectID</B> and checks _adena and _ancientAdena
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be dropped
	 * @param count : long Quantity of items to be dropped
	 * @param actor : L2Player Player requesting the item drop
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
	 */
	@Override
	public L2ItemInstance dropItem(String process, int objectId, long count, L2Player actor, L2Object reference)
	{
		L2ItemInstance item = super.dropItem(process, objectId, count, actor, reference);
		
		if (_adena != null && (_adena.getCount() <= 0 || _adena.getOwnerId() != getOwnerId()))
			_adena = null;
		
		if (_ancientAdena != null && (_ancientAdena.getCount() <= 0 || _ancientAdena.getOwnerId() != getOwnerId()))
			_ancientAdena = null;

		return item;
	}

	/**
	 * <b>Overloaded</b>, when removes item from inventory, remove also owner shortcuts.
	 * @param item : L2ItemInstance to be removed from inventory
	 */
	@Override
	protected boolean removeItem(L2ItemInstance item)
	{
		// Removes any reference to the item from Shortcut bar
		getOwner().getPlayerSettings().removeItemFromShortCut(item.getObjectId());

		// Removes active Enchant Scroll
		if(item.equals(getOwner().getActiveEnchantItem()))
			getOwner().setActiveEnchantItem(null);
		
		if (item.getItemId() == ADENA_ID)
			_adena = null;
		else if (item.getItemId() == ANCIENT_ADENA_ID)
			_ancientAdena = null;
		
		return super.removeItem(item);
	}
	
	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	public void refreshWeight()
	{
		super.refreshWeight();
		getOwner().refreshOverloaded();
	}
	
	/**
	 * Get back items in inventory from database
	 */
	@Override
	public void restore()
	{
		super.restore();
		_adena = getItemByItemId(ADENA_ID);
		_ancientAdena = getItemByItemId(ANCIENT_ADENA_ID);
	}

	public static int[][] restoreVisibleInventory(int objectId)
	{
		int[][] paperdoll = new int[Inventory.PAPERDOLL_TOTALSLOTS][4];
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement2 = con.prepareStatement(
					"SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'");
			statement2.setInt(1, objectId);
			ResultSet invdata = statement2.executeQuery();

			int slot, objId, itemId, enchant, displayId;
			while (invdata.next())
			{
				slot = invdata.getInt("loc_data");
				objId = invdata.getInt("object_id");
				itemId = invdata.getInt("item_id");
				enchant = invdata.getInt("enchant_level");
				displayId = ItemTable.getInstance().getTemplate(itemId).getItemDisplayId();

				paperdoll[slot][0] = objId;
				paperdoll[slot][1] = itemId;
				paperdoll[slot][2] = enchant;
				paperdoll[slot][3] = displayId;
				if (slot == Inventory.PAPERDOLL_LRHAND)
				{
					paperdoll[Inventory.PAPERDOLL_RHAND][0] = objId;
					paperdoll[Inventory.PAPERDOLL_RHAND][1] = itemId;
					paperdoll[Inventory.PAPERDOLL_RHAND][2] = enchant;
					paperdoll[Inventory.PAPERDOLL_RHAND][3] = displayId;
				}
			}
			
			invdata.close();
			statement2.close();
		}
		catch (Exception e)
		{
			_log.warn( "could not restore inventory:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		return paperdoll;
	}
	
	
	public boolean validateCapacity(L2ItemInstance item)
	{
		int slots = 0;
		
		if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null) && item.getItemType() != L2EtcItemType.HERB)
			slots++;
		
		return validateCapacity(slots);
	}
	
	public boolean validateCapacity(FastList<L2ItemInstance> items)
	{
	   int slots = 0;
	   
	   for (L2ItemInstance item : items)
		   if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null))
				slots++;
			  
	   return validateCapacity(slots);
	}
		  
	public boolean validateCapacityByItemId(int ItemId)
	{
	   int slots = 0;
			  
	   L2ItemInstance invItem = getItemByItemId(ItemId);
	   if (!(invItem != null && invItem.isStackable()))
		   slots++;
			  
	   return validateCapacity(slots);
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return (_items.size() + slots <= _owner.getInventoryLimit());
	}

	@Override
	public boolean validateWeight(int weight)
	{
		return (_totalWeight + weight <= _owner.getMaxLoad());
	}
	
	/**
	 * Set inventory block for specified IDs<br>
	 * array reference is used for {@link PcInventory#_blockItems}
	 * @param items array of Ids to block/allow
	 * @param mode blocking mode {@link PcInventory#_blockMode}
	 */
	public void setInventoryBlock(int[] items, int mode)
	{
		_blockMode = mode;
		_blockItems = items;
		
		_owner.sendPacket(new ItemList(_owner, false));
	}
	
	/**
	 * Unblock blocked itemIds
	 */
	public void unblock()
	{
		_blockMode = -1;
		_blockItems = null;
		
		_owner.sendPacket(new ItemList(_owner, false));
	}
	
	/**
	 * Check if player inventory is in block mode.
	 * @return true if some itemIds blocked
	 */
	public boolean hasInventoryBlock()
	{
		return (_blockMode > -1 && _blockItems != null && _blockItems.length > 0);
	}
	
	/**
	 * Block all player items
	 */
	public void blockAllItems()
	{
		// temp fix, some id must be sended
		setInventoryBlock(new int[] {(ItemTable.getInstance().getArraySize() + 2)}, 1);
	}
	
	/**
	 * Return block mode
	 * @return int  {@link PcInventory#_blockMode}
	 */
	public int getBlockMode()
	{
		return _blockMode;
	}
	
	/**
	 * Return TIntArrayList with blocked item ids
	 * @return TIntArrayList
	 */
	public int[] getBlockItems()
	{
		return _blockItems;
	}

	/**
	 * @see Inventory#updateInventory()
	 */
	@Override
	public void updateInventory(L2ItemInstance newItem)
	{
		if(newItem == null) return;
		L2Player targetPlayer = getOwner();
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(newItem);
			targetPlayer.sendPacket(playerIU);
			playerIU = null;
		}
		else targetPlayer.sendPacket(new ItemList(targetPlayer, false));

		// Update current load as well
		if(newItem.getItem().getWeight() <= 0) return;
		StatusUpdate playerSU = new StatusUpdate(targetPlayer.getObjectId());
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
		targetPlayer.sendPacket(playerSU);
		playerSU = null;
	}
}
