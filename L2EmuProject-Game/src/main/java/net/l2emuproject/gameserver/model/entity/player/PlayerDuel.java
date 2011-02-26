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
package net.l2emuproject.gameserver.model.entity.player;

import net.l2emuproject.gameserver.events.global.siege.SiegeManager;
import net.l2emuproject.gameserver.model.restriction.global.DuelRestriction;
import net.l2emuproject.gameserver.model.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.duel.Duel;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.zone.L2Zone;

public final class PlayerDuel extends PlayerExtension
{
	private int		_duelState		= Duel.DUELSTATE_NODUEL;
	private boolean	_isInDuel		= false;
	private int		_duelId			= 0;
	private int		_noDuelReason	= 0;

	public PlayerDuel(L2Player activeChar)
	{
		super(activeChar);
	}

	public final boolean isInDuel()
	{
		return _isInDuel;
	}

	public final int getDuelId()
	{
		return _duelId;
	}

	public final void setDuelState(int mode)
	{
		_duelState = mode;
	}

	public final int getDuelState()
	{
		return _duelState;
	}

	/**
	 * Sets up the duel state using a non 0 duelId.
	 * @param duelId 0=not in a duel
	 */
	public final void setIsInDuel(int duelId)
	{
		if (duelId > 0)
		{
			_isInDuel = true;
			_duelState = Duel.DUELSTATE_DUELLING;
			_duelId = duelId;
		}
		else
		{
			if (_duelState == Duel.DUELSTATE_DEAD)
			{
				getPlayer().enableAllSkills();
				getPlayer().getStatus().startHpMpRegeneration();
			}
			_isInDuel = false;
			_duelState = Duel.DUELSTATE_NODUEL;
			_duelId = 0;
		}
	}

	/**
	 * This returns a SystemMessage stating why
	 * the player is not available for duelling.
	 * @return S1_CANNOT_DUEL... message
	 */
	public final SystemMessage getNoDuelReason()
	{
		// This is somewhat hacky - but that case should never happen anyway...
		if (_noDuelReason == 0)
			_noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL.getId();

		SystemMessage sm = new SystemMessage(SystemMessageId.getSystemMessageId(_noDuelReason));
		sm.addPcName(getPlayer());
		_noDuelReason = 0;
		return sm;
	}

	/**
	 * Checks if this player might join / start a duel.
	 * To get the reason use getNoDuelReason() after calling this function.
	 * @return true if the player might join/start a duel.
	 */
	public final boolean canDuel()
	{
		if (getPlayer().isInCombat() || getPlayer().isInJail())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE.getId();
			return false;
		}
		if (getPlayer().isDead()
				|| getPlayer().isAlikeDead()
				|| (getPlayer().getStatus().getCurrentHp() < getPlayer().getStat().getMaxHp() / 2 || getPlayer().getStatus().getCurrentMp() < getPlayer()
						.getStat().getMaxMp() / 2))
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_HP_OR_MP_IS_BELOW_50_PERCENT.getId();
			return false;
		}
		if (isInDuel())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL.getId();
			return false;
		}
		if (getPlayer().getPlayerOlympiad().isInOlympiadMode())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD.getId();
			return false;
		}
		if (getPlayer().isCursedWeaponEquipped())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE.getId();
			return false;
		}
		if (getPlayer().getPrivateStoreType() != 0)
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_C1_IS_IN_A_PRIVATE_STORE_OR_MANUFACTURE.getId();
			return false;
		}
		if (getPlayer().isMounted() || getPlayer().isInBoat())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER.getId();
			return false;
		}
		if (getPlayer().getPlayerFish().isFishing())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING.getId();
			return false;
		}
		if (getPlayer().isInsideZone(L2Zone.FLAG_PVP) || getPlayer().isInsideZone(L2Zone.FLAG_PEACE) || SiegeManager.getInstance().checkIfInZone(getPlayer()))
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_DUEL_PROHIBITED_AREA.getId();
			return false;
		}
		if (GlobalRestrictions.isRestricted(getPlayer(), DuelRestriction.class))
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE.getId(); // TODO
			return false;
		}
		return true;
	}
}
