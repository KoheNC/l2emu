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
package net.l2emuproject.gameserver.events.global.krateicube;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.events.L2Event;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExPVPMatchCCMyRecord;
import net.l2emuproject.gameserver.system.restriction.global.KrateiCubeRestriction;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2DoorInstance;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author lord_rex
 */
public final class KrateiCube extends L2Event
{
	private static final Log	_log	= LogFactory.getLog(KrateiCube.class);

	private static final class SingletonHolder
	{
		private static final KrateiCube	INSTANCE	= new KrateiCube();
	}

	public static KrateiCube getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public static final String					HTML_PATH			= "data/html/kratei_cube/";

	// DOOR FIRST GROUP
	private static final int					DOORS_GROUP1_1		= 17150014;
	private static final int					DOORS_GROUP1_2		= 17150013;
	private static final int					DOORS_GROUP1_3		= 17150019;
	private static final int					DOORS_GROUP1_4		= 17150024;
	private static final int					DOORS_GROUP1_5		= 17150039;
	private static final int					DOORS_GROUP1_6		= 17150044;
	private static final int					DOORS_GROUP1_7		= 17150059;
	private static final int					DOORS_GROUP1_8		= 17150064;
	private static final int					DOORS_GROUP1_9		= 17150079;
	private static final int					DOORS_GROUP1_10		= 17150084;
	private static final int					DOORS_GROUP1_11		= 17150093;
	private static final int					DOORS_GROUP1_12		= 17150094;
	private static final int					DOORS_GROUP1_13		= 17150087;
	private static final int					DOORS_GROUP1_14		= 17150088;
	private static final int					DOORS_GROUP1_15		= 17150082;
	private static final int					DOORS_GROUP1_16		= 17150077;

	// DOOR SECOND GROUP
	private static final int					DOORS_GROUP2_1		= 17150062;
	private static final int					DOORS_GROUP2_2		= 17150057;
	private static final int					DOORS_GROUP2_3		= 17150008;
	private static final int					DOORS_GROUP2_4		= 17150007;
	private static final int					DOORS_GROUP2_5		= 17150018;
	private static final int					DOORS_GROUP2_6		= 17150023;
	private static final int					DOORS_GROUP2_7		= 17150038;
	private static final int					DOORS_GROUP2_8		= 17150043;
	private static final int					DOORS_GROUP2_9		= 17150058;
	private static final int					DOORS_GROUP2_10		= 17150090;
	private static final int					DOORS_GROUP2_11		= 17150078;
	private static final int					DOORS_GROUP2_12		= 17150083;
	private static final int					DOORS_GROUP2_13		= 17150030;
	private static final int					DOORS_GROUP2_14		= 17150029;
	private static final int					DOORS_GROUP2_15		= 17150028;
	private static final int					DOORS_GROUP2_16		= 17150027;
	private static final int					DOORS_GROUP2_17		= 17150036;
	private static final int					DOORS_GROUP2_18		= 17150041;
	private static final int					DOORS_GROUP2_19		= 17150069;
	private static final int					DOORS_GROUP2_20		= 17150061;
	private static final int					DOORS_GROUP2_21		= 17150020;
	private static final int					DOORS_GROUP2_22		= 17150025;

