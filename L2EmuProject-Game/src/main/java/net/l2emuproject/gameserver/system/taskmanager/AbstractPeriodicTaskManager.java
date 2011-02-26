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
package net.l2emuproject.gameserver.system.taskmanager;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.l2emuproject.gameserver.L2GameServer;
import net.l2emuproject.gameserver.L2GameServer.StartupHook;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author NB4L1
 */
public abstract class AbstractPeriodicTaskManager implements Runnable, StartupHook
{
	protected static final Log _log = LogFactory.getLog(AbstractPeriodicTaskManager.class);
	
	private final ReentrantReadWriteLock _lock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock.ReadLock _readLock = _lock.readLock();
	private final ReentrantReadWriteLock.WriteLock _writeLock = _lock.writeLock();
	
	private final int _period;
	
	public AbstractPeriodicTaskManager(int period)
	{
		_period = period;
		
		L2GameServer.addStartupHook(this);
		
		_log.info(getClass().getSimpleName() + " : Initialized.");
	}
	
	public final void readLock()
	{
		_readLock.lock();
	}
	
	public final void readUnlock()
	{
		_readLock.unlock();
	}
	
	public final void writeLock()
	{
		_writeLock.lock();
	}
	
	public final void writeUnlock()
	{
		_writeLock.unlock();
	}
	
	@Override
	public final void onStartup()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000 + Rnd.get(_period), Rnd.get(_period - 5, _period + 5));
	}
	
	@Override
	public abstract void run();
}
