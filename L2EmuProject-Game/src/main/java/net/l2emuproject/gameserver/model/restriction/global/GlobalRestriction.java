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

/**
 * @author NB4L1
 */
public interface GlobalRestriction
{
	public boolean isRestricted(L2Player activeChar, Class<? extends GlobalRestriction> callingRestriction);

	public boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage, L2Player attacker_, L2Player target_,
			boolean isOffensive);

	public boolean canInviteToParty(L2Player activeChar, L2Player target);
	
	public boolean canRequestRevive(L2Player activeChar);

	public boolean canTeleport(L2Player activeChar);

	public boolean canUseItemHandler(Class<? extends IItemHandler> clazz, int itemId, L2Playable activeChar, L2ItemInstance item, L2Player player);

	public boolean canPickUp(L2Player activeChar, L2ItemInstance item, L2PetInstance pet);

	public void playerLoggedIn(L2Player activeChar);

	public void playerDisconnected(L2Player activeChar);

	public void playerKilled(L2Character activeChar, L2Player target, L2Player killer);

	public boolean onBypassFeedback(L2Npc npc, L2Player activeChar, String command);

	public boolean onAction(L2Character target, L2Character activeChar);

	public boolean canUseVoicedCommand(String command, L2Player activeChar, String target);

	public boolean canUseSkill(L2Player player, L2Skill skill);

	public boolean canBeSummoned(L2Player target);
}
