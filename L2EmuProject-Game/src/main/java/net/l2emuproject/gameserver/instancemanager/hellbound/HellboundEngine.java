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
package net.l2emuproject.gameserver.instancemanager.hellbound;

import net.l2emuproject.gameserver.Announcements;
import net.l2emuproject.gameserver.datatables.DoorTable;
import net.l2emuproject.gameserver.model.actor.instance.L2DoorInstance;

/**
 * @author Gigiikun, L2JNightFall Team
 *	<br>Remade for L2EmuProject by lord_rex
 */
public final class HellboundEngine implements Runnable
{
	private static final int[][]	DOOR_LIST		=
													{
													{ 19250001, 5 },
													{ 19250002, 5 },
													{ 20250001, 9 },
													{ 20250002, 7 } };

	private static final int[]		MAX_TRUST		=
													{ 0, 300000, 600000, 1000000, 0 };

	private static final String		ANNOUNCE		= "Hellbound now has reached level: %lvl%";

	private int						_cachedLevel	= -1;

	private final void onLevelChange(int newLevel)
	{
		try
		{
			HellboundManager.getInstance().setMaxTrustPoints(MAX_TRUST[newLevel]);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			HellboundManager.getInstance().setMaxTrustPoints(0);
		}

		HellboundManager.getInstance().doSpawn();

		for (int[] doorData : DOOR_LIST)
		{
			try
			{
				L2DoorInstance door = DoorTable.getInstance().getDoor(doorData[0]);
				if (door.isOpen())
				{
					if (newLevel < doorData[1])
						door.closeMe();
				}
				else
				{
					if (newLevel >= doorData[1])
						door.openMe();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (_cachedLevel >= 0)
			Announcements.getInstance().announceToAll(ANNOUNCE.replace("%lvl%", String.valueOf(newLevel)));

		_cachedLevel = newLevel;
	}

	@Override
	public final void run()
	{
		int level = HellboundManager.getInstance().getHellboundLevel();
		if (level == _cachedLevel)
		{
			boolean nextLevel = false;
			switch (level)
			{
				case 1:
				case 2:
				case 3:
					if (HellboundManager.getInstance().getTrustPoints() == HellboundManager.getInstance().getMaxTrustPoints())
						nextLevel = true;
				default:
			}

			if (nextLevel)
			{
				level++;
				HellboundManager.getInstance().setHellboundLevel(level);
				onLevelChange(level);
			}
		}
		else
			onLevelChange(level); // first run or changed by admin
	}
}
