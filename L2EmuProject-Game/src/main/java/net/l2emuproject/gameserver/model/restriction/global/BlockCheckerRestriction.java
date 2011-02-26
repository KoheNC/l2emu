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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.events.global.blockchecker.HandysBlockCheckerManager;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author lord_rex
 */
public final class BlockCheckerRestriction extends AbstractRestriction
{
	private static final class SingletonHolder
	{
		private static final BlockCheckerRestriction	INSTANCE	= new BlockCheckerRestriction();
	}

	public static BlockCheckerRestriction getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	@Override
	public final boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage, L2Player attacker_,
			L2Player target_, boolean isOffensive)
	{
		if (activeChar == null || target == null || attacker_ == null || target_ == null)
			return false;
		
		if (target_.getBlockCheckerArena() != -1)
			return true;

		return false;
	}

	@Override
	public final void playerDisconnected(L2Player player)
	{
		if (Config.ENABLE_BLOCK_CHECKER_EVENT && player.getBlockCheckerArena() != -1)
			HandysBlockCheckerManager.getInstance().onDisconnect(player);
	}

	@Override
	public final boolean canBeSummoned(L2Player target)
	{
		if (target.getBlockCheckerArena() != -1)
			return false;

		return true;
	}
}
