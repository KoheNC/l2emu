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
package net.l2emuproject.gameserver.handler;

import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.handler.itemhandlers.BeastSoulShot;
import net.l2emuproject.gameserver.handler.itemhandlers.BeastSpice;
import net.l2emuproject.gameserver.handler.itemhandlers.BeastSpiritShot;
import net.l2emuproject.gameserver.handler.itemhandlers.BlessedSpiritShot;
import net.l2emuproject.gameserver.handler.itemhandlers.Book;
import net.l2emuproject.gameserver.handler.itemhandlers.ColorName;
import net.l2emuproject.gameserver.handler.itemhandlers.Disguise;
import net.l2emuproject.gameserver.handler.itemhandlers.DoorKey;
import net.l2emuproject.gameserver.handler.itemhandlers.Elixir;
import net.l2emuproject.gameserver.handler.itemhandlers.EnchantAttribute;
import net.l2emuproject.gameserver.handler.itemhandlers.EnchantScrolls;
import net.l2emuproject.gameserver.handler.itemhandlers.EnergyStarStone;
import net.l2emuproject.gameserver.handler.itemhandlers.EventItem;
import net.l2emuproject.gameserver.handler.itemhandlers.ExtractableItems;
import net.l2emuproject.gameserver.handler.itemhandlers.FishShots;
import net.l2emuproject.gameserver.handler.itemhandlers.Harvester;
import net.l2emuproject.gameserver.handler.itemhandlers.ItemSkills;
import net.l2emuproject.gameserver.handler.itemhandlers.ItemSkillsTemplate;
import net.l2emuproject.gameserver.handler.itemhandlers.Maps;
import net.l2emuproject.gameserver.handler.itemhandlers.MercTicket;
import net.l2emuproject.gameserver.handler.itemhandlers.PetFood;
import net.l2emuproject.gameserver.handler.itemhandlers.Potions;
import net.l2emuproject.gameserver.handler.itemhandlers.Recipes;
import net.l2emuproject.gameserver.handler.itemhandlers.RollingDice;
import net.l2emuproject.gameserver.handler.itemhandlers.ScrollOfResurrection;
import net.l2emuproject.gameserver.handler.itemhandlers.Seed;
import net.l2emuproject.gameserver.handler.itemhandlers.SevenSignsRecord;
import net.l2emuproject.gameserver.handler.itemhandlers.SoulCrystals;
import net.l2emuproject.gameserver.handler.itemhandlers.SoulShots;
import net.l2emuproject.gameserver.handler.itemhandlers.SpecialXMas;
import net.l2emuproject.gameserver.handler.itemhandlers.SpiritShot;
import net.l2emuproject.gameserver.handler.itemhandlers.SummonItems;
import net.l2emuproject.gameserver.handler.itemhandlers.TeleportBookmark;
import net.l2emuproject.gameserver.handler.itemhandlers.WondrousCubic;
import net.l2emuproject.gameserver.handler.itemhandlers.WrappedPack;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.model.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.templates.item.L2EtcItem;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.util.HandlerRegistry;
import net.l2emuproject.util.NumberHandlerRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class ItemHandler
{
	private static final Log _log = LogFactory.getLog(ItemHandler.class);
	
	private static final class SingletonHolder
	{
		private static final ItemHandler INSTANCE = new ItemHandler();
	}
	
	public static ItemHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private final NumberHandlerRegistry<IItemHandler> _byItemId = new NumberHandlerRegistry<IItemHandler>() {
		@Override
		protected String getName()
		{
			return "ItemHandlerByItemId";
		}
	};
	private final HandlerRegistry<String, IItemHandler> _byHandlerName = new HandlerRegistry<String, IItemHandler>() {
		@Override
		protected String getName()
		{
			return "ItemHandlerByHandlerName";
		}
	};
	
	private ItemHandler()
	{
		registerItemHandler(new BeastSoulShot());
		registerItemHandler(new BeastSpice());
		registerItemHandler(new BeastSpiritShot());
		registerItemHandler(new BlessedSpiritShot());
		registerItemHandler(new Book());
		registerItemHandler(new ColorName());
		registerItemHandler(new DoorKey());
		registerItemHandler(new Elixir());
		registerItemHandler(new EventItem());
		registerItemHandler(new Disguise());
		registerItemHandler(new EnchantAttribute());
		registerItemHandler(new EnchantScrolls());
		registerItemHandler(new EnergyStarStone());
		registerItemHandler(new ExtractableItems());
		registerItemHandler(new FishShots());
		registerItemHandler(new Harvester());
		registerItemHandler(new ItemSkills());
		registerItemHandler(new ItemSkillsTemplate());
		registerItemHandler(new Maps());
		registerItemHandler(new MercTicket());
		registerItemHandler(new PetFood());
		registerItemHandler(new Potions());
		registerItemHandler(new Recipes());
		registerItemHandler(new RollingDice());
		registerItemHandler(new ScrollOfResurrection());
		registerItemHandler(new Seed());
		registerItemHandler(new SevenSignsRecord());
		registerItemHandler(new SoulCrystals());
		registerItemHandler(new SoulShots());
		registerItemHandler(new SpecialXMas());
		registerItemHandler(new SpiritShot());
		registerItemHandler(new SummonItems());
		registerItemHandler(new TeleportBookmark());
		registerItemHandler(new WrappedPack());
		// L2EMU_ADD
		registerItemHandler(new WondrousCubic());
		// L2EMU_ADD
		
		_log.info("ItemHandler: Loaded " + _byHandlerName.size() + " handlers by handlerName.");
		
		for (L2Item item : ItemTable.getInstance().getAllTemplates())
		{
			if (!(item instanceof L2EtcItem))
				continue;
			
			final String handlerName = ((L2EtcItem)item).getHandlerName();
			
			if (handlerName.isEmpty())
				continue;
			
			final IItemHandler handlerByHandlerName = _byHandlerName.get(handlerName);
			
			if (handlerByHandlerName == null)
			{
				_log.warn("ItemHandler: Missing handler for '" + handlerName + "'!");
				continue;
			}
			
			_byItemId.register(item.getItemId(), handlerByHandlerName);
		}
		
		_log.info("ItemHandler: Loaded " + _byItemId.size() + " handlers by itemId.");
	}
	
	public void registerItemHandler(IItemHandler handler)
	{
		_byHandlerName.register(handler.getClass().getSimpleName().intern(), handler);
	}
	
	public boolean hasItemHandler(int itemId, L2ItemInstance item)
	{
		return get(itemId, item) != null;
	}
	
	public boolean useItem(int itemId, L2Playable playable, L2ItemInstance item)
	{
		return useItem(itemId, playable, item, true);
	}
	
	public boolean useItem(int itemId, L2Playable playable, L2ItemInstance item, boolean warn)
	{
		final IItemHandler handler = get(itemId, item);
		
		if (handler == null)
		{
			if (warn)
				_log.warn("No item handler registered for item ID " + itemId + ".");
			return false;
		}
		
		if (!GlobalRestrictions.canUseItemHandler(handler.getClass(), itemId, playable, item))
			return true;
		
		handler.useItem(playable, item);
		return true;
	}
	
	private IItemHandler get(int itemId, L2ItemInstance item)
	{
		return _byItemId.get(itemId);
	}
}
