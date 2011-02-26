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
package net.l2emuproject.gameserver.entity.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.l2emuproject.gameserver.system.L2DatabaseFactory;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class PlayerCertification extends PlayerExtension
{
	private static final String	STORE_CHAR_CERTIFICATION	= "INSERT INTO character_subclass_certification (charId,class_index,certif_level) VALUES (?,?,?)";
	private static final String	UPDATE_CHAR_CERTIFICATION	= "UPDATE character_subclass_certification SET certif_level=? WHERE charId=? AND class_index=?";
	private static final String	DELETE_CHAR_CERTIFICATION	= "DELETE FROM character_subclass_certification WHERE charId=?";
	private static final String	GET_CHAR_CERTIFICATION		= "SELECT certif_level FROM character_subclass_certification WHERE charId=? AND class_index=?";

	public PlayerCertification(L2Player activeChar)
	{
		super(activeChar);
	}

	public final int getCertificationLevel(int classIndex)
	{
		Connection con = null;
		int certificationLevel = -1;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(GET_CHAR_CERTIFICATION);

			statement.setInt(1, getPlayer().getObjectId());
			statement.setInt(2, classIndex);

			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				certificationLevel = rset.getInt("certif_level");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not get subclass certification level: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return certificationLevel;
	}

	public final void storeCertificationLevel(int classIndex)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(STORE_CHAR_CERTIFICATION);

			statement.setInt(1, getPlayer().getObjectId());
			statement.setInt(2, classIndex);
			statement.setInt(3, 0);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store character subclass certification: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final void updateCertificationLevel(int classIndex, int level)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_CERTIFICATION);

			statement.setInt(1, level);
			statement.setInt(2, getPlayer().getObjectId());
			statement.setInt(3, classIndex);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not update character subclass certification: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final void deleteSubclassCertifications()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_CERTIFICATION);

			statement.setInt(1, getPlayer().getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not delete character subclass certifications: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
