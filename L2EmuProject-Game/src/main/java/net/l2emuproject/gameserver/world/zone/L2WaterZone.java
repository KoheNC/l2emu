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
package net.l2emuproject.gameserver.world.zone;

import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

public class L2WaterZone extends L2Zone
{
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(FLAG_WATER, true);
		
		if (character instanceof L2Player)
		{
			L2Player player = (L2Player)character;
			if (player.getPlayerTransformation().isTransformed() && !player.isCursedWeaponEquipped())
				character.stopTransformation(true);
		}
		
		super.onEnter(character);
		
		character.broadcastFullInfo();
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(FLAG_WATER, false);
		
		super.onExit(character);
		
		character.broadcastFullInfo();
	}
	
	@Override
	protected boolean checkDynamicConditions(L2Character character)
	{
		if (character instanceof L2Player && ((L2Player)character).isInBoat())
			return false;
		
		return super.checkDynamicConditions(character);
	}
}
