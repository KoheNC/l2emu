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

import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.items.L2ItemInstance.ItemLocation;
import net.l2emuproject.gameserver.network.serverpackets.PetInventoryUpdate;
import net.l2emuproject.gameserver.templates.item.L2EtcItemType;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.world.object.instance.L2PetInstance;

public class PetInventory extends Inventory
{
	private final L2PetInstance	_owner;

	public PetInventory(L2PetInstance owner)
	{
		_owner = owner;
	}

	@Override
	public L2PetInstance getOwner()
	{
		return _owner;
	}

	@Override
	public int getOwnerId()
	{
		// gets the L2Player-owner's ID
		int id = 0;

		if (_owner.getOwner() != null)
		{
			id = _owner.getOwner().getObjectId();
		}

		return id;
	}

	public boolean validateCapacity(L2ItemInstance item)
	{
		int slots = 0;

		if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null) && item.getItemType() != L2EtcItemType.HERB)
			slots++;

		return validateCapacity(slots);
	}

	@Override
	public boolean validateCapacity(int slots)
	{
		return (_items.size() + slots <= _owner.getInventoryLimit());
	}

	public boolean validateWeight(L2ItemInstance item, long count)
	{
		int weight = 0;
		L2Item template = ItemTable.getInstance().getTemplate(item.getItemId());
		if (template == null)
			return false;
		weight += count * template.getWeight();
		return validateWeight(weight);
	}

	@Override
	public boolean validateWeight(int weight)
	{
		return (_totalWeight + weight <= _owner.getMaxLoad());
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.PET;
	}

	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PET_EQUIP;
	}

	@Override
	public void refreshWeight()
	{
		super.refreshWeight();
		getOwner().broadcastFullInfo();
	}

	@Override
	public void updateInventory(L2ItemInstance newItem)
	{
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(newItem);
		getOwner().getOwner().sendPacket(petIU);
	}
}
