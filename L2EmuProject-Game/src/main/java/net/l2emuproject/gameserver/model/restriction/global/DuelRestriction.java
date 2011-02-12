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

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author NB4L1
 */
public final class DuelRestriction extends AbstractRestriction
{
	@Override
	public final boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		if (activeChar.getPlayerDuel().isInDuel())
		{
			activeChar.sendMessage("You are participating in a duel!");
			return true;
		}
		
		return false;
	}
	
	@Override
	public final boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		if (activeChar.getPlayerDuel().isInDuel() || target.getPlayerDuel().isInDuel())
			return false;
		
		return true;
	}
	
	@Override
	public final boolean canTeleport(L2PcInstance activeChar)
	{
		// Check to see if player is in a duel
		if (activeChar.getPlayerDuel().isInDuel())
		{
			activeChar.sendMessage("You can't teleport during a duel.");
			return false;
		}
		
		return true;
	}
}
