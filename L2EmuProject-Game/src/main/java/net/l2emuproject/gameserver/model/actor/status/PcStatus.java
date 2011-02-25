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
package net.l2emuproject.gameserver.model.actor.status;

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance.ConditionListenerDependency;
import net.l2emuproject.gameserver.model.actor.instance.L2SummonInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.util.Util;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.lang.L2Math;


public final class PcStatus extends CharStatus
{
	private double _currentCp = 0;
	
	public PcStatus(L2PcInstance activeChar)
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
		
		if (attacker != null && attacker != getActiveChar())
		{
			// Check and calculate transfered damage
			L2Summon summon = getActiveChar().getPet();
			
			if (summon instanceof L2SummonInstance && Util.checkIfInRange(900, getActiveChar(), summon, true))
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
	public L2PcInstance getActiveChar()
	{
		return (L2PcInstance)_activeChar;
	}
}
