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

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.skills.Env;

/**
 * The Class ConditionPlayerPkCount.
 */
public final class ConditionPlayerPkCount extends Condition
{
	public final int	_pk;

	/**
	 * Instantiates a new condition player pk count.
	 *
	 * @param pk the pk
	 */
	public ConditionPlayerPkCount(int pk)
	{
		_pk = pk;
	}

	@Override
	public final boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
			return false;

		return ((L2PcInstance) env.player).getPkKills() <= _pk;
	}
}