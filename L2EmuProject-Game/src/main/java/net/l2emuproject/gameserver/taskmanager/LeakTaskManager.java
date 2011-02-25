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
package net.l2emuproject.gameserver.taskmanager;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.ai.FactionAggressionNotificationQueue;
import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.model.actor.reference.ImmutableReference;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.spawn.L2Spawn;
import net.l2emuproject.lang.L2Thread;
import net.l2emuproject.util.L2FastSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author NB4L1
 */
public final class LeakTaskManager
{
	private static final Log _log = LogFactory.getLog(LeakTaskManager.class);
	
	private static final long MINIMUM_DELAY_BETWEEN_CLEANUPS = TimeUnit.MINUTES.toMillis(30);
	private static final long MINIMUM_DELAY_BETWEEN_MEMORY_DUMPS = TimeUnit.HOURS.toMillis(4);

	public static LeakTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final Map<ImmutableReference<L2Player>, Long> _players = new FastMap<ImmutableReference<L2Player>, Long>().shared();
	private final Map<ImmutableReference<L2Summon>, Long> _summons = new FastMap<ImmutableReference<L2Summon>, Long>().shared();
	
	private LeakTaskManager()
	{
		new Finalizable();
		
		_log.info("LeakTaskManager: Initialized.");
	}
	
	private final class Finalizable
	{
		@Override
		protected void finalize()
		{
			System.runFinalization();
			
			ThreadPoolManager.getInstance().executeLongRunning(new Cleanup());
		}
	}
	
	private long _lastDump = System.currentTimeMillis();
	
	private final class Cleanup implements Runnable
	{
		@Override
		public void run()
		{
			synchronized (LeakTaskManager.this)
			{
				boolean shouldDump = System.currentTimeMillis() > _lastDump + MINIMUM_DELAY_BETWEEN_MEMORY_DUMPS;
				
				check(false);
				
				if (_players.size() + _summons.size() > Math.max(200, L2World.getInstance().getAllPlayersCount()))
				{
					cleanup();
					
					ThreadPoolManager.getInstance().purge();
					FactionAggressionNotificationQueue.purgeAll();
					System.gc();
					System.runFinalization();
					
					check(true);
					
					shouldDump = true;
				}
				
				if (shouldDump)
				{
					for (String line : L2Thread.getMemoryUsageStatistics())
						_log.info(line);
					
					_lastDump = System.currentTimeMillis();
				}
			}
			
			ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run()
				{
					new Finalizable();
				}
			}, MINIMUM_DELAY_BETWEEN_CLEANUPS);
		}
	}
	
	public void add(L2Player player)
	{
		_players.put(player.getImmutableReference(), System.currentTimeMillis());
	}
	
	public void add(L2Summon summon)
	{
		_summons.put(summon.getImmutableReference(), System.currentTimeMillis());
	}
	
	private synchronized void cleanup()
	{
		final L2Object[] tmp = L2World.getInstance().getAllVisibleObjects();
		
		final Set<L2Object> set = new L2FastSet<L2Object>(tmp.length);
		
		for (L2Object obj : tmp)
		{
			if (obj == null)
				continue;
			
			set.add(obj);
		}
		
		for (L2Spawn spawn : SpawnTable.getInstance().getAllTemplates().values())
		{
			if (spawn == null)
				continue;
			
			final L2Npc npc = spawn.getLastSpawn();
			
			if (npc == null)
				continue;
			
			set.add(npc);
		}
		
		final L2Object[] objects = set.toArray(new L2Object[set.size()]);
		
		_log.info("LeakTaskManager: " + _players.size() + " player(s) are waiting for cleanup.");
		for (ImmutableReference<L2Player> ref : _players.keySet())
		{
			L2Player player = ref.get();
			if (player != null)
				player.removeFromLists(objects);
		}
		
		_log.info("LeakTaskManager: " + _summons.size() + " summons(s) are waiting for cleanup.");
		for (ImmutableReference<L2Summon> ref : _summons.keySet())
		{
			L2Summon summon = ref.get();
			if (summon != null)
				summon.removeFromLists(objects);
		}
	}
	
	private static final DecimalFormat _df = new DecimalFormat("##0.00");
	
	private synchronized void check(boolean forced)
	{
		FastList<String> list = new FastList<String>();
		
		for (ImmutableReference<L2Player> ref : _players.keySet())
		{
			if (ref.get() != null)
				continue;
			
			double diff = (double)(System.currentTimeMillis() - _players.get(ref)) / 60000;
			list.add("LeakTaskManager: Removed after " + _df.format(diff) + " minutes -> (" + ref.getName() + ")");
			_players.remove(ref);
		}
		
		if (!list.isEmpty())
		{
			_log.info(list.getFirst());
			if (list.size() >= 2)
				_log.info(list.getLast());
			_log.info("LeakTaskManager: " + list.size() + " player(s) are removed.");
		}
		
		if (!list.isEmpty() || forced)
			_log.info("LeakTaskManager: " + _players.size() + " player(s) are leaking.");
		
		// ================================================================================
		
		list.clear();
		
		for (ImmutableReference<L2Summon> ref : _summons.keySet())
		{
			if (ref.get() != null)
				continue;
			
			double diff = (double)(System.currentTimeMillis() - _summons.get(ref)) / 60000;
			list.add("LeakTaskManager: Removed after " + _df.format(diff) + " minutes -> (" + ref.getName() + ")");
			_summons.remove(ref);
		}
		
		if (!list.isEmpty())
		{
			_log.info(list.getFirst());
			if (list.size() >= 2)
				_log.info(list.getLast());
			_log.info("LeakTaskManager: " + list.size() + " summon(s) are removed.");
		}
		
		if (!list.isEmpty() || forced)
			_log.info("LeakTaskManager: " + _summons.size() + " summon(s) are leaking.");
	}
	
	// ================================================================================
	
	public synchronized void clean()
	{
		check(true);
		
		cleanup();
	}
	
	public synchronized void clear()
	{
		check(true);
		
		for (L2Object obj : L2World.getInstance().getAllVisibleObjects())
			if (obj != null)
				obj.reset();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final LeakTaskManager _instance = new LeakTaskManager();
	}
}
