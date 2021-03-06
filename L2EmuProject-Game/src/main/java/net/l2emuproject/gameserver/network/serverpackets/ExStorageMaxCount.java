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
package net.l2emuproject.gameserver.network.serverpackets;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.skills.Stats;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Format: (ch)ddddddd
 * d: Number of Inventory Slots
 * d: Number of Warehouse Slots
 * d: Number of Freight Slots (unconfirmed) (200 for a low level dwarf)
 * d: Private Sell Store Slots (unconfirmed) (4 for a low level dwarf)
 * d: Private Buy Store Slots (unconfirmed) (5 for a low level dwarf)
 * d: Dwarven Recipe Book Slots
 * d: Normal Recipe Book Slots
 * @author -Wooden-
 * format from KenM
 */
public final class ExStorageMaxCount extends L2GameServerPacket
{
	private static final String	_S__FE_2E_EXSTORAGEMAXCOUNT	= "[S] FE:2E ExStorageMaxCount";
	private final L2Player	_activeChar;
	private final int			_inventory;
	private final int			_warehouse;
	private final int			_clan;
	private final int			_privateSell;
	private final int			_privateBuy;
	private final int			_receipeD;
	private final int			_recipe;
	private final int			_inventoryExtraSlots;
	private final int			_inventoryQuestItems;

	public ExStorageMaxCount(L2Player character)
	{
		_activeChar = character;
		_inventory = _activeChar.getInventoryLimit();
		_warehouse = _activeChar.getWareHouseLimit();
		_privateSell = _activeChar.getPrivateSellStoreLimit();
		_privateBuy = _activeChar.getPrivateBuyStoreLimit();
		_clan = Config.WAREHOUSE_SLOTS_CLAN;
		_receipeD = _activeChar.getDwarfRecipeLimit();
		_recipe = _activeChar.getCommonRecipeLimit();
		_inventoryExtraSlots = (int) _activeChar.getStat().calcStat(Stats.INV_LIM, 0, null, null);
		_inventoryQuestItems = Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2f);

		writeD(_inventory);
		writeD(_warehouse);
		writeD(_clan);
		writeD(_privateSell);
		writeD(_privateBuy);
		writeD(_receipeD);
		writeD(_recipe);
		writeD(_inventoryExtraSlots); // Belt inventory slots increase count
		writeD(_inventoryQuestItems);
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.BasePacket#getType()
	 */
	@Override
	public final String getType()
	{
		return _S__FE_2E_EXSTORAGEMAXCOUNT;
	}
}
