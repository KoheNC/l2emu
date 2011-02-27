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
package net.l2emuproject.gameserver.skills.conditions;

import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.templates.item.L2Weapon;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author nBd
 */
class ConditionChangeWeapon extends Condition
{
	private final boolean _required;
	
	/**
	 * 
	 */
	public ConditionChangeWeapon(boolean required)
	{
		_required = required;
	}
	
	/**
	 * @see net.l2emuproject.gameserver.skills.conditions.Condition#testImpl(net.l2emuproject.gameserver.skills.Env)
	 */
	@Override
	boolean testImpl(Env env)
	{
		if (!(env.getPlayer() instanceof L2Player))
			return false;
		
		if (_required)
		{
			L2Weapon weaponItem = env.getPlayer().getActiveWeaponItem();
			
			if (weaponItem == null)
				return false;
			
			if (weaponItem.getChangeWeaponId() == 0)
				return false;
		}
		return true;
	}
	
}
