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
package net.l2emuproject.gameserver.manager.boss;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.instance.L2RaidBossInstance;
import net.l2emuproject.gameserver.world.spawn.L2Spawn;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author lord_rex
 *	<br> new RaidBossManager for L2EmuProject!
 *
 * @since 4/12/2011
 */
public final class RaidBossManager
{
	private static final Log	_log	= LogFactory.getLog(RaidBossManager.class);

	private static final class SingletonHolder
	{
		private static final RaidBossManager	INSTANCE	= new RaidBossManager();
	}

	public static RaidBossManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private static final String						UPDATE_QUERY			= "UPDATE raidboss_status SET currentHp = ?, currentMp = ?, respawn_time =? WHERE bossId = ?";
	private static final String						REPLACE_QUERY			= "REPLACE INTO raidboss_status (bossId, currentHp, currentMp, respawn_time) VALUES (?, ?, ?, ?)";
	private static final String						SELECT_QUERY			= "SELECT * FROM raidboss_status";

	private static final int						MINIMUM_RESPAWN_DELAY	= (int) (43200 * 1000 * Config.RAID_MIN_RESPAWN_MULTIPLIER);
	private static final int						MAXIMUM_RESPAWN_DELAY	= (int) (129600 * 1000 * Config.RAID_MAX_RESPAWN_MULTIPLIER);
	private static final long						RESPAWN_DELAY			= Rnd.get(MINIMUM_RESPAWN_DELAY, MAXIMUM_RESPAWN_DELAY);

	private final Map<Integer, L2RaidBossInstance>	_bosses					= new FastMap<Integer, L2RaidBossInstance>(250).shared();
	private final Map<Integer, ScheduledFuture<?>>	_schedules				= new FastMap<Integer, ScheduledFuture<?>>(250).shared();
	private final Map<Integer, L2Spawn>				_spawns					= new FastMap<Integer, L2Spawn>(250).shared();
	private final Map<Integer, StoredInfo>			_storedInfo				= new FastMap<Integer, StoredInfo>().shared();

	private RaidBossManager()
	{
		loadDB();
		loadData();

		_log.info(getClass().getSimpleName() + " : Loaded " + _bosses.size() + "/" + _spawns.size() + " spawn(s).");
		_log.info(getClass().getSimpleName() + " : Loaded " + _schedules.size() + "/" + _spawns.size() + " spawn schedule(s).");
	}

	private final class StoredInfo
	{
		private double		_currentHp, _currentMp;
		private Timestamp	_respawnTime;

		public void setCurrentHp(final double currentHp)
		{
			_currentHp = currentHp;
		}

		public double getCurrentHp()
		{
			return _currentHp;
		}

		public void setCurrentMp(final double currentMp)
		{
			_currentMp = currentMp;
		}

		public double getCurrentMp()
		{
			return _currentMp;
		}

		public void setRespawnTime(final Timestamp respawnTime)
		{
			_respawnTime = respawnTime;
		}

		public Timestamp getRespawnTime()
		{
			return _respawnTime;
		}
	}

	private void loadData()
	{
		Document doc = null;
		final File xml = new File(Config.DATAPACK_ROOT, "data/npc_data/spawns/boss/raid_spawns.xml");

		L2Spawn spawnData = null;
		L2NpcTemplate npcTemplate = null;

		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(xml);

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("spawnList".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						int bossId = 0, x = 0, y = 0, z = 0;

						if ("spawn".equalsIgnoreCase(d.getNodeName()))
						{
							bossId = Integer.parseInt(d.getAttributes().getNamedItem("bossId").getNodeValue());
							x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
							y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
							z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());

							npcTemplate = NpcTable.getInstance().getTemplate(bossId);
							spawnData = new L2Spawn(npcTemplate);
							spawnData.setLocx(x);
							spawnData.setLocy(y);
							spawnData.setLocz(z);
							spawnData.setAmount(1);
							spawnData.setHeading(0);
							spawnData.setInstanceId(0);

							init(spawnData);
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			_log.warn("Can not find " + xml.getAbsolutePath() + " !", e);
		}
		catch (Exception e)
		{
			_log.warn("Error while loading " + xml.getAbsolutePath() + " !", e);
		}
	}

	private void loadDB()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement(SELECT_QUERY);
			final ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				final StoredInfo info = new StoredInfo();
				final int bossId = rset.getInt("bossId");
				info.setCurrentHp(rset.getDouble("currentHp"));
				info.setCurrentMp(rset.getDouble("currentMp"));
				info.setRespawnTime(rset.getTimestamp("respawn_time"));
				_storedInfo.put(bossId, info);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void updateDB(final int bossId, final boolean updateDB)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement(updateDB ? UPDATE_QUERY : REPLACE_QUERY);

