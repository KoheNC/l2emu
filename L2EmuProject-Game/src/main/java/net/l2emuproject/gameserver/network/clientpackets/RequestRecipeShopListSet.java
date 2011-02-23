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

import static net.l2emuproject.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.zone.L2Zone;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.RecipeShopMsg;
import net.l2emuproject.gameserver.services.crafting.L2ManufactureItem;
import net.l2emuproject.gameserver.services.crafting.L2ManufactureList;
import net.l2emuproject.gameserver.services.crafting.L2RecipeList;
import net.l2emuproject.gameserver.services.crafting.RecipeController;

import org.apache.commons.lang.ArrayUtils;


public class RequestRecipeShopListSet extends L2GameClientPacket
{
	private static final String	_C__B2_RequestRecipeShopListSet	= "[C] b2 RequestRecipeShopListSet";

	private static final int	BATCH_LENGTH					= 12;									// length of the one item

	private Recipe[]			_items							= null;

	@Override
	protected void readImpl()
	{
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != getByteBuffer().remaining())
		{
			return;
		}

		_items = new Recipe[count];
		for (int i = 0; i < count; i++)
		{
			int id = readD();
			long cost = readQ();
			if (cost < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new Recipe(id, cost);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_items == null)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			sendAF();
			return;
		}

		if (player.getPlayerDuel().isInDuel())
		{
			requestFailed(SystemMessageId.CANT_CRAFT_DURING_COMBAT);
			return;
		}

		// Prevents player to start a craft shop inside a nostore zone. By heX1r0
		if (player.isInsideZone(L2Zone.FLAG_NOSTORE))
		{
			requestFailed(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE);
			return;
		}

		L2ManufactureList createList = new L2ManufactureList();

		L2RecipeList[] dwarven = player.getPlayerRecipe().getDwarvenRecipeBook();
		L2RecipeList[] common = player.getPlayerRecipe().getCommonRecipeBook();

		for (Recipe i : _items)
		{
			L2RecipeList list = RecipeController.getInstance().getRecipeList(i.getRecipeId());

			if (!ArrayUtils.contains(dwarven, list) && !ArrayUtils.contains(common, list))
			{
				requestFailed(SystemMessageId.RECIPE_INCORRECT);
				return;
			}

			if (!i.addToList(createList))
			{
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
		}

		createList.setStoreName(player.getCreateList() != null ? player.getCreateList().getStoreName() : "");
		player.setCreateList(createList);

		player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_MANUFACTURE);
		player.sitDown();
		player.broadcastUserInfo();
		sendPacket(new RecipeShopMsg(player));
		player.broadcastPacket(new RecipeShopMsg(player));

		sendAF();
	}

	private class Recipe
	{
		private final int	_recipeId;
		private final long	_cost;

		public Recipe(int id, long c)
		{
			_recipeId = id;
			_cost = c;
		}

		public boolean addToList(L2ManufactureList list)
		{
			if (_cost > MAX_ADENA)
				return false;
			list.add(new L2ManufactureItem(_recipeId, _cost));
			return true;
		}

		public int getRecipeId()
		{
			return _recipeId;
		}
	}

	@Override
	public String getType()
	{
		return _C__B2_RequestRecipeShopListSet;
	}
}
