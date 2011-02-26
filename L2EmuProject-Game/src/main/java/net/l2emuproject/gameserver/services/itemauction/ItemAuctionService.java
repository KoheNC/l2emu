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
package net.l2emuproject.gameserver.services.itemauction;

import gnu.trove.TIntObjectHashMap;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilderFactory;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.system.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Forsaiken
 */
public final class ItemAuctionService
{
	private static final Log	_log	= LogFactory.getLog(ItemAuctionService.class);

	public static final ItemAuctionService getInstance()
	{
		return SingletonHolder._instance;
	}

	private final TIntObjectHashMap<ItemAuctionInstance>	_managerInstances;
	private final AtomicInteger								_auctionIds;

	private ItemAuctionService()
	{
		_managerInstances = new TIntObjectHashMap<ItemAuctionInstance>();
		_auctionIds = new AtomicInteger(1);

		if (!Config.ALT_ITEM_AUCTION_ENABLED)
		{
			_log.info(getClass().getSimpleName() + " : Disabled by config.");
			return;
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT auctionId FROM item_auction ORDER BY auctionId DESC LIMIT 0, 1");
			ResultSet rset = statement.executeQuery();
			if (rset.next())
				_auctionIds.set(rset.getInt(1) + 1);
		}
		catch (final SQLException e)
		{
			_log.error(getClass().getSimpleName() + " : Failed loading auctions.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		final File file = new File(Config.DATAPACK_ROOT + "/data/ItemAuctions.xml");
		if (!file.exists())
		{
			_log.warn(getClass().getSimpleName() + " : Missing ItemAuctions.xml!");
			return;
		}

		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);

		try
		{
			final Document doc = factory.newDocumentBuilder().parse(file);
			for (Node na = doc.getFirstChild(); na != null; na = na.getNextSibling())
			{
				if ("list".equalsIgnoreCase(na.getNodeName()))
				{
					for (Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
					{
						if ("instance".equalsIgnoreCase(nb.getNodeName()))
						{
							final NamedNodeMap nab = nb.getAttributes();
							final int instanceId = Integer.parseInt(nab.getNamedItem("id").getNodeValue());

							if (_managerInstances.containsKey(instanceId))
								throw new Exception("Dublicated instanceId " + instanceId);

							final ItemAuctionInstance instance = new ItemAuctionInstance(instanceId, _auctionIds, nb);
							_managerInstances.put(instanceId, instance);
						}
					}
				}
			}
			_log.info(getClass().getSimpleName() + " : Loaded " + _managerInstances.size() + " instance(s).");
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + " : Failed loading auctions from xml.", e);
		}
	}

	public final void shutdown()
	{
		final ItemAuctionInstance[] instances = _managerInstances.getValues(new ItemAuctionInstance[_managerInstances.size()]);
		for (final ItemAuctionInstance instance : instances)
		{
			instance.shutdown();
		}
	}

	public final ItemAuctionInstance getManagerInstance(final int instanceId)
	{
		return _managerInstances.get(instanceId);
	}

	public final int getNextAuctionId()
	{
		return _auctionIds.getAndIncrement();
	}

	public final static void deleteAuction(final int auctionId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM item_auction WHERE auctionId=?");
			statement.setInt(1, auctionId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=?");
			statement.setInt(1, auctionId);
			statement.execute();
			statement.close();
		}
		catch (final SQLException e)
		{
			_log.error("L2ItemAuctionManagerInstance: Failed deleting auction: " + auctionId, e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ItemAuctionService	_instance	= new ItemAuctionService();
	}
}
