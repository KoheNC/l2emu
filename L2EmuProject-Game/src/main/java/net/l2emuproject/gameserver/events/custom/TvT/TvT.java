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
package net.l2emuproject.gameserver.events.custom.TvT;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.events.L2Event;
import net.l2emuproject.gameserver.events.L2EventTeam;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.system.restriction.global.TvTRestriction;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.system.util.Broadcast;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author lord_rex
 *	 same Announcements messages/Automatization by savormix, thanks.
 */
public final class TvT extends L2Event
{
	private static final class SingletonHolder
	{
		private static final TvT	INSTANCE	= new TvT();
	}

	public static TvT getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private final Map<Integer, L2Player>	_gamers;
	private final L2EventTeam[]				_teams;

	private int								_instanceId	= 0;

	private final AutoEventTask				_task;
	private volatile byte					_status;
	private short							_announced;
	private ScheduledFuture<?>				_event;

	private TvT()
	{
		_status = STATUS_NOT_IN_PROGRESS;
		_announced = 0;
		_task = new AutoEventTask();

		_gamers = new FastMap<Integer, L2Player>(Config.TVT_PARTICIPANTS_MAX);
		_teams = new L2EventTeam[2];

		_teams[0] = new L2EventTeam(Config.TVT_FIRST_TEAM_NAME, Config.TVT_FIRST_TEAM_COLOR, Config.TVT_FIRST_TEAM_COORDS);
		_teams[1] = new L2EventTeam(Config.TVT_SECOND_TEAM_NAME, Config.TVT_SECOND_TEAM_COLOR, Config.TVT_SECOND_TEAM_COORDS);

		_log.info("TvT : Initialized.");
	}

