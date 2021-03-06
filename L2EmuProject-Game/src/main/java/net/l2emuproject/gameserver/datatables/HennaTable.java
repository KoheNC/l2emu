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
import java.util.HashMap;
import java.util.Map;

import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.templates.StatsSet;
import net.l2emuproject.gameserver.templates.item.L2Henna;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class HennaTable
{
	private static final Log	_log = LogFactory.getLog(HennaTable.class);
	private static final String LOAD_HENNA = "SELECT symbol_id,symbol_name,dye_id,dye_amount,price,mod_INT,mod_STR,mod_CON,mod_MEN,mod_DEX,mod_WIT FROM henna";

	private final Map<Integer, L2Henna> _henna = new HashMap<Integer, L2Henna>();

	public static HennaTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private HennaTable()
	{
		restoreHennaData();
	}

	private void restoreHennaData()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(LOAD_HENNA);
			ResultSet hennadata = statement.executeQuery();
			fillHennaTable(hennadata);
			hennadata.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("error while creating henna table!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void fillHennaTable(ResultSet hennaData) throws Exception
	{
		while (hennaData.next())
		{
			final int id = hennaData.getInt("symbol_id");

			StatsSet hennaDat = new StatsSet();
			hennaDat.set("symbol_id", id);
			hennaDat.set("symbol_name", hennaData.getString("symbol_name"));
			hennaDat.set("dye_id", hennaData.getInt("dye_id"));
			hennaDat.set("price", hennaData.getInt("price"));
			hennaDat.set("dye_amount", hennaData.getInt("dye_amount"));
			hennaDat.set("mod_INT", hennaData.getInt("mod_INT"));
			hennaDat.set("mod_STR", hennaData.getInt("mod_STR"));
			hennaDat.set("mod_CON", hennaData.getInt("mod_CON"));
			hennaDat.set("mod_MEN", hennaData.getInt("mod_MEN"));
			hennaDat.set("mod_DEX", hennaData.getInt("mod_DEX"));
			hennaDat.set("mod_WIT", hennaData.getInt("mod_WIT"));

			_henna.put(id, new L2Henna(hennaDat));
		}
		_log.info(getClass().getSimpleName() + " : Loaded " + _henna.size() + " Template(s).");
	}

	public L2Henna getTemplate(int id)
	{
		return _henna.get(id);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final HennaTable _instance = new HennaTable();
	}
}
