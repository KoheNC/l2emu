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

import net.l2emuproject.config.L2Properties;
import net.l2emuproject.gameserver.Announcements;
import net.l2emuproject.gameserver.PersistentProperties;
import net.l2emuproject.gameserver.datatables.DoorTable;
import net.l2emuproject.gameserver.model.actor.instance.L2DoorInstance;
import net.l2emuproject.gameserver.taskmanager.AbstractPeriodicTaskManager;

/**
 * @author lord_rex
 */
public final class HellboundEngine extends AbstractPeriodicTaskManager
{
	private static final class SingletonHolder
	{
		private static final HellboundEngine	INSTANCE	= new HellboundEngine();
	}

	public static HellboundEngine getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public HellboundEngine()
	{
		super(15000);
	}

	private static final int		TRUST_POINTS_0_1	= 300000;
	private static final int		TRUST_POINTS_1_2	= 600000;
	private static final int		TRUST_POINTS_2_3	= 1000000;
	private static final int		TRUST_POINTS_3_4	= 1030000;
	private static final int		TRUST_POINTS_4_5	= 1060000;
	private static final int		TRUST_POINTS_5_6	= 1090000;
	private static final int		TRUST_POINTS_6_7	= 1110000;
	private static final int		TRUST_POINTS_7_8	= 1140000;
	private static final int		TRUST_POINTS_8_9	= 2000000;

	private static final int[][]	DOOR_LIST			=
														{
														{ 19250001, 5 },
														{ 19250002, 5 },
														{ 20250001, 9 },
														{ 20250002, 7 } };

	private static final String		ANNOUNCE			= "Hellbound now has reached level: %lvl%";

	private int						_cachedLevel		= -1;

	@Override
	public final void run()
	{
		int level = HellboundManager.getInstance().getHellboundLevel();
		if (level == _cachedLevel)
		{
			boolean nextLevel = false;
			switch (level)
			{
				case 0:
					if (HellboundManager.getInstance().getTrustPoints() == TRUST_POINTS_0_1)
						nextLevel = true;
					break;
				case 1:
					if (HellboundManager.getInstance().getTrustPoints() == TRUST_POINTS_1_2)
						nextLevel = true;
					break;
				case 2:
					if (HellboundManager.getInstance().getTrustPoints() == TRUST_POINTS_2_3)
						nextLevel = true;
					break;
				case 3:
					if (HellboundManager.getInstance().getTrustPoints() == TRUST_POINTS_3_4)
						nextLevel = true;
					break;
				case 4:
					if (HellboundManager.getInstance().getTrustPoints() == TRUST_POINTS_4_5)
						nextLevel = true;
					break;
				case 5:
					if (HellboundManager.getInstance().getTrustPoints() == TRUST_POINTS_5_6)
						nextLevel = true;
					break;
				case 6:
					if (HellboundManager.getInstance().getTrustPoints() == TRUST_POINTS_6_7)
						nextLevel = true;
					break;
				case 7:
					if (HellboundManager.getInstance().getTrustPoints() == TRUST_POINTS_7_8)
						nextLevel = true;
					break;
				case 8:
					if (HellboundManager.getInstance().getTrustPoints() == TRUST_POINTS_8_9)
						nextLevel = true;
					break;
				default:
					nextLevel = false;
					break;
			}

			if (nextLevel)
			{
				level++;
				onLevelChange(level);
			}
		}
		else
			onLevelChange(level); // first run or changed by admin
	}

	private final void onLevelChange(int newLevel)
	{
		updateHellboundLevel();
		HellboundManager.getInstance().doSpawn();

		for (final int[] doorData : DOOR_LIST)
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
		{
			Announcements.getInstance().announceToAll(ANNOUNCE.replace("%lvl%", String.valueOf(newLevel)));
			_log.info("HellboundManager : " + ANNOUNCE.replace("%lvl%", String.valueOf(newLevel)));
		}

		_cachedLevel = newLevel;
	}

	private final void updateHellboundLevel()
	{
		final int trustPoints = HellboundManager.getInstance().getTrustPoints();

		if (trustPoints < TRUST_POINTS_0_1)
			HellboundManager.getInstance().setHellboundLevel(0);
		else if (trustPoints >= TRUST_POINTS_0_1 && trustPoints < TRUST_POINTS_1_2)
			HellboundManager.getInstance().setHellboundLevel(1);
		else if (trustPoints >= TRUST_POINTS_1_2 && trustPoints < TRUST_POINTS_2_3)
			HellboundManager.getInstance().setHellboundLevel(2);
		else if (trustPoints >= TRUST_POINTS_2_3 && trustPoints < TRUST_POINTS_3_4)
			HellboundManager.getInstance().setHellboundLevel(3);
		else if (trustPoints >= TRUST_POINTS_3_4 && trustPoints < TRUST_POINTS_4_5)
			HellboundManager.getInstance().setHellboundLevel(4);
		else if (trustPoints >= TRUST_POINTS_4_5 && trustPoints < TRUST_POINTS_5_6)
			HellboundManager.getInstance().setHellboundLevel(5);
		else if (trustPoints >= TRUST_POINTS_5_6 && trustPoints < TRUST_POINTS_6_7)
			HellboundManager.getInstance().setHellboundLevel(6);
		else if (trustPoints >= TRUST_POINTS_6_7 && trustPoints < TRUST_POINTS_7_8)
			HellboundManager.getInstance().setHellboundLevel(7);
		else if (trustPoints >= TRUST_POINTS_7_8 && trustPoints < TRUST_POINTS_8_9)
			HellboundManager.getInstance().setHellboundLevel(8);
		else if (trustPoints >= TRUST_POINTS_8_9)
			HellboundManager.getInstance().setHellboundLevel(9);

		final L2Properties props = PersistentProperties.getProperties(HellboundManager.class);
		HellboundManager.getInstance().saveProperties();
		HellboundManager.getInstance().loadProperties(props);
	}
}
