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
package net.l2emuproject.status.commands;

import java.util.StringTokenizer;

import net.l2emuproject.gameserver.model.GMAudit;
import net.l2emuproject.gameserver.model.L2ItemInstance;
import net.l2emuproject.gameserver.model.L2World;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.itemcontainer.Inventory;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.status.GameStatusCommand;

public final class Enchant extends GameStatusCommand
{
	public Enchant()
	{
		super("enchants players item (itemType: 1 - Helmet, 2 - Chest, 3 - Gloves, 4 - Feet, 5 - Legs,"
				+ " 6 - Right Hand, 7 - Left Hand, 8 - Left Ear, 9 - Right Ear , 10 - Left Finger,"
				+ " 11 - Right Finger, 12- Necklace, 13 - Underwear, 14 - Back, 15 - Belt, 0 - No Enchant)", "enchant");
	}
	
	@Override
	protected String getParameterUsage()
	{
		return "player itemType enchant";
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		StringTokenizer st = new StringTokenizer(params, " ");
		int enchant = 0, itemType = 0;
		
		try
		{
			L2PcInstance player = L2World.getInstance().getPlayer(st.nextToken());
			itemType = Integer.parseInt(st.nextToken());
			enchant = Integer.parseInt(st.nextToken());
			
			switch (itemType)
			{
				case 1:
					itemType = Inventory.PAPERDOLL_HEAD;
					break;
				case 2:
					itemType = Inventory.PAPERDOLL_CHEST;
					break;
				case 3:
					itemType = Inventory.PAPERDOLL_GLOVES;
					break;
				case 4:
					itemType = Inventory.PAPERDOLL_FEET;
					break;
				case 5:
					itemType = Inventory.PAPERDOLL_LEGS;
					break;
				case 6:
					itemType = Inventory.PAPERDOLL_RHAND;
					break;
				case 7:
					itemType = Inventory.PAPERDOLL_LHAND;
					break;
				case 8:
					itemType = Inventory.PAPERDOLL_LEAR;
					break;
				case 9:
					itemType = Inventory.PAPERDOLL_REAR;
					break;
				case 10:
					itemType = Inventory.PAPERDOLL_LFINGER;
					break;
				case 11:
					itemType = Inventory.PAPERDOLL_RFINGER;
					break;
				case 12:
					itemType = Inventory.PAPERDOLL_NECK;
					break;
				case 13:
					itemType = Inventory.PAPERDOLL_UNDER;
					break;
				case 14:
					itemType = Inventory.PAPERDOLL_BACK;
					break;
				case 15:
					itemType = Inventory.PAPERDOLL_BELT;
					break;
				default:
					itemType = 0;
			}
			
			if (enchant > 65535)
				enchant = 65535;
			else if (enchant < 0)
				enchant = 0;
			
			boolean success = false;
			
			if (player != null && itemType > 0)
			{
				success = setEnchant(player, enchant, itemType);
				if (success)
					println("Item enchanted successfully.");
			}
			else if (!success)
				println("Item failed to enchant.");
		}
		catch (Exception e)
		{
			
		}
	}
	
	private boolean setEnchant(L2PcInstance activeChar, int ench, int armorType)
	{
		// now we need to find the equipped weapon of the targeted character...
		int curEnchant = 0; // display purposes only
		L2ItemInstance itemInstance = null;
		
		// only attempt to enchant if there is a weapon equipped
		L2ItemInstance parmorInstance = activeChar.getInventory().getPaperdollItem(armorType);
		if (parmorInstance != null && parmorInstance.getLocationSlot() == armorType)
		{
			itemInstance = parmorInstance;
		}
		else
		{
			// for bows/crossbows and double handed weapons
			parmorInstance = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
			if (parmorInstance != null && parmorInstance.getLocationSlot() == Inventory.PAPERDOLL_LRHAND)
				itemInstance = parmorInstance;
		}
		
		if (itemInstance != null)
		{
			curEnchant = itemInstance.getEnchantLevel();
			
			// set enchant value
			activeChar.getInventory().unEquipItemInSlotAndRecord(armorType);
			itemInstance.setEnchantLevel(ench);
			activeChar.getInventory().equipItemAndRecord(itemInstance);
			
			// send packets
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(itemInstance);
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
			
			// informations
			activeChar.sendMessage("Changed enchantment of " + activeChar.getName() + "'s "
					+ itemInstance.getItem().getName() + " from " + curEnchant + " to " + ench + ".");
			activeChar.sendMessage("Admin has changed the enchantment of your " + itemInstance.getItem().getName()
					+ " from " + curEnchant + " to " + ench + ".");
			
			String IP = getHostAddress();
			// log
			GMAudit.auditGMAction(IP, activeChar.getName(), "telnet-enchant", "telnet-enchant", itemInstance.getItem()
					.getName()
					+ "(" + itemInstance.getObjectId() + ")" + " from " + curEnchant + " to " + ench);
			return true;
		}
		return false;
	}
}
