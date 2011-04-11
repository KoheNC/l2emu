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
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.object.instance.L2SummonInstance;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.util.ArrayBunch;

/**
 * @author Intrepid
 *
 */
public final class TargetAreaHandler implements ISkillTargetHandler
{
	private static final SkillTargetTypes[]	TARGET_TYPE	=
														{
			SkillTargetTypes.TARGET_AREA,
			SkillTargetTypes.TARGET_FRONT_AREA,
			SkillTargetTypes.TARGET_BEHIND_AREA,
			SkillTargetTypes.TARGET_AREA_UNDEAD		};

	@Override
	public L2Character[] useSkillTargetHandler(final L2Character caster, L2Character target, L2Skill skill, boolean onlyFirst)
	{
		final ArrayBunch<L2Character> targetList = new ArrayBunch<L2Character>();

		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		final SkillTargetTypes targetType = skill.getTargetType();

		switch (targetType)
		{
			case TARGET_AREA:
			{
				if (!(target instanceof L2Attackable || target instanceof L2Playable) || // Target is not L2Attackable or L2Playable
						skill.getCastRange() >= 0 && (target == caster || target.isAlikeDead())) // target is null or self or dead/faking
				{
					caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}

				L2Character cha;

				if (skill.getCastRange() >= 0)
				{
					cha = target;

					if (!onlyFirst)
						targetList.add(cha); // Add target to target list
					else
						return new L2Character[]
						{ cha };
				}
				else
					cha = caster;

				final boolean effectOriginIsL2Playable = cha instanceof L2Playable;
				final boolean srcIsSummon = caster instanceof L2Summon;

				final L2Player src = caster.getActingPlayer();

				final int radius = skill.getSkillRadius();

				final boolean srcInPvP = caster.isInsideZone(L2Zone.FLAG_PVP) && !caster.isInsideZone(L2Zone.FLAG_SIEGE);

				for (final L2Object obj : caster.getKnownList().getKnownObjects().values())
				{
					if (!(obj instanceof L2Attackable || obj instanceof L2Playable))
						continue;
					if (obj == cha)
						continue;
					target = (L2Character) obj;
					final boolean targetInPvP = target.isInsideZone(L2Zone.FLAG_PVP) && !target.isInsideZone(L2Zone.FLAG_SIEGE);

					if (!target.isDead() && target != caster)
					{
						if (!Util.checkIfInRange(radius, obj, cha, true))
							continue;
						if (src != null) // caster is L2Playable and exists
						{
							if (obj instanceof L2Player)
							{
								final L2Player trg = (L2Player) obj;
								if (trg == src)
									continue;
								if (src.getParty() != null && trg.getParty() != null
										&& src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									continue;

								if (trg.isInsideZone(L2Zone.FLAG_PEACE))
									continue;

								if (!srcInPvP && !targetInPvP)
								{
									if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
										continue;

									if (src.getClan() != null && trg.getClan() != null)
										if (src.getClan().getClanId() == trg.getClan().getClanId())
											continue;

									if (!src.checkPvpSkill(obj, skill, srcIsSummon))
										continue;
								}
							}
							if (obj instanceof L2Summon)
							{
								final L2Player trg = ((L2Summon) obj).getOwner();
								if (trg == src)
									continue;

								if (src.getParty() != null && trg.getParty() != null
										&& src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									continue;

								if (!srcInPvP && !targetInPvP)
								{
									if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
										continue;

									if (src.getClan() != null && trg.getClan() != null)
										if (src.getClan().getClanId() == trg.getClan().getClanId())
											continue;

									if (!src.checkPvpSkill(trg, skill, srcIsSummon))
										continue;
								}

								if (trg.isInsideZone(L2Zone.FLAG_PEACE))
									continue;
							}
						}
						else if (effectOriginIsL2Playable && // If effect starts at L2Playable and
								!(obj instanceof L2Playable)) // Object is not L2Playable
							continue;

						if (!GeoData.getInstance().canSeeTarget(caster, target))
							continue;

						targetList.add(target);
					}
				}

				if (targetList.size() == 0)
					return null;

				return targetList.moveToArray(new L2Character[targetList.size()]);
			}
			case TARGET_FRONT_AREA:
			{
				if (!(target instanceof L2Attackable || target instanceof L2Playable) || //   Target is not L2Attackable or L2Playable
						skill.getCastRange() >= 0 && (target == caster || target.isAlikeDead())) //target is null or self or dead/faking
				{
					caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}

				L2Character cha;

				if (skill.getCastRange() >= 0)
				{
					cha = target;

					if (!onlyFirst)
						targetList.add(cha); // Add target to target list
					else
						return new L2Character[]
						{ cha };
				}
				else
					cha = caster;

				final boolean effectOriginIsL2Playable = cha instanceof L2Playable;

				final L2Player src = caster.getActingPlayer();

				final int radius = skill.getSkillRadius();

				final boolean srcInArena = caster.isInsideZone(L2Zone.FLAG_PVP) && !caster.isInsideZone(L2Zone.FLAG_SIEGE);

				for (final L2Object obj : caster.getKnownList().getKnownObjects().values())
				{
					if (obj == cha)
						continue;

					if (!(obj instanceof L2Attackable || obj instanceof L2Playable))
						continue;

					target = (L2Character) obj;

					if (!target.isDead() && target != caster)
					{
						if (!Util.checkIfInRange(radius, target, caster, true))
							continue;

						if (!target.isInFrontOf(caster))
							continue;

						if (src != null) // caster is L2Playable and exists
						{
							final boolean targetInPvP = target.isInsideZone(L2Zone.FLAG_PVP) && !target.isInsideZone(L2Zone.FLAG_SIEGE);
							if (obj instanceof L2Player)
							{
								final L2Player trg = (L2Player) obj;
								if (trg == src)
									continue;
								if (src.getParty() != null && trg.getParty() != null
										&& src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
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
							if (obj instanceof L2Summon)
							{
								final L2Player trg = ((L2Summon) obj).getOwner();
								if (trg == src)
									continue;

								if (src.getParty() != null && trg.getParty() != null
										&& src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
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

								if (trg.isInsideZone(L2Zone.FLAG_PEACE))
									continue;
							}
						}
						else if (effectOriginIsL2Playable && // If effect starts at L2Playable and
								!(obj instanceof L2Playable)) // Object is not L2Playable
							continue;

						if (!GeoData.getInstance().canSeeTarget(caster, target))
							continue;

						targetList.add(target);
					}
				}

				if (targetList.size() == 0)
					return null;

				return targetList.moveToArray(new L2Character[targetList.size()]);
			}
			case TARGET_BEHIND_AREA:
			{
				if (!(target instanceof L2Attackable || target instanceof L2Playable) || //   Target is not L2Attackable or L2Playable
						skill.getCastRange() >= 0 && (target == caster || target.isAlikeDead())) //target is null or self or dead/faking
				{
					caster.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return null;
				}

				L2Character cha;

				if (skill.getCastRange() >= 0)
				{
					cha = target;

					if (!onlyFirst)
						targetList.add(cha); // Add target to target list
					else
						return new L2Character[]
						{ cha };
				}
				else
					cha = caster;

				final boolean effectOriginIsL2Playable = cha instanceof L2Playable;

				final L2Player src = caster.getActingPlayer();

				final int radius = skill.getSkillRadius();

				final boolean srcInArena = caster.isInsideZone(L2Zone.FLAG_PVP) && !caster.isInsideZone(L2Zone.FLAG_SIEGE);

				for (final L2Object obj : caster.getKnownList().getKnownObjects().values())
				{
					if (obj == cha)
						continue;
					if (!(obj instanceof L2Attackable || obj instanceof L2Playable))
						continue;
					target = (L2Character) obj;

					if (!target.isDead() && target != caster)
					{
						if (!Util.checkIfInRange(radius, obj, caster, true))
							continue;

						if (!target.isBehind(caster))
							continue;

						if (src != null) // caster is L2Playable and exists
						{
							final boolean targetInPvP = target.isInsideZone(L2Zone.FLAG_PVP) && !target.isInsideZone(L2Zone.FLAG_SIEGE);
							if (obj instanceof L2Player)
							{
								final L2Player trg = (L2Player) obj;
								if (trg == src)
									continue;
								if (src.getParty() != null && trg.getParty() != null
										&& src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
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
							if (obj instanceof L2Summon)
							{
								final L2Player trg = ((L2Summon) obj).getOwner();
								if (trg == src)
									continue;

								if (src.getParty() != null && trg.getParty() != null
										&& src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									continue;

								if (!srcInArena && !targetInPvP)
								{
									if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
										continue;

									if (trg.isInsideZone(L2Zone.FLAG_PEACE))
										continue;

									if (src.getClan() != null && trg.getClan() != null)
										if (src.getClan().getClanId() == trg.getClan().getClanId())
											continue;

									if (!src.checkPvpSkill(trg, skill))
										continue;

								}
							}
						}
						else // If effect starts at L2Playable and object is not L2Playable
						if (effectOriginIsL2Playable && !(obj instanceof L2Playable))
							continue;

						if (!GeoData.getInstance().canSeeTarget(caster, target))
							continue;

						targetList.add(target);
					}
				}

				if (targetList.size() == 0)
					return null;

				return targetList.moveToArray(new L2Character[targetList.size()]);
			}
			case TARGET_AREA_UNDEAD:
			{
				L2Character cha;
				final int radius = skill.getSkillRadius();
				if (skill.getCastRange() >= 0 && (target instanceof L2Npc || target instanceof L2SummonInstance) && target.isUndead() && !target.isAlikeDead())
				{
					cha = target;

					if (!onlyFirst)
						targetList.add(cha); // Add target to target list
					else
						return new L2Character[]
						{ cha };
				}
				else
					cha = caster;

				for (final L2Object obj : cha.getKnownList().getKnownObjects().values())
				{
					if (obj instanceof L2Npc)
						target = (L2Npc) obj;
					else if (obj instanceof L2SummonInstance)
						target = (L2SummonInstance) obj;
					else
						continue;

					if (!target.isAlikeDead()) // If target is not dead/fake death and not self
					{
						if (!target.isUndead())
							continue;
						if (!Util.checkIfInRange(radius, cha, obj, true)) // Go to next obj if obj isn't in range
							continue;
						if (!GeoData.getInstance().canSeeTarget(caster, target))
							continue;

						if (!onlyFirst)
							targetList.add((L2Character) obj); // Add obj to target lists
						else
							return new L2Character[]
							{ (L2Character) obj };
					}
				}

				if (targetList.size() == 0)
					return null;
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
