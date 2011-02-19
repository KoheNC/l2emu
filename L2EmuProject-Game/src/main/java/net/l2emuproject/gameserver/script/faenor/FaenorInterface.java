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
package net.l2emuproject.gameserver.script.faenor;

import net.l2emuproject.gameserver.Announcements;
import net.l2emuproject.gameserver.datatables.EventDroplist;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.model.npc.L2DropCategory;
import net.l2emuproject.gameserver.model.npc.L2DropData;
import net.l2emuproject.gameserver.script.DateRange;
import net.l2emuproject.gameserver.script.EngineInterface;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Luis Arias
 */
public class FaenorInterface implements EngineInterface
{
	public static FaenorInterface getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private FaenorInterface()
	{
	}
	
	/**
	 * Adds a new Quest Drop to an NPC
	 */
	@Override
	public void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states)
	{
		L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcID);
		if (npc == null)
		{
			throw new NullPointerException();
		}
		L2DropData drop = new L2DropData();
		drop.setItemId(itemID);
		drop.setMinDrop(min);
		drop.setMaxDrop(max);
		drop.setChance(chance);
		drop.setQuestID(questID);
		drop.addStates(states);
		addDrop(npc, drop, false);
	}
	
	/**
	 * Adds a new drop to an NPC. If the drop is sweep, it adds it to the NPC's Sweep category
	 * If the drop is non-sweep, it creates a new category for this drop.
	 * 
	 * @param npc
	 * @param drop
	 * @param sweep
	 */
	@Override
	public void addDrop(L2NpcTemplate npc, L2DropData drop, boolean sweep)
	{
		if (sweep)
			addDrop(npc, drop, -1);
		else
		{
			int maxCategory = -1;
			if (npc.getDropData() != null)
			{
				for (L2DropCategory cat : npc.getDropData())
				{
					if (maxCategory < cat.getCategoryType())
						maxCategory = cat.getCategoryType();
				}
			}
			maxCategory++;
			npc.addDropData(drop, maxCategory);
		}
	}
	
	/**
	 * Adds a new drop to an NPC, in the specified category. If the category does not exist,
	 * it is created.
	 * 
	 * @param npc
	 * @param drop
	 * @param category
	 */
	@Override
	public void addDrop(L2NpcTemplate npc, L2DropData drop, int category)
	{
		npc.addDropData(drop, category);
	}
	
	@Override
	public void addEventDrop(int[] items, int[] count, double chance, DateRange range)
	{
		EventDroplist.getInstance().addGlobalDrop(items, count, (int)(chance * L2DropData.MAX_CHANCE), range);
	}
	
	@Override
	public void onPlayerLogin(String[] message, DateRange validDateRange)
	{
		Announcements.getInstance().addEventAnnouncement(validDateRange, message);
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final FaenorInterface _instance = new FaenorInterface();
	}
}
