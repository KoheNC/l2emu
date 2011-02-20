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

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.model.actor.instance.L2CubicInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.olympiad.Olympiad;
import net.l2emuproject.gameserver.model.world.Location;
import net.l2emuproject.gameserver.network.serverpackets.ExOlympiadMode;
import net.l2emuproject.gameserver.network.serverpackets.GMHide;

public final class PlayerOlympiad extends PlayerExtension
{
	private boolean	_inOlympiadMode		= false;
	private boolean	_olympiadStart		= false;
	private int		_olympiadGameId		= -1;
	private int		_olympiadSide		= -1;
	private int		_olympiadOpponentId	= 0;
	public int		olyBuff				= 0;

	public PlayerOlympiad(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	public final void leaveOlympiadObserverMode()
	{
		getPlayer().setTarget(null);
		getPlayer().sendPacket(ExOlympiadMode.RETURN);
		getPlayer().teleToLocation(getPlayer().getPlayerObserver().getObsX(), getPlayer().getPlayerObserver().getObsY(),
				getPlayer().getPlayerObserver().getObsZ());
		getPlayer().sendPacket(GMHide.DISABLE);
		if (!getPlayer().isGM())
		{
			getPlayer().getAppearance().setVisible();
			getPlayer().setIsInvul(false);
		}
		if (getPlayer().getAI() != null)
		{
			getPlayer().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		Olympiad.removeSpectator(_olympiadGameId, getPlayer());
		_olympiadGameId = -1;
		getPlayer().getPlayerObserver().setIsInObserverMode(false);
		getPlayer().broadcastUserInfo();
	}

	public final void enterOlympiadObserverMode(Location loc, int id, boolean storeCoords)
	{
		if (getPlayer().getPet() != null)
			getPlayer().getPet().unSummon(getPlayer());

		if (!getPlayer().getCubics().isEmpty())
		{
			for (L2CubicInstance cubic : getPlayer().getCubics().values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}

			getPlayer().getCubics().clear();
		}

		if (getPlayer().getParty() != null)
			getPlayer().getParty().removePartyMember(getPlayer());

		_olympiadGameId = id;
		if (getPlayer().isSitting())
			getPlayer().standUp();
		if (storeCoords)
		{
			getPlayer().getPlayerObserver().saveCoords();
		}
		getPlayer().setTarget(null);
		getPlayer().setIsInvul(true);
		getPlayer().getAppearance().setInvisible();
		getPlayer().teleToLocation(loc, false);
		getPlayer().sendPacket(GMHide.ENABLE);
		getPlayer().sendPacket(ExOlympiadMode.SPECTATE);
		getPlayer().getPlayerObserver().setIsInObserverMode(true);

		getPlayer().updateInvisibilityStatus();
	}

	public final void setOlympiadSide(int i)
	{
		_olympiadSide = i;
	}

	public final int getOlympiadSide()
	{
		return _olympiadSide;
	}

	public final void setOlympiadGameId(int id)
	{
		_olympiadGameId = id;
	}

	public final int getOlympiadGameId()
	{
		return _olympiadGameId;
	}

	public final int getOlympiadOpponentId()
	{
		return _olympiadOpponentId;
	}

	public final void setOlympiadOpponentId(int value)
	{
		_olympiadOpponentId = value;
	}

	public final boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}

	public final void setIsInOlympiadMode(boolean b)
	{
		_inOlympiadMode = b;
	}

	public final void setIsOlympiadStart(boolean b)
	{
		_olympiadStart = b;
	}

	public final boolean isOlympiadStart()
	{
		return _olympiadStart;
	}
}
