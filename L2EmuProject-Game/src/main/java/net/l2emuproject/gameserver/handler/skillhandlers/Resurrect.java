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

import javolution.util.FastList;
import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2PetInstance;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.L2Skill.SkillTargetType;
import net.l2emuproject.gameserver.skills.formulas.Formulas;
import net.l2emuproject.gameserver.system.taskmanager.DecayTaskManager;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;


/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.5.2.4 $ $Date: 2005/04/03 15:55:03 $
 */

public class Resurrect implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.RESURRECT };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		L2Player player = null;
		if (activeChar instanceof L2Player)
			player = (L2Player) activeChar;

		L2Player targetPlayer;
		FastList<L2Character> targetToRes = new FastList<L2Character>();

		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			if (target.isEradicated())
				continue;
			
			if (target instanceof L2Player)
			{
				targetPlayer = (L2Player) target;

				// Check for same party or for same clan, if target is for clan.
				if (skill.getTargetType() == SkillTargetType.TARGET_CORPSE_CLAN)
				{
					if (player != null && player.getClanId() != targetPlayer.getClanId())
						continue;
				}
			}
			
			if (target.isVisible())
				targetToRes.add(target);
		}

		for (L2Character cha : targetToRes)
		{
			if (activeChar instanceof L2Player)
			{
				if (cha instanceof L2Player)
				{
					((L2Player) cha).reviveRequest((L2Player) activeChar, skill);
				}
				else if (cha instanceof L2PetInstance)
				{
					if (((L2PetInstance) cha).getOwner() == activeChar)
						cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill, activeChar));
					else
						((L2PetInstance) cha).getOwner().revivePetRequest((L2Player) activeChar, skill);
				}
				else
					cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill, activeChar));
			}
			else
			{
				DecayTaskManager.getInstance().cancelDecayTask(cha);
				cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill, activeChar));
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
