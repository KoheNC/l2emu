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

/**
 * @author Intrepid
 *
 */
public enum AttributeItems
{
	fireStone(9546, Attributes.FIRE, AttributeItemType.STONE),
	waterStone(9547, Attributes.WATER, AttributeItemType.STONE),
	windStone(9549, Attributes.WIND, AttributeItemType.STONE),
	earthStone(9548, Attributes.EARTH, AttributeItemType.STONE),
	divineStone(9551, Attributes.HOLY, AttributeItemType.STONE),
	darkStone(9550, Attributes.DARK, AttributeItemType.STONE),
	
	fireRoughtore(10521, Attributes.FIRE, AttributeItemType.ROUGHORE),
	waterRoughtore(10522, Attributes.WATER, AttributeItemType.ROUGHORE),
	windRoughtore(10524, Attributes.WIND, AttributeItemType.ROUGHORE),
	earthRoughtore(10523, Attributes.EARTH, AttributeItemType.ROUGHORE),
	divineRoughtore(10526, Attributes.HOLY, AttributeItemType.ROUGHORE),
	darkRoughtore(10525, Attributes.DARK, AttributeItemType.ROUGHORE),
	
	fireCrystal(9552, Attributes.FIRE, AttributeItemType.CRYSTAL),
	waterCrystal(9553, Attributes.WATER, AttributeItemType.CRYSTAL),
	windCrystal(9555, Attributes.WIND, AttributeItemType.CRYSTAL),
	earthCrystal(9554, Attributes.EARTH, AttributeItemType.CRYSTAL),
	divineCrystal(9557, Attributes.HOLY, AttributeItemType.CRYSTAL),
	darkCrystal(9556, Attributes.DARK, AttributeItemType.CRYSTAL),
	
	fireJewel(9558, Attributes.FIRE, AttributeItemType.JEWEL),
	waterJewel(9559, Attributes.WATER, AttributeItemType.JEWEL),
	windJewel(9561, Attributes.WIND, AttributeItemType.JEWEL),
	earthJewel(9560, Attributes.EARTH, AttributeItemType.JEWEL),
	divineJewel(9563, Attributes.HOLY, AttributeItemType.JEWEL),
	darkJewel(9562, Attributes.DARK, AttributeItemType.JEWEL),
	
	// not yet supported by client (Freya pts)
	fireEnergy(9564, Attributes.FIRE, AttributeItemType.ENERGY),
	waterEnergy(9565, Attributes.WATER, AttributeItemType.ENERGY),
	windEnergy(9567, Attributes.WIND, AttributeItemType.ENERGY),
	earthEnergy(9566, Attributes.EARTH, AttributeItemType.ENERGY),
	divineEnergy(9569, Attributes.HOLY, AttributeItemType.ENERGY),
	darkEnergy(9568, Attributes.DARK, AttributeItemType.ENERGY);
	
	private int _itemId;
	private byte _attributeType;
	private AttributeItemType _itemType;
	
	private AttributeItems(int itemId, byte attributeType, AttributeItemType itemType)
	{
		_itemId = itemId;
		_attributeType = attributeType;
		_itemType = itemType;
	}

	/**
	 * @param _itemId the _itemId to set
	 */
	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}

	/**
	 * @return the _itemId
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * @param _attributeType the _attributeType to set
	 */
	public void setAttributeType(byte attributeType)
	{
		_attributeType = attributeType;
	}

	/**
	 * @return the _attributeType
	 */
	public byte getAttributeType()
	{
		return _attributeType;
	}

	/**
	 * @param _itemType the _itemType to set
	 */
	public void setItemType(AttributeItemType itemType)
	{
		_itemType = itemType;
	}

	/**
	 * @return the _itemType
	 */
	public AttributeItemType getItemType()
	{
		return _itemType;
	}
}