	private final class AutoEventTask implements Runnable
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
					if (_announced < (Config.TVT_REGISTRATION_ANNOUNCEMENT_COUNT + 2))
						registrationAnnounce();
					else
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
					ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.TVT_DELAY_BETWEEN_EVENTS);
					break;
				default:
					_log.fatal("Incorrect status set in TvT, terminating the event!");
					break;
			}
		}
	}

	public final void init()
	{
		if (Config.TVT_ENABLED)
		{
			final String text = "TvT : Registration is starting in " + Config.TVT_DELAY_INITIAL_REGISTRATION / 1000 + " seconds!";
			_log.info(text);
			Announcements.getInstance().announceToAll(text);
			ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.TVT_DELAY_INITIAL_REGISTRATION);
		}
		else
			_log.info("TvT : This event is disabled.");
	}

	@Override
	public final void startRegistration()
	{
		_status = STATUS_REGISTRATION;

		TvTRestriction.getInstance().activate();

		Announcements.getInstance().announceToAll(SystemMessageId.REGISTRATION_PERIOD);
		SystemMessage time = new SystemMessage(SystemMessageId.REGISTRATION_TIME_S1_S2_S3);
		long timeLeft = Config.TVT_PERIOD_LENGHT_REGISTRATION / 1000;
		time.addNumber((int) (timeLeft / 3600));
		time.addNumber((int) (timeLeft % 3600 / 60));
		time.addNumber((int) (timeLeft % 3600 % 60));
		Broadcast.toAllOnlinePlayers(time);
		Announcements.getInstance().announceToAll("To join the TvT you must type .jointvt");

		_log.info("TvT : Registration period started!");
		ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.TVT_PERIOD_LENGHT_REGISTRATION / (Config.TVT_REGISTRATION_ANNOUNCEMENT_COUNT + 2));
	}

	@Override
	public final void registrationAnnounce()
	{
		SystemMessage time = new SystemMessage(SystemMessageId.REGISTRATION_TIME_S1_S2_S3);
		long timeLeft = Config.TVT_PERIOD_LENGHT_REGISTRATION;
		long elapsed = timeLeft / (Config.TVT_REGISTRATION_ANNOUNCEMENT_COUNT + 2) * _announced;
		timeLeft -= elapsed;
		timeLeft /= 1000;
		time.addNumber((int) (timeLeft / 3600));
		time.addNumber((int) (timeLeft % 3600 / 60));
		time.addNumber((int) (timeLeft % 3600 % 60));
		Broadcast.toAllOnlinePlayers(time);
		Announcements.getInstance().announceToAll("To join the TvT you must type .jointvt");

		_announced++;
		ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.TVT_PERIOD_LENGHT_REGISTRATION / (Config.TVT_REGISTRATION_ANNOUNCEMENT_COUNT + 2));
	}

	@Override
	public final void endRegistration()
	{
		_announced = 0;
		_status = STATUS_PREPARATION;

		if (_gamers.size() < Config.TVT_PARTICIPANTS_MIN)
		{
			Announcements.getInstance().announceToAll("TvT won't start, not enough players!");
			_gamers.clear();
			_status = STATUS_NOT_IN_PROGRESS;
			_log.info("TvT : Won't start, not enough players!");
			ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.TVT_DELAY_BETWEEN_EVENTS);
			return;
		}

		initInstance();

		byte teamId = 0;

		for (L2Player player : _gamers.values())
		{
			// Check to which team the player should be added
			if (_teams[0].getPlayersCount() == _teams[1].getPlayersCount())
			{
				teamId = (byte) (Rnd.get(2));
			}
			else
			{
				teamId = (byte) (_teams[0].getPlayersCount() > _teams[1].getPlayersCount() ? 1 : 0);
			}

			_teams[teamId].addPlayer(player);
			setTeamColor(player, _teams[getPlayerTeamId(player)].getColor());
			teleportPlayer(player, _teams[getPlayerTeamId(player)].getCoords(), _instanceId);
			player.setIsPetrified(true);
			sendMessage(player, "You are petrified to avoid cheats! Battle starts in " + Config.TVT_PERIOD_LENGHT_PREPARATION / 1000 + " seconds.", 5000);
		}

		_log.info("TvT : Registration to TvT is over! TvT Event is starting in " + Config.TVT_PERIOD_LENGHT_PREPARATION / 1000 + " seconds!");
		ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.TVT_PERIOD_LENGHT_PREPARATION);
	}

	@Override
	public final void startEvent()
	{
		_status = STATUS_COMBAT;

		for (L2Player player : _gamers.values())
		{
			player.setIsPetrified(false);
			sendMessage(player, "Petrification is ended now you can fight! Battle ends in " + Config.TVT_PERIOD_LENGHT_EVENT / 1000 + " seconds.", 5000);
		}

		_log.info("TvT : Event is in progress, let's fight! You have " + Config.TVT_PERIOD_LENGHT_EVENT / 1000 + " seconds to fight!");
		_event = ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.TVT_PERIOD_LENGHT_EVENT);
	}

	@Override
	public final void endEvent()
	{
		if (_status != STATUS_COMBAT)
			return;
		_status = STATUS_REWARDS;
		if (!_event.cancel(false))
			return;

		int winnerTeam = getWinnerTeam();

		for (L2Player player : _gamers.values())
		{
			if (getPlayerTeamId(player) == winnerTeam)
			{
				giveRewards(player);
			}

			player.getPlayerEventData().setPoints(0);
			player.getPlayerEventData().setDeathPoints(0);

			// After TvT, teleport back players to Giran.
			int coords[] =
			{ 82698, 148638, -3473 };
			teleportPlayer(player, coords, 0);

			// Set color to default.
			player.getAppearance().updateNameTitleColor();
		}
		_gamers.clear();
		_teams[0].clear();
		_teams[1].clear();

		TvTRestriction.getInstance().deactivate();

		if (winnerTeam != -1)
		{
			Announcements.getInstance().announceToAll("TvT : Team " + getEventTeams()[winnerTeam].getTeamName() + " wins!");
			Announcements.getInstance().announceToAll("TvT : Cumulative score: " + getEventTeams()[winnerTeam].getPoints());
			_log.info("TvT : Event is over! Team " + getEventTeams()[winnerTeam].getTeamName() + " wins!");
		}
		else
		{
			Announcements.getInstance().announceToAll("TvT : There is no winner team.");
			_log.info("TvT : Event is over! There is no winner team.");
		}

		ThreadPoolManager.getInstance().scheduleGeneral(_task, Config.TVT_PERIOD_LENGHT_REWARDS);
	}

	@Override
	protected final void giveRewards(final L2Player player)
	{
		final int deathPoints = player.getPlayerEventData().getDeathPoints();
		final int points = player.getPlayerEventData().getPoints();

		if (points < Config.REQUIRED_KILLS_FOR_REWARD)
		{
			Announcements.getInstance().announceToAll("TvT : Not enough kills to earn reward. Required kills are " + Config.REQUIRED_KILLS_FOR_REWARD + ".");
			return;
		}

		if (deathPoints == 0)
			Announcements.getInstance().announceToAll("TvT : Player " + player.getName() + " is God-like!");

		for (int i = 0; i < Config.TVT_REWARD_IDS.length; i++)
		{
			player.addItem("TvT Reward", Config.TVT_REWARD_IDS[i], Config.TVT_REWARD_COUNT[i], null, false, true);
			player.sendPacket(new SystemMessage(SystemMessageId.CONGRATULATIONS_RECEIVED_S1).addItemName(Config.TVT_REWARD_IDS[i]));
		}
	}

	@Override
	protected final boolean canJoin(final L2Player player)
	{
		if (_status != STATUS_REGISTRATION || _gamers.size() >= Config.TVT_PARTICIPANTS_MAX)
		{
			sendMessage(player, "You are too late, there is no registration period.", 5000);
			return false;
		}

		if (player.getLevel() < Config.MINIMUM_LEVEL_FOR_TVT)
		{
			sendMessage(player, "Your level is too low for TvT.", 5000);
			return false;
		}

		if (player.getLevel() > Config.MAXIMUM_LEVEL_FOR_TVT)
		{
			sendMessage(player, "Your level is too high for TvT.", 5000);
			return false;
		}

		if (_gamers.containsKey(player.getObjectId()))
		{
			sendMessage(player, "You are already registered to TvT.", 5000);
			return false;
		}

		return true;
	}

	@Override
	public final void registerPlayer(final L2Player player)
	{
		if (!canJoin(player))
			return;

		_gamers.put(player.getObjectId(), player);
		sendMessage(player, "You are registered to TvT.", 5000);
	}

	@Override
	public final void cancelRegistration(final L2Player player)
	{
		if (_gamers.containsKey(player.getObjectId()))
		{
			_gamers.remove(player.getObjectId());
			sendMessage(player, "You are not registered to TvT anymore.", 5000);
		}
	}

	@Override
	public final void removeDisconnected(final L2Player player)
	{
		_gamers.remove(player.getObjectId());
	}

	@Override
	protected final void initInstance()
	{
		try
		{
			_instanceId = InstanceManager.getInstance().createDynamicInstance(Config.TVT_INSTANCE_FILE);
			InstanceManager.getInstance().getInstance(_instanceId).setAllowSummon(false);
			InstanceManager.getInstance().getInstance(_instanceId).setPvPInstance(true);
			InstanceManager.getInstance().getInstance(_instanceId).setEmptyDestroyTime(Config.TVT_START_LEAVE_TELEPORT_DELAY);
			_log.info(getClass().getSimpleName() + " : Instance " + _instanceId + " is created, empty destroy time is " + Config.TVT_START_LEAVE_TELEPORT_DELAY
					+ ".");
		}
		catch (Exception e)
		{
			_instanceId = 0;
			_log.warn("TvT[initInstance]: Exception: " + e.getMessage(), e);
		}
	}

	private final void reviveTask(final L2Player player)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new ReviveTask(player, _teams[getPlayerTeamId(player)].getCoords(), _instanceId),
				Config.TVT_REVIVE_DELAY);
	}

	public static final void revive(final L2Player player)
	{
		getInstance().sendMessage(player, "You will be revived in " + Config.TVT_REVIVE_DELAY / 1000 + " seconds!", 5000);
		getInstance().reviveTask(player);
	}

	public static final boolean isInProgress()
	{
		switch (getInstance()._status)
		{
			case STATUS_PREPARATION:
			case STATUS_COMBAT:
				return true;
			default:
				return false;
		}
	}

	public static final boolean isPlaying(final L2Player player)
	{
		return isInProgress() && isMember(player.getObjectId());
	}

	private final static boolean isMember(final int objectId)
	{
		return getInstance()._gamers.containsKey(objectId);
	}

	public static byte getPlayerTeamId(final L2Player player)
	{
		return (byte) (getInstance()._teams[0].containsPlayer(player) ? 0 : (getInstance()._teams[1].containsPlayer(player) ? 1 : -1));
	}

	private final int getWinnerTeam()
	{
		int maxPts = 0, winTeam = -1, temp;
		for (int i = 0; i < _teams.length; i++)
		{
			temp = _teams[i].getPoints();
			if (temp > maxPts)
			{
				maxPts = temp;
				winTeam = i;
			}
		}
		return winTeam;
	}

	public static L2EventTeam[] getEventTeams()
	{
		return getInstance()._teams;
	}

	public static final void givePoint(final L2Player killer)
	{
		L2EventTeam team = getInstance()._teams[getPlayerTeamId(killer)];
		team.addPoint();
		Announcements.getInstance().announceToAll("TvT : Number " + team.getTeamName() + " team points : " + team.getPoints());
	}

	public static final void removePoint(final L2Player target)
	{
		L2EventTeam team = getInstance()._teams[getPlayerTeamId(target)];
		team.removePoint();
		Announcements.getInstance().announceToAll("TvT : Number " + team.getTeamName() + " team points : " + team.getPoints());
	}
}
