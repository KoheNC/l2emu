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
package net.l2emuproject.gameserver.model.entity.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.system.L2DatabaseFactory;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class PlayerCustom extends PlayerExtension
{
	private static final String	RESTORE_CUSTOM_DATA	= "SELECT hero, noble, donator FROM characters_custom_data WHERE charId = ?";
	private static final String	INSERT_CUSTOM_DATA	= "REPLACE INTO characters_custom_data (charId, char_name, hero, noble, donator) VALUES (?,?,?,?,?)";
	private static final String	DELETE_CUSTOM_DATA	= "DELETE FROM characters_custom_data WHERE charId=?";

	public enum CharCustomData
	{
		HERO, NOBLE, DONATOR, DELETE
	}

	public PlayerCustom(L2Player activeChar)
	{
		super(activeChar);
	}

	/**
	 * Restore all Custom Data:
	 * Hero, Noble, Donator
	 */
	public final void restoreCustomStatus()
	{
		Connection con = null;

		try
		{
			int hero = 0;
			int noble = 0;
			int donator = 0;

			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(RESTORE_CUSTOM_DATA);
			statement.setInt(1, getPlayer().getObjectId());

			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				hero = rset.getInt("hero");
				noble = rset.getInt("noble");
				donator = rset.getInt("donator");
			}

			rset.close();
			statement.close();

			if (hero > 0)
				getPlayer().setHero(true);

			if (noble > 0)
				getPlayer().setNoble(true);

			if (donator > 0)
				getPlayer().setDonator(true);
		}
		catch (SQLException e)
		{
			_log.error("Error: could not restore char custom data info: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final void updateCustomStatus(CharCustomData status)
	{
		Connection con = null;
		PreparedStatement statement;

		try
		{
			// Prevents any NPE.
			// ----------------
			if (getPlayer() == null)
				return;

			con = L2DatabaseFactory.getInstance().getConnection(con);

			switch (status)
			{
				case HERO:
					statement = con.prepareStatement(INSERT_CUSTOM_DATA);
					statement.setInt(1, getPlayer().getObjectId());
					statement.setString(2, getPlayer().getName());
					statement.setInt(3, 1);
					statement.setInt(4, getPlayer().isNoble() ? 1 : 0);
					statement.setInt(5, getPlayer().isDonator() ? 1 : 0);
					statement.execute();
					statement.close();
					break;
				case NOBLE:
					statement = con.prepareStatement(INSERT_CUSTOM_DATA);
					statement.setInt(1, getPlayer().getObjectId());
					statement.setString(2, getPlayer().getName());
					statement.setInt(3, getPlayer().isHero() ? 1 : 0);
					statement.setInt(4, 1);
					statement.setInt(5, getPlayer().isDonator() ? 1 : 0);
					statement.execute();
					statement.close();
					break;
				case DONATOR:
					statement = con.prepareStatement(INSERT_CUSTOM_DATA);
					statement.setInt(1, getPlayer().getObjectId());
					statement.setString(2, getPlayer().getName());
					statement.setInt(3, getPlayer().isHero() ? 1 : 0);
					statement.setInt(4, getPlayer().isNoble() ? 1 : 0);
					statement.setInt(5, 1);
					statement.execute();
					statement.close();
					break;
				case DELETE:
					statement = con.prepareStatement(DELETE_CUSTOM_DATA);
					statement.setInt(1, getPlayer().getObjectId());
					statement.execute();
					statement.close();
					break;
			}
		}
		catch (SQLException e)
		{
			_log.error("Error: could not update database: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private int	_announcecount;
	private int	_lastAnnounceDate;

	public final void setAnnounces(int count)
	{
		_announcecount = count;
	}

	public final void increaseAnnounces()
	{
		setAnnounces(getAnnounceCount() + 1);
	}

	public final int getAnnounceCount()
	{
		return _announcecount;
	}

	public final int setLastAnnounceDate()
	{
		return _lastAnnounceDate = Calendar.DAY_OF_WEEK;
	}

	public final int setDelayForNextAnnounce()
	{
		return 0;
		//TODO: create a delay between announces.
	}

	public final int getLastAnnounceDate()
	{
		return _lastAnnounceDate;
	}

	public final int getRemainingAnnounces()
	{
		return Config.MAX_ANNOUNCES_PER_DAY - getAnnounceCount();
	}
}
