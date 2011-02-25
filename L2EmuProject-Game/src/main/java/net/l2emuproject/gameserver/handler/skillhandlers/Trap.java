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
import net.l2emuproject.gameserver.model.actor.instance.L2TrapInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

public class Trap implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.DETECT_TRAP, L2SkillType.REMOVE_TRAP };

	/**
	 * 
	 * @see net.l2emuproject.gameserver.handler.ISkillHandler#useSkill(net.l2emuproject.gameserver.world.object.L2Character, net.l2emuproject.gameserver.skills.L2Skill, net.l2emuproject.gameserver.world.object.L2Character...)
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (skill == null)
			return;
		
		switch (skill.getSkillType())
		{
			case DETECT_TRAP:
			{
				for (L2Character element : targets)
				{
					if (!(element instanceof L2TrapInstance))
						continue;
	
					L2TrapInstance target = (L2TrapInstance) element;
	
					if (target.isAlikeDead())
						continue;
	
					if (target.getLevel() <= skill.getPower())
					{
						target.setDetected();
						if (activeChar instanceof L2Player)
							((L2Player)activeChar).sendMessage("A Trap has been detected!");
					}
				}
				break;
			}
			case REMOVE_TRAP:
			{
				for (L2Character element : targets)
				{
					if (!(element instanceof L2TrapInstance))
						continue;
	
					L2TrapInstance target = (L2TrapInstance) element;
	
					if (!target.isDetected())
						continue;
	
					if (target.getLevel() > skill.getPower())
						continue;
	
					L2Player trapOwner = null;
					trapOwner = target.getOwner();
	
					target.unSummon(trapOwner);
					if (activeChar instanceof L2Player)
						activeChar.sendPacket(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED);
				}
			}
		}
	}

	/**
	 * 
	 * @see net.l2emuproject.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
