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
package net.l2emuproject.gameserver.entity.status;

import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Player.ConditionListenerDependency;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.object.instance.L2SummonInstance;
import net.l2emuproject.lang.L2Math;

public final class PcStatus extends CharStatus
{
	private double	_currentCp	= 0;

	public PcStatus(L2Player activeChar)
	{
		super(activeChar);
	}

	@Override
	public final double getCurrentCp()
	{
		return _currentCp;
	}

	@Override
	protected boolean setCurrentCp0(double newCp)
	{
		double maxCp = getActiveChar().getStat().getMaxCp();
		if (newCp < 0)
			newCp = 0;

		boolean requireRegen;

		synchronized (this)
		{
			final double currentCp = _currentCp;

			if (newCp >= maxCp)
			{
				_currentCp = maxCp;
				requireRegen = false;
			}
			else
			{
				_currentCp = newCp;
				requireRegen = true;
			}

			if (currentCp != _currentCp)
				getActiveChar().broadcastStatusUpdate();
		}

		return requireRegen;
	}

	@Override
	protected boolean setCurrentHp0(double newHp)
	{
		boolean requireRegen = super.setCurrentHp0(newHp);

		if (getCurrentHp() <= getActiveChar().getStat().getMaxHp() * 0.3)
		{
			QuestState qs = getActiveChar().getQuestState("_255_Tutorial");
			if (qs != null)
				qs.getQuest().notifyEvent("CE45", null, getActiveChar());
		}

		getActiveChar().refreshConditionListeners(ConditionListenerDependency.PLAYER_HP);

		return requireRegen;
	}

	@Override
	void reduceHp0(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isConsume)
	{
		if (!isConsume)
		{
			if (awake && getActiveChar().isSleeping())
				getActiveChar().stopSleeping(true);

			if (getActiveChar().isSitting())
				getActiveChar().standUp();

			if (getActiveChar().isFakeDeath())
				getActiveChar().stopFakeDeath(true);
		}

		double realValue = value;
		double tDmg = 0;
		int mpDam = 0;

		if (attacker != null && attacker != getActiveChar())
		{
			// Check and calculate transfered damage
			L2Summon summon = getActiveChar().getPet();

			if (summon instanceof L2SummonInstance && Util.checkIfInRange(1000, getActiveChar(), summon, true))
			{
				tDmg = value * getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;

				// Only transfer dmg up to current HP, it should not be killed
				tDmg = L2Math.limit(0, tDmg, summon.getStatus().getCurrentHp() - 1);

				if (tDmg > 0)
				{
					summon.reduceCurrentHp(tDmg, attacker, null);
					value -= tDmg;
					realValue = value;
				}
			}

			mpDam = (int) value * (int) getActiveChar().getStat().calcStat(Stats.MANA_SHIELD_PERCENT, 0, null, null) / 100;

			if (mpDam > 0)
			{
				mpDam = (int) (value - mpDam);
				if (mpDam > getActiveChar().getCurrentMp())
				{
					getActiveChar().sendPacket(SystemMessageId.MP_BECAME_0_ARCANE_SHIELD_DISAPPEARING);
					getActiveChar().getFirstEffect(1556).exit();
					value = mpDam - getActiveChar().getCurrentMp();
					setCurrentMp(0);
				}
				else
				{
					getActiveChar().reduceCurrentMp(mpDam);
					SystemMessage smsg = new SystemMessage(SystemMessageId.ARCANE_SHIELD_DECREASED_YOUR_MP_BY_S1_INSTEAD_OF_HP);
					smsg.addNumber(mpDam);
					getActiveChar().sendPacket(smsg);
					return;
				}
			}

			if (attacker instanceof L2Playable)
			{
				if (getCurrentCp() >= value)
				{
					setCurrentCp(getCurrentCp() - value); // Set Cp to diff of Cp vs value
					value = 0; // No need to subtract anything from Hp
				}
				else
				{
					value -= getCurrentCp(); // Get diff from value vs Cp; will apply diff to Hp
					setCurrentCp(0); // Set Cp to 0
				}
			}
		}

		final L2Player caster = getActiveChar().getTransferingDamageTo();
		if (caster != null && getActiveChar().getParty() != null && Util.checkIfInRange(1000, getActiveChar(), caster, true) && !caster.isDead()
				&& getActiveChar() != caster && getActiveChar().getParty().getPartyMembers().contains(caster))
		{
			int transferDmg = 0;

			transferDmg = (int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_TO_PLAYER, 0, null, null) / 100;
			transferDmg = Math.min((int) caster.getCurrentHp() - 1, transferDmg);
			if (transferDmg > 0 && attacker instanceof L2Playable)
			{
				int membersInRange = 0;
				for (L2Player member : caster.getParty().getPartyMembers())
				{
					if (Util.checkIfInRange(1000, member, caster, false) && member != caster)
						membersInRange++;
				}

				if (caster.getCurrentCp() > 0)
				{
					if (caster.getCurrentCp() > transferDmg)
						reduceCp(transferDmg);
					else
					{
						transferDmg = (int) (transferDmg - caster.getCurrentCp());
						reduceCp((int) caster.getCurrentCp());
					}
				}

				caster.reduceCurrentHp(transferDmg / membersInRange, attacker, null);
				value -= transferDmg;
				realValue = (int) value;
			}
		}

		super.reduceHp0(value, attacker, awake, isDOT, isConsume);

		if (attacker != getActiveChar() && realValue > 0)
		{
			SystemMessage smsg = new SystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_C2);
			smsg.addPcName(getActiveChar());
			smsg.addCharName(attacker);
			smsg.addNumber((int) realValue);
			getActiveChar().sendPacket(smsg);
		}

		// Notify the tamed beast of attacks
		if (getActiveChar().getTrainedBeast() != null)
			getActiveChar().getTrainedBeast().onOwnerGotAttacked(attacker);
	}

	@Override
	public L2Player getActiveChar()
	{
		return (L2Player) _activeChar;
	}
}
