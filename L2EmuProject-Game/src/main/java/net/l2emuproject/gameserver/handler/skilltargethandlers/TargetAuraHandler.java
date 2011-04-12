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
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.util.ArrayBunch;

/**
 * @author Intrepid
 */
public final class TargetAuraHandler implements ISkillTargetHandler
{
	private static final SkillTargetTypes[]	TARGET_TYPE	=
														{
			SkillTargetTypes.TARGET_AURA,
			SkillTargetTypes.TARGET_FRONT_AURA,
			SkillTargetTypes.TARGET_BEHIND_AURA,
			SkillTargetTypes.TARGET_SERVITOR_AURA		};

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
				case TARGET_AURA:
				case TARGET_SERVITOR_AURA:
				{
					final int radius = skill.getSkillRadius();
					final boolean srcInPvP = caster.isInsideZone(L2Zone.FLAG_PVP) && !caster.isInsideZone(L2Zone.FLAG_SIEGE);

					final L2Player src = caster.getActingPlayer();

					// Go through the L2Character _knownList
					for (final L2Character cha : caster.getKnownList().getKnownCharactersInRadius(radius))
						if (cha instanceof L2Attackable || cha instanceof L2Playable)
						{
							final boolean targetInPvP = cha.isInsideZone(L2Zone.FLAG_PVP) && !cha.isInsideZone(L2Zone.FLAG_SIEGE);

							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (cha == caster || cha == src || cha.isDead())
								continue;
							if (src != null)
							{
								// check if both attacker and target are L2PcInstances and if they are in same party
								if (cha instanceof L2Player)
								{
									final L2Player player = (L2Player) cha;
									if (!src.checkPvpSkill(cha, skill))
										continue;
									if (src.getParty() != null && player.getParty() != null
											&& src.getParty().getPartyLeaderOID() == player.getParty().getPartyLeaderOID())
										continue;
									if (!srcInPvP && !targetInPvP)
									{
										if (src.getAllyId() == player.getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == player.getClanId())
											continue;
									}
								}
								else if (cha instanceof L2Summon)
								{
									final L2Player trg = ((L2Summon) cha).getOwner();
									if (trg == src)
										continue;
									if (!src.checkPvpSkill(trg, skill))
										continue;
									if (src.getParty() != null && trg.getParty() != null
											&& src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;
									if (!srcInPvP && !targetInPvP)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == trg.getClanId())
											continue;
									}
								}
							}
							else if (!(cha instanceof L2Playable) // Target is not L2Playable
									&& !caster.isConfused()) // and caster not confused (?)
								continue;

							if (!GeoData.getInstance().canSeeTarget(caster, cha))
								continue;

							if (!onlyFirst)
								targetList.add(cha);
							else
								return new L2Character[]
								{ cha };
						}
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_FRONT_AURA:
				{
					final int radius = skill.getSkillRadius();
					final boolean srcInArena = caster.isInsideZone(L2Zone.FLAG_PVP) && !caster.isInsideZone(L2Zone.FLAG_SIEGE);

					final L2Player src = caster.getActingPlayer();

					// Go through the L2Character _knownList
					for (final L2Character cha : caster.getKnownList().getKnownCharactersInRadius(radius))
						if (cha instanceof L2Attackable || cha instanceof L2Playable)
						{
							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (cha == caster || cha == src || cha.isDead())
								continue;
							if (src != null)
							{
								if (!cha.isInFrontOf(caster))
									continue;

								final boolean objInPvpZone = cha.isInsideZone(L2Zone.FLAG_PVP) && !cha.isInsideZone(L2Zone.FLAG_SIEGE);
								// check if both attacker and target are L2PcInstances and if they are in same party
								if (cha instanceof L2Player)
								{
									final L2Player player = (L2Player) cha;
									if (!src.checkPvpSkill(cha, skill))
										continue;
									if (src.getParty() != null && player.getParty() != null
											&& src.getParty().getPartyLeaderOID() == player.getParty().getPartyLeaderOID())
										continue;
									if (!srcInArena && !objInPvpZone)
									{
										if (src.getAllyId() == player.getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == player.getClanId())
											continue;
									}
								}
								if (cha instanceof L2Summon)
								{
									final L2Player trg = ((L2Summon) cha).getOwner();
									if (trg == src)
										continue;
									if (!src.checkPvpSkill(trg, skill))
										continue;
									if (src.getParty() != null && trg.getParty() != null
											&& src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;
									if (!srcInArena && !objInPvpZone)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == trg.getClanId())
											continue;
									}
								}
							}
							else if (!(cha instanceof L2Playable) // Target is not L2Playable
									&& !caster.isConfused()) // and caster not confused (?)
								continue;

							if (!GeoData.getInstance().canSeeTarget(caster, cha))
								continue;

							if (!onlyFirst)
								targetList.add(cha);
							else
								return new L2Character[]
								{ cha };
						}
					return targetList.moveToArray(new L2Character[targetList.size()]);
				}
				case TARGET_BEHIND_AURA:
				{
					final int radius = skill.getSkillRadius();
					final boolean srcInArena = caster.isInsideZone(L2Zone.FLAG_PVP) && !caster.isInsideZone(L2Zone.FLAG_SIEGE);

					final L2Player src = caster.getActingPlayer();

					// Go through the L2Character _knownList
					for (final L2Character cha : caster.getKnownList().getKnownCharactersInRadius(radius))
						if (cha instanceof L2Attackable || cha instanceof L2Playable)
						{
							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (cha == caster || cha == src || cha.isDead())
								continue;
							if (src != null)
							{
								if (!cha.isBehind(caster))
									continue;

								final boolean objInPvpZone = cha.isInsideZone(L2Zone.FLAG_PVP) && !cha.isInsideZone(L2Zone.FLAG_SIEGE);
								// check if both attacker and target are L2PcInstances and if they are in same party
								if (cha instanceof L2Player)
								{
									final L2Player player = (L2Player) cha;
									if (!src.checkPvpSkill(cha, skill))
										continue;
									if (src.getParty() != null && player.getParty() != null
											&& src.getParty().getPartyLeaderOID() == player.getParty().getPartyLeaderOID())
										continue;
									if (!srcInArena && !objInPvpZone)
									{
										if (src.getAllyId() == player.getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == player.getClanId())
											continue;
									}
								}
								if (cha instanceof L2Summon)
								{
									final L2Player trg = ((L2Summon) cha).getOwner();
									if (trg == src)
										continue;
									if (!src.checkPvpSkill(trg, skill))
										continue;
									if (src.getParty() != null && trg.getParty() != null
											&& src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
										continue;
									if (!srcInArena && !objInPvpZone)
									{
										if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
											continue;
										if (src.getClanId() != 0 && src.getClanId() == trg.getClanId())
											continue;
									}
								}
							}
							else if (!(cha instanceof L2Playable) // Target is not L2Playable
									&& !caster.isConfused()) // and caster not confused (?)
								continue;
							if (!GeoData.getInstance().canSeeTarget(caster, cha))
								continue;

							if (!onlyFirst)
								targetList.add(cha);
							else
								return new L2Character[]
								{ cha };
						}
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