	// DOOR THIRD GROUP
	private static final int					DOORS_GROUP3_1		= 17150034;
	private static final int					DOORS_GROUP3_2		= 17150033;
	private static final int					DOORS_GROUP3_3		= 17150032;
	private static final int					DOORS_GROUP3_4		= 17150031;
	private static final int					DOORS_GROUP3_5		= 17150012;
	private static final int					DOORS_GROUP3_6		= 17150011;
	private static final int					DOORS_GROUP3_7		= 17150073;
	private static final int					DOORS_GROUP3_8		= 17150010;
	private static final int					DOORS_GROUP3_9		= 17150009;
	private static final int					DOORS_GROUP3_10		= 17150017;
	private static final int					DOORS_GROUP3_11		= 17150016;
	private static final int					DOORS_GROUP3_12		= 17150021;
	private static final int					DOORS_GROUP3_13		= 17150048;
	private static final int					DOORS_GROUP3_14		= 17150042;
	private static final int					DOORS_GROUP3_15		= 17150051;
	private static final int					DOORS_GROUP3_16		= 17150053;
	// DOORS GROUP 4
	private static final int					DOORS_GROUP4_1		= 17150052;
	private static final int					DOORS_GROUP4_2		= 17150054;
	private static final int					DOORS_GROUP4_3		= 17150050;
	private static final int					DOORS_GROUP4_4		= 17150049;
	private static final int					DOORS_GROUP4_5		= 17150037;
	private static final int					DOORS_GROUP4_6		= 17150047;
	private static final int					DOORS_GROUP4_7		= 17150085;
	private static final int					DOORS_GROUP4_8		= 17150080;
	private static final int					DOORS_GROUP4_9		= 17150074;
	private static final int					DOORS_GROUP4_10		= 17150063;
	private static final int					DOORS_GROUP4_11		= 17150072;
	private static final int					DOORS_GROUP4_12		= 17150071;
	private static final int					DOORS_GROUP4_13		= 17150070;
	private static final int					DOORS_GROUP4_14		= 17150056;
	private static final int					DOORS_GROUP4_15		= 17150068;
	private static final int					DOORS_GROUP4_16		= 17150067;
	private static final int					DOORS_GROUP4_17		= 17150076;
	private static final int					DOORS_GROUP4_18		= 17150081;
	private static final int					DOORS_GROUP4_19		= 17150092;
	private static final int					DOORS_GROUP4_20		= 17150091;
	private static final int					DOORS_GROUP4_21		= 17150010;
	private static final int					DOORS_GROUP4_22		= 17150089;

	private static final int[]					DOORS_GROUP1		=
																	{
			DOORS_GROUP1_1,
			DOORS_GROUP1_2,
			DOORS_GROUP1_3,
			DOORS_GROUP1_4,
			DOORS_GROUP1_5,
			DOORS_GROUP1_6,
			DOORS_GROUP1_7,
			DOORS_GROUP1_8,
			DOORS_GROUP1_9,
			DOORS_GROUP1_10,
			DOORS_GROUP1_11,
			DOORS_GROUP1_12,
			DOORS_GROUP1_13,
			DOORS_GROUP1_14,
			DOORS_GROUP1_15,
			DOORS_GROUP1_16,										};

	private static final int[]					DOORS_GROUP2		=
																	{
			DOORS_GROUP2_1,
			DOORS_GROUP2_2,
			DOORS_GROUP2_3,
			DOORS_GROUP2_4,
			DOORS_GROUP2_5,
			DOORS_GROUP2_6,
			DOORS_GROUP2_7,
			DOORS_GROUP2_8,
			DOORS_GROUP2_9,
			DOORS_GROUP2_10,
			DOORS_GROUP2_11,
			DOORS_GROUP2_12,
			DOORS_GROUP2_13,
			DOORS_GROUP2_14,
			DOORS_GROUP2_15,
			DOORS_GROUP2_16,
			DOORS_GROUP2_17,
			DOORS_GROUP2_18,
			DOORS_GROUP2_19,
			DOORS_GROUP2_20,
			DOORS_GROUP2_21,
			DOORS_GROUP2_22,										};

	private static final int[]					DOORS_GROUP3		=
																	{
			DOORS_GROUP3_1,
			DOORS_GROUP3_2,
			DOORS_GROUP3_3,
			DOORS_GROUP3_4,
			DOORS_GROUP3_5,
			DOORS_GROUP3_6,
			DOORS_GROUP3_7,
			DOORS_GROUP3_8,
			DOORS_GROUP3_9,
			DOORS_GROUP3_10,
			DOORS_GROUP3_11,
			DOORS_GROUP3_12,
			DOORS_GROUP3_13,
			DOORS_GROUP3_14,
			DOORS_GROUP3_15,
			DOORS_GROUP3_16,										};

	private static final int[]					DOORS_GROUP4		=
																	{
			DOORS_GROUP4_1,
			DOORS_GROUP4_2,
			DOORS_GROUP4_3,
			DOORS_GROUP4_4,
			DOORS_GROUP4_5,
			DOORS_GROUP4_6,
			DOORS_GROUP4_7,
			DOORS_GROUP4_8,
			DOORS_GROUP4_9,
			DOORS_GROUP4_10,
			DOORS_GROUP4_11,
			DOORS_GROUP4_12,
			DOORS_GROUP4_13,
			DOORS_GROUP4_14,
			DOORS_GROUP4_15,
			DOORS_GROUP4_16,
			DOORS_GROUP4_17,
			DOORS_GROUP4_18,
			DOORS_GROUP4_19,
			DOORS_GROUP4_20,
			DOORS_GROUP4_21,
			DOORS_GROUP4_22,										};

