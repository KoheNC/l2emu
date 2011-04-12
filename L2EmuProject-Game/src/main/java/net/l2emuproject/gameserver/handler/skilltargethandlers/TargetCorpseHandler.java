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
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.SkillTargetTypes;
import net.l2emuproject.gameserver.system.taskmanager.DecayTaskManager;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.object.instance.L2PetInstance;
import net.l2emuproject.gameserver.world.object.instance.L2SummonInstance;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.util.ArrayBunch;

/**
 * @author Intrepid
 */
public final class TargetCorpseHandler implements ISkillTargetHandler
{
	private static final SkillTargetTypes[]	TARGET_TYPE	=
														{
			SkillTargetTypes.TARGET_CORPSE_PLAYER,
			SkillTargetTypes.TARGET_CORPSE_PET,
			SkillTargetTypes.TARGET_AREA_CORPSE_MOB,
			SkillTargetTypes.TARGET_CORPSE_MOB,
			SkillTargetTypes.TARGET_AREA_CORPSES		};

	@Override
	public L2Character[] useSkillTargetHandler(final L2Character caster, L2Character target, L2Skill skill, boolean onlyFirst)
	{
		final ArrayBunch<L2Character> targetList = new ArrayBunch<L2Character>();

		try
		{
			// Get the target type of the skill
			// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
			final SkillTargetTypes targetType = skill.getTargetType();

			// Get the type of the skill
			// (ex : PDAM, MDAM, DOT, BLEED, POISON, HEAL, HOT, MANAHEAL, MANARECHARGE, AGGDAMAGE, BUFF, DEBUFF, STUN, ROOT, RESURRECT, PASSIVE...)
			final L2SkillType skillType = skill.getSkillType();

			switch (targetType)
			{
				case TARGET_CORPSE_PET:
				{
					if (caster instanceof L2Player)
					{
						target = caster.getPet();
						if (target != null && target.isDead())
							return new L2Character[]
							{ target };
					}

					return null;
				}
				case TARGET_CORPSE_PLAYER:
				{
					if (target != null && target.isDead())
					{
						L2Player player = null;

						if (caster instanceof L2Player)
							player = (L2Player) caster;

						L2Player targetPlayer = null;
						if (target instanceof L2Player)
							targetPlayer = (L2Player) target;

						L2PetInstance targetPet = null;
						if (target instanceof L2PetInstance)
							targetPet = (L2PetInstance) target;

						if (player != null && (targetPlayer != null || targetPet != null))
						{
							boolean condGood = true;

							if (skillType == L2SkillType.RESURRECT)
							{
								// check target is not in a active siege zone
								Siege siege = null;

								if (targetPlayer != null)
									siege = SiegeManager.getInstance().getSiege(targetPlayer);
								else if (targetPet != null)
									siege = SiegeManager.getInstance().getSiege(targetPet);

								if (siege != null && siege.getIsInProgress() && targetPlayer != null
										&& (!targetPlayer.getCharmOfCourage() || player.getSiegeState() == 0))
								{
									condGood = false;
									player.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
								}

								if (targetPlayer != null)
								{
									if (targetPlayer.isFestivalParticipant()) // Check to see if the current player target is in a festival.
									{
										condGood = false;
										player.sendMessage("You may not resurrect participants in a festival.");
									}
									if (targetPlayer.isReviveRequested())
									{
										player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been
										// proposed.
										condGood = false;
									}
								}
								else if (targetPet != null)
									if (targetPet.getOwner() != player)
										if (targetPet.getOwner().isPetReviveRequested())
										{
											player.sendPacket(SystemMessageId.CANNOT_RES_PET2); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
											condGood = false;
										}
							}

							if (condGood)
							{
								if (!onlyFirst)
								{
									targetList.add(target);
									return targetList.moveToArray(new L2Character[targetList.size()]);
								}

								return new L2Character[]
								{ target };

							}
						}
					}
					caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}
				case TARGET_CORPSE_MOB:
				{
					if (!(target instanceof L2Attackable) || !target.isDead())
					{
						caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					// Corpse mob only available for half time
					switch (skillType)
					{
						case DRAIN:
						case SUMMON:
						{
							if (DecayTaskManager.getInstance().hasDecayTask(target))
								if (DecayTaskManager.getInstance().getRemainingDecayTime(target) < 0.5)
								{
									caster.sendPacket(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED);
									return null;
								}
						}
					}

					if (!onlyFirst)
					{
						targetList.add(target);
						return targetList.moveToArray(new L2Character[targetList.size()]);
					}

					return new L2Character[]
					{ target };

				}
				case TARGET_AREA_CORPSE_MOB:
				{
					if (!(target instanceof L2Attackable || target instanceof L2SummonInstance) || !target.isDead())
					{
						caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					if (!onlyFirst)
						targetList.add(target);
					else
						return new L2Character[]
						{ target };

					final boolean srcInArena = caster.isInsideZone(L2Zone.FLAG_PVP) && !caster.isInsideZone(L2Zone.FLAG_SIEGE);
					L2Player src = null;
					if (caster instanceof L2Player)
						src = (L2Player) caster;
					L2Player trg = null;

					final int radius = skill.getSkillRadius();
					for (final L2Object obj : caster.getKnownList().getKnownObjects().values())
					{
						if (!(obj instanceof L2Attackable || obj instanceof L2Playable) || ((L2Character) obj).isDead() || obj == caster)
							continue;

						final boolean targetInPvP = ((L2Character) obj).isInsideZone(L2Zone.FLAG_PVP) && !((L2Character) obj).isInsideZone(L2Zone.FLAG_SIEGE);

						if (!Util.checkIfInRange(radius, target, obj, true))
							continue;

						if (!GeoData.getInstance().canSeeTarget(caster, obj))
							continue;

						if (obj instanceof L2Player && src != null)
						{
							trg = (L2Player) obj;

							if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
								continue;

							if (trg.isInsideZone(L2Zone.FLAG_PEACE))
								continue;

							if (!srcInArena && !targetInPvP)
							{
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
									continue;

								if (src.getClan() != null && trg.getClan() != null)
									if (src.getClan().getClanId() == trg.getClan().getClanId())
										continue;

								if (!src.checkPvpSkill(obj, skill))
									continue;
							}
						}
						if (obj instanceof L2Summon && src != null)
						{
							trg = ((L2Summon) obj).getOwner();

							if (src.getParty() != null && trg.getParty() != null && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
								continue;

							if (!srcInArena && !targetInPvP)
							{
								if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
									continue;

								if (src.getClan() != null && trg.getClan() != null)
									if (src.getClan().getClanId() == trg.getClan().getClanId())
										continue;

								if (!src.checkPvpSkill(trg, skill))
									continue;
							}

							if (((L2Summon) obj).isInsideZone(L2Zone.FLAG_PEACE))
								continue;
						}

						targetList.add((L2Character) obj);
					}

					if (targetList.size() == 0)
						return null;
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_AREA_CORPSES:
				{
					if (!(target instanceof L2Attackable) || !target.isDead())
					{
						caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
						return null;
					}

					if (!onlyFirst)
						targetList.add(target);
					else
						return new L2Character[]
						{ target };

					final int radius = skill.getSkillRadius();
					if (caster.getKnownList() != null)
						for (final L2Object obj : caster.getKnownList().getKnownObjects().values())
						{
							if (obj == null || !(obj instanceof L2Attackable))
								continue;
							final L2Character cha = (L2Character) obj;

							if (!cha.isDead() || !Util.checkIfInRange(radius, target, cha, true))
								continue;

							if (!GeoData.getInstance().canSeeTarget(caster, cha))
								continue;

							targetList.add(cha);
						}

					if (targetList.size() == 0)
						return null;
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
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
