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
package net.l2emuproject.gameserver.model.restriction.global;

import net.l2emuproject.gameserver.manager.games.KrateiCube;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author lord_rex
 */
public final class KrateiCubeRestriction extends AbstractRestriction
{
	private static final class SingletonHolder
	{
		private static final KrateiCubeRestriction	INSTANCE	= new KrateiCubeRestriction();
	}

	public static KrateiCubeRestriction getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	@Override
	public final void playerKilled(L2Character activeChar, L2Player target, L2Player killer)
	{
		if (killer instanceof L2Player && target instanceof L2Player && KrateiCube.isPlaying(target) && KrateiCube.isPlaying(killer))
		{
			killer.getPlayerEventData().givePoints(10);
		}
	}

	@Override
	public final boolean canRequestRevive(L2Player player)
	{
		if (KrateiCube.isPlaying(player))
		{
			KrateiCube.revive(player);
			return false;
		}

		return true;
	}

	@Override
	public final boolean canBeSummoned(L2Player target)
	{
		if (KrateiCube.isPlaying(target))
			return false;

		return true;
	}

	@Override
	public final boolean canTeleport(L2Player player)
	{
		if (KrateiCube.isPlaying(player))
			return false;

		return true;
	}

	@Override
	public final void playerDisconnected(L2Player player)
	{
		if (KrateiCube.isPlaying(player))
			KrateiCube.getInstance().removeDisconnected(player);
	}
}
