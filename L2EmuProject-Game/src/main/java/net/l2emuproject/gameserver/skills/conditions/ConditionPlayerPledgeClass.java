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
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author MrPoke
 */
final class ConditionPlayerPledgeClass extends Condition
{
	private final int _pledgeClass;
	
	public ConditionPlayerPledgeClass(int pledgeClass)
	{
		_pledgeClass = pledgeClass;
	}
	
	/**
	 * @see net.l2emuproject.gameserver.skills.conditions.Condition#testImpl(net.l2emuproject.gameserver.skills.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.getPlayer() instanceof L2Player))
			return false;
		
		if (((L2Player)env.getPlayer()).getClan() == null)
			return false;
		
		if (_pledgeClass == -1)
			return ((L2Player)env.getPlayer()).isClanLeader();
		
		return ((L2Player)env.getPlayer()).getPledgeClass() >= _pledgeClass;
	}
}
