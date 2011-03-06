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
package net.l2emuproject.gameserver.services.blocklist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2Integer;
import net.l2emuproject.util.SingletonSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author NB4L1
 */
public final class BlockListService
{
	private static final Log	_log			= LogFactory.getLog(BlockListService.class);

	private static final String	SELECT_QUERY	= "SELECT charId, name FROM character_blocks";
	private static final String	INSERT_QUERY	= "INSERT INTO character_blocks (charId, name) VALUES (?,?)";
	private static final String	DELETE_QUERY	= "DELETE FROM character_blocks WHERE charId=? AND name=?";

	public static BlockListService getInstance()
	{
		return SingletonHolder._instance;
	}

	private final Map<Integer, Set<String>>	_blocks	= new FastMap<Integer, Set<String>>();

	private BlockListService()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			final PreparedStatement statement = con.prepareStatement(SELECT_QUERY);
			final ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				final Integer objectId = L2Integer.valueOf(rset.getInt("charId"));
				final String name = rset.getString("name");

				getBlockList(objectId).add(name);
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

		int size = 0;

		for (Set<String> set : _blocks.values())
			size += set.size();

		_log.info(getClass().getSimpleName() + " : Loaded " + size + " character block(s).");
	}

	public final synchronized Set<String> getBlockList(Integer objectId)
	{
		Set<String> set = _blocks.get(objectId);

		if (set == null)
			_blocks.put(objectId, set = new SingletonSet<String>());

		return set;
	}

	public final synchronized void insert(L2Player listOwner, L2Player blocked)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			final PreparedStatement statement = con.prepareStatement(INSERT_QUERY);
			statement.setInt(1, listOwner.getObjectId());
			statement.setString(2, blocked.getName());

			statement.execute();

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

	public final synchronized void remove(L2Player listOwner, String name)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			final PreparedStatement statement = con.prepareStatement(DELETE_QUERY);
			statement.setInt(1, listOwner.getObjectId());
			statement.setString(2, name);

			statement.execute();

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

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final BlockListService	_instance	= new BlockListService();
	}
}
