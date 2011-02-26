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

import java.sql.Connection;
import java.sql.SQLException;

import net.l2emuproject.gameserver.system.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.sql.SQLQueryQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author NB4L1
 */
public final class SQLQueue extends SQLQueryQueue
{
	private static final Log _log = LogFactory.getLog(SQLQueue.class);
	
	private static final class SingletonHolder
	{
		private static final SQLQueue INSTANCE = new SQLQueue();
	}
	
	public static SQLQueue getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private SQLQueue()
	{
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 60000, 60000);
		
		_log.info(getClass().getSimpleName() + " : Initialized.");
	}
	
	@Override
	protected Connection getConnection() throws SQLException
	{
		return L2DatabaseFactory.getInstance().getConnection();
	}
}
