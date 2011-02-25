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

import java.util.concurrent.ScheduledFuture;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.actor.stat.PcStat;
import net.l2emuproject.gameserver.network.serverpackets.ExVitalityPointInfo;
import net.l2emuproject.gameserver.world.zone.L2Zone;

public final class PlayerVitality extends PlayerExtension
{
	/** Vitality recovery task */
	private ScheduledFuture<?>	_vitalityTask;

	public PlayerVitality(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	private final class VitalityTask implements Runnable
	{
		private VitalityTask()
		{
		}

		@Override
		public final void run()
		{
			if (getPlayer() == null)
				return;

			if (!getPlayer().isInsideZone(L2Zone.FLAG_PEACE))
				return;

			if (getPlayer().getPlayerVitality().getVitalityPoints() >= PcStat.MAX_VITALITY_POINTS)
				return;

			getPlayer().getPlayerVitality().updateVitalityPoints(Config.RATE_RECOVERY_VITALITY_PEACE_ZONE, false, false);
			getPlayer().sendPacket(new ExVitalityPointInfo(getVitalityPoints()));
		}
	}

	public final void startVitalityTask()
	{
		if (Config.ENABLE_VITALITY && _vitalityTask == null)
		{
			_vitalityTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new VitalityTask(), 1000, 60000);
		}
	}

	public final void stopVitalityTask()
	{
		if (_vitalityTask != null)
		{
			_vitalityTask.cancel(true);
			_vitalityTask = null;
		}
	}

	public final int getVitalityPoints()
	{
		return getPlayer().getStat().getVitalityPoints();
	}

	public final void setVitalityPoints(int points, boolean quiet)
	{
		getPlayer().getStat().setVitalityPoints(points, quiet);
	}

	public synchronized final void updateVitalityPoints(float points, boolean useRates, boolean quiet)
	{
		getPlayer().getStat().updateVitalityPoints(points, useRates, quiet);
	}
}
