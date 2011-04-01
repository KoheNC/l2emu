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
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExAttributeEnchantResult;
import net.l2emuproject.gameserver.network.serverpackets.ExBrExtraUserInfo;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.network.serverpackets.UserInfo;
import net.l2emuproject.gameserver.services.attribute.AttributeItems;
import net.l2emuproject.gameserver.services.attribute.Attributes;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.templates.item.L2ArmorType;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.templates.item.L2WeaponType;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

public class RequestExEnchantItemAttribute extends L2GameClientPacket
{
	private static final String D0_35_REQUEST_EX_ENCHANT_ITEM_ATTRIBUTE = "[C] D0:35 RequestExEnchantItemAttribute";
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (_objectId == 0xFFFFFFFF)
		{
			// Player canceled enchant
			player.setActiveEnchantAttrItem(null);
			requestFailed(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
			return;
		}
		
		if (player.isOnline() == 0)
		{
			player.setActiveEnchantAttrItem(null);
			return;
		}
		
		if (player.getPrivateStoreType() != 0)
		{
			requestFailed(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			player.setActiveEnchantAttrItem(null);
			return;
		}
		
		// Restrict enchant during a trade (bug if enchant fails)
		if (player.getActiveRequester() != null)
		{
			// Cancel trade
			player.cancelActiveTrade();
			player.setActiveEnchantAttrItem(null);
			player.sendMessage("Enchanting items is not allowed during a trade.");
			return;
		}
		
		L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		L2ItemInstance stone = player.getActiveEnchantAttrItem();
		if (item == null || stone == null)
		{
			player.setActiveEnchantAttrItem(null);
			return;
		}
		if ((item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY) && (item.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL))
		{
			player.setActiveEnchantAttrItem(null);
			return;
		}
		
		//can't enchant rods, shadow items, adventurers', PvP items, hero items, cloaks, bracelets, underwear (e.g. shirt), belt, necklace, earring, ring
		if (item.getItem().getItemType() == L2WeaponType.ROD || item.isShadowItem() || item.isPvp() || item.isHeroItem() || item.isTimeLimitedItem() ||
				(item.getItemId() >= 7816 && item.getItemId() <= 7831) || (item.getItem().getItemType() == L2WeaponType.NONE) ||
				item.getItem().getItemGradeSPlus() != L2Item.CRYSTAL_S || item.getItem().getBodyPart() == L2Item.SLOT_BACK ||
				item.getItem().getBodyPart() == L2Item.SLOT_R_BRACELET || item.getItem().getBodyPart() == L2Item.SLOT_UNDERWEAR ||
				item.getItem().getBodyPart() == L2Item.SLOT_BELT || item.getItem().getBodyPart() == L2Item.SLOT_NECK ||
				(item.getItem().getBodyPart() & L2Item.SLOT_R_EAR) != 0 || (item.getItem().getBodyPart() & L2Item.SLOT_R_FINGER) != 0 ||
				item.getItem().getElementals() != null || /*item.getItemType() == L2ArmorType.SHIELD ||*/ item.getItemType() == L2ArmorType.SIGIL)
		{
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_REQUIREMENT_NOT_SUFFICIENT);
			player.setActiveEnchantAttrItem(null);
			return;
		}
		
		switch (item.getLocation())
		{
			case INVENTORY:
			case PAPERDOLL:
			{
				if (item.getOwnerId() != player.getObjectId())
				{
					player.setActiveEnchantAttrItem(null);
					return;
				}
				break;
			}
			default:
			{
				player.setActiveEnchantAttrItem(null);
				Util.handleIllegalPlayerAction(player, "Player "+player.getName()+" tried to use enchant Exploit!", Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		int stoneId = stone.getItemId();
		byte elementToAdd = Attributes.getItemElementById(stoneId);
		// Armors have the opposite element
		if (item.isArmor())
			elementToAdd = Attributes.getOppositeElement(elementToAdd);
		byte opositeElement = Attributes.getOppositeElement(elementToAdd);
		
		Attributes oldElement = item.getAttribute(elementToAdd);
		int elementValue = oldElement == null ? 0 : oldElement.getValue();
		int limit = getLimit(item, stoneId);
		int powerToAdd = getPowerToAdd(stoneId, elementValue, item);
		
		if ((item.isWeapon() && oldElement != null && oldElement.getElement() != elementToAdd && oldElement.getElement() != -2)
				|| (item.isArmor() && item.getAttribute(elementToAdd) == null && item.getElementals() != null && item.getElementals().length >= 3))
		{
			player.sendPacket(SystemMessageId.ANOTHER_ELEMENTAL_POWER_ALREADY_ADDED);
			player.setActiveEnchantAttrItem(null);
			return;
		}
		
		if (item.isArmor() && item.getElementals() != null)
		{
			//cant add opposite element
			for (Attributes elm : item.getElementals())
			{
				if (elm.getElement() == opositeElement)
				{
					player.setActiveEnchantAttrItem(null);
					Util.handleIllegalPlayerAction(player, "Player "+player.getName()+" tried to add oposite attribute to item!", Config.DEFAULT_PUNISH);
					return;
				}
			}
		}
		
		int newPower = elementValue + powerToAdd;
		if (newPower > limit)
		{
			newPower = limit;
			powerToAdd = limit - elementValue;
		}
		
		if (powerToAdd <= 0)
		{
			player.sendPacket(SystemMessageId.ELEMENTAL_ENHANCE_CANCELED);
			player.setActiveEnchantAttrItem(null);
			return;
		}
		
		if(!player.destroyItem("AttrEnchant", stone, 1, player, true))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			Util.handleIllegalPlayerAction(player, "Player "+player.getName()+" tried to attribute enchant with a stone he doesn't have", Config.DEFAULT_PUNISH);
			player.setActiveEnchantAttrItem(null);
			return;
		}
		boolean success = false;
		switch(Attributes.getItemElemental(stoneId).getItemType())
		{
			case STONE:
			case ROUGHORE:
				success = Rnd.get(100) < 50;
				break;
			case CRYSTAL:
				success = Rnd.get(100) < 50;
				break;
			case JEWEL:
				success = Rnd.get(100) < 50;
				break;
			case ENERGY:
				success = Rnd.get(100) < 50;
				break;
		}
		if (success)
		{			
			SystemMessage sm;
			if (item.getEnchantLevel() == 0)
			{
				if (item.isArmor())
					sm = new SystemMessage(SystemMessageId.THE_S2_ATTRIBUTE_WAS_SUCCESSFULLY_BESTOWED_ON_S1_RES_TO_S3_INCREASED);
				else
					sm = new SystemMessage(SystemMessageId.ELEMENTAL_POWER_S2_SUCCESSFULLY_ADDED_TO_S1);
				sm.addItemName(item);
				sm.addString(Attributes.getElementName(elementToAdd));
			}
			else
			{
				if (item.isArmor())
					sm = new SystemMessage(SystemMessageId.THE_S3_ATTRIBUTE_BESTOWED_ON_S1_S2_RESISTANCE_TO_S4_INCREASED);
				else
					sm = new SystemMessage(SystemMessageId.ELEMENTAL_POWER_S3_SUCCESSFULLY_ADDED_TO_S1_S2);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item);
				sm.addString(Attributes.getElementName(elementToAdd));
			}
			player.sendPacket(sm);
			item.setElementAttr(elementToAdd, newPower);
			if (item.isEquipped())
				item.updateElementAttrBonus(player);
			
			// send packets
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(item);
			player.sendPacket(iu);
		}
		else
			player.sendPacket(SystemMessageId.FAILED_ADDING_ELEMENTAL_POWER);
		
		player.sendPacket(new ExAttributeEnchantResult(powerToAdd));
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new ExBrExtraUserInfo(player));
		player.setActiveEnchantAttrItem(null);
	}
	
	public int getLimit(L2ItemInstance item, int sotneId)
	{
		AttributeItems elementItem = Attributes.getItemElemental(sotneId);
		if (elementItem == null)
			return 0;
		
		if (item.isWeapon())
			return Attributes.WEAPON_VALUES[elementItem.getItemType().getMaxLevel()];
		else
			return Attributes.ARMOR_VALUES[elementItem.getItemType().getMaxLevel()];
	}
	
	public int getPowerToAdd(int stoneId, int oldValue, L2ItemInstance item)
	{
		if (Attributes.getItemElementById(stoneId) != Attributes.NONE)
		{
			if (item.isWeapon())
			{
				if (oldValue == 0)
					return Attributes.FIRST_WEAPON_BONUS;
				else
					return Attributes.NEXT_WEAPON_BONUS;
			}
			else if (item.isArmor())
				return Attributes.ARMOR_BONUS;
		}
		
		return 0;
	}
	
	@Override
	public String getType()
	{
		return D0_35_REQUEST_EX_ENCHANT_ITEM_ATTRIBUTE;
	}
}
