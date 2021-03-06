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

import java.util.StringTokenizer;

import net.l2emuproject.gameserver.events.global.siege.Castle;
import net.l2emuproject.gameserver.events.global.siege.CastleManager;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.BuyListSeed;
import net.l2emuproject.gameserver.network.serverpackets.ExShowCropInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExShowCropSetting;
import net.l2emuproject.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExShowProcureCropDetail;
import net.l2emuproject.gameserver.network.serverpackets.ExShowSeedInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExShowSeedSetting;
import net.l2emuproject.gameserver.network.serverpackets.ExShowSellCropList;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.manor.CastleManorService;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2CastleChamberlainInstance;
import net.l2emuproject.gameserver.world.object.instance.L2ManorManagerInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MerchantInstance;

public final class ManorMenuSelect implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "manor_menu_select" };

	@Override
	public boolean useBypass(String command, L2Player activeChar, L2Character target)
	{
		final L2Npc manager = activeChar.getLastFolkNPC();
		final boolean isCastle = manager instanceof L2CastleChamberlainInstance;
		if (!(manager instanceof L2ManorManagerInstance || isCastle))
			return false;

		if (!activeChar.isInsideRadius(manager, L2Npc.INTERACTION_DISTANCE, true, false))
			return false;

		try
		{
			final Castle castle = manager.getCastle();
			if (isCastle)
			{
				if (activeChar.getClan() == null || castle.getOwnerId() != activeChar.getClanId()
						|| (activeChar.getClanPrivileges() & L2Clan.CP_CS_MANOR_ADMIN) != L2Clan.CP_CS_MANOR_ADMIN)
				{
					manager.showChatWindow(activeChar, "data/npc_data/html/chamberlain/chamberlain-noprivs.htm");
					return false;
				}
				if (castle.getSiege().getIsInProgress())
				{
					manager.showChatWindow(activeChar, "data/npc_data/html/chamberlain/chamberlain-busy.htm");
					return false;
				}
			}

			if (CastleManorService.getInstance().isUnderMaintenance())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE));
				return true;
			}

			final StringTokenizer st = new StringTokenizer(command, "&");
			final int ask = Integer.parseInt(st.nextToken().split("=")[1]);
			final int state = Integer.parseInt(st.nextToken().split("=")[1]);
			final int time = Integer.parseInt(st.nextToken().split("=")[1]);

			final int castleId;
			if (state < 0)
				castleId = castle.getCastleId(); // info for current manor
			else
				castleId = state; // info for requested manor

			switch (ask)
			{
				case 1: // Seed purchase
					if (isCastle)
						break;
					if (castleId != castle.getCastleId())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR);
						sm.addString(manager.getCastle().getName());
						activeChar.sendPacket(sm);
					}
					else
					{
						activeChar.sendPacket(new BuyListSeed(activeChar.getAdena(), castleId, castle
								.getSeedProduction(CastleManorService.PERIOD_CURRENT)));
					}
					break;
				case 2: // Crop sales
					if (isCastle)
						break;
					activeChar.sendPacket(new ExShowSellCropList(activeChar, castleId, castle.getCropProcure(CastleManorService.PERIOD_CURRENT)));
					break;
				case 3: // Current seeds (Manor info)
					if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
						activeChar.sendPacket(new ExShowSeedInfo(castleId, null));
					else
						activeChar.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
					break;
				case 4: // Current crops (Manor info)
					if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
						activeChar.sendPacket(new ExShowCropInfo(castleId, null));
					else
						activeChar.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
					break;
				case 5: // Basic info (Manor info)
					activeChar.sendPacket(new ExShowManorDefaultInfo());
					break;
				case 6: // Buy harvester
					if (isCastle)
						break;
					((L2MerchantInstance) manager).showBuySellRefundWindow(activeChar, 300000 + manager.getNpcId());
					break;
				case 7: // Edit seed setup
					if (!isCastle)
						break;
					if (castle.isNextPeriodApproved())
						activeChar.sendPacket(new SystemMessage(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM));
					else
						activeChar.sendPacket(new ExShowSeedSetting(castle.getCastleId()));
					break;
				case 8: // Edit crop setup
					if (!isCastle)
						break;
					if (castle.isNextPeriodApproved())
						activeChar.sendPacket(new SystemMessage(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM));
					else
						activeChar.sendPacket(new ExShowCropSetting(castle.getCastleId()));
				case 9: // Edit sales (Crop sales)
					if (isCastle)
						break;
					activeChar.sendPacket(new ExShowProcureCropDetail(state));
					break;
				default:
					return false;
			}
			return true;
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage());
		}
		return false;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
