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
package net.l2emuproject.gameserver.system.restriction.global;

import net.l2emuproject.gameserver.events.custom.TvT.TvT;
import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.handler.itemhandlers.Potions;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author lord_rex
 */
public final class TvTRestriction extends AbstractRestriction
{
	private static final class SingletonHolder
	{
		private static final TvTRestriction	INSTANCE	= new TvTRestriction();
	}

	public static TvTRestriction getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	@Override
	public final void playerKilled(L2Character activeChar, L2Player target, L2Player killer)
	{
		if (TvT.isPlaying(killer) && TvT.isPlaying(target))
		{
			TvT.givePoint(killer);
			killer.getPlayerEventData().givePoints(1);
			TvT.removePoint(target);
			target.getPlayerEventData().giveDeathPoints(1);
			TvT.revive(target);
		}
	}

	@Override
	public final boolean canInviteToParty(L2Player activeChar, L2Player target)
	{
		if (TvT.isPlaying(activeChar) && TvT.isPlaying(target) && TvT.getPlayerTeamId(activeChar) != TvT.getPlayerTeamId(target))
		{
			activeChar.sendMessage("You can't invite your enemy to party in TvT!");
			return false;
		}

		return true;
	}

	@Override
	public final boolean canTeleport(L2Player activeChar)
	{
		if (TvT.isPlaying(activeChar))
		{
			activeChar.sendMessage("You can't teleport in TvT.");
			return false;
		}

		return true;
	}

	@Override
	public final boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item, L2Player player)
	{
		if (player != null && TvT.isPlaying(player))
		{
			if (clazz == Potions.class)
			{
				player.sendMessage("You can't use potions in TvT!");
				return false;
			}
		}

		return true;
	}

	@Override
	public final boolean canBeSummoned(L2Player target)
	{
		if (TvT.isPlaying(target))
			return false;

		return true;
	}

	@Override
	public final boolean canRequestRevive(L2Player activeChar)
	{
		if (TvT.isPlaying(activeChar))
		{
			return false;
		}

		return true;
	}

	@Override
	public final boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage, L2Player attacker_,
			L2Player target_, boolean isOffensive)
	{
		if (TvT.isPlaying(attacker_) && TvT.isPlaying(target_) && TvT.getPlayerTeamId(attacker_) == TvT.getPlayerTeamId(target_) && isOffensive)
		{
			attacker_.sendMessage("You can't kill your team mate in TvT!");
			return true;
		}

		return false;
	}

	@Override
	public final void playerDisconnected(L2Player player)
	{
		if (TvT.isPlaying(player))
			TvT.getInstance().removeDisconnected(player);
	}
}
