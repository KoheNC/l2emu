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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.SevenSigns;
import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.instancemanager.InstanceManager;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.model.restriction.AvailableRestriction;
import net.l2emuproject.gameserver.model.restriction.ObjectRestrictions;
import net.l2emuproject.gameserver.model.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.clientpackets.ConfirmDlgAnswer.AnswerHandler;
import net.l2emuproject.gameserver.network.serverpackets.ConfirmDlg;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.util.Util;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.zone.L2Zone;

/**
 * @authors BiTi, Sami
 * 
 */
public class SummonFriend implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.SUMMON_FRIEND };

	public static boolean checkSummonerStatus(L2Player summonerChar)
	{
		if (summonerChar == null)
			return false;

		if (summonerChar.getPlayerOlympiad().isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}

		if (summonerChar.getPlayerObserver().inObserverMode())
		{
			return false;
		}

		if (summonerChar.isInsideZone(L2Zone.FLAG_NOSUMMON) || summonerChar.isFlyingMounted())
		{
			summonerChar.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
			return false;
		}

		if (summonerChar.isInInstance())
		{
			Instance summonerInstance = InstanceManager.getInstance().getInstance(summonerChar.getInstanceId());
			if (summonerInstance != null && (!Config.ALLOW_SUMMON_TO_INSTANCE || !summonerInstance.isSummonAllowed()))
			{
				summonerChar.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return false;
			}
		}

		return true;
	}

	public static boolean checkTargetStatus(L2Player targetChar, L2Player summonerChar)
	{
		if (targetChar == null)
			return false;

		if (targetChar.isAlikeDead())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(targetChar);
			summonerChar.sendPacket(sm);
			return false;
		}

		if (targetChar.isInStoreMode())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(targetChar);
			summonerChar.sendPacket(sm);
			return false;
		}

		if (targetChar.isRooted() || targetChar.isInCombat())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(targetChar);
			summonerChar.sendPacket(sm);
			return false;
		}

		if (targetChar.getPlayerOlympiad().isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
			return false;
		}

		if (targetChar.getPlayerObserver().inObserverMode())
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}

		if (targetChar.isFestivalParticipant() || targetChar.isFlyingMounted())
		{
			summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}

		if (targetChar.isInsideZone(L2Zone.FLAG_NOSUMMON))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_IN_SUMMON_BLOCKING_AREA);
			sm.addString(targetChar.getName());
			summonerChar.sendPacket(sm);
			return false;
		}

		if (ObjectRestrictions.getInstance()
				.checkRestriction(targetChar, AvailableRestriction.PlayerSummonFriend))
		{
			summonerChar.sendMessage("You cannot summon your friend due to his restrictions.");
			targetChar.sendMessage("You cannot be summoned due to a restriction.");
			return false;
		}
		
		if (GlobalRestrictions.isRestricted(summonerChar, null) || GlobalRestrictions.isRestricted(targetChar, null))
		{
			summonerChar.sendMessage("You cannot summon your friend due to events restrictions.");
			targetChar.sendMessage("You cannot be summoned due to events restriction.");
			return false;
		}

		// On retail character can enter 7s dungeon with summon friend,
		// but will be teleported away by mobs
		// because currently this is not working in L2J we do not allowing summoning
		if (summonerChar.isIn7sDungeon())
		{
			int targetCabal = SevenSigns.getInstance().getPlayerCabal(targetChar);
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
					return false;
				}
			}
			else
			{
				if (targetCabal == SevenSigns.CABAL_NULL)
				{
					summonerChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
					return false;
				}
			}
		}
		
		if (!GlobalRestrictions.canBeSummoned(targetChar))
		{
			summonerChar.sendMessage("You cannot summon your friend due to his restrictions.");
			targetChar.sendMessage("You cannot be summoned due to a restriction.");
			return false;
		}

		return true;
	}

	public static void teleToTarget(L2Player targetChar, L2Player summonerChar, L2Skill summonSkill)
	{
		if (targetChar == null || summonerChar == null || summonSkill == null)
			return;

		if (!checkSummonerStatus(summonerChar))
			return;
		if (!checkTargetStatus(targetChar, summonerChar))
			return;

		int itemConsumeId = summonSkill.getTargetConsumeId();
		int itemConsumeCount = summonSkill.getTargetConsume();
		if (itemConsumeId != 0 && itemConsumeCount != 0)
		{
			if (targetChar.getInventory().getInventoryItemCount(itemConsumeId, 0) < itemConsumeCount)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
				sm.addItemName(itemConsumeId);
				targetChar.sendPacket(sm);
				return;
			}
			targetChar.getInventory().destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, summonerChar, targetChar);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
			sm.addItemName(itemConsumeId);
			targetChar.sendPacket(sm);
		}
		// Set correct instance id
		targetChar.setInstanceId(summonerChar.getInstanceId());
		targetChar.setIsIn7sDungeon(summonerChar.isIn7sDungeon());
		targetChar.teleToLocation(summonerChar.getX(), summonerChar.getY(), summonerChar.getZ(), true);
	}

	@Override
	public void useSkill(L2Character activeChar, final L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2Player))
			return; // currently not implemented for others

		final L2Player activePlayer = (L2Player) activeChar;
		if (!checkSummonerStatus(activePlayer))
			return;

		for (L2Character element : targets)
		{
			if (!(element instanceof L2Player))
				continue;
			
			if (activeChar == element)
				continue;

			final L2Player target = (L2Player) element;

			if (!checkTargetStatus(target, activePlayer))
				continue;

			if (!Util.checkIfInRange(0, activeChar, target, false))
			{
				if (target.hasActiveConfirmDlg())
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_ALREADY_SUMMONED);
					sm.addString(target.getName());
					activePlayer.sendPacket(sm);
					continue;
				}

				if (skill.getId() == 1403) //summon friend
				{
					// Send message
					ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT);
					confirm.addCharName(activeChar);
					confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
					confirm.addTime(30000);
					//confirm.addRequesterId(activePlayer.getCharId());
					confirm.addAnswerHandler(new AnswerHandler() {
						@Override
						public void handle(boolean answer)
						{
							if (answer)
								teleToTarget(target, activePlayer, skill);
						}
					});
					target.sendPacket(confirm);
				}
				else
				{
					teleToTarget(target, activePlayer, skill);
				}
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
