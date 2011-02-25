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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.instancemanager.CastleManager;
import net.l2emuproject.gameserver.instancemanager.TerritoryWarManager;
import net.l2emuproject.gameserver.model.actor.instance.L2MercenaryManagerInstance;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.ExBrExtraUserInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExShowDominionRegistry;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.network.serverpackets.UserInfo;
import net.l2emuproject.gameserver.services.transactions.L2Multisell;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public class TerritoryWar implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "Territory", "TW_Multisell", "TW_Buy_List", "TW_Buy", "CalcRewards", "ReceiveRewards" };

	@Override
	public boolean useBypass(String command, L2Player activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		try
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken(); // Get actual command

			if (actualCommand.equalsIgnoreCase("Territory"))
			{
				if (st.countTokens() < 1)
					return false;

				int castleId = Integer.parseInt(st.nextToken());
				activeChar.sendPacket(new ExShowDominionRegistry(castleId, activeChar));
			}
			else if (!(target instanceof L2MercenaryManagerInstance))
				return false;

			L2MercenaryManagerInstance mercman = ((L2MercenaryManagerInstance) target);
			if (actualCommand.equalsIgnoreCase("TW_Multisell"))
			{
				if (st.countTokens() < 1)
					return false;
				int territoryItemId = Integer.parseInt(st.nextToken());
				if (activeChar.getInventory().getItemByItemId(territoryItemId) == null)
				{
					mercman.showChatWindow(activeChar, 1);
					return true;
				}

				int val = Integer.parseInt(st.nextToken());
				L2Multisell.getInstance().separateAndSend(val, activeChar, mercman.getNpcId(), false, mercman.getCastle().getTaxRate());
			}
			else if (actualCommand.equalsIgnoreCase("TW_Buy_List"))
			{
				if (st.countTokens() < 1)
					return false;

				String itemId = st.nextToken();
				NpcHtmlMessage html = new NpcHtmlMessage(mercman.getObjectId());
				html.setFile("data/html/mercmanager/" + st.nextToken());
				html.replace("%itemId%", itemId);
				html.replace("%noblessBadge%", String.valueOf(Config.MINTWBADGEFORNOBLESS));
				html.replace("%striderBadge%", String.valueOf(Config.MINTWBADGEFORSTRIDERS));
				html.replace("%gstriderBadge%", String.valueOf(Config.MINTWBADGEFORBIGSTRIDER));
				html.replace("%objectId%", String.valueOf(mercman.getObjectId()));
				activeChar.sendPacket(html);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (actualCommand.equalsIgnoreCase("TW_Buy"))
			{
				int itemId = Integer.parseInt(st.nextToken());
				int count = Integer.parseInt(st.nextToken());
				int type = Integer.parseInt(st.nextToken());
				if (activeChar.getInventory().getItemByItemId(itemId) != null)
				{
					long playerItemCount = activeChar.getInventory().getItemByItemId(itemId).getCount();
					if (count <= playerItemCount)
					{
						int boughtId = 0;
						switch (type)
						{
							case 0:
								if (activeChar.isNoble())
									return false;
								boughtId = 7694;
								activeChar.setNoble(true);
								activeChar.sendPacket(new UserInfo(activeChar));
								activeChar.sendPacket(new ExBrExtraUserInfo(activeChar));
								break;
							case 1:
								boughtId = 4422;
								break;
							case 2:
								boughtId = 4423;
								break;
							case 3:
								boughtId = 4424;
								break;
							case 4:
								boughtId = 14819;
								break;
							default:
								_log.warn("TerritoryWar buy: not handled type: " + type);
								return false;
						}
						activeChar.destroyItemByItemId("QUEST", itemId, count, mercman, true);
						activeChar.addItem("QUEST", boughtId, 1, mercman, false);
						mercman.showChatWindow(activeChar, 7);
						return true;
					}
				}
				mercman.showChatWindow(activeChar, 6);
			}
			else if (actualCommand.equalsIgnoreCase("CalcRewards"))
			{
				int territoryId = Integer.parseInt(st.nextToken());
				int[] reward = TerritoryWarManager.getInstance().calcReward(activeChar);
				NpcHtmlMessage html = new NpcHtmlMessage(mercman.getObjectId());
				if (TerritoryWarManager.getInstance().isTWInProgress() || reward[0] == 0)
					html.setFile("data/html/mercmanager/reward-0a.htm");
				else if (reward[0] != territoryId)
				{
					html.setFile("data/html/mercmanager/reward-0b.htm");
					html.replace("%castle%", CastleManager.getInstance().getCastleById(reward[0] - 80).getName());
				}
				else if (reward[1] == 0)
					html.setFile("data/html/mercmanager/reward-0a.htm");
				else
				{
					html.setFile("data/html/mercmanager/reward-1.htm");
					html.replace("%castle%", CastleManager.getInstance().getCastleById(reward[0] - 80).getName());
					html.replace("%badge%", String.valueOf(reward[1]));
					html.replace("%adena%", String.valueOf(reward[1] * 5000));
				}
				html.replace("%territoryId%", String.valueOf(territoryId));
				html.replace("%objectId%", String.valueOf(mercman.getObjectId()));
				activeChar.sendPacket(html);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (actualCommand.equalsIgnoreCase("ReceiveRewards"))
			{
				int territoryId = Integer.parseInt(st.nextToken());
				int badgeId = 57;
				if (TerritoryWarManager.getInstance().TERRITORY_ITEM_IDS.containsKey(territoryId))
					badgeId = TerritoryWarManager.getInstance().TERRITORY_ITEM_IDS.get(territoryId);
				int[] reward = TerritoryWarManager.getInstance().calcReward(activeChar);
				NpcHtmlMessage html = new NpcHtmlMessage(mercman.getObjectId());
				if (TerritoryWarManager.getInstance().isTWInProgress() || reward[0] == 0)
					html.setFile("data/html/mercmanager/reward-0a.htm");
				else if (reward[0] != territoryId)
				{
					html.setFile("data/html/mercmanager/reward-0b.htm");
					html.replace("%castle%", CastleManager.getInstance().getCastleById(reward[0] - 80).getName());
				}
				else if (reward[1] == 0)
					html.setFile("data/html/mercmanager/reward-0a.htm");
				else
				{
					html.setFile("data/html/mercmanager/reward-2.htm");
					activeChar.addItem("QUEST", badgeId, reward[1], mercman, true);
					activeChar.addAdena("QUEST", reward[1] * 5000, mercman, true);
					TerritoryWarManager.getInstance().resetReward(activeChar);
				}

				html.replace("%objectId%", String.valueOf(mercman.getObjectId()));
				activeChar.sendPacket(html);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			return true;
		}
		catch (Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName());
		}
		return false;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
