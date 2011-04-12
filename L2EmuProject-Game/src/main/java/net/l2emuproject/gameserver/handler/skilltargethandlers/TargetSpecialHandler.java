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
import net.l2emuproject.gameserver.services.couple.Couple;
import net.l2emuproject.gameserver.services.couple.CoupleService;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.SkillTargetTypes;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.ArrayBunch;

/**
 * @author Intrepid
 */
public final class TargetSpecialHandler implements ISkillTargetHandler
{
	private static final SkillTargetTypes[]	TARGET_TYPE	=
														{
			SkillTargetTypes.TARGET_COUPLE,
			SkillTargetTypes.TARGET_GROUND,
			SkillTargetTypes.TARGET_INITIATOR,
			SkillTargetTypes.TARGET_KNOWNLIST,			};

	@Override
	public L2Character[] useSkillTargetHandler(final L2Character caster, L2Character target, L2Skill skill, boolean onlyFirst)
	{
		final ArrayBunch<L2Character> targetList = new ArrayBunch<L2Character>();

		try
		{
			// Get the target type of the skill
			// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
			final SkillTargetTypes targetType = skill.getTargetType();

			switch (targetType)
			{
				case TARGET_GROUND:
				{
					return new L2Character[]
					{ caster };
				}
				case TARGET_COUPLE:
				{
					if (target != null && target instanceof L2Player)
					{
						final int _chaid = caster.getObjectId();
						final int targetId = target.getObjectId();
						for (final Couple cl : CoupleService.getInstance().getCouples())
							if (cl.getPlayer1Id() == _chaid && cl.getPlayer2Id() == targetId || cl.getPlayer2Id() == _chaid && cl.getPlayer1Id() == targetId)
								return new L2Character[]
								{ target };
					}

					return null;
				}
				case TARGET_KNOWNLIST:
				{
					if (target != null && target.getKnownList() != null)
						for (final L2Object obj : target.getKnownList().getKnownObjects().values())
							if (obj instanceof L2Attackable || obj instanceof L2Playable)
								return new L2Character[]
								{ (L2Character) obj };

					if (targetList.size() == 0)
						return null;
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_INITIATOR:
					if (target != null)
						return new L2Character[]
						{ target };
					else
						return null;
			}
		}
		finally
		{
			targetList.clear();
		}

		return null;
	}

	@Override
	public SkillTargetTypes[] getSkillTargetTypes()
	{
		return TARGET_TYPE;
	}
}
