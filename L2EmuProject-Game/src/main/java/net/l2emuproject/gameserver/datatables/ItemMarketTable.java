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
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.L2DatabaseFactory;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.model.item.L2ItemMarketModel;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ItemMarketTable
{
	private final static Log						_log			= LogFactory.getLog(ItemMarketTable.class);
	private Map<Integer, String>					_itemIcons		= null;
	private Map<Integer, List<L2ItemMarketModel>>	_marketItems	= null;
	private Map<Integer, Integer>					_sellers		= null;

	private ItemMarketTable()
	{
		load();
	}

	private static final class SingletonHolder
	{
		private static final ItemMarketTable	INSTANCE	= new ItemMarketTable();
	}

	public static ItemMarketTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private void load()
	{
		_marketItems = new FastMap<Integer, List<L2ItemMarketModel>>();
		Connection con = null;
		int mrktCount = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM item_market ORDER BY ownerId");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int ownerId = rset.getInt("ownerId");
				String ownerName = rset.getString("ownerName");
				int itemObjId = rset.getInt("itemObjId");
				int itemId = rset.getInt("itemId");
				String itemName = rset.getString("itemName");
				String itemType = rset.getString("itemType");
				String l2Type = rset.getString("l2Type");
				int itemGrade = rset.getInt("itemGrade");
				int enchLvl = rset.getInt("enchLvl");
				int count = rset.getInt("_count");
				int price = rset.getInt("price");
				L2ItemMarketModel mrktItem = new L2ItemMarketModel();
				mrktItem.setOwnerId(ownerId);
				mrktItem.setOwnerName(ownerName);
				mrktItem.setItemObjId(itemObjId);
				mrktItem.setItemId(itemId);
				mrktItem.setItemName(itemName);
				mrktItem.setItemType(itemType);
				mrktItem.setL2Type(l2Type);
				mrktItem.setItemGrade(itemGrade);
				mrktItem.setEnchLvl(enchLvl);
				mrktItem.setCount(count);
				mrktItem.setPrice(price);
				if (_marketItems.containsKey(ownerId))
				{
					List<L2ItemMarketModel> list = _marketItems.get(ownerId);
					list.add(mrktItem);
					_marketItems.put(ownerId, list);
				}
				else
				{
					List<L2ItemMarketModel> list = new FastList<L2ItemMarketModel>();
					list.add(mrktItem);
					_marketItems.put(ownerId, list);
				}
				mrktCount++;
			}
			_log.info("ItemMarketTable: Loaded: " + mrktCount + " market item(s).");
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error while loading market items " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		loadSellers();
		loadIcons();
	}

	private void loadSellers()
	{
		_sellers = new FastMap<Integer, Integer>();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM market_seller");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int sellerId = rset.getInt("sellerId");
				int money = rset.getInt("money");
				_sellers.put(sellerId, money);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error while loading market sellers " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void loadIcons()
	{
		_itemIcons = new FastMap<Integer, String>();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM market_icons");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int itemId = rset.getInt("itemId");
				String itemIcon = rset.getString("itemIcon").intern();
				_itemIcons.put(itemId, itemIcon);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error while loading market icons " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void addItemToMarket(L2ItemMarketModel itemToMarket, L2Player owner)
	{
		synchronized (this)
		{
			if (_marketItems != null && owner != null && itemToMarket != null)
			{
				List<L2ItemMarketModel> list = _marketItems.get(owner.getObjectId());
				if (list != null)
				{
					list.add(itemToMarket);
					_marketItems.put(owner.getObjectId(), list);
				}
				else
				{
					list = new FastList<L2ItemMarketModel>();
					list.add(itemToMarket);
					_marketItems.put(owner.getObjectId(), list);
				}
				ThreadPoolManager.getInstance().scheduleGeneral(new SaveTask(itemToMarket), 2000);
			}
		}
	}

	public void removeItemFromMarket(int ownerId, int itemObjId, int count)
	{
		L2ItemMarketModel mrktItem = getItem(itemObjId);
		List<L2ItemMarketModel> list = getItemsByOwnerId(ownerId);
		synchronized (this)
		{
			if (list != null && mrktItem != null && !list.isEmpty())
			{
				if (mrktItem.getCount() == count)
				{
					list.remove(mrktItem);
					_marketItems.put(ownerId, list);
					ThreadPoolManager.getInstance().scheduleGeneral(new DeleteTask(mrktItem), 2000);
				}
				else
				{
					list.remove(mrktItem);
					mrktItem.setCount(mrktItem.getCount() - count);
					list.add(mrktItem);
					_marketItems.put(ownerId, list);
					ThreadPoolManager.getInstance().scheduleGeneral(new UpdateTask(mrktItem), 2000);
				}
			}
		}
	}

	public void addMoney(int sellerId, int money)
	{
		synchronized (this)
		{
			if (_sellers != null)
			{
				if (_sellers.containsKey(sellerId))
				{
					int oldMoney = _sellers.get(sellerId);
					money += oldMoney;
					_sellers.put(sellerId, money);
					ThreadPoolManager.getInstance().scheduleGeneral(new AddMoneyTask(sellerId, money), 2000);
				}
				else
				{
					_sellers.put(sellerId, money);
					ThreadPoolManager.getInstance().scheduleGeneral(new AddSellerTask(sellerId, money), 2000);
				}
			}
		}
	}

	public int getMoney(int sellerId)
	{
		synchronized (this)
		{
			if (_sellers != null && !_sellers.isEmpty())
			{
				if (_sellers.containsKey(sellerId))
					return _sellers.get(sellerId);
				else
					return 0;
			}
		}
		return 0;
	}

	public void takeMoney(int sellerId, int amount)
	{
		synchronized (this)
		{
			if (_sellers != null && !_sellers.isEmpty())
			{
				if (_sellers.containsKey(sellerId))
				{
					int oldMoney = _sellers.get(sellerId);
					if (oldMoney >= amount)
					{
						oldMoney -= amount;
						_sellers.put(sellerId, oldMoney);
						ThreadPoolManager.getInstance().scheduleGeneral(new AddMoneyTask(sellerId, oldMoney), 2000);
					}
				}
			}
		}
	}

	public List<L2ItemMarketModel> getItemsByOwnerId(int ownerId)
	{
		synchronized (this)
		{
			if (_marketItems != null && !_marketItems.isEmpty())
				return _marketItems.get(ownerId);
		}
		return null;
	}

	public L2ItemMarketModel getItem(int itemObjId)
	{
		List<L2ItemMarketModel> list = getAllItems();
		synchronized (this)
		{
			for (L2ItemMarketModel model : list)
			{
				if (model.getItemObjId() == itemObjId)
					return model;
			}
		}
		return null;
	}

	public List<L2ItemMarketModel> getAllItems()
	{
		synchronized (this)
		{
			if (_marketItems != null && !_marketItems.isEmpty())
			{
				List<L2ItemMarketModel> list = new FastList<L2ItemMarketModel>();
				for (List<L2ItemMarketModel> lst : _marketItems.values())
				{
					if (lst != null && !lst.isEmpty())
					{
						for (L2ItemMarketModel auctItem : lst)
						{
							if (auctItem != null)
								list.add(auctItem);
						}
					}
				}
				return list;
			}
		}
		return null;
	}

	public String getItemIcon(int itemId)
	{
		if (_itemIcons == null && _itemIcons.isEmpty())
			return ""; // TODO: No Icon, need to set a default icon.
		return _itemIcons.get(itemId);
	}

	private static class SaveTask implements Runnable
	{
		private final L2ItemMarketModel	_marketItem;

		public SaveTask(L2ItemMarketModel marketItem)
		{
			_marketItem = marketItem;
		}

		@Override
		public void run()
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO item_market VALUES (?,?,?,?,?,?,?,?,?,?,?)");
				statement.setInt(1, _marketItem.getOwnerId());
				statement.setString(2, _marketItem.getOwnerName());
				statement.setString(3, _marketItem.getItemName());
				statement.setInt(4, _marketItem.getEnchLvl());
				statement.setInt(5, _marketItem.getItemGrade());
				statement.setString(6, _marketItem.getL2Type());
				statement.setString(7, _marketItem.getItemType());
				statement.setInt(8, _marketItem.getItemId());
				statement.setInt(9, _marketItem.getItemObjId());
				statement.setInt(10, _marketItem.getCount());
				statement.setInt(11, _marketItem.getPrice());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Error while saving market item into DB " + e.getMessage());
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	private static class DeleteTask implements Runnable
	{
		private final L2ItemMarketModel	_marketItem;

		public DeleteTask(L2ItemMarketModel marketItem)
		{
			_marketItem = marketItem;
		}

		@Override
		public void run()
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM item_market WHERE ownerId = ? AND itemObjId = ?");
				statement.setInt(1, _marketItem.getOwnerId());
				statement.setInt(2, _marketItem.getItemObjId());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Error while deleting market item from DB " + e.getMessage());
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	private static class UpdateTask implements Runnable
	{
		private final L2ItemMarketModel	_marketItem;

		public UpdateTask(L2ItemMarketModel marketItem)
		{
			_marketItem = marketItem;
		}

		@Override
		public void run()
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE item_market SET _count = ? WHERE itemObjId = ? AND ownerId = ?");
				statement.setInt(1, _marketItem.getCount());
				statement.setInt(2, _marketItem.getItemObjId());
				statement.setInt(3, _marketItem.getOwnerId());
				statement.executeUpdate();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Error while updating market item in DB " + e.getMessage());
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	private static class AddMoneyTask implements Runnable
	{
		private final int	_sellerId;
		private final int	_money;

		public AddMoneyTask(int sellerId, int money)
		{
			_sellerId = sellerId;
			_money = money;
		}

		@Override
		public void run()
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE market_seller SET money = ? WHERE sellerId = ?");
				statement.setInt(1, _money);
				statement.setInt(2, _sellerId);
				statement.executeUpdate();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Error while adding money in DB " + e.getMessage());
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	private static class AddSellerTask implements Runnable
	{
		private final int	_sellerId;
		private final int	_money;

		public AddSellerTask(int sellerId, int money)
		{
			_sellerId = sellerId;
			_money = money;
		}

		@Override
		public void run()
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO market_seller(sellerId, money) VALUES (?,?)");
				statement.setInt(1, _sellerId);
				statement.setInt(2, _money);
				statement.executeUpdate();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Error while adding seller in DB " + e.getMessage());
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}
}