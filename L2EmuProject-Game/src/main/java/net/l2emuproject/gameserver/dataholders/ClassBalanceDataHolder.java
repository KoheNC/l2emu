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
package net.l2emuproject.gameserver.dataholders;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import net.l2emuproject.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author L0ngh0rn
 */
public final class ClassBalanceDataHolder
{
	private static Log						_log		= LogFactory.getLog(ClassBalanceDataHolder.class);
	private Map<Integer, ClassBalance>	_balance;

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ClassBalanceDataHolder	_instance	= new ClassBalanceDataHolder();
	}

	public static ClassBalanceDataHolder getInstance()
	{
		return SingletonHolder._instance;
	}

	public ClassBalanceDataHolder()
	{
		loadClassBalance();
	}

	public void loadClassBalance()
	{
		_balance = new HashMap<Integer, ClassBalance>();

		Document doc = null;
		File file = new File(Config.DATAPACK_ROOT, "data/char_data/class_balance.xml");

		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						int classId = 0;
						double fvh = 0, fvl = 0, fvr = 0, mvh = 0, mvl = 0, mvr = 0;
						if ("class".equalsIgnoreCase(d.getNodeName()))
						{
							classId = Integer.parseInt(d.getAttributes().getNamedItem("classId").getNodeValue());
							fvh = Double.parseDouble(d.getAttributes().getNamedItem("PhysicalvHeavy").getNodeValue());
							fvl = Double.parseDouble(d.getAttributes().getNamedItem("PhysicalvLight").getNodeValue());
							fvr = Double.parseDouble(d.getAttributes().getNamedItem("PhysicalvRobe").getNodeValue());
							mvh = Double.parseDouble(d.getAttributes().getNamedItem("MagicalvHeavy").getNodeValue());
							mvl = Double.parseDouble(d.getAttributes().getNamedItem("MagicalvLight").getNodeValue());
							mvr = Double.parseDouble(d.getAttributes().getNamedItem("MagicalvRobe").getNodeValue());
							
							_balance.put(classId, new ClassBalance(classId, new ArmorBalance(fvh, fvl, fvr), new ArmorBalance(mvh, mvl, mvr)));
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warn("ClassBalanceTable: Could not load class_balance: ", e);
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
			case FvH:
				return getBalance(class_id).getFight().getHeavy();
			case FvL:
				return getBalance(class_id).getFight().getLight();
			case FvR:
				return getBalance(class_id).getFight().getRobe();
			case MvH:
				return getBalance(class_id).getMage().getHeavy();
			case MvL:
				return getBalance(class_id).getMage().getLight();
			case MvR:
				return getBalance(class_id).getMage().getRobe();
			default:
				return 1D;
		}
	}

	public enum TypeBalance
	{
		FvH, FvL, FvR, MvH, MvL, MvR;
	}

	public final class ClassBalance
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

	public final class ArmorBalance
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
