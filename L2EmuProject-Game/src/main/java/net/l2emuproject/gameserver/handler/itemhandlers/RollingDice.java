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
package net.l2emuproject.gameserver.handler.itemhandlers;

import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.instancemanager.TownManager;
import net.l2emuproject.gameserver.model.actor.L2Playable;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.Dice;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.util.Broadcast;
import net.l2emuproject.gameserver.util.FloodProtector.Protected;
import net.l2emuproject.tools.random.Rnd;

public class RollingDice implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[]	ITEM_IDS	=
											{ 4625, 4626, 4627, 4628 };

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;
		int itemId = item.getItemId();

		if (activeChar.getPlayerOlympiad().isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}

		if (itemId == 4625 || itemId == 4626 || itemId == 4627 || itemId == 4628)
		{
			int number = rollDice(activeChar);
			if (number == 0)
			{
				activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER);
				return;
			}

			Dice d = new Dice(activeChar.getObjectId(), item.getItemId(), number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ());
			Broadcast.toSelfAndKnownPlayers(activeChar, d);

			SystemMessage sm = new SystemMessage(SystemMessageId.C1_ROLLED_S2);
			sm.addString(activeChar.getName());
			sm.addNumber(number);

			activeChar.sendPacket(sm);
			if (!TownManager.getInstance().checkIfInZone(activeChar))
				Broadcast.toKnownPlayers(activeChar, sm);
			else if (activeChar.isInParty())
				activeChar.getParty().broadcastToPartyMembers(activeChar, sm);
		}
	}

	private int rollDice(L2PcInstance player)
	{
		// Check if the dice is ready
		if (!player.getFloodProtector().tryPerformAction(Protected.ROLLDICE))
			return 0;
		return Rnd.get(1, 6);
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
