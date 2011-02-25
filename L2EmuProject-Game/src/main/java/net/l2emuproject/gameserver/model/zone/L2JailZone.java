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
package net.l2emuproject.gameserver.model.zone;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.GmListTable;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.world.Location;

public class L2JailZone extends L2Zone
{
	public static final Location JAIL_LOCATION = new Location(-114356, -249645, -2984);
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(FLAG_JAIL, true);
			character.setInsideZone(FLAG_NOSUMMON, true);
			
			if (Config.JAIL_IS_PVP)
				character.setInsideZone(FLAG_PVP, true);
		}
		
		super.onEnter(character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(FLAG_JAIL, false);
			character.setInsideZone(FLAG_NOSUMMON, false);
			
			if (Config.JAIL_IS_PVP)
				character.setInsideZone(FLAG_PVP, false);
		}
		
		super.onExit(character);
		
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)character;
			// This is for when a player tries to bug his way out of jail
			if (player.isInJail())
			{
				player.teleToLocation(JAIL_LOCATION, false);
				player.sendMessage("You dare try and escape from jail before your time is up? Think again!");
				
				String msg = "Player: " + player.getName() + " tried to escape from jail.";
				_log.warn(msg);
				GmListTable.broadcastMessageToGMs(msg);
			}
		}
	}
}
