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

import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.handler.itemhandlers.Potions;
import net.l2emuproject.gameserver.handler.itemhandlers.SummonItems;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.model.olympiad.Olympiad;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Playable;

/**
 * @author NB4L1
 */
public final class OlympiadRestriction extends AbstractRestriction
{
	@Override
	public final boolean isRestricted(L2Player activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		// TODO: merge different checking methods to one
		if (activeChar.getPlayerOlympiad().isInOlympiadMode() || Olympiad.getInstance().isRegistered(activeChar) || activeChar.getPlayerOlympiad().getOlympiadGameId() != -1)
		{
			activeChar.sendMessage("You are registered on Grand Olympiad Games!");
			return true;
		}

		return false;
	}

	@Override
	public final boolean canInviteToParty(L2Player activeChar, L2Player target)
	{
		if (activeChar.getPlayerOlympiad().isInOlympiadMode() || target.getPlayerOlympiad().isInOlympiadMode())
			return false;

		return true;
	}

	@Override
	public final boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item, L2Player player)
	{
		if (clazz == SummonItems.class)
		{
			if (player != null && player.getPlayerOlympiad().isInOlympiadMode())
			{
				player.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
				return false;
			}
		}
		else if (clazz == Potions.class)
		{
			if (player != null && player.getPlayerOlympiad().isInOlympiadMode())
			{
				player.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
				return false;
			}
		}

		return true;
	}

	@Override
	public final boolean onAction(L2Character target, L2Character activeChar)
	{
		final L2Player attacker_ = L2Object.getActingPlayer(activeChar);
		final L2Player target_ = L2Object.getActingPlayer(target);

		if (attacker_ == null || target_ == null || attacker_ == target_ || attacker_.isGM())
			return true;

		if (attacker_.getPlayerOlympiad().isInOlympiadMode() != target_.getPlayerOlympiad().isInOlympiadMode())
			return false;

		if (attacker_.getPlayerOlympiad().isInOlympiadMode() && target_.getPlayerOlympiad().isInOlympiadMode())
		{
			if (attacker_.getPlayerOlympiad().getOlympiadGameId() != target_.getPlayerOlympiad().getOlympiadGameId())
				return false;

			if (!attacker_.getPlayerOlympiad().isOlympiadStart() || !target_.getPlayerOlympiad().isOlympiadStart())
				return false;
		}

		return true;
	}

	@Override
	public final void playerDisconnected(L2Player activeChar)
	{
		if (activeChar.getPlayerOlympiad().isInOlympiadMode())
			Olympiad.getInstance().unRegisterNoble(activeChar);

		// handle removal from olympiad game
		if (Olympiad.getInstance().isRegistered(activeChar) || activeChar.getPlayerOlympiad().getOlympiadGameId() != -1)
			Olympiad.getInstance().removeDisconnectedCompetitor(activeChar);
	}
}
