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
package net.l2emuproject.gameserver.handler.skillhandlers;

import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.zone.L2Zone;

/**
 * @author Ahmed
 */
public class TransformDispel implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.TRANSFORMDISPEL };
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2Player))
			return;
		
		final L2Player pc = (L2Player)activeChar;
		
		if (pc.isAlikeDead() || pc.isCursedWeaponEquipped())
			return;
		
		if (pc.getPlayerTransformation().getTransformation() != null)
		{
			if (pc.isFlyingMounted() && !pc.isInsideZone(L2Zone.FLAG_LANDING))
				pc.sendPacket(SystemMessageId.BOARD_OR_CANCEL_NOT_POSSIBLE_HERE);
			else
				pc.stopTransformation(true);
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
