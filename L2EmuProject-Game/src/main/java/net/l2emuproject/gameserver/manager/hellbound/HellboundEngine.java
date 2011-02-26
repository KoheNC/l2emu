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
package net.l2emuproject.gameserver.manager.hellbound;

import net.l2emuproject.config.L2Properties;
import net.l2emuproject.gameserver.config.PersistentProperties;
import net.l2emuproject.gameserver.datatables.DoorTable;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.system.taskmanager.AbstractPeriodicTaskManager;
import net.l2emuproject.gameserver.world.object.instance.L2DoorInstance;

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
		super(UPDATE_INTERVAL);
	}

	private static final int		UPDATE_INTERVAL		= 15000;

	public static final byte		LEVEL_0				= 0;
	public static final byte		LEVEL_1				= 1;
	public static final byte		LEVEL_2				= 2;
	public static final byte		LEVEL_3				= 3;
	public static final byte		LEVEL_4				= 4;
	public static final byte		LEVEL_5				= 5;
	public static final byte		LEVEL_6				= 6;
	public static final byte		LEVEL_7				= 7;
	public static final byte		LEVEL_8				= 8;
	public static final byte		LEVEL_9				= 9;
	public static final byte		LEVEL_10			= 10;
	public static final byte		LEVEL_11			= 11;

	private static final int		TRUST_POINTS_0_1	= 300000;
	private static final int		TRUST_POINTS_1_2	= 600000;
	private static final int		TRUST_POINTS_2_3	= 1000000;
	private static final int		TRUST_POINTS_3_4	= 1030000;

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
		final int trustPoints = HellboundManager.getInstance().getTrustPoints();
		if (level == _cachedLevel)
		{
			boolean nextLevel = false;
			switch (level)
			{
				case LEVEL_0:
					if (trustPoints == TRUST_POINTS_0_1)
						nextLevel = true;
					break;
				case LEVEL_1:
					if (trustPoints == TRUST_POINTS_1_2)
						nextLevel = true;
					break;
				case LEVEL_2:
					if (trustPoints == TRUST_POINTS_2_3)
						nextLevel = true;
					break;
				case LEVEL_3:
					if (trustPoints == TRUST_POINTS_3_4)
						nextLevel = true;
					break;
				case LEVEL_4:
				case LEVEL_5:
				case LEVEL_6:
				case LEVEL_7:
				case LEVEL_8:
				case LEVEL_9:
				case LEVEL_10:
				case LEVEL_11:
					nextLevel = false;
					break;
				default:
					nextLevel = false;
					break;
			}

			if (nextLevel)
			{
				level++;
				updateHellboundLevel(level);
				onLevelChange(level);
			}
		}
		else
			onLevelChange(level); // first run or changed by admin
	}

	private final void onLevelChange(final int newLevel)
	{
		HellboundManager.getInstance().doSpawn();

		for (final int[] doorData : DOOR_LIST)
		{
			try
			{
				final L2DoorInstance door = DoorTable.getInstance().getDoor(doorData[0]);
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
				_log.warn("", e);
			}
		}

		if (_cachedLevel >= 0)
		{
			Announcements.getInstance().announceToAll(ANNOUNCE.replace("%lvl%", String.valueOf(newLevel)));
			_log.info("HellboundManager : " + ANNOUNCE.replace("%lvl%", String.valueOf(newLevel)));
		}

		_cachedLevel = newLevel;
	}

	private final void updateHellboundLevel(final int level)
	{
		final int trustPoints = HellboundManager.getInstance().getTrustPoints();

		if (trustPoints < TRUST_POINTS_0_1)
			HellboundManager.getInstance().setHellboundLevel(LEVEL_0);
		else if (trustPoints >= TRUST_POINTS_0_1 && trustPoints < TRUST_POINTS_1_2)
			HellboundManager.getInstance().setHellboundLevel(LEVEL_1);
		else if (trustPoints >= TRUST_POINTS_1_2 && trustPoints < TRUST_POINTS_2_3)
			HellboundManager.getInstance().setHellboundLevel(LEVEL_2);
		else if (trustPoints >= TRUST_POINTS_2_3 && trustPoints < TRUST_POINTS_3_4)
			HellboundManager.getInstance().setHellboundLevel(LEVEL_3);
		else if (trustPoints >= TRUST_POINTS_3_4)
			HellboundManager.getInstance().setHellboundLevel(LEVEL_4);
		else
			HellboundManager.getInstance().setHellboundLevel(level);

		final L2Properties props = PersistentProperties.getProperties(HellboundManager.class);
		HellboundManager.getInstance().saveProperties();
		HellboundManager.getInstance().loadProperties(props);
	}
}
