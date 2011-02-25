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

import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author savormix
 *
 */
public final class ProtectionBlessingRestriction extends AbstractRestriction
{
	@Override
	public final boolean isProtected(L2Character activeChar, L2Character target, L2Skill skill, boolean sendMessage,
			L2Player attacker_, L2Player target_, boolean isOffensive)
	{
		if (attacker_ == null || target_ == null || attacker_ == target_)
			return false;
		
		// Keeps you safe from an attack by a chaotic character who is more than 10 levels apart from you.
		if (target_.getProtectionBlessing() && attacker_.getKarma() > 0 &&
				target_.getLevel() + 10 < attacker_.getLevel())
		{
			if (sendMessage)
				attacker_.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return true;
		}
		
		return false;
	}
}
