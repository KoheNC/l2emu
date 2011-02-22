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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.L2DatabaseFactory;
import net.l2emuproject.config.L2Properties;
import net.l2emuproject.gameserver.PersistentProperties;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Gigiikun, L2JNightFall Team
 *	<br>Remade for L2EmuProject by lord_rex
 */
public final class HellboundManager
{
	private static final Log	_log	= LogFactory.getLog(HellboundManager.class);

	private static final class SingletonHolder
	{
		private static final HellboundManager	INSTANCE	= new HellboundManager();
	}

	public static final HellboundManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private static final String					LOAD_SPAWNS				= "SELECT id, npc_templateid, locx, locy, locz, heading, "
																				+ "respawn_min_delay, respawn_max_delay, min_hellbound_level, "
																				+ "max_hellbound_level, max_trust_level, trust_points "
																				+ "FROM hellbound_spawnlist ORDER BY id";

	private static final int					POINTS_TO_OPEN_WARPGATE	= 100000;

	private int									_hellboundLevel			= 0;
	private int									_trustPoints			= 0;
	private int									_warpgateEnergy			= 0;

	private int									_maxTrustPoints			= 0;

	private ScheduledFuture<?>					_engine					= null;

	private final Map<HellboundSpawns, L2Npc>	_spawns;

