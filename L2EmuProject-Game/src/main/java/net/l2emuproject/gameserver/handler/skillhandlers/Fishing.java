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
package net.l2emuproject.gameserver.handler.skillhandlers;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.model.itemcontainer.Inventory;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.ItemList;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.templates.item.L2Weapon;
import net.l2emuproject.gameserver.templates.item.L2WeaponType;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.gameserver.world.zone.ZoneManager;
import net.l2emuproject.tools.random.Rnd;


public class Fishing implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.FISHING };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2Player))
			return;

		L2Player player = (L2Player) activeChar;

		if (!Config.ALLOW_FISHING)
		{
			player.sendMessage("Fishing is disabled.");
			return;
		}
		if (player.getPlayerFish().isFishing())
		{
			if (player.getPlayerFish().getFishCombat() != null)
				player.getPlayerFish().getFishCombat().doDie(false);
			else
				player.getPlayerFish().endFishing(false);
			// Cancel fishing
			player.sendPacket(SystemMessageId.FISHING_ATTEMPT_CANCELLED);
			return;
		}
		if (player.isInBoat())
		{
			// You can't fish while you are on boat
			player.sendPacket(SystemMessageId.CANNOT_FISH_ON_BOAT);
			return;
		}
		if (!player.isInsideZone(L2Zone.FLAG_FISHING) || player.isInsideZone(L2Zone.FLAG_PEACE))
		{
			// You can't fish here
			player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
			return;
		}
		if (player.isInsideZone(L2Zone.FLAG_WATER))
		{
			// You can't fish in water
			player.sendPacket(SystemMessageId.CANNOT_FISH_UNDER_WATER);
			return;
		}

		// Calculate point of a float
		int d = Rnd.get(50) + 150;
		double angle = Util.convertHeadingToDegree(player.getHeading());
		double radian = Math.toRadians(angle);

		int dx = (int) (d * Math.cos(radian));
		int dy = (int) (d * Math.sin(radian));

		int x = activeChar.getX() + dx;
		int y = activeChar.getY() + dy;

		L2Zone water = ZoneManager.getInstance().isInsideZone(L2Zone.ZoneType.Water, x, y);

		// Float must be in water
		if (water == null)
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
			return;
		}

		int z = water.getMaxZ(x, y, activeChar.getZ());

		if (Config.GEODATA > 0 && !GeoData.getInstance().canSeeTarget(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, z, activeChar.getInstanceId())
				|| (Config.GEODATA == 0 && (Util.calculateDistance(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, z, true) > d * 1.73)))
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_HERE);
			return;
		}
		if (player.isInCraftMode() || player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK);
			return;
		}
		L2Weapon weaponItem = player.getActiveWeaponItem();
		if ((weaponItem == null || weaponItem.getItemType() != L2WeaponType.ROD))
		{
			//Fishing poles are not installed
			player.sendPacket(SystemMessageId.FISHING_POLE_NOT_EQUIPPED);
			return;
		}
		L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null)
		{
			//Bait not equiped.
			player.sendPacket(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING);
			return;
		}
		player.getPlayerFish().setLure(lure);
		L2ItemInstance lure2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		if (lure2 == null || lure2.getCount() < 1) //Not enough bait.
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_BAIT);
			player.sendPacket(new ItemList(player, false));
		}
		else
		// Has enough bait, consume 1 and update inventory. Start fishing follows.
		{
			lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, player, null);
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(lure2);
			player.sendPacket(iu);
		}

		// Client itself find z coord of a float
		player.getPlayerFish().startFishing(x, y, z + 10);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
