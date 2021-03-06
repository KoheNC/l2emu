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
package net.l2emuproject.gameserver.skills.conditions;

import net.l2emuproject.gameserver.entity.itemcontainer.Inventory;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.templates.item.L2ArmorType;
import net.l2emuproject.gameserver.templates.item.L2Equip;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author mkizub
 */
public class ConditionUsingItemType extends Condition
{
	
	private final int _mask;
	
	public ConditionUsingItemType(int mask)
	{
		_mask = mask;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.getPlayer() instanceof L2Player))
			return false;
		Inventory inv = ((L2Player)env.getPlayer()).getInventory();
		
		//If ConditionUsingItemType is one between Light, Heavy or Magic
		if (_mask == L2ArmorType.LIGHT.mask() || _mask == L2ArmorType.HEAVY.mask() || _mask == L2ArmorType.MAGIC.mask())
		{
			//Get the itemMask of the weared chest (if exists)
			L2ItemInstance chest = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest == null || !(chest.getItem() instanceof L2Equip))
				return false;
			int chestMask = ((L2Equip)chest.getItem()).getItemMask();
			
			//If chest armor is different from the condition one return false
			if ((_mask & chestMask) == 0)
				return false;
			
			//So from here, chest armor matches conditions
			
			int chestBodyPart = chest.getItem().getBodyPart();
			//return True if chest armor is a Full Armor
			if (chestBodyPart == L2Item.SLOT_FULL_ARMOR)
				return true;
			
			//check legs armor
			L2ItemInstance legs = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if (legs == null || !(legs.getItem() instanceof L2Equip))
				return false;
			int legMask = ((L2Equip)legs.getItem()).getItemMask();
			//return true if legs armor matches too
			return (_mask & legMask) != 0;
		}
		
		return (_mask & inv.getWearedMask()) != 0;
	}
}
