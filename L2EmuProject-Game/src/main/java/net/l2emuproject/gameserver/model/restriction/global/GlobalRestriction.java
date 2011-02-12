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

/**
 * @author NB4L1
 */
public interface GlobalRestriction
{
	public boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction);

	public boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage, L2PcInstance attacker_, L2PcInstance target_,
			boolean isOffensive);

	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target);
	
	public boolean canRequestRevive(L2PcInstance activeChar);

	public boolean canTeleport(L2PcInstance activeChar);

	public boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item, L2PcInstance player);

	public boolean canPickUp(L2PcInstance activeChar, L2ItemInstance item, L2PetInstance pet);

	public void playerLoggedIn(L2PcInstance activeChar);

	public void playerDisconnected(L2PcInstance activeChar);

	public void playerKilled(L2Character activeChar, L2PcInstance target, L2PcInstance killer);

	public boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command);

	public boolean onAction(L2Character target, L2Character activeChar);

	public boolean canUseVoicedCommand(String command, L2PcInstance activeChar, String target);

	public boolean canUseSkill(L2PcInstance player, L2Skill skill);

	public boolean canBeSummoned(L2PcInstance target);
}
