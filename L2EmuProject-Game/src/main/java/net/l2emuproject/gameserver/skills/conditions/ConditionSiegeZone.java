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
package net.l2emuproject.gameserver.skills.conditions;

import net.l2emuproject.gameserver.events.global.clanhallsiege.ClanHall;
import net.l2emuproject.gameserver.events.global.clanhallsiege.ClanHallManager;
import net.l2emuproject.gameserver.events.global.fortsiege.Fort;
import net.l2emuproject.gameserver.events.global.fortsiege.FortManager;
import net.l2emuproject.gameserver.events.global.siege.Castle;
import net.l2emuproject.gameserver.events.global.siege.CastleManager;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Gigiikun
 */
final class ConditionSiegeZone extends Condition
{
	//	conditional values
	public final static int	COND_NOT_ZONE		= 0x0001;
	public final static int	COND_CAST_ATTACK	= 0x0002;
	public final static int	COND_CAST_DEFEND	= 0x0004;
	public final static int	COND_CAST_NEUTRAL	= 0x0008;
	public final static int	COND_FORT_ATTACK	= 0x0010;
	public final static int	COND_FORT_DEFEND	= 0x0020;
	public final static int	COND_FORT_NEUTRAL	= 0x0040;

	private final int		_value;
	private final boolean	_self;

	public ConditionSiegeZone(int value, boolean self)
	{
		_value = value;
		_self = self;
	}

	@Override
	public boolean testImpl(Env env)
	{
		L2Character target = _self ? env.getPlayer() : env.getTarget();
		Castle castle = CastleManager.getInstance().getCastle(target);
		Fort fort = FortManager.getInstance().getFort(target);
		ClanHall ch = ClanHallManager.getInstance().getClanHall(target);

		if (castle == null && fort == null && (ch == null || ch.getSiege() == null))
		{
			if ((_value & COND_NOT_ZONE) != 0)
				return true;
			else
				return false;
		}
		if (castle != null)
			return checkIfOk(target, castle, _value);
		else if (fort != null)
			return checkIfOk(target, fort, _value);
		else
			return checkIfOk(target, ch, _value);
	}

	private boolean checkIfOk(L2Character activeChar, Castle castle, int value)
	{
		if (activeChar == null || !(activeChar instanceof L2Player))
			return false;

		L2Player player = (L2Player) activeChar;

		if ((castle == null || castle.getCastleId() <= 0))
		{
			if ((value & COND_NOT_ZONE) != 0)
				return true;
		}
		else if (!castle.getSiege().getIsInProgress())
		{
			if ((value & COND_NOT_ZONE) != 0)
				return true;
		}
		else if (castle.getSiege().getAttackerClan(player.getClan()) != null && (value & COND_CAST_ATTACK) != 0
				&& player.isRegisteredOnThisSiegeField(castle.getCastleId()) && player.getSiegeState() == L2Player.SIEGE_STATE_ATTACKER)
			return true;
		else if (castle.getSiege().getDefenderClan(player.getClan()) != null && (value & COND_CAST_DEFEND) != 0
				&& player.isRegisteredOnThisSiegeField(castle.getCastleId()) && player.getSiegeState() == L2Player.SIEGE_STATE_DEFENDER)
			return true;
		else if (castle.getSiege().getAttackerClan(player.getClan()) == null && castle.getSiege().getDefenderClan(player.getClan()) == null
				&& (value & COND_CAST_NEUTRAL) != 0 && player.getSiegeState() == L2Player.SIEGE_STATE_NOT_INVOLVED)
			return true;

		return false;
	}

	private boolean checkIfOk(L2Character activeChar, Fort fort, int value)
	{
		if (activeChar == null || !(activeChar instanceof L2Player))
			return false;

		L2Player player = (L2Player) activeChar;

		if ((fort == null || fort.getFortId() <= 0))
		{
			if ((value & COND_NOT_ZONE) != 0)
				return true;
		}
		else if (!fort.getSiege().getIsInProgress())
		{
			if ((value & COND_NOT_ZONE) != 0)
				return true;
		}
		else if (fort.getSiege().getAttackerClan(player.getClan()) != null && (value & COND_FORT_ATTACK) != 0
				&& player.isRegisteredOnThisSiegeField(fort.getFortId()) && player.getSiegeState() == L2Player.SIEGE_STATE_ATTACKER)
			return true;
		else if (fort.getSiege().getDefenderClan(player.getClan()) != null && (value & COND_FORT_DEFEND) != 0
				&& player.isRegisteredOnThisSiegeField(fort.getFortId()) && player.getSiegeState() == L2Player.SIEGE_STATE_DEFENDER)
			return true;
		else if (fort.getSiege().getAttackerClan(player.getClan()) == null && fort.getSiege().getDefenderClan(player.getClan()) == null
				&& (value & COND_FORT_NEUTRAL) != 0 && player.getSiegeState() == L2Player.SIEGE_STATE_NOT_INVOLVED)
			return true;

		return false;
	}

	private boolean checkIfOk(L2Character activeChar, ClanHall ch, int value)
	{
		if (activeChar == null || !(activeChar instanceof L2Player))
			return false;

		L2Player player = activeChar.getActingPlayer();

		if (ch == null || ch.getSiege() == null)
		{
			if ((value & COND_NOT_ZONE) != 0)
				return true;
		}
		else if (!ch.getSiege().getIsInProgress())
		{
			if ((value & COND_NOT_ZONE) != 0)
				return true;
		}
		else if (ch.getSiege().getAttackerClan(player.getClan()) != null && (value & COND_CAST_ATTACK) != 0)
			return true;
		else if (ch.getSiege().getDefenderClan(player.getClan()) != null && (value & COND_CAST_DEFEND) != 0)
			return true;
		else if (ch.getSiege().getAttackerClan(player.getClan()) == null && ch.getSiege().getDefenderClan(player.getClan()) == null
				&& (value & COND_CAST_NEUTRAL) != 0)
			return true;

		return false;
	}
}