	private static final int[][]				TELEPORT_LOCATIONS	=
																	{
																	{ -77906, -85809, -8362 },
																	{ -79903, -85807, -8364 },
																	{ -81904, -85807, -8364 },
																	{ -83901, -85806, -8364 },
																	{ -85903, -85807, -8364 },
																	{ -77904, -83808, -8364 },
																	{ -79904, -83807, -8364 },
																	{ -81905, -83810, -8364 },
																	{ -83903, -83807, -8364 },
																	{ -85899, -83807, -8364 },
																	{ -77903, -81808, -8364 },
																	{ -79906, -81807, -8364 },
																	{ -81901, -81808, -8364 },
																	{ -83905, -81805, -8364 },
																	{ -85907, -81809, -8364 },
																	{ -77904, -79807, -8364 },
																	{ -79905, -79807, -8364 },
																	{ -81908, -79808, -8364 },
																	{ -83907, -79806, -8364 },
																	{ -85912, -79806, -8364 },
																	{ -77905, -77808, -8364 },
																	{ -79902, -77805, -8364 },
																	{ -81904, -77808, -8364 },
																	{ -83904, -77808, -8364 },
																	{ -85904, -77807, -8364 } };

	public static final int						RED_WATCHER			= 18601;

	public static final int						BLUE_WATCHER		= 18602;

	public static final int[]					MONSTERS70			=
																	{ 18587, 18580, 18581, 18584, 18591, 18589, 18583 };
	public static final int[]					MONSTERS76			=
																	{ 18590, 18591, 18589, 18585, 18586, 18583, 18592, 18582 };
	public static final int[]					MONSTERS80			=
																	{ 18595, 18597, 18596, 18598, 18593, 18600, 18594, 18599 };

	private static final int					MATCH_MANAGER1		= 32504;
	private static final int					MATCH_MANAGER2		= 32505;
	private static final int					MATCH_MANAGER3		= 32506;

	private static final int					FANTASY_COIN		= 13067;

	private static final int[]					WAIT_ROOM_LOCATION	=
																	{ -87028, -81780, -8365 };

	private static final int[]					FANTASY_ISLAND		=
																	{ -59193, -56893, -2039 };

	private final Map<Integer, L2Player>	_gamers;

	private volatile int						_status;
	private final KrateiCubeTask				_task;

	private ScheduledFuture<?>					_event;

	private int									_instance70Id		= 0;
	private int									_instance76Id		= 0;
	private int									_instance80Id		= 0;

	private ScheduledFuture<?>					_doorTaskClose		= null;
	private ScheduledFuture<?>					_doorTaskOpen		= null;
	private ScheduledFuture<?>					_showScore			= null;

	private KrateiCube()
	{
		_status = STATUS_NOT_IN_PROGRESS;
		_task = new KrateiCubeTask();

		_gamers = new FastMap<Integer, L2Player>();

		_log.info(getClass().getSimpleName() + " : Initialized.");
	}

