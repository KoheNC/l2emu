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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.templates.item.L2WeaponType;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.LookupTable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author MrPoke
 */
public final class EnchantItemData
{
	private final static Log					_log				= LogFactory.getLog(EnchantItemData.class);

	private final LookupTable<EnchantScroll>	_scrolls			= new LookupTable<EnchantScroll>();
	private final LookupTable<EnchantItem>		_supports			= new LookupTable<EnchantItem>();
	private final Vector<Integer>				_enchantBlackList	= new Vector<Integer>();

	public static final EnchantItemData getInstance()
	{
		return SingletonHolder._instance;
	}

	private EnchantItemData()
	{
		if (Config.ENCHANT_BLACK_LIST != "0" || Config.ENCHANT_BLACK_LIST != "")
			setEnchantBlackList();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "data/enchantItemData.xml");
		Document doc = null;
		if (file.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(file);
			}
			catch (SAXException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (ParserConfigurationException e)
			{
				e.printStackTrace();
			}
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("scrolls".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att;
							int id, grade, maxEnchant, chance;
							boolean isWeapon, isBlessed, isSafe;
							int[] allowedItemID;
							att = attrs.getNamedItem("id");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing id, skipping.");
								continue;
							}
							id = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("isWeapon");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing isWeapon, skipping.");
								continue;
							}
							isWeapon = Boolean.valueOf(att.getNodeValue());
							att = attrs.getNamedItem("isBlessed");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing isBlessed, skipping.");
								continue;
							}
							isBlessed = Boolean.valueOf(att.getNodeValue());
							att = attrs.getNamedItem("isSafe");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing isSafe, skipping.");
								continue;
							}
							isSafe = Boolean.valueOf(att.getNodeValue());
							att = attrs.getNamedItem("grade");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing grade, skipping.");
								continue;
							}
							grade = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("maxEnchant");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing maxEnchant, skipping.");
								continue;
							}
							maxEnchant = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("chance");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing chance, skipping.");
								continue;
							}
							chance = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("allowedItemID");
							if (att == null)
							{
								allowedItemID = null;
							}
							else
							{
								StringTokenizer st = new StringTokenizer(att.getNodeValue(), ",");
								int tokenCount = st.countTokens();
								allowedItemID = new int[tokenCount];
								for (int i = 0; i < tokenCount; i++)
								{
									Integer value = Integer.decode(st.nextToken().trim());
									if (value == null)
									{
										_log.warn("EnchantItemData: Bad allowedItemID, skipping.");
										value = 0;
									}
									allowedItemID[i] = value;
								}
							}
							_scrolls.put(id, new EnchantScroll(isWeapon, isBlessed, isSafe, grade, maxEnchant, chance, allowedItemID));
						}
						if ("supports".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att;
							int id, grade, maxEnchant, chance;
							boolean isWeapon;
							int[] allowedItemID;
							att = attrs.getNamedItem("id");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing id, skipping.");
								continue;
							}
							id = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("isWeapon");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing isWeapon, skipping.");
								continue;
							}
							isWeapon = Boolean.valueOf(att.getNodeValue());
							att = attrs.getNamedItem("grade");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing grade, skipping.");
								continue;
							}
							grade = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("maxEnchant");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing maxEnchant, skipping.");
								continue;
							}
							maxEnchant = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("chance");
							if (att == null)
							{
								_log.warn("EnchantItemData: Missing chance, skipping.");
								continue;
							}
							chance = Integer.parseInt(att.getNodeValue());
							att = attrs.getNamedItem("allowedItemID");
							if (att == null)
								allowedItemID = null;
							else
							{
								StringTokenizer st = new StringTokenizer(att.getNodeValue(), ",");
								int tokenCount = st.countTokens();
								allowedItemID = new int[tokenCount];
								for (int i = 0; i < tokenCount; i++)
								{
									Integer value = Integer.decode(st.nextToken().trim());
									if (value == null)
									{
										_log.warn("EnchantItemData: Bad allowedItemID, skipping.");
										value = 0;
									}
									allowedItemID[i] = value;
								}
							}
							_supports.put(id, new EnchantItem(isWeapon, grade, maxEnchant, chance, allowedItemID));
						}
					}
				}
			}
		}
		_log.info("EnchantItemData: Loaded: " + _scrolls.size() + " enchant scroll(s) and " + _supports.size() + " enchant support item(s).");
	}

	private void setEnchantBlackList()
	{
		if (!_enchantBlackList.isEmpty())
			_enchantBlackList.clear();
		String enchantBlackList = Config.ENCHANT_BLACK_LIST;
		String[] splitEnchantBlackList = enchantBlackList.split(";");
		if (splitEnchantBlackList.length >= 1)
		{
			String[] splitEnchantBlackListIntervar;
			for (int i = 0; i < splitEnchantBlackList.length; i++)
			{
				splitEnchantBlackListIntervar = splitEnchantBlackList[i].split("-");
				if (splitEnchantBlackListIntervar.length > 1)
				{
					int intervalStart = Integer.parseInt(splitEnchantBlackListIntervar[0]);
					int intervalFifnish = Integer.parseInt(splitEnchantBlackListIntervar[1]);
					if (intervalFifnish <= intervalStart)
					{
						_log.warn("[EnchantItemData] Bad interval EnchantBlackList:" + splitEnchantBlackList[i]);
						continue;
					}
					for (int j = intervalStart; j <= intervalFifnish; j++)
						addBlackListId(j);
				}
				else
					addBlackListId(Integer.parseInt(splitEnchantBlackList[i]));
			}
		}
		_log.info("EnchantItemData: Loaded: " + _enchantBlackList.size() + " EnchantBlackList Item(s).");
	}

	private void addBlackListId(int id)
	{
		if (isBlackListItem(id))
			return;
		_enchantBlackList.add(id);
	}

	private boolean isBlackListItem(int id)
	{
		return _enchantBlackList.contains(id);
	}

	/**
	 * Return enchant template for scroll
	 */
	public final EnchantScroll getEnchantScroll(L2ItemInstance scroll)
	{
		return _scrolls.get(scroll.getItemId());
	}

	/**
	 * Return enchant template for support item
	 */
	public final EnchantItem getSupportItem(L2ItemInstance item)
	{
		return _supports.get(item.getItemId());
	}

	/**
	 * Return true if item can be enchanted
	 * 
	 * @return
	 */
	public final boolean isEnchantable(L2Player activeChar, L2ItemInstance item)
	{
		if (activeChar == null)
			return false;
		if (item == null)
			return false;
		if (item.isHeroItem() && !Config.ENCHANT_HERO_WEAPONS)
			return false;
		if (item.isShadowItem())
			return false;
		if (item.isCommonItem())
			return false;
		if (item.isEtcItem())
			return false;
		if (item.isTimeLimitedItem())
			return false;
		if (item.isWear())
			return false;
		// rods
		if (item.getItem().getItemType() == L2WeaponType.ROD)
			return false;
		// EnchantBlackList
		if (isBlackListItem(item.getItemId()))
			return false;
		// bracelets
		if (item.getItem().getBodyPart() == L2Item.SLOT_L_BRACELET)
			return false;
		if (item.getItem().getBodyPart() == L2Item.SLOT_R_BRACELET)
			return false;
		if (item.getItem().getBodyPart() == L2Item.SLOT_BACK)
			return false;
		// only items in inventory and equipped can be enchanted
		if (item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY && item.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL)
			return false;
		return true;
	}

	public static class EnchantItem
	{
		protected final boolean	_isWeapon;
		protected final int		_grade;
		protected final int		_maxEnchantLevel;
		protected final int		_chance;
		protected final int[]	_itemIds;

		public EnchantItem(boolean wep, int type, int level, int chance, int[] items)
		{
			_isWeapon = wep;
			_grade = type;
			_maxEnchantLevel = level;
			_chance = chance;
			_itemIds = items;
		}

		/*
		 * Return true if support item can be used for this item
		 */
		public final boolean isValid(L2ItemInstance enchantItem)
		{
			if (enchantItem == null)
				return false;
			int type2 = enchantItem.getItem().getType2();
			// checking scroll type and configured maximum enchant level
			switch (type2)
			{
				// weapon scrolls can enchant only weapons
				case L2Item.TYPE2_WEAPON:
					if (!_isWeapon || (Config.ENCHANT_MAX_WEAPON > 0 && enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_WEAPON)
							&& enchantItem.getItemId() != 13539)
						return false;
					break;
				// armor scrolls can enchant only accessory and armors
				case L2Item.TYPE2_SHIELD_ARMOR:
					if (_isWeapon || (Config.ENCHANT_MAX_ARMOR > 0 && enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_ARMOR))
						return false;
					break;
				case L2Item.TYPE2_ACCESSORY:
					if (_isWeapon || (Config.ENCHANT_MAX_JEWELRY > 0 && enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_JEWELRY))
						return false;
					break;
				default:
					return false;
			}
			// check for crystal types
			if (_grade != enchantItem.getItem().getItemGradeSPlus())
				return false;
			// check for maximum enchant level
			if (_maxEnchantLevel != 0 && enchantItem.getEnchantLevel() >= _maxEnchantLevel)
				return false;
			if (_itemIds != null && Arrays.binarySearch(_itemIds, enchantItem.getItemId()) < 0)
				return false;
			return true;
		}

		/*
		 * return chance increase
		 */
		public final int getChance()
		{
			return _chance;
		}
	}

	public static final class EnchantScroll extends EnchantItem
	{
		private final boolean	_isBlessed;
		private final boolean	_isSafe;

		public EnchantScroll(boolean wep, boolean bless, boolean safe, int type, int level, int chance, int[] items)
		{
			super(wep, type, level, chance, items);
			_isBlessed = bless;
			_isSafe = safe;
		}

		/*
		 * Return true for blessed scrolls
		 */
		public final boolean isBlessed()
		{
			return _isBlessed;
		}

		/*
		 * Return true for safe-enchant scrolls (enchant level will remain on failure)
		 */
		public final boolean isSafe()
		{
			return _isSafe;
		}

		public final boolean isValid(L2ItemInstance enchantItem, EnchantItem supportItem)
		{
			// blessed scrolls can't use support items
			if (supportItem != null && (!supportItem.isValid(enchantItem) || isBlessed()))
				return false;
			return isValid(enchantItem);
		}

		public final int getChance(L2ItemInstance enchantItem, EnchantItem supportItem)
		{
			if (!isValid(enchantItem, supportItem))
				return -1;
			boolean fullBody = enchantItem.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR;
			if (enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX || (fullBody && enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL))
				return 100;
			int chance = 0;
			if (_isBlessed)
			{
				// blessed scrolls does not use support items
				if (supportItem != null)
					return -1;
			}
			chance = _chance;
			if (supportItem != null)
				chance += supportItem.getChance();
			return chance;
		}
	}

	@SuppressWarnings("synthetic-access")
	private static final class SingletonHolder
	{
		private static final EnchantItemData	_instance	= new EnchantItemData();
	}
}