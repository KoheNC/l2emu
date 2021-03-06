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

public class L2BossZone extends L2Zone
{
	@Override
	protected void register()
	{

	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(FLAG_NOSUMMON, true);
		
		super.onEnter(character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		switch (getBoss())
		{
			case SUNLIGHTROOM:
			{
				character.setInsideZone(FLAG_SUNLIGHTROOM, false);
				break;
			}
		}
		
		character.setInsideZone(FLAG_NOSUMMON, false);
		
		super.onExit(character);
	}
}
