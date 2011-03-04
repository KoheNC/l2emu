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
package net.l2emuproject.gameserver.datatables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.events.global.clanhallsiege.ClanHall;
import net.l2emuproject.gameserver.events.global.clanhallsiege.ClanHallManager;
import net.l2emuproject.gameserver.manager.instances.Instance;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.system.idfactory.IdFactory;
import net.l2emuproject.gameserver.templates.StatsSet;
import net.l2emuproject.gameserver.templates.chars.L2CharTemplate;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.mapregion.MapRegionManager;
import net.l2emuproject.gameserver.world.object.instance.L2DoorInstance;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public final class DoorTable
{
	private static final Log _log = LogFactory.getLog(DoorTable.class);

	public static DoorTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private final Map<Integer, L2DoorInstance> _doors = new FastMap<Integer, L2DoorInstance>();

	private DoorTable()
	{
		reloadAll();
	}

	public void reloadAll()
	{
		_doorArray = null;
		_doors.clear();

		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(new File(Config.DATAPACK_ROOT, "data/door.csv")));

			for (String line; (line = br.readLine()) != null;)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
					continue;

				final L2DoorInstance door = parseLine(line);
				if (door == null)
					continue;

				putDoor(door);

				door.spawnMe(door.getX(), door.getY(), door.getZ());

				// Garden of Eva (every 7 minutes)
				if (door.getDoorName().startsWith("goe"))
					door.setAutoActionDelay(420000);

				// Tower of Insolence (every 5 minutes)
				else if (door.getDoorName().startsWith("aden_tower"))
					door.setAutoActionDelay(300000);

				/* TODO: check which are automatic
				// devils (every 5 minutes)
				else if (door.getDoorName().startsWith("pirate_isle"))
					door.setAutoActionDelay(300000);
				// Cruma Tower (every 20 minutes)
				else if (door.getDoorName().startsWith("cruma"))
					door.setAutoActionDelay(1200000);
				// Coral Garden Gate (every 15 minutes)
				else if (door.getDoorName().startsWith("Coral_garden"))
					door.setAutoActionDelay(900000);
				// Normil's cave (every 5 minutes)
				else if (door.getDoorName().startsWith("Normils_cave"))
					door.setAutoActionDelay(300000);
				// Normil's Garden (every 15 minutes)
				else if (door.getDoorName().startsWith("Normils_garden"))
					door.setAutoActionDelay(900000);
				*/
			}

			_log.info(getClass().getSimpleName() + " : Loaded " + _doors.size() + " Door Template(s).");
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			IOUtils.closeQuietly(br);
		}
	}

	public void registerToClanHalls()
	{
		for (L2DoorInstance door : getDoors())
		{
			ClanHall clanhall = ClanHallManager.getInstance().getNearbyClanHall(door.getX(), door.getY(), 700);
			if (clanhall != null)
			{
				clanhall.getDoors().add(door);
				door.setClanHall(clanhall);
			}
		}
	}

	public void setCommanderDoors()
	{
		for (L2DoorInstance door : getDoors())
		{
			if (door.getFort() != null && door.isOpen())
			{
				door.setOpen(false);
				door.setIsCommanderDoor(true);
			}
		}
	}

	public static L2DoorInstance parseLine(String line)
	{
		L2DoorInstance door = null;
		try
		{
			final StringTokenizer st = new StringTokenizer(line, ";");

			final String name = st.nextToken();
			final int id = Integer.parseInt(st.nextToken());
			final int x = Integer.parseInt(st.nextToken());
			final int y = Integer.parseInt(st.nextToken());
			final int z = Integer.parseInt(st.nextToken());
			final int rangeXMin = Integer.parseInt(st.nextToken());
			final int rangeYMin = Integer.parseInt(st.nextToken());
			final int rangeZMin = Integer.parseInt(st.nextToken());
			final int rangeXMax = Integer.parseInt(st.nextToken());
			final int rangeYMax = Integer.parseInt(st.nextToken());
			final int rangeZMax = Integer.parseInt(st.nextToken());
			final int hp = Integer.parseInt(st.nextToken());
			final int pdef = Integer.parseInt(st.nextToken());
			final int mdef = Integer.parseInt(st.nextToken());
			boolean unlockable = false;
			if (st.hasMoreTokens())
				unlockable = Boolean.parseBoolean(st.nextToken());
			boolean startOpen = false;
			if (st.hasMoreTokens())
				startOpen = Boolean.parseBoolean(st.nextToken());

			if (rangeXMin > rangeXMax)
				_log.fatal("Error in door data, XMin > XMax, ID:" + id);
			if (rangeYMin > rangeYMax)
				_log.fatal("Error in door data, YMin > YMax, ID:" + id);
			if (rangeZMin > rangeZMax)
				_log.fatal("Error in door data, ZMin > ZMax, ID:" + id);

			int collisionRadius; // (max) radius for movement checks
			if (rangeXMax - rangeXMin > rangeYMax - rangeYMin)
				collisionRadius = rangeYMax - rangeYMin;
			else
				collisionRadius = rangeXMax - rangeXMin;

			final StatsSet npcDat = new StatsSet();
			npcDat.set("npcId", id);
			npcDat.set("level", 0);
			npcDat.set("jClass", "door");

			npcDat.set("baseSTR", 0);
			npcDat.set("baseCON", 0);
			npcDat.set("baseDEX", 0);
			npcDat.set("baseINT", 0);
			npcDat.set("baseWIT", 0);
			npcDat.set("baseMEN", 0);

			npcDat.set("baseShldDef", 0);
			npcDat.set("baseShldRate", 0);
			npcDat.set("baseAccCombat", 38);
			npcDat.set("baseEvasRate", 38);
			npcDat.set("baseCritRate", 38);

			//npcDat.set("name", "");
			npcDat.set("collision_radius", collisionRadius);
			npcDat.set("collision_height", rangeZMax - rangeZMin & 0xfff0);
			npcDat.set("fcollision_radius", collisionRadius);
			npcDat.set("fcollision_height", rangeZMax - rangeZMin & 0xfff0);
			npcDat.set("sex", "male");
			npcDat.set("type", "");
			npcDat.set("baseAtkRange", 0);
			npcDat.set("baseMpMax", 0);
			npcDat.set("baseCpMax", 0);
			npcDat.set("rewardExp", 0);
			npcDat.set("rewardSp", 0);
			npcDat.set("basePAtk", 0);
			npcDat.set("baseMAtk", 0);
			npcDat.set("basePAtkSpd", 0);
			npcDat.set("aggroRange", 0);
			npcDat.set("baseMAtkSpd", 0);
			npcDat.set("rhand", 0);
			npcDat.set("lhand", 0);
			npcDat.set("armor", 0);
			npcDat.set("baseWalkSpd", 0);
			npcDat.set("baseRunSpd", 0);
			npcDat.set("name", name);
			npcDat.set("baseHpMax", hp);
			npcDat.set("baseHpReg", 3.e-3f);
			npcDat.set("baseMpReg", 3.e-3f);
			npcDat.set("basePDef", pdef);
			npcDat.set("baseMDef", mdef);

			final L2CharTemplate template = new L2CharTemplate(npcDat);
			door = new L2DoorInstance(IdFactory.getInstance().getNextId(), template, id, name, unlockable);
			door.setRange(rangeXMin, rangeYMin, rangeZMin, rangeXMax, rangeYMax, rangeZMax);
			door.setMapRegion(MapRegionManager.getInstance().getRegion(x, y, z));
			door.getStatus().setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
			door.setOpen(startOpen);
			door.getPosition().setXYZInvisible(x, y, z);

			door.setMapRegion(MapRegionManager.getInstance().getRegion(x, y));
		}
		catch (Exception e)
		{
			_log.error("Error in door data at line: " + line, e);
		}

		return door;
	}

	public final L2DoorInstance getDoor(Integer id)
	{
		return _doors.get(id);
	}

	public final void putDoor(L2DoorInstance door)
	{
		_doorArray = null;
		_doors.put(door.getDoorId(), door);
		GeoData.getInstance().initDoorGeodata(door);
	}

	private L2DoorInstance[] _doorArray;

	public final L2DoorInstance[] getDoors()
	{
		if (_doorArray == null)
			_doorArray = _doors.values().toArray(new L2DoorInstance[_doors.size()]);

		return _doorArray;
	}
	
	/**
	 * Open list of doors in the instance
	 */
	public static final void openInstanceDoors(final int instanceId, final int[] doorIds)
	{
		final Instance instance = InstanceManager.getInstance().getInstance(instanceId);
		if (instance == null || doorIds == null)
			return;
		
		for (int doorId : doorIds)
		{
			final L2DoorInstance door = instance.getDoor(doorId);
			if (door != null)
				door.openMe();
		}
	}
	
	/**
	 * Close list of doors in the instance
	 */
	public static final void closeInstanceDoors(final int instanceId, final int[] doorIds)
	{
		final Instance instance = InstanceManager.getInstance().getInstance(instanceId);
		if (instance == null || doorIds == null)
			return;
		
		for (int doorId : doorIds)
		{
			final L2DoorInstance door = instance.getDoor(doorId);
			if (door != null)
				door.closeMe();
		}
	}
	
	/**
	 * Close list of doors
	 */
	public final void closeDoors(final int[] doorIds)
	{
		if (doorIds == null)
			return;
		
		for (int doorId : doorIds)
			closeDoor(doorId);
	}
	
	public final void closeDoor(int doorId)
	{
		final L2DoorInstance door = getDoor(doorId);
		if (door != null)
			door.closeMe();
	}
	
	/**
	 * Open list of doors
	 */
	public final void openDoors(final int[] doorIds)
	{
		if (doorIds == null)
			return;
		
		for (int doorId : doorIds)
			openDoor(doorId);
	}
	
	public final void openDoor(int doorId)
	{
		final L2DoorInstance door = getDoor(doorId);
		if (door != null)
			door.openMe();
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final DoorTable _instance = new DoorTable();
	}
}
