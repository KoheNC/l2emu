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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author lord_rex
 * 	<br> Global Drop table in SQL for events.
 */
public final class GlobalDropTable
{
	private static final Log	_log		= LogFactory.getLog(GlobalDropTable.class);
	private static final String	LOAD_QUERRY	= "SELECT itemId, countMin, countMax, chance, dateStart, dateEnd FROM global_drops WHERE eventId";

	private static final class SingletonHolder
	{
		private static final GlobalDropTable	INSTANCE	= new GlobalDropTable();
	}

	public static GlobalDropTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private final List<GlobalDrop>	_table	= new ArrayList<GlobalDrop>();

	private GlobalDropTable()
	{
		load();
	}

	private void load()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement(LOAD_QUERRY);
			final ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				final int itemId = rset.getInt("itemId");
				final int countMin = rset.getInt("countMin");
				final int countMax = rset.getInt("countMax");
				final int chance = rset.getInt("chance");
				final Timestamp dateStart = rset.getTimestamp("dateStart");
				final Timestamp dateEnd = rset.getTimestamp("dateEnd");

				_table.add(new GlobalDrop(itemId, countMin, countMax, chance, dateStart, dateEnd));
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

		_log.info(getClass().getSimpleName() + " : Loaded " + _table.size() + " global drop(s).");
	}

	public final class GlobalDrop
	{
		private final int		_itemId;
		private final int		_countMin;
		private final int		_countMax;
		private final int		_chance;
		private final Timestamp	_dateStart;
		private final Timestamp	_dateEnd;

		private GlobalDrop(final int itemId, final int countMin, final int countMax, final int chance, final Timestamp dateStart, final Timestamp dateEnd)
		{
			_itemId = itemId;
			_countMin = countMin;
			_countMax = countMax;
			_chance = chance;
			_dateStart = dateStart;
			_dateEnd = dateEnd;
		}

		public int getItemId()
		{
			return _itemId;
		}

		public int getCountMin()
		{
			return _countMin;
		}

		public int getCountMax()
		{
			return _countMax;
		}

		public int getChance()
		{
			return _chance;
		}

		public Timestamp getDateStart()
		{
			return _dateStart;
		}

		public Timestamp getDateEnd()
		{
			return _dateEnd;
		}
	}

	public final void addGlobalDrop(final int itemId, final int countMin, final int countMax, final int chance, final Timestamp dateStart,
			final Timestamp dateEnd)
	{
		_table.add(new GlobalDrop(itemId, countMin, countMax, chance, dateStart, dateEnd));
	}

	public final ArrayList<GlobalDrop> getDrops()
	{
		final ArrayList<GlobalDrop> list = new ArrayList<GlobalDrop>();

		for (GlobalDrop drop : _table)
		{
			final Timestamp startDate = drop.getDateStart();
			final Timestamp endDate = drop.getDateEnd();
			final long current = System.currentTimeMillis();

			if (startDate == null && endDate == null || current >= startDate.getTime() && current <= endDate.getTime())
				list.add(drop);
		}

		return list;
	}
}
