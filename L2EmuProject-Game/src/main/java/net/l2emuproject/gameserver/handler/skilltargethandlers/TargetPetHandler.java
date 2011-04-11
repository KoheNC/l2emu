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
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.SkillTargetTypes;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.object.instance.L2SummonInstance;
import net.l2emuproject.gameserver.world.zone.L2Zone;

/**
 * @author Intrepid
 *
 */
public final class TargetPetHandler implements ISkillTargetHandler
{
	private static final SkillTargetTypes[]	TARGET_TYPE	=
														{
			SkillTargetTypes.TARGET_PET,
			SkillTargetTypes.TARGET_OWNER_PET,
			SkillTargetTypes.TARGET_ENEMY_PET,
			SkillTargetTypes.TARGET_SUMMON,
			SkillTargetTypes.TARGET_ENEMY_SUMMON		};

	@Override
	public L2Character[] useSkillTargetHandler(final L2Character caster, L2Character target, L2Skill skill, boolean onlyFirst)
	{
		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		final SkillTargetTypes targetType = skill.getTargetType();

		switch (targetType)
		{
			case TARGET_PET:
			{
				target = caster.getPet();
				if (target != null && !target.isDead())
					return new L2Character[]
					{ target };

				return null;
			}
			case TARGET_SUMMON:
			{
				target = caster.getPet();
				if (target != null && !target.isDead() && target instanceof L2SummonInstance)
					return new L2Character[]
					{ target };

				return null;
			}
			case TARGET_OWNER_PET:
			{
				if (caster instanceof L2Summon)
				{
					target = ((L2Summon) caster).getOwner();
					if (target != null && !target.isDead())
						return new L2Character[]
						{ target };
				}

				return null;
			}
			case TARGET_ENEMY_PET:
			{
				if (target != null && target instanceof L2Summon)
				{
					L2Summon targetPet = null;
					targetPet = (L2Summon) target;
					if (caster instanceof L2Player && caster.getPet() != targetPet && !targetPet.isDead() && targetPet.getOwner().getPvpFlag() != 0)
						return new L2Character[]
						{ target };
				}
				return null;
			}
			case TARGET_ENEMY_SUMMON:
			{
				if (target instanceof L2Summon)
				{
					final L2Summon targetSummon = (L2Summon) target;
					if (caster instanceof L2Player && caster.getPet() != targetSummon && !targetSummon.isDead()
							&& (targetSummon.getOwner().getPvpFlag() != 0 || targetSummon.getOwner().getKarma() > 0)
							|| targetSummon.getOwner().isInsideZone(L2Zone.FLAG_PVP) && caster.isInsideZone(L2Zone.FLAG_PVP))
						return new L2Character[]
						{ targetSummon };
				}
				return null;
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
