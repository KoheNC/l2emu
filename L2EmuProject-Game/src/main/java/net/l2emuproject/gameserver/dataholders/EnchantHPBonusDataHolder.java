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
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.skills.funcs.FuncTemplate;
import net.l2emuproject.gameserver.templates.item.L2Armor;
import net.l2emuproject.gameserver.templates.item.L2Equip;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.templates.item.L2Weapon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author MrPoke
 */
public final class EnchantHPBonusDataHolder
{
	private static final Log	_log	= LogFactory.getLog(EnchantHPBonusDataHolder.class);

	public static EnchantHPBonusDataHolder getInstance()
	{
		return SingletonHolder._instance;
	}

	private final Map<Integer, Integer[]>	_singleArmorHPBonus	= new HashMap<Integer, Integer[]>();
	private final Map<Integer, Integer[]>	_fullArmorHPBonus	= new HashMap<Integer, Integer[]>();

	private EnchantHPBonusDataHolder()
	{
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		final File file = new File(Config.DATAPACK_ROOT, "data/item_data/enchant/enchantHPBonus.xml");
		Document doc = null;

		try
		{
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch (Exception e)
		{
			_log.warn("", e);
			return;
		}

		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("enchantHP".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						Node att;
						boolean fullArmor;

						att = attrs.getNamedItem("grade");
						if (att == null)
						{
							_log.warn("[EnchantHPBonusData] Missing grade, skipping");
							continue;
						}

						int grade = Integer.parseInt(att.getNodeValue());

						att = attrs.getNamedItem("fullArmor");
						if (att == null)
						{
							_log.warn("[EnchantHPBonusData] Missing fullArmor, skipping");
							continue;
						}
						fullArmor = Boolean.valueOf(att.getNodeValue());

						att = attrs.getNamedItem("values");
						if (att == null)
						{
							_log.warn("[EnchantHPBonusData] Missing bonus id: " + grade + ", skipping");
							continue;
						}
						StringTokenizer st = new StringTokenizer(att.getNodeValue(), ",");
						int tokenCount = st.countTokens();
						Integer[] bonus = new Integer[tokenCount];
						for (int i = 0; i < tokenCount; i++)
						{
							Integer value = Integer.decode(st.nextToken().trim());
							if (value == null)
							{
								_log.warn("[EnchantHPBonusData] Bad Hp value!! grade: " + grade + " FullArmor? " + fullArmor + " token: " + i);
								value = 0;
							}
							bonus[i] = value;
						}
						if (fullArmor)
							_fullArmorHPBonus.put(grade, bonus);
						else
							_singleArmorHPBonus.put(grade, bonus);
					}
				}
			}
		}

		if (_fullArmorHPBonus.isEmpty() && _singleArmorHPBonus.isEmpty())
			return;

		int count = 0;

		for (L2Item item0 : ItemTable.getInstance().getAllTemplates())
		{
			if (!(item0 instanceof L2Equip))
				continue;

			final L2Equip item = (L2Equip) item0;

			if (item.getCrystalType() == L2Item.CRYSTAL_NONE)
				continue;

			boolean shouldAdd = false;

			// normally for armors
			if (item instanceof L2Armor)
			{
				switch (item.getBodyPart())
				{
					case L2Item.SLOT_CHEST:
					case L2Item.SLOT_FEET:
					case L2Item.SLOT_GLOVES:
					case L2Item.SLOT_HEAD:
					case L2Item.SLOT_LEGS:
					case L2Item.SLOT_BACK:
					case L2Item.SLOT_FULL_ARMOR:
					case L2Item.SLOT_UNDERWEAR:
					case L2Item.SLOT_L_HAND:
						shouldAdd = true;
						break;
				}
			}
			// shields in the weapons table
			else if (item instanceof L2Weapon)
			{
				switch (item.getBodyPart())
				{
					case L2Item.SLOT_L_HAND:
						shouldAdd = true;
						break;
				}
			}

			if (shouldAdd)
			{
				count++;
				item.attach(new FuncTemplate(null, "EnchantHp", Stats.MAX_HP, 0x60, 0));
			}
		}

		_log.info(getClass().getSimpleName() + " : Enchant HP Bonus registered for " + count + " item(s).");
	}

	public final int getHPBonus(L2ItemInstance item)
	{
		final Integer[] values;

		if (item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
			values = _fullArmorHPBonus.get(item.getItem().getItemGradeSPlus());
		else
			values = _singleArmorHPBonus.get(item.getItem().getItemGradeSPlus());

		if (values == null || values.length == 0)
			return 0;

		return values[Math.min(item.getEnchantLevel(), values.length) - 1];
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final EnchantHPBonusDataHolder	_instance	= new EnchantHPBonusDataHolder();
	}
}
