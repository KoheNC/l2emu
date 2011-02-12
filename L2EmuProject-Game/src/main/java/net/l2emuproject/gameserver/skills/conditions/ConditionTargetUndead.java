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

import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2SummonInstance;
import net.l2emuproject.gameserver.skills.Env;

class ConditionTargetUndead extends Condition
{
	final boolean _isUndead;
	
	public ConditionTargetUndead(boolean isUndead)
	{
		_isUndead = isUndead;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		L2Character target = (L2Character)env.player.getTarget();
		
		if (target == null)
			return false;
		if (target instanceof L2MonsterInstance)
			return target.isUndead() == _isUndead;
		if (target instanceof L2SummonInstance)
			return target.isUndead() == _isUndead;
		
		return false;
	}
}
