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
package net.l2emuproject.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.l2emuproject.gameserver.model.ItemInfo;
import net.l2emuproject.gameserver.model.L2ItemInstance;

/**
 * 
 * 37                // Packet Identifier <BR>
 * 01 00             // Number of ItemInfo Trame of the Packet <BR><BR>
 * 
 * 03 00             // Update type : 01-add, 02-modify, 03-remove <BR>
 * 04 00             // Item Type 1 : 00-weapon/ring/earring/necklace, 01-armor/shield, 04-item/questitem/adena <BR>
 * c6 37 50 40       // ObjectId <BR>
 * cd 09 00 00       // ItemId <BR>
 * 05 00 00 00       // Quantity <BR>
 * 05 00             // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item <BR>
 * 00 00             // Filler (always 0) <BR>
 * 00 00             // Equipped    : 00-No, 01-yes <BR>
 * 00 00             // Slot        : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand <BR>
 * 00 00             // Enchant level (pet level shown in control item) <BR>
 * 00 00             // Pet name exists or not shown in control item <BR><BR><BR>
 * 
 *
 * format   h (hh dddhhhh hh)    revision 377 <BR>
 * format   h (hh dddhhhd hh)   revision 415 <BR><BR>
 *          h (hh dddhhhd hhhhd) rev 729
 * @version $Revision: 1.3.2.2.2.4 $ $Date: 2005/03/27 15:29:39 $
 * Rebuild 23.2.2006 by Advi
 */
public final class InventoryUpdate extends L2GameServerPacket
{
	private static final String	_S__37_INVENTORYUPDATE	= "[S] 27 InventoryUpdate";

	private List<ItemInfo>		_items;

	public InventoryUpdate()
	{
		_items = new ArrayList<ItemInfo>();
		if (_log.isDebugEnabled())
		{
			showDebug();
		}
	}

	public InventoryUpdate(List<ItemInfo> items)
	{
		_items = items;
		if (_log.isDebugEnabled())
		{
			showDebug();
		}
	}

	public final void addItem(L2ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item));
	}

	public final void addNewItem(L2ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item, 1));
	}

	public final void addModifiedItem(L2ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item, 2));
	}

	public final void addRemovedItem(L2ItemInstance item)
	{
		if (item != null)
			_items.add(new ItemInfo(item, 3));
	}

	public final void addItems(List<L2ItemInstance> items)
	{
		if (items != null)
			for (L2ItemInstance item : items)
				if (item != null)
					_items.add(new ItemInfo(item));
	}

	public final void addEquipItems(L2ItemInstance[] items)
	{
		if (items != null)
			for (L2ItemInstance item : items)
				if (item != null)
					_items.add(new ItemInfo(item, 2));
	}

	private final void showDebug()
	{
		for (ItemInfo item : _items)
		{
			_log.debug("oid:" + Integer.toHexString(item.getObjectId()) + " item:" + item.getItem().getName() + " last change:" + item.getChange());
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x21);
		int count = _items.size();
		writeH(count);
		for (ItemInfo item : _items)
		{
			writeH(item.getChange()); // Update type : 01-add, 02-modify, 03-remove
			writeD(item.getObjectId()); // ObjectId
			writeD(item.getItem().getItemId()); // ItemId
			writeD(item.getLocation()); // T1
			writeQ(item.getCount()); // Quantity
			writeH(item.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
			writeH(item.getCustomType1()); // Filler (always 0)
			writeH(item.getEquipped()); // Equipped    : 00-No, 01-yes
			writeD(item.getItem().getBodyPart()); // Slot        : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand
			writeH(item.getEnchant()); // Enchant level (pet level shown in control item)
			writeH(item.getCustomType2()); // Pet name exists or not shown in control item
			writeD(item.getAugemtationBonus());
			writeD(item.getMana());
			writeD(item.getTime());
			// T1
			writeElementalInfo(item);
			// Enchant Effects
			writeEnchantEffectInfo();
		}
		_items.clear();
		_items = null;
	}

	@Override
	public final String getType()
	{
		return _S__37_INVENTORYUPDATE;
	}
}