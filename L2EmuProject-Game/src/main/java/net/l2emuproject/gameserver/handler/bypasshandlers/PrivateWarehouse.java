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
package net.l2emuproject.gameserver.handler.bypasshandlers;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2WarehouseInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import net.l2emuproject.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import net.l2emuproject.gameserver.network.serverpackets.WareHouseDepositList;
import net.l2emuproject.gameserver.network.serverpackets.WareHouseWithdrawalList;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class PrivateWarehouse implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "WithdrawP", "WithdrawSortedP", "DepositP" };

	@Override
	public boolean useBypass(String command, L2Player activeChar, L2Character target)
	{
		if (!(target instanceof L2WarehouseInstance))
			return false;

		try
		{
			if (command.startsWith(COMMANDS[0]))
			{
				if (Config.ENABLE_WAREHOUSESORTING_PRIVATE)
				{
					String htmFile = "data/html/custom/WhSortedP.htm";
					String htmContent = HtmCache.getInstance().getHtm(htmFile);
					if (htmContent != null)
					{
						NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(target.getObjectId());
						npcHtmlMessage.setHtml(htmContent);
						npcHtmlMessage.replace("%objectId%", String.valueOf(target.getObjectId()));
						activeChar.sendPacket(npcHtmlMessage);
					}
					else
					{
						_log.warn("Missing htm: " + htmFile + " !");
					}
				}
				else
					showRetrieveWindow(activeChar, null, (byte) 0);
			}
			else if (command.startsWith(COMMANDS[1]))
			{
				String param[] = command.split("_");

				if (param.length > 2)
					showRetrieveWindow(activeChar, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
				else if (param.length > 1)
					showRetrieveWindow(activeChar, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
				else
					showRetrieveWindow(activeChar, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
			}
			else if (command.equals(COMMANDS[2]))
			{
				showDepositWindow(activeChar);
			}

			return true;
		}
		catch (Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName());
		}
		return false;
	}

	private final void showRetrieveWindow(L2Player player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());

		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}

		if (_log.isDebugEnabled())
			_log.debug("Showing stored items");

		if (itemtype != null)
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, itemtype, sortorder));
		else
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
	}

	private final void showDepositWindow(L2Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		player.tempInventoryDisable();
		if (_log.isDebugEnabled())
			_log.debug("Showing items to deposit");

		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
