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
package net.l2emuproject.gameserver.services.attribute;

import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.skills.funcs.FuncAdd;
import net.l2emuproject.gameserver.skills.funcs.FuncOwner;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.LookupTable;

public final class Attributes implements FuncOwner
{
	private static final LookupTable<AttributeItems> TABLE = new LookupTable<AttributeItems>();
	
	static
	{
		for (AttributeItems item : AttributeItems.values())
			TABLE.put(item.getItemId(), item);
	}
	
	public final static byte NONE = -1;
	public final static byte FIRE = 0;
	public final static byte WATER = 1;
	public final static byte WIND = 2;
	public final static byte EARTH = 3;
	public final static byte HOLY = 4;
	public final static byte DARK = 5;
	
	public final static byte FIRST_WEAPON_BONUS = 20;
	public final static byte NEXT_WEAPON_BONUS = 5;
	public final static byte ARMOR_BONUS = 6;
	
	public final static short[] WEAPON_VALUES = { 0, // Level 1
		25, // Level 2
		75, // Level 3
		150, // Level 4
		175, // Level 5
		225, // Level 6
		300, // Level 7
		325, // Level 8
		375, // Level 9
		450, // Level 10
		475, // Level 11
		525, // Level 12
		600, // Level 13
	};
	
	public final static short[] ARMOR_VALUES = { 0, // Level 1
		12, // Level 2
		30, // Level 3
		60, // Level 4
		72, // Level 5
		90, // Level 6
		120, // Level 7
		132, // Level 8
		150, // Level 9
		180, // Level 10
		192, // Level 11
		210, // Level 12
		240, // Level 13
	};
	
	private byte _element;
	private int _value;
	
	
	public Attributes(byte type, int value)
	{
		_element = type;
		_value = value;
	}

	/**
	 * @param element the _element to set
	 */
	public void setElement(byte element)
	{
		_element = element;
	}

	/**
	 * @return the _element
	 */
	public byte getElement()
	{
		return _element;
	}

	/**
	 * @param value the _value to set
	 */
	public void setValue(int value)
	{
		_value = value;
	}

	/**
	 * @return the _value
	 */
	public int getValue()
	{
		return _value;
	}
	
	public static String getElementName(byte element)
	{
		switch (element)
		{
			case FIRE:
				return "Fire";
			case WATER:
				return "Water";
			case WIND:
				return "Wind";
			case EARTH:
				return "Earth";
			case DARK:
				return "Dark";
			case HOLY:
				return "Holy";
		}
		return "None";
	}
	
	public static byte getElementId(String name)
	{
		String tmp = name.toLowerCase();
		if (tmp.equals("fire"))
			return FIRE;
		if (tmp.equals("water"))
			return WATER;
		if (tmp.equals("wind"))
			return WIND;
		if (tmp.equals("earth"))
			return EARTH;
		if (tmp.equals("dark"))
			return DARK;
		if (tmp.equals("holy"))
			return HOLY;
		return NONE;
	}
	
	public static byte getItemElementById(int itemId)
	{
		AttributeItems item = TABLE.get(itemId);
		if (item != null)
			return item.getAttributeType();
		return NONE;
	}
	
	public static AttributeItems getItemElemental(int itemId)
	{
		return TABLE.get(itemId);
	}
	
	public static int getMaxAttributeLevelById(int itemId)
	{
		AttributeItems item = TABLE.get(itemId);
		if (item != null)
			return item.getItemType().getMaxLevel();
		return -1;
	}
	
	public static Stats getResist(byte element)
	{
		switch (element)
		{
			case FIRE:
				return Stats.FIRE_RES;
			case WATER:
				return Stats.WATER_RES;
			case WIND:
				return Stats.WIND_RES;
			case EARTH:
				return Stats.EARTH_RES;
			case DARK:
				return Stats.DARK_RES;
			case HOLY:
				return Stats.HOLY_RES;
			default:
				return null;
		}
	}
	
	public static Stats getPower(byte element)
	{
		switch (element)
		{
			case FIRE:
				return Stats.FIRE_POWER;
			case WATER:
				return Stats.WATER_POWER;
			case WIND:
				return Stats.WIND_POWER;
			case EARTH:
				return Stats.EARTH_POWER;
			case DARK:
				return Stats.DARK_POWER;
			case HOLY:
				return Stats.HOLY_POWER;
			default:
				return null;
		}
	}
	
	public static byte getOppositeElement(byte element)
	{
		return (byte)((element % 2 == 0) ? (element + 1) : (element - 1));
	}

	/**
	 * Applies the bonuses to the player.
	 * 
	 * @param player
	 */
	public void applyBonus(L2Player player, boolean isArmor)
	{
		if (isArmor)
			player.addStatFunc(new FuncAdd(getResist(_element), 0x40, this, _value, null));
		else
			player.addStatFunc(new FuncAdd(getPower(_element), 0x40, this, _value, null));
	}
	
	/**
	 * Removes the elemetal bonuses from the player.
	 * 
	 * @param player
	 */
	public void removeBonus(L2Player player)
	{
		player.removeStatsOwner(this);
	}
	
	/**
	 * Update the elemetal bonuses from the player.
	 * 
	 * @param player
	 */
	public void updateBonus(L2Player player, boolean isArmor)
	{
		removeBonus(player);
		applyBonus(player, isArmor);
	}
	
	@Override
	public String toString()
	{
		return getElementName(_element) + " +" + _value;
	}

	@Override
	public String getFuncOwnerName()
	{
		return null;
	}

	@Override
	public L2Skill getFuncOwnerSkill()
	{
		return null;
	}
}