	private HellboundManager()
	{
		_spawns = new FastMap<HellboundSpawns, L2Npc>();
		L2Properties props = PersistentProperties.getProperties(HellboundManager.class);
		loadProperties(props);
		saveProperties();

		_log.info(getClass().getSimpleName() + " : Initialized.");
		_log.info(getClass().getSimpleName() + " : Current Hellbound Level is " + _hellboundLevel + ".");
		_log.info(getClass().getSimpleName() + " : Current Trust Points are " + _trustPoints + ".");
		_log.info(getClass().getSimpleName() + " : Warpgates are " + (isWarpgateActive() ? "open." : "closed."));

		loadSpawns();

		registerEngine(new HellboundEngine(), 15000);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new SaveHellbound(), 24 * 60 * 60 * 1000, 24 * 60 * 60 * 1000);
	}

	private final void loadProperties(L2Properties props)
	{
		_hellboundLevel = props.getInteger("hellbound_level", 0);
		_trustPoints = props.getInteger("trust_points", 0);
		_warpgateEnergy = props.getInteger("warpgates_energy", 0);
	}

	private final void saveProperties()
	{
		PersistentProperties.addStoreListener(new PersistentProperties.StoreListener()
		{
			@Override
			public final void update()
			{
				final L2Properties properties = new L2Properties();

				properties.setProperty("hellbound_level", _hellboundLevel);
				properties.setProperty("trust_points", _trustPoints);
				properties.setProperty("warpgates_energy", _warpgateEnergy);

				PersistentProperties.setProperties(HellboundManager.class, properties);

				_log.info("HellboundManager : Current state saved.");
			}
		});
	}

	public final int getHellboundLevel()
	{
		return _hellboundLevel;
	}

	public final void setHellboundLevel(int hellboundLevel)
	{
		_hellboundLevel = hellboundLevel;
	}

	public final int getMaxTrustPoints()
	{
		return _maxTrustPoints;
	}

	public final void setMaxTrustPoints(int trust)
	{
		_maxTrustPoints = trust;
		if (_maxTrustPoints > 0 && _trustPoints > _maxTrustPoints)
			_trustPoints = _maxTrustPoints;
	}

	public final synchronized void updateTrustPoints(int t, boolean useRates)
	{
		if (isWarpgateActive())
			return;

		final int trust = Math.max(_trustPoints + (useRates ? (int) (Config.RATE_TRUST_POINT * t) : t), 0);
		if (_maxTrustPoints > 0)
			_trustPoints = Math.min(trust, _maxTrustPoints);
		else
			_trustPoints = trust;
	}

	public final int getTrustPoints()
	{
		return _trustPoints;
	}

	private final void setTrustPoints(int trustPoints)
	{
		_trustPoints = trustPoints;
	}

	public final void addTrustPoints(int trustPoints)
	{
		setTrustPoints(getTrustPoints() + trustPoints);
	}

	public final void removeTrustPoints(int trustPoints)
	{
		setTrustPoints(getTrustPoints() - trustPoints);
	}

	public int getWarpgateEnergy()
	{
		return _warpgateEnergy;
	}

	public void addWarpgateEnergy(int amount)
	{
		_warpgateEnergy += amount;
	}

	public void subWarpgateEnergy(int amount)
	{
		_warpgateEnergy = Math.max(0, _warpgateEnergy - amount);
	}

	public boolean isWarpgateActive()
	{
		return getWarpgateEnergy() >= POINTS_TO_OPEN_WARPGATE;
	}

	public final void cleanUp()
	{
		if (_engine != null)
		{
			_engine.cancel(true);
			_engine = null;
		}

		_spawns.clear();
	}

	public final void doSpawn()
	{
		int added = 0;
		int deleted = 0;
		for (HellboundSpawns spawnDat : _spawns.keySet())
		{
			try
			{
				if (spawnDat == null)
					continue;

				L2Npc npc = _spawns.get(spawnDat);
				if (_hellboundLevel < spawnDat.getMinLevel() || _hellboundLevel > spawnDat.getMaxLevel())
				{
					// npc should be removed
					spawnDat.stopRespawn();

					if (npc != null && npc.isVisible())
					{
						npc.deleteMe();
						deleted++;
					}
				}
				else
				{
					// npc should be added
					spawnDat.startRespawn();

					if (npc == null)
					{
						npc = spawnDat.doSpawn();
						_spawns.put(spawnDat, npc);
						added++;
					}
					else
					{
						if (npc.isDecayed())
							npc.setDecayed(false);
						if (npc.isDead())
							npc.doRevive();
						if (!npc.isVisible())
							added++;

						npc.getStatus().setCurrentHp(npc.getMaxHp());
						npc.getStatus().setCurrentMp(npc.getMaxMp());
						npc.spawnMe(spawnDat.getLocx(), spawnDat.getLocy(), spawnDat.getLocz());
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (added > 0)
			_log.info(getClass().getSimpleName() + " : Spawned " + added + " NPCs.");
		if (deleted > 0)
			_log.info(getClass().getSimpleName() + " : Removed " + deleted + " NPCs.");
	}

	private final void loadSpawns()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(LOAD_SPAWNS);
			ResultSet rset = statement.executeQuery();

			HellboundSpawns spawnDat;
			L2NpcTemplate template;

			while (rset.next())
			{
				template = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template != null)
				{
					spawnDat = new HellboundSpawns(template);
					spawnDat.setAmount(1);
					spawnDat.setId(rset.getInt("id"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnMinDelay(rset.getInt("respawn_min_delay"));
					spawnDat.setRespawnMaxDelay(rset.getInt("respawn_max_delay"));
					spawnDat.setMinLevel(rset.getInt("min_hellbound_level"));
					spawnDat.setMaxLevel(rset.getInt("max_hellbound_level"));
					spawnDat.setMaxTrustLevel(rset.getInt("max_trust_level"));
					spawnDat.setTrustPoints(rset.getInt("trust_points"));

					// Random respawn time, if needed
					if (spawnDat.getRespawnMaxDelay() > spawnDat.getRespawnMinDelay())
						spawnDat.setRespawnDelay(Rnd.get(spawnDat.getRespawnMinDelay(), spawnDat.getRespawnMaxDelay()));
					else
						spawnDat.setRespawnDelay(spawnDat.getRespawnMinDelay());

					_spawns.put(spawnDat, null);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				}
				else
				{
					_log.warn(getClass().getSimpleName() + " : Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + " : Problem while loading spawns: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		_log.info(getClass().getSimpleName() + " : Loaded " + _spawns.size() + " NPC Spawn Locations.");
	}

	private final void registerEngine(Runnable r, int interval)
	{
		if (_engine != null)
			_engine.cancel(false);

		_engine = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(r, interval, interval);
	}

	private static final class SaveHellbound implements Runnable
	{
		@Override
		public final void run()
		{
			L2Properties props = PersistentProperties.getProperties(HellboundManager.class);
			getInstance().saveProperties();
			getInstance().loadProperties(props);
		}
	}
}
