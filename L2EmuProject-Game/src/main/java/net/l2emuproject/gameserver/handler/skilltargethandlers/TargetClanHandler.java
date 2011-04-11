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

import net.l2emuproject.gameserver.events.global.siege.Siege;
import net.l2emuproject.gameserver.events.global.siege.SiegeManager;
import net.l2emuproject.gameserver.handler.ISkillTargetHandler;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.clan.L2ClanMember;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.SkillTargetTypes;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.util.ArrayBunch;

/**
 * @author Intrepid
 *
 */
public final class TargetClanHandler implements ISkillTargetHandler
{
	private static final SkillTargetTypes[]	TARGET_TYPE	=
														{
			SkillTargetTypes.TARGET_CLAN,
			SkillTargetTypes.TARGET_CORPSE_CLAN,
			SkillTargetTypes.TARGET_ALLY,
			SkillTargetTypes.TARGET_CORPSE_ALLY,
			SkillTargetTypes.TARGET_ENEMY_ALLY			};

	@Override
	public L2Character[] useSkillTargetHandler(final L2Character caster, L2Character target, L2Skill skill, boolean onlyFirst)
	{
		final ArrayBunch<L2Character> targetList = new ArrayBunch<L2Character>();

		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		final SkillTargetTypes targetType = skill.getTargetType();

		// Get the type of the skill
		// (ex : PDAM, MDAM, DOT, BLEED, POISON, HEAL, HOT, MANAHEAL, MANARECHARGE, AGGDAMAGE, BUFF, DEBUFF, STUN, ROOT, RESURRECT, PASSIVE...)
		final L2SkillType skillType = skill.getSkillType();

		switch (targetType)
		{
			case TARGET_CORPSE_CLAN:
			case TARGET_CLAN:
			{
				if (caster instanceof L2Playable)
				{
					final int radius = skill.getSkillRadius();
					final L2Player player = caster.getActingPlayer();
					if (player == null)
						return null;

					final L2Clan clan = player.getClan();

					if (player.getPlayerOlympiad().isInOlympiadMode())
					{
						if (player.getPet() == null)
							return new L2Character[]
							{ player };

						return new L2Character[]
						{ player, player.getPet() };
					}

					if (targetType != SkillTargetTypes.TARGET_CORPSE_CLAN)
						if (!onlyFirst)
							targetList.add(player);
						else
							return new L2Character[]
							{ player };

					if (caster.getPet() != null)
						if (targetType != SkillTargetTypes.TARGET_CORPSE_CLAN && !caster.getPet().isDead())
							targetList.add(caster.getPet());

					if (clan != null)
						// Get all visible objects in a spheric area near the L2Character
						// Get Clan Members
						for (final L2ClanMember member : clan.getMembers())
						{
							final L2Player newTarget = member.getPlayerInstance();

							if (newTarget == null || newTarget == player)
								continue;

							if (player.getPlayerDuel().isInDuel()
									&& (player.getPlayerDuel().getDuelId() != newTarget.getPlayerDuel().getDuelId() || player.getParty() == null
											&& player.getParty() != newTarget.getParty()))
								continue;

							if (!skill.eventCheck(player, newTarget))
								continue;

							final L2Summon pet = newTarget.getPet();
							if (pet != null
									&& Util.checkIfInRange(radius, caster, pet, true)
									&& !onlyFirst
									&& (targetType == SkillTargetTypes.TARGET_CORPSE_CLAN && pet.isDead() || targetType == SkillTargetTypes.TARGET_CLAN
											&& !pet.isDead()) && player.checkPvpSkill(newTarget, skill))
								targetList.add(pet);

							if (targetType == SkillTargetTypes.TARGET_CORPSE_CLAN)
							{
								if (!newTarget.isDead())
									continue;
								if (skillType == L2SkillType.RESURRECT)
								{
									// check for charm of courage and caster being a siege participant, otherwise do not allow resurrection
									// on siege battlefield
									final Siege siege = SiegeManager.getInstance().getSiege(newTarget);
									if (siege != null && siege.getIsInProgress())
										// could/should be a more accurate check for siege clans
										if (!newTarget.getCharmOfCourage() || player.getSiegeState() == 0)
											continue;
								}
							}

							if (!Util.checkIfInRange(radius, caster, newTarget, true))
								continue;

							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, skill))
								continue;

							if (!onlyFirst)
								targetList.add(newTarget);
							else
								return new L2Character[]
								{ newTarget };
						}
				}
				else if (caster instanceof L2Npc)
				{
					// for buff purposes, returns one unbuffed friendly mob nearby or mob itself?
					final L2Npc npc = (L2Npc) caster;
					for (final L2Object newTarget : caster.getKnownList().getKnownObjects().values())
						if (newTarget instanceof L2Npc && ((L2Npc) newTarget).getFactionId() == npc.getFactionId())
						{
							if (!Util.checkIfInRange(skill.getCastRange(), caster, newTarget, true))
								continue;
							if (((L2Npc) newTarget).getFirstEffect(skill) != null)
							{
								targetList.add((L2Npc) newTarget);
								break;
							}
						}
					if (targetList.isEmpty())
						targetList.add(caster);
				}

				return targetList.moveToArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_ALLY:
			case TARGET_ALLY:
			{
				if (caster instanceof L2Playable)
				{
					final int radius = skill.getSkillRadius();
					final L2Player player = caster.getActingPlayer();
					if (player == null)
						return null;

					final L2Clan clan = player.getClan();

					if (player.getPlayerOlympiad().isInOlympiadMode())
					{
						if (player.getPet() == null)
							return new L2Character[]
							{ player };

						return new L2Character[]
						{ player, player.getPet() };
					}

					if (targetType != SkillTargetTypes.TARGET_CORPSE_ALLY)
						if (!onlyFirst)
							targetList.add(player);
						else
							return new L2Character[]
							{ player };

					if (caster.getPet() != null)
						if (targetType != SkillTargetTypes.TARGET_CORPSE_ALLY && !caster.getPet().isDead())
							targetList.add(caster.getPet());

					if (clan != null)
						// Get all visible objects in a spheric area near the L2Character
						// Get Clan Members
						for (final L2Object obj : caster.getKnownList().getKnownObjects().values())
						{
							if (obj == player || !(obj instanceof L2Playable) || obj.getActingPlayer() == null)
								continue;

							final L2Player newTarget = obj.getActingPlayer();

							if ((newTarget.getAllyId() == 0 || newTarget.getAllyId() != player.getAllyId())
									&& (newTarget.getClan() == null || newTarget.getClanId() != player.getClanId()))
								continue;

							if (player.getPlayerDuel().isInDuel()
									&& (player.getPlayerDuel().getDuelId() != newTarget.getPlayerDuel().getDuelId() || player.getParty() != null
											&& player.getParty() != newTarget.getParty()))
								continue;

							if (!skill.eventCheck(player, newTarget))
								continue;

							final L2Summon pet = newTarget.getPet();
							if (pet != null
									&& Util.checkIfInRange(radius, caster, pet, true)
									&& !onlyFirst
									&& (targetType == SkillTargetTypes.TARGET_CORPSE_ALLY && pet.isDead() || targetType == SkillTargetTypes.TARGET_ALLY
											&& !pet.isDead()) && player.checkPvpSkill(newTarget, skill))
								targetList.add(pet);

							if (targetType == SkillTargetTypes.TARGET_CORPSE_ALLY)
							{
								if (!newTarget.isDead())
									continue;
								// Siege battlefield resurrect has been made possible for participants
								if (skill.getSkillType() == L2SkillType.RESURRECT)
									if (newTarget.isInsideZone(L2Zone.FLAG_SIEGE) && !newTarget.isInSiege())
										continue;
							}

							if (!Util.checkIfInRange(radius, caster, newTarget, true))
								continue;

							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, skill))
								continue;

							if (!onlyFirst)
								targetList.add(newTarget);

							return new L2Character[]
							{ newTarget };

						}
				}
				return targetList.moveToArray(new L2Character[targetList.size()]);
			}
			case TARGET_ENEMY_ALLY:
			{
				// int charX, charY, charZ, targetX, targetY, targetZ, dx, dy, dz;
				final int radius = skill.getSkillRadius();
				L2Character newTarget;

				if (skill.getCastRange() > -1 && target != null)
					newTarget = target;
				else
					newTarget = caster;

				if (newTarget != caster || skill.isOffensive())
					targetList.add(newTarget);

				for (final L2Character obj : caster.getKnownList().getKnownCharactersInRadius(radius))
				{
					if (obj == newTarget || obj == caster)
						continue;

					if (obj instanceof L2Attackable)
						if (!obj.isAlikeDead())
						{
							// Don't add this target if this is a PC->PC pvp casting and pvp condition not met
							if (caster instanceof L2Player && !((L2Player) caster).checkPvpSkill(obj, skill))
								continue;

							// check if both attacker and target are L2PcInstances and if they are in same party or clan
							if (caster instanceof L2Player
									&& obj instanceof L2Player
									&& (((L2Player) caster).getClanId() != ((L2Player) obj).getClanId() || ((L2Player) caster).getAllyId() != ((L2Player) obj)
											.getAllyId()
											&& caster.getParty() != null
											&& obj.getParty() != null
											&& caster.getParty().getPartyLeaderOID() != obj.getParty().getPartyLeaderOID()))
								continue;

							targetList.add(obj);
						}
				}
				//FIXME: (Noctarius) Added return here to deny fallthrough - is it wished to add more targets?
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
