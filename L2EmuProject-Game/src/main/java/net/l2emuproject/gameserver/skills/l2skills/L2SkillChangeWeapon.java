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
package net.l2emuproject.gameserver.skills.l2skills;

import net.l2emuproject.gameserver.entity.itemcontainer.Inventory;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.attribute.Attributes;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.StatsSet;
import net.l2emuproject.gameserver.templates.item.L2Weapon;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 *
 * @author nBd
 */
public class L2SkillChangeWeapon extends L2Skill
{

	/**
	 * @param set
	 */
	public L2SkillChangeWeapon(StatsSet set)
	{
		super(set);
	}

	/**
	 * @see net.l2emuproject.gameserver.skills.L2Skill#useSkill(net.l2emuproject.gameserver.world.object.L2Character, net.l2emuproject.gameserver.world.object.L2Character...)
	 */
	@Override
	public void useSkill(L2Character caster, L2Character... targets)
	{
		if (caster.isAlikeDead())
			return;

		if (!(caster instanceof L2Player))
			return;

		L2Player player = (L2Player) caster;

		L2Weapon weaponItem = player.getActiveWeaponItem();

		if (weaponItem == null)
			return;

		L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);

		if (wpn != null)
		{
			if (wpn.isWear())
				return;

			if (wpn.isAugmented())
				return;

			int newItemId = 0;
			int enchantLevel = 0;
			Attributes elementals = null;


			if (weaponItem.getChangeWeaponId() != 0)
			{
				newItemId = weaponItem.getChangeWeaponId();
				enchantLevel = wpn.getEnchantLevel();
				elementals = wpn.getElementals() == null ? null : wpn.getElementals()[0];


				if (newItemId == -1)
					return;

				L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
				InventoryUpdate iu = new InventoryUpdate();
				for (L2ItemInstance item: unequiped)
					iu.addModifiedItem(item);

				player.sendPacket(iu);

				if (unequiped.length > 0)
				{
					byte count = 0;

					for (L2ItemInstance item: unequiped)
					{
						if (!(item.getItem() instanceof L2Weapon))
						{
							count++;
							continue;
						}

						SystemMessage sm = null;
						if (item.getEnchantLevel() > 0)
						{
							sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
							sm.addNumber(item.getEnchantLevel());
							sm.addItemName(item);
						}
						else
						{
							sm = new SystemMessage(SystemMessageId.S1_DISARMED);
							sm.addItemName(item);
						}
						player.sendPacket(sm);
					}

					if (count == unequiped.length)
						return;
				}
				else
				{
					return;
				}

				L2ItemInstance destroyItem = player.getInventory().destroyItem("ChangeWeapon", wpn, player, null);

				if (destroyItem == null)
					return;

				L2ItemInstance newItem = player.getInventory().addItem("ChangeWeapon", newItemId, 1, player, destroyItem);

				if (newItem == null)
					return;

                if (elementals != null && elementals.getElement() != -1 && elementals.getValue() != -1)
                    newItem.setElementAttr(elementals.getElement(), elementals.getValue());
				newItem.setEnchantLevel(enchantLevel);
				player.getInventory().equipItem(newItem);
				player.refreshExpertisePenalty();

				SystemMessage msg = null;

				if (newItem.getEnchantLevel() > 0)
				{
					msg = new SystemMessage(SystemMessageId.S1_S2_EQUIPPED);
					msg.addNumber(newItem.getEnchantLevel());
					msg.addItemName(newItem);
				}
				else
				{
					msg = new SystemMessage(SystemMessageId.S1_EQUIPPED);
					msg.addItemName(newItem);
				}
				player.sendPacket(msg);

				InventoryUpdate u = new InventoryUpdate();
				u.addRemovedItem(destroyItem);
				u.addNewItem(newItem);
				player.sendPacket(u);

				player.broadcastUserInfo();
			}
		}
	}
}
