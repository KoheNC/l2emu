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
package net.l2emuproject.gameserver.services.couple;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.system.L2DatabaseFactory;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.SingletonList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author evill33t
 */
public final class CoupleService
{
	private static final Log	_log	= LogFactory.getLog(CoupleService.class);

	public static final CoupleService getInstance()
	{
		return SingletonHolder._instance;
	}

	private CoupleService()
	{
		load();
	}

	// =========================================================
	// Data Field
	private List<Couple>	_couples;

	// =========================================================
	// Method - Public
	public void reload()
	{
		getCouples().clear();
		load();
	}

	// =========================================================
	// Method - Private
	private final void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			final PreparedStatement statement = con.prepareStatement("Select id from couples order by id");
			final ResultSet rs = statement.executeQuery();

			while (rs.next())
			{
				getCouples().add(new Couple(rs.getInt("id")));
			}

			statement.close();

			_log.info(getClass().getSimpleName() + " : Loaded " + getCouples().size() + " couples(s)");
		}
		catch (Exception e)
		{
			_log.error("Exception: CoupleService.load(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	// =========================================================
	// Property - Public
	public final Couple getCouple(int coupleId)
	{
		final int index = getCoupleIndex(coupleId);
		if (index >= 0)
			return getCouples().get(index);
		return null;
	}

	public final void createCouple(L2Player player1, L2Player player2)
	{
		if (player1 != null && player2 != null)
		{
			if (player1.getPartnerId() == 0 && player2.getPartnerId() == 0)
			{
				final int player1id = player1.getObjectId();
				final int player2id = player2.getObjectId();

				final Couple newCouple = new Couple(player1, player2);
				getCouples().add(newCouple);
				player1.setPartnerId(player2id);
				player2.setPartnerId(player1id);
				player1.setCoupleId(newCouple.getId());
				player2.setCoupleId(newCouple.getId());
			}
		}
	}

	public final void deleteCouple(int coupleId)
	{
		final int index = getCoupleIndex(coupleId);
		final Couple couple = getCouples().get(index);
		if (couple != null)
		{
			final L2Player player1 = L2World.getInstance().getPlayer(couple.getPlayer1Id());
			final L2Player player2 = L2World.getInstance().getPlayer(couple.getPlayer2Id());
			L2ItemInstance item = null;
			if (player1 != null)
			{
				player1.setPartnerId(0);
				player1.setMaried(false);
				player1.setCoupleId(0);
				item = player1.getInventory().getItemByItemId(9140);
				if (player1.isOnline() == 1 && item != null)
				{
					player1.destroyItem("Removing Cupids Bow", item, player1, true);

					// No need to update every item in the inventory
					//player1.getInventory().updateDatabase();
				}
				if (player1.isOnline() == 0 && item != null)
				{
					final Integer playerId = player1.getObjectId();
					final Integer ItemId = 9140;
					Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection(con);
						final PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id = ? AND item_id = ?");
						statement.setInt(1, playerId);
						statement.setInt(2, ItemId);
						statement.execute();
						statement.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						L2DatabaseFactory.close(con);
					}
				}
			}
			if (player2 != null)
			{
				player2.setPartnerId(0);
				player2.setMaried(false);
				player2.setCoupleId(0);
				item = player2.getInventory().getItemByItemId(9140);
				if (player2.isOnline() == 1 && item != null)
				{
					player2.destroyItem("Removing Cupids Bow", item, player2, true);

					// No need to update every item in the inventory
					//player2.getInventory().updateDatabase();
				}
				if (player2.isOnline() == 0 && item != null)
				{
					Integer Player2Id = player2.getObjectId();
					Integer Item2Id = 9140;
					Connection con = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection(con);
						final PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id = ? AND item_id = ?");
						statement.setInt(1, Player2Id);
						statement.setInt(2, Item2Id);
						statement.execute();
						statement.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						L2DatabaseFactory.close(con);
					}
				}
			}
			couple.divorce();
			getCouples().remove(index);
		}
	}

	public final int getCoupleIndex(int coupleId)
	{
		int i = 0;
		for (Couple temp : getCouples())
		{
			if (temp != null && temp.getId() == coupleId)
				return i;
			i++;
		}
		return -1;
	}

	public final List<Couple> getCouples()
	{
		if (_couples == null)
			_couples = new SingletonList<Couple>();
		return _couples;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CoupleService	_instance	= new CoupleService();
	}
}
