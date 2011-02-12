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
import net.l2emuproject.gameserver.model.L2ItemInstance;
import net.l2emuproject.gameserver.model.L2Skill;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.L2Playable;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PetInstance;

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
	public boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage, L2PcInstance attacker_, L2PcInstance target_,
			boolean isOffensive)
	{
		throw new AbstractMethodError();
	}
	
	@Override
	@DisabledRestriction
	public boolean canRequestRevive(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canTeleport(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item, L2PcInstance player)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canPickUp(L2PcInstance activeChar, L2ItemInstance item, L2PetInstance pet)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public void playerDisconnected(L2PcInstance activeChar)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public void playerKilled(L2Character activeChar, L2PcInstance target, L2PcInstance killer)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command)
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
	public boolean canUseVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canUseSkill(L2PcInstance player, L2Skill skill)
	{
		throw new AbstractMethodError();
	}

	@Override
	@DisabledRestriction
	public boolean canBeSummoned(L2PcInstance target)
	{
		throw new AbstractMethodError();
	}
}