			final L2RaidBossInstance boss = _bosses.get(bossId);
			final StoredInfo info = _storedInfo.get(bossId);

			statement.setInt(1, bossId);
			statement.setDouble(2, info.getCurrentHp());
			statement.setDouble(3, info.getCurrentMp());
			statement.setTimestamp(4, info.getRespawnTime());
			statement.execute();
			statement.close();

			_log.info(getClass().getSimpleName() + " : Database updated for raidboss " + boss.getName() + ", status: "
					+ (info.getCurrentHp() < 1 ? "DEAD." : "LIVE."));
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void updateDeathStatus(final L2RaidBossInstance boss)
	{
		final int bossId = boss.getNpcId();
		final StoredInfo info = _storedInfo.get(bossId);

		if (!_bosses.containsKey(bossId))
			return;

		info.setCurrentHp(boss.getCurrentHp());
		info.setCurrentMp(boss.getCurrentMp());
		info.setRespawnTime(new Timestamp(System.currentTimeMillis() + RESPAWN_DELAY));
		_storedInfo.put(bossId, info);

		_log.info(getClass().getSimpleName() + " : Raidboss " + boss.getName() + " has DEAD, updated respawn time to " + info.getRespawnTime() + ".");

		if (!_schedules.containsKey(bossId))
		{
			final long respawnDelay = info.getRespawnTime().getTime() - System.currentTimeMillis();
			final ScheduledFuture<?> futureSpawn = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(bossId), respawnDelay);
			_schedules.put(bossId, futureSpawn);
			updateDB(bossId, false);
		}
	}

	private final class SpawnTask implements Runnable
	{
		private final int	_bossId;

		private SpawnTask(final int bossId)
		{
			_bossId = bossId;
		}

		@Override
		public void run()
		{
			final L2RaidBossInstance boss = (L2RaidBossInstance) _spawns.get(_bossId).doSpawn();

			if (boss != null)
			{
				final StoredInfo info = _storedInfo.get(_bossId);

				boss.getStatus().setCurrentHp(boss.getMaxHp());
				boss.getStatus().setCurrentMp(boss.getMaxMp());

				info.setCurrentHp(boss.getMaxHp());
				info.setCurrentMp(boss.getMaxMp());
				info.setRespawnTime(null);

				_storedInfo.put(_bossId, info);
				_bosses.put(_bossId, boss);
				updateDB(_bossId, false);
			}

			_log.info(RaidBossManager.class.getSimpleName() + " : Raidboss " + _bosses.get(_bossId) + " has spawned.");

			if (_schedules.containsKey(_bossId))
				_schedules.remove(_bossId);
		}
	}

	private void init(final L2Spawn spawnData)
	{
		final int bossId = spawnData.getNpcId();
		final StoredInfo info = _storedInfo.get(bossId);
		final Timestamp respawn = info.getRespawnTime();

		if (dateIsOk(bossId))
		{
			final L2RaidBossInstance boss = (L2RaidBossInstance) spawnData.doSpawn();

			if (boss != null)
			{
				boss.getStatus().setCurrentHp(info.getCurrentHp());
				boss.getStatus().setCurrentMp(info.getCurrentMp());

				if (respawn != null)
				{
					if (respawn.getTime() < System.currentTimeMillis())
					{
						boss.getStatus().setCurrentHp(boss.getMaxHp());
						boss.getStatus().setCurrentMp(boss.getMaxMp());
						info.setRespawnTime(null);
						_storedInfo.put(bossId, info);
					}
				}
				_bosses.put(bossId, boss);
			}
		}
		else
		{
			if (respawn != null)
			{
				final long respawnDelay = respawn.getTime() - System.currentTimeMillis();
				final ScheduledFuture<?> futureSpawn = ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(bossId), respawnDelay);
				_schedules.put(bossId, futureSpawn);
			}
		}

		_spawns.put(bossId, spawnData);
	}

	private boolean dateIsOk(final int bossId)
	{
		final StoredInfo info = _storedInfo.get(bossId);
		final Timestamp respawnTime = info.getRespawnTime();
		final long current = System.currentTimeMillis();

		if (respawnTime == null)
			return true;

		if (respawnTime.getTime() > current)
			return false;

		return true;
	}

	public void cleanUP()
	{
		for (Integer bossId : _bosses.keySet())
			updateDB(bossId, true);

		_bosses.clear();

		if (_schedules != null)
		{
			for (Integer bossId : _schedules.keySet())
			{
				ScheduledFuture<?> f = _schedules.get(bossId);
				f.cancel(true);
			}
		}

		_storedInfo.clear();
		_schedules.clear();
		_spawns.clear();
	}

	public boolean isRaidBoss(final int bossId)
	{
		return _bosses.containsKey(bossId);
	}
}
