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

import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.network.serverpackets.GMHide;
import net.l2emuproject.gameserver.network.serverpackets.ObservationMode;
import net.l2emuproject.gameserver.network.serverpackets.ObservationReturn;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class PlayerObserver extends PlayerExtension
{
	private int		_obsX;
	private int		_obsY;
	private int		_obsZ;
	private boolean	_observerMode	= false;

	public PlayerObserver(L2Player activeChar)
	{
		super(activeChar);
	}

	public final void enterObserverMode(int x, int y, int z)
	{
		_obsX = getPlayer().getX();
		_obsY = getPlayer().getY();
		_obsZ = getPlayer().getZ();

		getPlayer().setTarget(null);
		getPlayer().stopMove(null);
		getPlayer().startParalyze();
		getPlayer().setIsInvul(true);
		getPlayer().getAppearance().setInvisible();
		getPlayer().sendPacket(GMHide.ENABLE);
		getPlayer().sendPacket(new ObservationMode(x, y, z));
		getPlayer().getPosition().setXYZ(x, y, z);

		_observerMode = true;

		getPlayer().updateInvisibilityStatus();
	}

	public final void leaveObserverMode()
	{
		getPlayer().setTarget(null);
		getPlayer().getPosition().setXYZ(_obsX, _obsY, _obsZ);
		getPlayer().sendPacket(GMHide.DISABLE);
		getPlayer().stopParalyze(false);

		if (!getPlayer().isGM())
		{
			getPlayer().getAppearance().setVisible();
			getPlayer().setIsInvul(false);
		}

		if (getPlayer().getAI() != null)
			getPlayer().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		getPlayer().teleToLocation(_obsX, _obsY, _obsZ);
		getPlayer().sendPacket(new ObservationReturn(getPlayer()));
		_observerMode = false;
		getPlayer().broadcastUserInfo();
	}

	public final int getObsX()
	{
		return _obsX;
	}

	public final int getObsY()
	{
		return _obsY;
	}

	public final int getObsZ()
	{
		return _obsZ;
	}

	public final boolean inObserverMode()
	{
		return _observerMode;
	}

	public final void setIsInObserverMode(boolean b)
	{
		_observerMode = b;
	}

	public final void saveCoords()
	{
		_obsX = getPlayer().getX();
		_obsY = getPlayer().getY();
		_obsZ = getPlayer().getZ();
	}
}