	public final void init()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new KrateiCubeTask(), Config.KRATEI_CUBE_REGISTRATION_START_TIME);
		_log.info(getClass().getSimpleName() + " : Registration is starting in " + Config.KRATEI_CUBE_REGISTRATION_START_TIME / 60 / 1000 + " minutes.");
	}

	private final class KrateiCubeTask implements Runnable
	{
		@Override
		public final void run()
		{
			switch (_status)
			{
				case STATUS_NOT_IN_PROGRESS:
					startRegistration();
					break;
				case STATUS_REGISTRATION:
					endRegistration();
					break;
				case STATUS_PREPARATION:
					startEvent();
					break;
				case STATUS_COMBAT:
					endEvent();
					break;
				case STATUS_REWARDS:
					_status = STATUS_NOT_IN_PROGRESS;
					ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.KRATEI_CUBE_REGISTRATION_START_TIME);
					_log.info("KrateiCube : Next registration is starting in " + Config.KRATEI_CUBE_REGISTRATION_START_TIME / 60 / 1000 + " minutes.");
					break;
				default:
					_log.fatal("Incorrect status set in Kratei Cube, terminating now!");
					break;
			}
		}
	}

	@Override
	public final void startRegistration()
	{
		_status = STATUS_REGISTRATION;

		KrateiCubeRestriction.getInstance().activate();

		registrationAnnounce();

		_log.info(getClass().getSimpleName() + " : Registration is started.");
		ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.KRATEI_CUBE_REGISTRATION_LENGHT_TIME);
	}

	@Override
	public final void registrationAnnounce()
	{
	}

	@Override
	public final void endRegistration()
	{
		_status = STATUS_PREPARATION;

		if (_gamers.size() < Config.KRATEI_CUBE_MIN_PARTICIPANTS)
		{
			_status = STATUS_NOT_IN_PROGRESS;
			_gamers.clear();
			_log.info(getClass().getSimpleName() + " : Match canceled due to lack of participant, next round in " + Config.KRATEI_CUBE_REGISTRATION_START_TIME
					/ 60 / 1000 + " minutes.");
			ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.KRATEI_CUBE_REGISTRATION_START_TIME);
			return;
		}

		initInstance();

		spawnNpc(MATCH_MANAGER1, -87148, -16396, -8320, _instance70Id);
		spawnNpc(MATCH_MANAGER2, -87148, -16396, -8320, _instance76Id);
		spawnNpc(MATCH_MANAGER3, -87148, -16396, -8320, _instance80Id);

		spawnWatchers(RED_WATCHER, _instance70Id);
		spawnWatchers(RED_WATCHER, _instance76Id);
		spawnWatchers(RED_WATCHER, _instance80Id);

		for (L2Player player : _gamers.values())
			teleportPlayer(player, WAIT_ROOM_LOCATION, 0);

		_log.info(getClass().getSimpleName() + " : Registration is ended.");
		ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.KRATEI_CUBE_PREPARATION_TIME);
	}

	@Override
	public final void startEvent()
	{
		_status = STATUS_COMBAT;

		for (L2Player player : _gamers.values())
		{
			randomTeleport(player);
			showScore();
			giveBuffs(player);
		}

		openDoors(Rnd.get(1, 4));
		_log.info(getClass().getSimpleName() + " : Match is started.");
		_event = ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.KRATEI_CUBE_MATCH_TIME);
	}

	@Override
	public final void endEvent()
	{
		if (_status != STATUS_COMBAT)
			return;
		_status = STATUS_REWARDS;
		if (!_event.cancel(false))
			return;
		closeAllDoors();
		KrateiCubeRestriction.getInstance().deactivate();
		for (L2Player player : _gamers.values())
		{
			giveRewards(player);
			leaveKrateiCube(player);
		}

		if (_doorTaskClose != null)
			_doorTaskClose.cancel(true);
		if (_doorTaskOpen != null)
			_doorTaskOpen.cancel(true);
		if (_showScore != null)
			_showScore.cancel(true);

		deleteNpc(MATCH_MANAGER1);
		deleteNpc(MATCH_MANAGER2);
		deleteNpc(MATCH_MANAGER3);
		deleteNpc(BLUE_WATCHER);
		deleteNpc(RED_WATCHER);

		_gamers.clear();
		_log.info(getClass().getSimpleName() + " : Match is finished.");
		ThreadPoolManager.getInstance().scheduleGeneral(_task, 1000);
	}

	@Override
	protected final void giveRewards(L2Player player)
	{
		final int points = player.getPlayerEventData().getPoints();

		final int level = player.getLevel();

		if (level >= 70 && level <= 75)
			player.addExpAndSp(points * 1800, points * 180);
		else if (level >= 76 && level <= 79)
			player.addExpAndSp(points * 2100, points * 210);
		else if (level >= 80)
			player.addExpAndSp(points * 2740, points * 274);

		int count = 0;
		if (_gamers.size() <= 4)
			count = 10;
		else
		{
			count = 40;
			// TODO: Add complicated rewarding...
		}

		if (points > 0)
			player.addItem("KrateiCube", FANTASY_COIN, count, null, true);
	}

	private final void showScore()
	{
		for (L2Player player : _gamers.values())
			player.sendPacket(new ExPVPMatchCCMyRecord(player.getPlayerEventData().getPoints()));

		_showScore = ThreadPoolManager.getInstance().scheduleGeneral(new ShowScore(), 10000);
	}

	private final class ShowScore implements Runnable
	{
		@Override
		public final void run()
		{
			showScore();
		}
	}

	@Override
	protected final boolean canJoin(L2Player player)
	{
		if (_status != STATUS_REGISTRATION)
		{
			player.showHTMLFile(HTML_PATH + "32503-2.htm");
			return false;
		}

		if (_gamers.containsKey(player.getObjectId()))
		{
			player.showHTMLFile(HTML_PATH + "32503-5.htm");
			return false;
		}

		if (player.getLevel() < 70)
		{
			player.showHTMLFile(HTML_PATH + "32503-7.htm");
			return false;
		}

		if (_gamers.size() > Config.KRATEI_CUBE_MAX_PARTICIPANTS)
		{
			player.sendPacket(SystemMessageId.REGISTRATION_PERIOD_OVER);
			return false;
		}

		return true;
	}

	@Override
	public final void registerPlayer(L2Player player)
	{
		if (!canJoin(player))
			return;

		_gamers.put(player.getObjectId(), player);

		player.showHTMLFile(HTML_PATH + "32503-4.htm");
	}

	@Override
	public final void cancelRegistration(L2Player player)
	{
		if (_gamers.containsKey(player.getObjectId()))
		{
			_gamers.remove(player.getObjectId());

			player.showHTMLFile(HTML_PATH + "32503-6.htm");
		}
	}

	@Override
	public final void removeDisconnected(L2Player player)
	{
		_gamers.remove(player.getObjectId());
	}

	@Override
	protected final void initInstance()
	{
		try
		{
			_instance70Id = InstanceManager.getInstance().createDynamicInstance("KrateiCube_70.xml");
			InstanceManager.getInstance().getInstance(_instance70Id).setAllowSummon(false);
			InstanceManager.getInstance().getInstance(_instance70Id).setPvPInstance(true);
			InstanceManager.getInstance().getInstance(_instance70Id).setEmptyDestroyTime(Config.KRATEI_CUBE_INSTANCE_EMPTY_DESTROY_TIME);
			_log.info(getClass().getSimpleName() + " : Instance " + _instance70Id + " is created, empty destroy time is "
					+ Config.KRATEI_CUBE_INSTANCE_EMPTY_DESTROY_TIME + ".");
		}
		catch (Exception e)
		{
			_instance70Id = 0;
			_log.warn("KrateiCube[initInstance]: Exception: " + e.getMessage(), e);
		}

		try
		{
			_instance76Id = InstanceManager.getInstance().createDynamicInstance("KrateiCube_76.xml");
			InstanceManager.getInstance().getInstance(_instance76Id).setAllowSummon(false);
			InstanceManager.getInstance().getInstance(_instance76Id).setPvPInstance(true);
			InstanceManager.getInstance().getInstance(_instance76Id).setEmptyDestroyTime(Config.KRATEI_CUBE_INSTANCE_EMPTY_DESTROY_TIME);
			_log.info(getClass().getSimpleName() + " : Instance " + _instance76Id + " is created, empty destroy time is "
					+ Config.KRATEI_CUBE_INSTANCE_EMPTY_DESTROY_TIME + ".");
		}
		catch (Exception e)
		{
			_instance76Id = 0;
			_log.warn("KrateiCube[initInstance]: Exception: " + e.getMessage(), e);
		}

		try
		{
			_instance80Id = InstanceManager.getInstance().createDynamicInstance("KrateiCube_80.xml");
			InstanceManager.getInstance().getInstance(_instance80Id).setAllowSummon(false);
			InstanceManager.getInstance().getInstance(_instance80Id).setPvPInstance(true);
			InstanceManager.getInstance().getInstance(_instance80Id).setEmptyDestroyTime(Config.KRATEI_CUBE_INSTANCE_EMPTY_DESTROY_TIME);
			_log.info(getClass().getSimpleName() + " : Instance " + _instance80Id + " is created, empty destroy time is "
					+ Config.KRATEI_CUBE_INSTANCE_EMPTY_DESTROY_TIME + ".");
		}
		catch (Exception e)
		{
			_instance80Id = 0;
			_log.warn("KrateiCube[initInstance]: Exception: " + e.getMessage(), e);
		}
	}

	public static final boolean isPlaying(L2Player player)
	{
		return getInstance()._gamers.containsKey(player.getObjectId());
	}

	public final void reviveTask(L2Player player)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new ReviveTask(player, WAIT_ROOM_LOCATION, getInstanceId(player)), 10000);
	}

	public static final void revive(L2Player player)
	{
		getInstance().sendMessage(player, "You will be revived in 10 seconds!", 5000);
		getInstance().reviveTask(player);
	}

	private final int getInstanceId(L2Player player)
	{
		final int level = player.getLevel();

		if (level >= 70 && level <= 75)
			return _instance70Id;
		else if (level >= 76 && level <= 79)
			return _instance76Id;
		else if (level >= 80)
			return _instance80Id;

		return 0;
	}

	public final void randomTeleport(L2Player player)
	{
		final int i = Rnd.get(TELEPORT_LOCATIONS.length);
		final int[] coords =
		{ TELEPORT_LOCATIONS[i][0], TELEPORT_LOCATIONS[i][1], TELEPORT_LOCATIONS[i][2] };

		teleportPlayer(player, coords, getInstanceId(player));
	}

	public final void giveBuffs(L2Player player)
	{
		SkillTable.getInstance().getInfo(1086, 2).getEffects(player, player);
		SkillTable.getInstance().getInfo(1204, 2).getEffects(player, player);
		SkillTable.getInstance().getInfo(1059, 3).getEffects(player, player);
		SkillTable.getInstance().getInfo(1085, 3).getEffects(player, player);
		SkillTable.getInstance().getInfo(1078, 4).getEffects(player, player);
		SkillTable.getInstance().getInfo(1068, 3).getEffects(player, player);
		SkillTable.getInstance().getInfo(1240, 3).getEffects(player, player);
		SkillTable.getInstance().getInfo(1077, 3).getEffects(player, player);
		SkillTable.getInstance().getInfo(1242, 3).getEffects(player, player);
		SkillTable.getInstance().getInfo(1062, 2).getEffects(player, player);
	}

	public final void leaveKrateiCube(L2Player player)
	{
		player.getPlayerEventData().setPoints(0);
		teleportPlayer(player, FANTASY_ISLAND, 0);
	}

	private final void openDoor(int doorId, int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			if (door.getDoorId() == doorId)
				door.openMe();
	}

	private final void closeDoor(int doorId, int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			if (door.getDoorId() == doorId)
				door.closeMe();
	}

	private final void openDoors(int groupId)
	{
		switch (groupId)
		{
			case 1:
				for (int i : DOORS_GROUP1)
				{
					openDoor(i, _instance70Id);
					openDoor(i, _instance76Id);
					openDoor(i, _instance80Id);
				}
				break;
			case 2:
				for (int i : DOORS_GROUP2)
				{
					openDoor(i, _instance70Id);
					openDoor(i, _instance76Id);
					openDoor(i, _instance80Id);
				}
				break;
			case 3:
				for (int i : DOORS_GROUP3)
				{
					openDoor(i, _instance70Id);
					openDoor(i, _instance76Id);
					openDoor(i, _instance80Id);
				}
				break;
			case 4:
				for (int i : DOORS_GROUP4)
				{
					openDoor(i, _instance70Id);
					openDoor(i, _instance76Id);
					openDoor(i, _instance80Id);
				}
				break;
		}
		_doorTaskClose = ThreadPoolManager.getInstance().scheduleGeneral(new CloseDoors(), 15000);
	}

	private final class OpenDoors implements Runnable
	{
		@Override
		public final void run()
		{
			openDoors(Rnd.get(1, 4));
		}
	}

	private final void closeDoors(int groupId)
	{
		switch (groupId)
		{
			case 1:
				for (int i : DOORS_GROUP1)
				{
					closeDoor(i, _instance70Id);
					closeDoor(i, _instance76Id);
					closeDoor(i, _instance80Id);
				}
				break;
			case 2:
				for (int i : DOORS_GROUP2)
				{
					closeDoor(i, _instance70Id);
					closeDoor(i, _instance76Id);
					closeDoor(i, _instance80Id);
				}
				break;
			case 3:
				for (int i : DOORS_GROUP3)
				{
					closeDoor(i, _instance70Id);
					closeDoor(i, _instance76Id);
					closeDoor(i, _instance80Id);
				}
				break;
			case 4:
				for (int i : DOORS_GROUP4)
				{
					closeDoor(i, _instance70Id);
					closeDoor(i, _instance76Id);
					closeDoor(i, _instance80Id);
				}
				break;
		}
		_doorTaskOpen = ThreadPoolManager.getInstance().scheduleGeneral(new OpenDoors(), 15000);
	}

	private final class CloseDoors implements Runnable
	{
		@Override
		public final void run()
		{
			closeDoors(Rnd.get(1, 4));
		}
	}

	private final void closeAllDoors()
	{
		for (int i : DOORS_GROUP1)
		{
			closeDoor(i, _instance70Id);
			closeDoor(i, _instance76Id);
			closeDoor(i, _instance80Id);
		}

		for (int i : DOORS_GROUP2)
		{
			closeDoor(i, _instance70Id);
			closeDoor(i, _instance76Id);
			closeDoor(i, _instance80Id);
		}

		for (int i : DOORS_GROUP3)
		{
			closeDoor(i, _instance70Id);
			closeDoor(i, _instance76Id);
			closeDoor(i, _instance80Id);
		}

		for (int i : DOORS_GROUP4)
		{
			closeDoor(i, _instance70Id);
			closeDoor(i, _instance76Id);
			closeDoor(i, _instance80Id);
		}
	}

	private final class ChangeWatcherTask implements Runnable
	{
		private final int	_lastWatcher;
		private final int	_watcher;

		private ChangeWatcherTask(int lastWatcher, int watcher)
		{
			_lastWatcher = lastWatcher;
			_watcher = watcher;
		}

		@Override
		public final void run()
		{
			deleteNpc(_lastWatcher);

			spawnWatchers(_watcher, _instance70Id);
			spawnWatchers(_watcher, _instance76Id);
			spawnWatchers(_watcher, _instance80Id);
		}
	}

	private final void spawnWatchers(int watcher, int instanceId)
	{
		spawnNpc(watcher, -77906, -85809, -8362, instanceId); // 1
		spawnNpc(watcher, -79903, -85807, -8364, instanceId); // 2
		spawnNpc(watcher, -81904, -85807, -8364, instanceId); // 3
		spawnNpc(watcher, -83901, -85806, -8364, instanceId); // 4
		spawnNpc(watcher, -85903, -85807, -8364, instanceId); // 5
		spawnNpc(watcher, -77904, -83808, -8364, instanceId); // 6
		spawnNpc(watcher, -79904, -83807, -8364, instanceId); // 7
		spawnNpc(watcher, -81905, -83810, -8364, instanceId); // 8
		spawnNpc(watcher, -83903, -83807, -8364, instanceId); // 9
		spawnNpc(watcher, -85899, -83807, -8364, instanceId); // 10
		spawnNpc(watcher, -77903, -81808, -8364, instanceId); // 11
		spawnNpc(watcher, -79906, -81807, -8364, instanceId); // 12
		spawnNpc(watcher, -81901, -81808, -8364, instanceId); // 13
		spawnNpc(watcher, -83905, -81805, -8364, instanceId); // 14
		spawnNpc(watcher, -85907, -81809, -8364, instanceId); // 15
		spawnNpc(watcher, -77904, -79807, -8364, instanceId); // 16
		spawnNpc(watcher, -79905, -79807, -8364, instanceId); // 17
		spawnNpc(watcher, -81908, -79808, -8364, instanceId); // 18
		spawnNpc(watcher, -83907, -79806, -8364, instanceId); // 19
		spawnNpc(watcher, -85912, -79806, -8364, instanceId); // 20
		spawnNpc(watcher, -77905, -77808, -8364, instanceId); // 21
		spawnNpc(watcher, -79902, -77805, -8364, instanceId); // 22
		spawnNpc(watcher, -81904, -77808, -8364, instanceId); // 23
		spawnNpc(watcher, -83904, -77808, -8364, instanceId); // 24
		spawnNpc(watcher, -85904, -77807, -8364, instanceId); // 25
	}

	public final void startWatcherTask(int lastWatcher, int watcher)
	{
		ThreadPoolManager.getInstance().schedule(new ChangeWatcherTask(lastWatcher, watcher), 3000);
	}
}
