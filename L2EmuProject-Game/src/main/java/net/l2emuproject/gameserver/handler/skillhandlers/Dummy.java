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

import net.l2emuproject.gameserver.events.global.blockchecker.HandysBlockCheckerManager;
import net.l2emuproject.gameserver.events.global.blockchecker.HandysBlockCheckerManager.ArenaParticipantsHolder;
import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2BlockInstance;

public final class Dummy implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.DUMMY };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2Player))
			return;

		switch (skill.getId())
		{
			case 5852:
			case 5853:
			{
				final L2Object obj = targets[0];
				if (obj != null)
					useBlockCheckerSkill((L2Player) activeChar, skill, obj);
				break;
			}
		}
	}

	@Override
	public final L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	private final void useBlockCheckerSkill(L2Player activeChar, L2Skill skill, L2Object target)
	{
		if (!(target instanceof L2BlockInstance))
			return;

		L2BlockInstance block = (L2BlockInstance) target;

		final int arena = activeChar.getBlockCheckerArena();
		if (arena != -1)
		{
			final ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(arena);
			if (holder == null)
				return;

			final int team = holder.getPlayerTeam(activeChar);
			final int color = block.getColorEffect();
			if (team == 0 && color == 0x00)
				block.changeColor(activeChar, holder, team);
			else if (team == 1 && color == 0x53)
				block.changeColor(activeChar, holder, team);
		}
	}
}
