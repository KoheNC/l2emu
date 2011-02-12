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
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.serverpackets.GMHide;
import net.l2emuproject.gameserver.network.serverpackets.ObservationMode;
import net.l2emuproject.gameserver.network.serverpackets.ObservationReturn;

public class PlayerObserver extends PlayerExtension
{
	private int		_obsX;
	private int		_obsY;
	private int		_obsZ;
	private boolean	_observerMode	= false;

	public PlayerObserver(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	public void enterObserverMode(int x, int y, int z)
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

	public void leaveObserverMode()
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

	public int getObsX()
	{
		return _obsX;
	}

	public int getObsY()
	{
		return _obsY;
	}

	public int getObsZ()
	{
		return _obsZ;
	}

	public boolean inObserverMode()
	{
		return _observerMode;
	}

	public void setIsInObserverMode(boolean b)
	{
		_observerMode = b;
	}

	public void saveCoords()
	{
		_obsX = getPlayer().getX();
		_obsY = getPlayer().getY();
		_obsZ = getPlayer().getZ();
	}
}
