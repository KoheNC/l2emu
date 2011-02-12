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
import net.l2emuproject.gameserver.datatables.TradeListTable;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.model.L2TradeList;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.ShopPreviewList;

/**
 * TODO: Check wear is working or not...
 */
public class Wear implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "Wear" };

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		if (!Config.ALLOW_WEAR)
			return false;

		try
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();

			if (st.countTokens() < 1)
				return false;

			showWearWindow(activeChar, Integer.parseInt(st.nextToken()));
			return true;
		}
		catch (Exception e)
		{
			_log.info("Exception in " + getClass().getSimpleName());
		}
		return false;
	}

	private static final void showWearWindow(L2PcInstance player, int val)
	{
		player.tempInventoryDisable();

		if (_log.isDebugEnabled())
			_log.debug("Showing wearlist.");

		L2TradeList list = TradeListTable.getInstance().getBuyList(val);

		if (list != null)
		{
			ShopPreviewList bl = new ShopPreviewList(list, player.getAdena(), player.getExpertiseIndex());
			player.sendPacket(bl);
		}
		else
		{
			_log.warn("No buylist with id: " + val);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
