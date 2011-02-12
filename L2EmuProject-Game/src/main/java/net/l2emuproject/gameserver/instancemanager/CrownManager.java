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
package net.l2emuproject.gameserver.instancemanager;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.CrownTable;
import net.l2emuproject.gameserver.model.L2Clan;
import net.l2emuproject.gameserver.model.L2ClanMember;
import net.l2emuproject.gameserver.model.L2ItemInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.entity.Castle;
import net.l2emuproject.gameserver.model.itemcontainer.Inventory;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author evill33t, NB4L1
 */
public final class CrownManager
{
	private static final Log _log = LogFactory.getLog(CrownManager.class);
	
	static
	{
		_log.info("CrownManager: Initialized.");
	}
	
	public static void checkCrowns(L2Clan clan)
	{
		if (clan == null)
			return;
		
		for (L2ClanMember member : clan.getMembers())
			if (member != null && member.isOnline())
				checkCrowns(member.getPlayerInstance());
	}
	
	public static void checkCrowns(L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;
		
		int crownId = -1;
		boolean isLeader = false;
		
		final L2Clan clan = activeChar.getClan();
		
		if (clan != null)
		{
			final Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			
			if (castle != null)
				crownId = CrownTable.getCrownId(castle.getCastleId());
			
			if (clan.getLeaderId() == activeChar.getObjectId())
				isLeader = true;
		}
		
		boolean alreadyFoundCirclet = false;
		boolean alreadyFoundCrown = false;
		
		for (L2ItemInstance item : activeChar.getInventory().getItems())
		{
			if (ArrayUtils.contains(CrownTable.getCrownIds(), item.getItemId()))
			{
				if (crownId > 0)
				{
					if (item.getItemId() == crownId)
					{
						if (!alreadyFoundCirclet)
						{
							alreadyFoundCirclet = true;
							continue;
						}
					}
					else if (item.getItemId() == 6841 && isLeader)
					{
						if (!alreadyFoundCrown)
						{
							alreadyFoundCrown = true;
							continue;
						}
					}
				}
				
				//WRONG! The crown is not sellable/tradeable/dropable
				//And the circlets are sellable!!!, but not tradeable or dropable
				//Unequip is what happens
				if (item.getItemId() == 6841 || Config.ALT_REMOVE_CASTLE_CIRCLETS)
					activeChar.destroyItem("Removing Crown", item, activeChar, true);
				else if (item.isEquipped())
					activeChar.getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_HAIR2);
				
				// No need to update every item in the inventory
				//activeChar.getInventory().updateDatabase();
			}
		}
	}
}
