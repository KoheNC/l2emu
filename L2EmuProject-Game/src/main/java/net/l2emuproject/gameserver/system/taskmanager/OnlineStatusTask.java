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
package net.l2emuproject.gameserver.system.taskmanager;

import java.util.Collection;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author lord_rex
 */
public final class OnlineStatusTask extends AbstractPeriodicTaskManager
{
	private static final class SingletonHolder
	{
		private static final OnlineStatusTask	INSTANCE	= new OnlineStatusTask();
	}

	public static OnlineStatusTask getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private OnlineStatusTask()
	{
		super(Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL);
	}

	@Override
	public final void run()
	{
		final Collection<L2Player> players = L2World.getInstance().getAllPlayers();

		// Announce just if there are more than 2 players on server.
		if (players.isEmpty() || players.size() == 1)
			return;

		Announcements.getInstance().announceToAll("Online players: " + players.size());
	}
}
