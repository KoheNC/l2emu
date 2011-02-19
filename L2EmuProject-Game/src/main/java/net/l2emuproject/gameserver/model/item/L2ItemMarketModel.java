/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE See the GNU General Public License for more
 * details
 * 
 * You should have received a copy of the GNU General Public License along with
 *  program If not, see <http://wwwgnuorg/licenses/>
 */
package net.l2emuproject.gameserver.model.item;

public class L2ItemMarketModel
{
	private int		_ownerId;
	private int		_enchLvl;
	private int		_itemId;
	private int		_itemGrade;
	private int		_itemObjId;
	private int		_price;
	private int		_count;
	private String	_ownerName	= null;
	private String	_itemName	= null;
	private String	_itemType	= null;
	private String	_l2Type		= null;

	public void setOwnerId(int ownerId)
	{
		_ownerId = ownerId;
	}

	public void setOwnerName(String ownerName)
	{
		_ownerName = ownerName;
	}

	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}

	public void setItemObjId(int itemObjId)
	{
		_itemObjId = itemObjId;
	}

	public void setItemName(String itemName)
	{
		_itemName = itemName;
	}

	public void setEnchLvl(int enchLvl)
	{
		_enchLvl = enchLvl;
	}

	public void setItemGrade(int itemGrade)
	{
		_itemGrade = itemGrade;
	}

	public void setItemType(String itemType)
	{
		_itemType = itemType;
	}

	public void setL2Type(String l2Type)
	{
		_l2Type = l2Type;
	}

	public void setPrice(int price)
	{
		_price = price;
	}

	public void setCount(int count)
	{
		_count = count;
	}

	public int getOwnerId()
	{
		return _ownerId;
	}

	public String getOwnerName()
	{
		return _ownerName;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getItemObjId()
	{
		return _itemObjId;
	}

	public String getItemName()
	{
		return _itemName;
	}

	public int getEnchLvl()
	{
		return _enchLvl;
	}

	public int getItemGrade()
	{
		return _itemGrade;
	}

	public String getItemType()
	{
		return _itemType;
	}

	public String getL2Type()
	{
		return _l2Type;
	}

	public int getPrice()
	{
		return _price;
	}

	public int getCount()
	{
		return _count;
	}
}