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
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.instance.L2PetInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author NB4L1
 */
public abstract class AbstractRestriction implements GlobalRestriction
{
	static final Log	_log	= LogFactory.getLog(AbstractRestriction.class);

	public void activate()
	{
		GlobalRestrictions.activate(this);
	}

	public void deactivate()
	{
		GlobalRestrictions.deactivate(this);
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}

	/**
	 * To avoid accidentally multiple times activated restrictions.
	 */
	@Override
	public boolean equals(Object obj)
	{
		return getClass().equals(obj.getClass());
	}

	@Override
	@DisabledRestriction
	public boolean isRestricted(L2Player activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canInviteToParty(L2Player activeChar, L2Player target)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage, L2Player attacker_, L2Player target_,
			boolean isOffensive)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canRequestRevive(L2Player activeChar)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canTeleport(L2Player activeChar)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item, L2Player player)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canPickUp(L2Player activeChar, L2ItemInstance item, L2PetInstance pet)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public void playerLoggedIn(L2Player activeChar)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public void playerDisconnected(L2Player activeChar)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public void playerKilled(L2Character activeChar, L2Player target, L2Player killer)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean onBypassFeedback(L2Npc npc, L2Player activeChar, String command)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean onAction(L2Character target, L2Character activeChar)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canUseVoicedCommand(String command, L2Player activeChar, String target)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canUseSkill(L2Player player, L2Skill skill)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canBeSummoned(L2Player target)
	{
		throw new AbstractMethodError();
	}
}
