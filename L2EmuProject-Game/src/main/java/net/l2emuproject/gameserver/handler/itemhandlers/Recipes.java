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
package net.l2emuproject.gameserver.handler.itemhandlers;

import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.model.actor.L2Playable;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.crafting.L2RecipeList;
import net.l2emuproject.gameserver.services.crafting.RecipeService;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.5.2.5 $ $Date: 2005/04/06 16:13:51 $
 */

public class Recipes implements IItemHandler
{
	// All the item IDs that this handler knows.
	private final int[]	ITEM_IDS;

	public Recipes()
	{
		ITEM_IDS = RecipeService.getInstance().getAllItemIds();
	}

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2RecipeList rp = RecipeService.getInstance().getRecipeByItemId(item.getItemId());
		if (rp == null)
			return;
		if (activeChar.getPlayerRecipe().hasRecipeList(rp.getId()))
		{
			activeChar.sendPacket(SystemMessageId.RECIPE_ALREADY_REGISTERED);
		}
		else
		{
			if (rp.isDwarvenRecipe())
			{
				if (activeChar.hasDwarvenCraft())
				{
					if (rp.getLevel() > activeChar.getDwarvenCraft())
					{
						// Can't add recipe, becouse create item level too low
						activeChar.sendPacket(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
					}
					else if (activeChar.getPlayerRecipe().getDwarvenRecipeBook().length >= activeChar.getDwarfRecipeLimit())
					{
						// Up to $s1 recipes can be registered.
						SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
						sm.addNumber(activeChar.getDwarfRecipeLimit());
						activeChar.sendPacket(sm);
					}
					else
					{
						activeChar.getPlayerRecipe().registerDwarvenRecipeList(rp, true);
						activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
						activeChar.sendMessage("Added recipe \"" + item.getItemName() + "\" to dwarven recipe book.");
						//activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_ADDED).addItemName(item));
					}
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
				}
			}
			else
			{
				if (activeChar.hasCommonCraft())
				{
					if (rp.getLevel() > activeChar.getCommonCraft())
					{
						// Can't add recipe, becouse create item level too low
						activeChar.sendPacket(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER);
					}
					else if (activeChar.getPlayerRecipe().getCommonRecipeBook().length >= activeChar.getCommonRecipeLimit())
					{
						// Up to $s1 recipes can be registered.
						SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
						sm.addNumber(activeChar.getCommonRecipeLimit());
						activeChar.sendPacket(sm);
					}
					else
					{
						activeChar.getPlayerRecipe().registerCommonRecipeList(rp, true);
						activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
						activeChar.sendMessage("Added recipe \"" + item.getItemName() + "\" to common recipe book.");
						//activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_ADDED).addItemName(item));
					}
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT);
				}
			}
		}
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
