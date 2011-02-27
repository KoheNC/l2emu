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

import net.l2emuproject.gameserver.entity.base.PlayerState;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author mkizub
 */
public final class ConditionPlayerState extends Condition
{
	private final PlayerState _check;
	private final boolean _required;
	
	public ConditionPlayerState(PlayerState check, boolean required)
	{
		_check = check;
		_required = required;
	}
	
	@Override
	boolean testImpl(Env env)
	{
		L2Player player;
		switch (_check)
		{
			case RESTING:
				if (env.getPlayer() instanceof L2Player)
					return ((L2Player)env.getPlayer()).isSitting() == _required;
				break;
			case MOVING:
				return env.getPlayer().isMoving() == _required;
			case RUNNING:
				return (env.getPlayer().isMoving() && env.getPlayer().isRunning()) == _required;
			case WALKING:
				return (env.getPlayer().isMoving() && !env.getPlayer().isRunning()) == _required;
			case BEHIND:
				if (env.getTarget() == null)
					return false;
				return env.getPlayer().isBehind(env.getTarget()) == _required;
			case FRONT:
				if (env.getTarget() == null)
					return false;
				return env.getPlayer().isInFrontOf(env.getTarget()) == _required;
			case CHAOTIC:
				player = env.getPlayer().getActingPlayer();
				if (player != null)
					return player.getKarma() > 0 == _required;
				break;
			case OLYMPIAD:
				player = env.getPlayer().getActingPlayer();
				if (player != null)
					return player.getPlayerOlympiad().isInOlympiadMode() == _required;
				break;
			case FLYING:
				return env.getPlayer().isFlying() == _required;
		}
		
		return !_required;
	}
}
