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
import java.util.Calendar;

import net.l2emuproject.L2DatabaseFactory;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

public class PlayerBirthday extends PlayerExtension
{
	private static final String	GET_CREATION_DATE	= "SELECT lastClaim,birthDate FROM character_birthdays WHERE charId=?";
	private static final String	CLAIM_CREATION_DAY	= "UPDATE character_birthdays SET lastClaim=? WHERE charId=?";

	private int					_lastClaim;
	private Calendar			_createdOn;

	public PlayerBirthday(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	public final void restoreCreationDate()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(GET_CREATION_DATE);
			ps.setInt(1, getPlayer().getObjectId());
			ResultSet rs = ps.executeQuery();
			rs.next();
			_lastClaim = rs.getInt("lastClaim");
			_createdOn = Calendar.getInstance();
			_createdOn.setTimeInMillis(rs.getDate("birthDate").getTime());
			rs.close();
			ps.close();
		}
		catch (Exception e)
		{
			_log.error("Could not load character creation date!", e);
			_lastClaim = Calendar.getInstance().get(Calendar.YEAR);
			_createdOn = Calendar.getInstance();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Update the database indicating that the player received this year's
	 * birthday gift. Should be called before giving items.
	 * @return whether updating gift receival status succeeds
	 */
	public final boolean claimCreationPrize()
	{
		_lastClaim = Calendar.getInstance().get(Calendar.YEAR);
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(CLAIM_CREATION_DAY);
			ps.setInt(1, _lastClaim);
			ps.setInt(2, getPlayer().getObjectId());
			ps.executeUpdate();
			ps.close();
			return true;
		}
		catch (Exception e)
		{
			_log.error(this + " could not claim creation day prize!", e);
			return false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/** @return Calendar object representing the exact creation date. */
	public final Calendar getCreationDate()
	{
		if (_createdOn == null) // this cannot happen, but just in-case
		{
			_log.error("Illegal method call: before load() -ing the actual player instance!", new Exception());
			restoreCreationDate();
		}
		return _createdOn;
	}

	/** @return true if birthday is on Feb 29th, false otherwise */
	public final boolean isBirthdayIllegal()
	{
		return (getCreationDate().get(Calendar.MONTH) == Calendar.FEBRUARY && getCreationDate().get(Calendar.DAY_OF_MONTH) == 29);
	}

	private final int getDaysUntilAnniversary()
	{
		Calendar now = Calendar.getInstance();
		Calendar birth = getCreationDate();
		int day = birth.get(Calendar.DAY_OF_MONTH);
		if (isBirthdayIllegal())
			day = 28;
		if (birth.get(Calendar.MONTH) == now.get(Calendar.MONTH) && day == now.get(Calendar.DAY_OF_MONTH))
		{
			if (now.get(Calendar.YEAR) > _lastClaim)
				return 0;
			else
				return 365;
		}

		Calendar anno = Calendar.getInstance();
		anno.setTimeInMillis(birth.getTimeInMillis());
		anno.set(Calendar.DAY_OF_MONTH, day);
		anno.set(Calendar.YEAR, now.get(Calendar.YEAR));

		if (anno.compareTo(now) < 0)
			anno.add(Calendar.YEAR, 1);
		long diff = (anno.getTimeInMillis() - now.getTimeInMillis());
		return (int) (diff / 86400000) + 1;
	}

	/**
	 * Returns -1 if this day is the anniversary day, but the player
	 * has already received the gift. Otherwise returns how many days
	 * are left until the birthday.<BR>
	 * 0 indicates that player can receive the gift.
	 * @return days until anniversary or -1
	 */
	public final int canReceiveAnnualPresent()
	{
		if (_lastClaim < Calendar.getInstance().get(Calendar.YEAR))
			return getDaysUntilAnniversary();
		else
			return -1;
	}
}
