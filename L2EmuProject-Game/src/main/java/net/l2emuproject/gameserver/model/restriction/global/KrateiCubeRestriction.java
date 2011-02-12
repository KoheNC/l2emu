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

import net.l2emuproject.gameserver.instancemanager.games.KrateiCube;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

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
	public final void playerKilled(L2Character activeChar, L2PcInstance target, L2PcInstance killer)
	{
		if (killer instanceof L2PcInstance && target instanceof L2PcInstance && KrateiCube.isPlaying(target) && KrateiCube.isPlaying(killer))
		{
			killer.getPlayerEventData().givePoints(10);
		}
	}

	@Override
	public final boolean canRequestRevive(L2PcInstance player)
	{
		if (KrateiCube.isPlaying(player))
		{
			KrateiCube.revive(player);
			return false;
		}

		return true;
	}

	@Override
	public final boolean canBeSummoned(L2PcInstance target)
	{
		if (KrateiCube.isPlaying(target))
			return false;

		return true;
	}

	@Override
	public final boolean canTeleport(L2PcInstance player)
	{
		if (KrateiCube.isPlaying(player))
			return false;

		return true;
	}

	@Override
	public final void playerDisconnected(L2PcInstance player)
	{
		if (KrateiCube.isPlaying(player))
			KrateiCube.getInstance().removeDisconnected(player);
	}
}
