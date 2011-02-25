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
package net.l2emuproject.gameserver.model.actor.instance;

import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Watcher;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author lord_rex
 */
public final class L2RedWatcherInstance extends L2Watcher
{
	private static final int[]	DEBUFFS	=
										{ 4104, 4034, 4035 };

	public L2RedWatcherInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected final void handleWatcherAI()
	{
		for (L2PcInstance player : getKnownList().getKnownPlayersInRadius(1000))
		{
			if (player == null)
				continue;

			handleCast(this, player, DEBUFFS[Rnd.get(0, 2)]);
		}
	}
}
