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

import java.util.List;

import net.l2emuproject.gameserver.handler.ISkillTargetHandler;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.SkillTargetTypes;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.util.ArrayBunch;

/**
 * @author Intrepid
 *
 */
public final class TargetPartyHandler implements ISkillTargetHandler
{
	private static final SkillTargetTypes[]	TARGET_TYPE	=
														{
			SkillTargetTypes.TARGET_PARTY,
			SkillTargetTypes.TARGET_PARTY_CLAN,
			SkillTargetTypes.TARGET_PARTY_MEMBER,
			SkillTargetTypes.TARGET_PARTY_NOTME,
			SkillTargetTypes.TARGET_PARTY_OTHER		};

	@Override
	public L2Character[] useSkillTargetHandler(final L2Character caster, L2Character target, L2Skill skill, boolean onlyFirst)
	{
		final ArrayBunch<L2Character> targetList = new ArrayBunch<L2Character>();

		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		final SkillTargetTypes targetType = skill.getTargetType();

		switch (targetType)
		{
			case TARGET_PARTY:
			{
				if (onlyFirst)
					return new L2Character[]
					{ caster };

				targetList.add(caster);

				L2Player player = null;

				if (caster instanceof L2Summon)
				{
					player = ((L2Summon) caster).getOwner();
					targetList.add(player);
				}
				else if (caster instanceof L2Player)
				{
					player = (L2Player) caster;
					if (caster.getPet() != null)
						targetList.add(caster.getPet());
				}

				if (caster.getParty() != null)
				{
					// Get all visible objects in a spheric area near the L2Character
					// Get a list of Party Members
					final List<L2Player> partyList = caster.getParty().getPartyMembers();

					for (final L2Player partyMember : partyList)
					{
						if (player == null || partyMember == null || partyMember == player)
							continue;

						if (player.getPlayerDuel().isInDuel() && player.getPlayerDuel().getDuelId() != partyMember.getPlayerDuel().getDuelId())
							continue;

						if (!skill.eventCheck(player, partyMember))
							continue;

						if (!partyMember.isDead() && Util.checkIfInRange(skill.getSkillRadius(), caster, partyMember, true))
						{
							targetList.add(partyMember);

							if (partyMember.getPet() != null && !partyMember.getPet().isDead())
								targetList.add(partyMember.getPet());
						}
					}
				}
				return targetList.moveToArray(new L2Character[targetList.size()]);
			}
			case TARGET_PARTY_MEMBER:
			{
				if (target != null && target == caster || target != null && caster.getParty() != null && target.getParty() != null
						&& caster.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID() || target != null && caster instanceof L2Player
						&& target instanceof L2Summon && caster.getPet() == target || target != null && caster instanceof L2Summon
						&& target instanceof L2Player && caster == target.getPet())
				{
					if (!target.isDead())
						// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
						return new L2Character[]
						{ target };

					return null;
				}

				caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return null;
			}
			case TARGET_PARTY_OTHER:
			{
				if (target != null && target != caster && caster.getParty() != null && target.getParty() != null
						&& caster.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())
				{
					if (!target.isDead())
					{
						if (target instanceof L2Player)
						{
							final L2Player player = (L2Player) target;
							switch (skill.getId())
							{
								// FORCE BUFFS may cancel here but there should be a proper condition
								case 426:
									if (!player.isMageClass())
										return new L2Character[]
										{ target };

									return null;
								case 427:
									if (player.isMageClass())
										return new L2Character[]
										{ target };

									return null;
							}
						}
						return new L2Character[]
						{ target };
					}

					return null;
				}

				caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return null;
			}
			case TARGET_PARTY_CLAN:
			{
				if (onlyFirst)
					return new L2Character[]
					{ caster };

				final L2Player player = caster.getActingPlayer();

				if (player == null)
					return null;

				targetList.add(player);

				final int radius = skill.getSkillRadius();
				final boolean hasClan = player.getClan() != null;
				final boolean hasParty = player.isInParty();

				if (L2Skill.addSummon(caster, player, radius, false))
					targetList.add(player.getPet());

				// if player in olympiad mode or not in clan and not in party
				if (player.getPlayerOlympiad().isInOlympiadMode() || !(hasClan || hasParty))
					return new L2Character[]
					{ player };

				for (final L2Player obj : caster.getKnownList().getKnownPlayersInRadius(radius))
				{
					if (obj == null)
						continue;

					if (player.getPlayerDuel().isInDuel())
					{
						if (player.getPlayerDuel().getDuelId() != obj.getPlayerDuel().getDuelId())
							continue;

						if (hasParty && obj.isInParty() && player.getParty().getPartyLeaderOID() != obj.getParty().getPartyLeaderOID())
							continue;
					}

					if (!(hasClan && obj.getClanId() == player.getClanId() || hasParty && obj.isInParty()
							&& player.getParty().getPartyLeaderOID() == obj.getParty().getPartyLeaderOID()))
						continue;

					// Don't add this target if this is a Pc->Pc pvp
					// casting and pvp condition not met
					if (!player.checkPvpSkill(obj, skill))
						continue;

					if (!skill.eventCheck(player, obj))
						continue;

					if (!onlyFirst && L2Skill.addSummon(caster, obj, radius, false))
						targetList.add(obj.getPet());

					if (!L2Skill.addCharacter(caster, obj, radius, false))
						continue;

					if (onlyFirst)
						return new L2Character[]
						{ obj };

					targetList.add(obj);
				}

				return targetList.moveToArray(new L2Character[targetList.size()]);
			}
			case TARGET_PARTY_NOTME:
			{
				//target all party members except yourself
				if (onlyFirst)
					return new L2Character[]
					{ caster };

				L2Player player = null;

				if (caster instanceof L2Summon)
				{
					player = ((L2Summon) caster).getOwner();
					targetList.add(player);
				}
				else if (caster instanceof L2Player)
				{
					player = (L2Player) caster;
					if (caster.getPet() != null)
						targetList.add(caster.getPet());
				}

				if (caster.getParty() != null)
				{
					final List<L2Player> partyList = caster.getParty().getPartyMembers();

					for (final L2Player partyMember : partyList)
					{
						if (partyMember == null)
							continue;
						if (partyMember == player)
							continue;
						if (!partyMember.isDead() && Util.checkIfInRange(skill.getSkillRadius(), caster, partyMember, true))
						{
							targetList.add(partyMember);

							if (partyMember.getPet() != null && !partyMember.getPet().isDead())
								targetList.add(partyMember.getPet());
						}
					}
				}
				return targetList.moveToArray(new L2Character[targetList.size()]);
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
