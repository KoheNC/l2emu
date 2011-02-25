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
package net.l2emuproject.gameserver.instancemanager;

import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.network.L2GameClient;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class AntiFeedManager
{
	private Map<Integer, Long>	_lastDeathTimes;

	private static final class SingletonHolder
	{
		private static final AntiFeedManager	INSTANCE	= new AntiFeedManager();
	}

	public static final AntiFeedManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private AntiFeedManager()
	{
		_lastDeathTimes = new FastMap<Integer, Long>().shared();
	}

	/**
	 * Set time of the last player's death to current
	 * @param objectId Player's objectId
	 */
	public final void setLastDeathTime(int objectId)
	{
		_lastDeathTimes.put(objectId, System.currentTimeMillis());
	}

	/**
	 * Check if current kill should be counted as non-feeded.
	 * @param attacker Attacker character
	 * @param target Target character
	 * @return True if kill is non-feeded.
	 */
	public final boolean check(L2Character attacker, L2Character target)
	{
		if (!Config.ALT_ANTIFEED_ENABLE)
		{
			return true;
		}

		if (target == null)
			return false;

		final L2Player targetPlayer = target.getActingPlayer();
		if (targetPlayer == null)
			return false;

		if (Config.ALT_ANTIFEED_INTERVAL > 0 && _lastDeathTimes.containsKey(targetPlayer.getObjectId()))
			return (System.currentTimeMillis() - _lastDeathTimes.get(targetPlayer.getObjectId())) > Config.ALT_ANTIFEED_INTERVAL;

		if (Config.ALT_ANTIFEED_DUALBOX && attacker != null)
		{
			final L2Player attackerPlayer = attacker.getActingPlayer();
			if (attackerPlayer == null)
				return false;

			final L2GameClient targetClient = targetPlayer.getClient();
			final L2GameClient attackerClient = attackerPlayer.getClient();
			if (targetClient == null || attackerClient == null || targetClient.getActiveChar().isInOfflineMode()
					|| attackerClient.getActiveChar().isInOfflineMode())
				// unable to check ip address
				return !Config.ALT_ANTIFEED_DISCONNECTED_AS_DUALBOX;

			return !targetClient.getHostAddress().equals(attackerClient.getHostAddress());
		}

		return true;
	}
}