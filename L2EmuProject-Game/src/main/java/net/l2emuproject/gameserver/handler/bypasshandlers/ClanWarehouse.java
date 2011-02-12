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
import net.l2emuproject.gameserver.cache.HtmCache;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.model.L2Clan;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2WarehouseInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import net.l2emuproject.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import net.l2emuproject.gameserver.network.serverpackets.WareHouseDepositList;
import net.l2emuproject.gameserver.network.serverpackets.WareHouseWithdrawalList;

public class ClanWarehouse implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "WithdrawC", "WithdrawSortedC", "DepositC" };

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2WarehouseInstance) && !(target instanceof L2ClanHallManagerInstance))
			return false;

		try
		{
			if (command.startsWith(COMMANDS[0]))
			{
				if (Config.ENABLE_WAREHOUSESORTING_CLAN)
				{
					String htmFile = "data/html/custom/WhSortedC.htm";
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
					showWithdrawWindowClan(activeChar, null, (byte) 0);
			}
			else if (command.startsWith(COMMANDS[1]))
			{
				String param[] = command.split("_");

				if (param.length > 2)
					showWithdrawWindowClan(activeChar, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
				else if (param.length > 1)
					showWithdrawWindowClan(activeChar, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
				else
					showWithdrawWindowClan(activeChar, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
			}
			else if (command.equals(COMMANDS[2]))
			{
				showDepositWindowClan(activeChar);
			}
			return false;
		}
		catch (Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName());
		}
		return false;
	}

	private void showDepositWindowClan(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if (player.getClan() != null)
		{
			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
			}
			else
			{
				if (!L2Clan.checkPrivileges(player, L2Clan.CP_CL_VIEW_WAREHOUSE))
					player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
				player.setActiveWarehouse(player.getClan().getWarehouse());
				player.tempInventoryDisable();
				if (_log.isDebugEnabled())
					_log.debug("Showing items to deposit - clan");
				player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.CLAN));
			}
		}
	}

	private void showWithdrawWindowClan(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		if (player.getClan() == null || player.getClan().getLevel() == 0)
		{
			player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
		}
		else if (!L2Clan.checkPrivileges(player, L2Clan.CP_CL_VIEW_WAREHOUSE))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return;
		}
		else
		{
			player.setActiveWarehouse(player.getClan().getWarehouse());
			if (_log.isDebugEnabled())
				_log.debug("Showing items to deposit - clan");

			if (itemtype != null)
				player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN, itemtype, sortorder));
			else
				player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
