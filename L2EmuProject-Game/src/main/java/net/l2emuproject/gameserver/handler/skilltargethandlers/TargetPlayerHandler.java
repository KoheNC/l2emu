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
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.SkillTargetTypes;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Intrepid
 */
public final class TargetPlayerHandler implements ISkillTargetHandler
{
	private static final SkillTargetTypes[]	TARGET_TYPE	=
														{
			SkillTargetTypes.TARGET_SELF,
			SkillTargetTypes.TARGET_ONE,
			SkillTargetTypes.TARGET_MULTIFACE,
			SkillTargetTypes.TARGET_MOB				};

	@Override
	public L2Character[] useSkillTargetHandler(final L2Character caster, L2Character target, L2Skill skill, boolean onlyFirst)
	{
		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		final SkillTargetTypes targetType = skill.getTargetType();

		// Get the type of the skill
		// (ex : PDAM, MDAM, DOT, BLEED, POISON, HEAL, HOT, MANAHEAL, MANARECHARGE, AGGDAMAGE, BUFF, DEBUFF, STUN, ROOT, RESURRECT, PASSIVE...)
		final L2SkillType skillType = skill.getSkillType();

		switch (targetType)
		{
			// The skill can only be used on the L2Character targeted, or on the caster itself
			case TARGET_ONE:
			{
				// automatically selects caster if no target is selected (only positive skills)
				if (skill.isPositive() && target == null)
					target = caster;

				boolean canTargetSelf = false;
				switch (skillType)
				{
					case BUFF:
					case HEAL:
					case HOT:
					case HEAL_PERCENT:
					case MANARECHARGE:
					case MANAHEAL:
					case RECOVER:
					case NEGATE:
					case CANCEL:
					case CANCEL_DEBUFF:
					case REFLECT:
					case COMBATPOINTHEAL:
					case CPHEAL_PERCENT:
					case MAGE_BANE:
					case WARRIOR_BANE:
					case BETRAY:
					case BALANCE_LIFE:
						canTargetSelf = true;
						break;
				}
				// Find me
				switch (skillType)
				{
					case CONFUSION:
					case DEBUFF:
					case STUN:
					case ROOT:
					case FEAR:
					case SLEEP:
					case MUTE:
					case WEAKNESS:
					case PARALYZE:
					case CANCEL:
					case MAGE_BANE:
					case WARRIOR_BANE:
					case DISARM:
						if (checkPartyCheckClan(caster, target))
						{
							caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
							caster.sendPacket(ActionFailed.STATIC_PACKET);
							return null;
						}
						break;
				}

				// Check for null target or any other invalid target
				if (target == null || target.isDead() || target == caster && !canTargetSelf)
				{
					caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}
				if (!GeoData.getInstance().canSeeTarget(caster, target))
					return null;
				return new L2Character[]
				{ target };
			}
			case TARGET_SELF:
			{
				return new L2Character[]
				   					{ caster };
			}
			case TARGET_MULTIFACE:
			{
				return skill.getMultiFaceTargetList(caster);
			}
			case TARGET_MOB:
			{
				// Check for null target or any other invalid target
				if (target == null || target.isDead() || !(target instanceof L2Attackable))
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

	private boolean checkPartyCheckClan(L2Character activeChar, L2Object target)
	{
		if (activeChar instanceof L2Player && target instanceof L2Player)
		{
			L2Player targetChar = (L2Player) target;
			L2Player activeCh = (L2Player) activeChar;

			if (activeCh.getPlayerOlympiad().isInOlympiadMode() && activeCh.getPlayerOlympiad().isOlympiadStart()
					&& targetChar.getPlayerOlympiad().isInOlympiadMode() && targetChar.getPlayerOlympiad().isOlympiadStart())
				return false;
			if (activeCh.getPlayerDuel().isInDuel() && targetChar.getPlayerDuel().isInDuel()
					&& activeCh.getPlayerDuel().getDuelId() == targetChar.getPlayerDuel().getDuelId())
				return false;
			if (activeCh.getParty() != null && targetChar.getParty() != null && activeCh.getParty().getPartyMembers().contains(targetChar))
				return true;
			if (activeCh.getClan() != null && targetChar.getClan() != null && activeCh.getClan().getClanId() == targetChar.getClan().getClanId())
				return true;

			//Finally set the targets to null.
			targetChar = null;
			activeCh = null;
		}
		return false;
	}

	@Override
	public SkillTargetTypes[] getSkillTargetTypes()
	{
		return TARGET_TYPE;
	}
}
