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

import javolution.util.FastMap;
import net.l2emuproject.gameserver.system.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author L0ngh0rn
 */
public class ClassBalanceTable
{
	private static Log						_log		= LogFactory.getLog(ClassBalanceTable.class);
	private FastMap<Integer, ClassBalance>	_balance;

	private static String					QRY_SELECT	= "SELECT c.class_id, c.FxH, c.FxL, c.FxR, c.MxH, c.MxL, c.MxR FROM class_balance AS c ORDER BY c.class_id ASC";

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ClassBalanceTable	_instance	= new ClassBalanceTable();
	}

	public static ClassBalanceTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public ClassBalanceTable()
	{
		loadClassBalance();
	}

	public void loadClassBalance()
	{
		_balance = new FastMap<Integer, ClassBalance>();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(QRY_SELECT);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				int class_id = rs.getInt("class_id");
				double fxh = rs.getDouble("FxH");
				double fxl = rs.getDouble("FxL");
				double fxr = rs.getDouble("FxR");
				double mxh = rs.getDouble("MxH");
				double mxl = rs.getDouble("MxL");
				double mxr = rs.getDouble("MxR");
				_balance.put(class_id, new ClassBalance(class_id, new ArmorBalance(fxh, fxl, fxr), new ArmorBalance(mxh, mxl, mxr)));
			}
			ps.close();
			rs.close();

			_log.info("ClassBalanceTable: (Re)Loaded " + _balance.size() + " classes.");
		}
		catch (Exception e)
		{
			_log.warn("ClassBalanceTable: Could not load class_balance: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public ClassBalance getBalance(int class_id)
	{
		if (_balance.containsKey(class_id))
			return _balance.get(class_id);
		return new ClassBalance();
	}

	public double getBalanceValue(TypeBalance type, int class_id)
	{
		switch (type)
		{
			case FxH:
				return getBalance(class_id).getFight().getHeavy();
			case FxL:
				return getBalance(class_id).getFight().getLight();
			case FxR:
				return getBalance(class_id).getFight().getRobe();
			case MxH:
				return getBalance(class_id).getMage().getHeavy();
			case MxL:
				return getBalance(class_id).getMage().getLight();
			case MxR:
				return getBalance(class_id).getMage().getRobe();
			default:
				return 1D;
		}
	}

	public enum TypeBalance
	{
		FxH, FxL, FxR, MxH, MxL, MxR;
	}

	public class ClassBalance
	{
		private int				_class_id;
		private ArmorBalance	_fight;
		private ArmorBalance	_mage;

		public ClassBalance(int class_id, ArmorBalance fight, ArmorBalance mage)
		{
			_class_id = class_id;
			_fight = fight;
			_mage = mage;
		}

		public ClassBalance()
		{
			_class_id = -1;
			_fight = new ArmorBalance();
			_mage = new ArmorBalance();
		}

		public int getClass_id()
		{
			return _class_id;
		}

		public void setClass_id(int class_id)
		{
			_class_id = class_id;
		}

		public ArmorBalance getFight()
		{
			return _fight;
		}

		public void setFight(ArmorBalance fight)
		{
			_fight = fight;
		}

		public ArmorBalance getMage()
		{
			return _mage;
		}

		public void setMage(ArmorBalance mage)
		{
			_mage = mage;
		}
	}

	public class ArmorBalance
	{
		private double	_heavy;
		private double	_light;
		private double	_robe;

		public ArmorBalance(double heavy, double light, double robe)
		{
			_heavy = heavy;
			_light = light;
			_robe = robe;
		}

		public ArmorBalance()
		{
			_heavy = 1.0;
			_light = 1.0;
			_robe = 1.0;
		}

		public double getHeavy()
		{
			return _heavy;
		}

		public void setHeavy(double heavy)
		{
			_heavy = heavy;
		}

		public double getLight()
		{
			return _light;
		}

		public void setLight(double light)
		{
			_light = light;
		}

		public double getRobe()
		{
			return _robe;
		}

		public void setRobe(double robe)
		{
			_robe = robe;
		}
	}
}
