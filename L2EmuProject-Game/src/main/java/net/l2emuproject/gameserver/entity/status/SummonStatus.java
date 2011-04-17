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

import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Playable;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2SummonInstance;

public final class SummonStatus extends CharStatus
{
	public SummonStatus(L2SummonInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	void reduceHp0(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isConsume)
	{
		if (attacker == null || getActiveChar().isDead())
			return;

		//final L2Player attackerPlayer = attacker.getActingPlayer();
		//if (attackerPlayer != null && (getActiveChar().getOwner() == null || getActiveChar().getOwner().getDuelId() != attackerPlayer.getDuelId()))
		//attackerPlayer.setDuelState(Duel.DUELSTATE_INTERRUPTED);

		if (getActiveChar().getOwner().getParty() != null)
		{
			final L2Player caster = getActiveChar().getTransferingDamageTo();
			if (caster != null && getActiveChar().getParty() != null && Util.checkIfInRange(1000, getActiveChar(), caster, true) && !caster.isDead()
					&& getActiveChar().getOwner() != caster && getActiveChar().getParty().getPartyMembers().contains(caster))
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
				}
			}
		}
		super.reduceHp(value, attacker, awake, isDOT, isConsume);
	}

	@Override
	public L2SummonInstance getActiveChar()
	{
		return (L2SummonInstance) _activeChar;
	}
}
