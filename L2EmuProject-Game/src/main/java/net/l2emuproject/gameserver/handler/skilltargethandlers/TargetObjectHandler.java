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
package net.l2emuproject.gameserver.handler.skilltargethandlers;

import net.l2emuproject.gameserver.handler.ISkillTargetHandler;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.SkillTargetTypes;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2ArtefactInstance;
import net.l2emuproject.gameserver.world.object.instance.L2ChestInstance;
import net.l2emuproject.gameserver.world.object.instance.L2DoorInstance;
import net.l2emuproject.util.ArrayBunch;

/**
 * @author Intrepid
 *
 */
public final class TargetObjectHandler implements ISkillTargetHandler
{
	private static final SkillTargetTypes[]	TARGET_TYPE	=
														{
			SkillTargetTypes.TARGET_UNLOCKABLE,
			SkillTargetTypes.TARGET_FLAGPOLE,
			SkillTargetTypes.TARGET_GATE,
			SkillTargetTypes.TARGET_HOLY,				};

	@Override
	public L2Character[] useSkillTargetHandler(final L2Character caster, L2Character target, L2Skill skill, boolean onlyFirst)
	{
		final ArrayBunch<L2Character> targetList = new ArrayBunch<L2Character>();

		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		final SkillTargetTypes targetType = skill.getTargetType();

		switch (targetType)
		{
			case TARGET_HOLY:
			{
				if (caster instanceof L2Player)
					if (target instanceof L2ArtefactInstance)
						return new L2Character[]
						{ target };

				return null;
			}
			case TARGET_FLAGPOLE:
			{
				return new L2Character[]
				{ caster };
			}
			case TARGET_UNLOCKABLE:
			{
				if (!(target instanceof L2DoorInstance) && !(target instanceof L2ChestInstance))
					// caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;

				if (!onlyFirst)
				{
					targetList.add(target);
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}

				return new L2Character[]
				{ target };

			}
			case TARGET_GATE:
			{
				// Check for null target or any other invalid target
				if (target == null || target.isDead() || !(target instanceof L2DoorInstance))
				{
					caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}
				// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
				return new L2Character[]
				{ target };
			}
		}

		return null;
	}

	@Override
	public SkillTargetTypes[] getSkillTargetTypes()
	{
		return TARGET_TYPE;
	}
}
