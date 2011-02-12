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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.ExtractableItemsData;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.items.model.L2ExtractableItem;
import net.l2emuproject.gameserver.items.model.L2ExtractableProductItem;
import net.l2emuproject.gameserver.model.L2ItemInstance;
import net.l2emuproject.gameserver.model.actor.L2Playable;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author FBIagent 11/12/2006
 *
 */

public class ExtractableItems implements IItemHandler
{
	protected static Log	_log						= LogFactory.getLog(ExtractableItems.class);

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		int itemID = item.getItemId();
		L2ExtractableItem exitem = ExtractableItemsData.getInstance().getExtractableItem(itemID);

		if (exitem == null)
			return;

		int rndNum = Rnd.get(100), chanceFrom = 0;
		int[] createItemID = new int[20];
		int[] createAmount = new int[20];

		// calculate extraction
		for (L2ExtractableProductItem expi : exitem.getProductItemsArray())
		{
			int chance = expi.getChance();

			if (rndNum >= chanceFrom && rndNum <= chance + chanceFrom)
			{
				createItemID = expi.getId();

				for (int i = 0; i < expi.getId().length; i++)
				{
					createItemID[i] = expi.getId()[i];

					if ((itemID >= 6411 && itemID <= 6518) || (itemID >= 7726 && itemID <= 7860) || (itemID >= 8403 && itemID <= 8483))
						createAmount[i] = expi.getAmmount()[i] * Config.RATE_EXTR_FISH;
					else
						createAmount[i] = expi.getAmmount()[i];
				}
				break;
			}

			chanceFrom += chance;
		}

		if (createItemID[0] <= 0 || createItemID.length == 0 )
		{
			activeChar.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
		}
		else
		{
			for (int i = 0; i < createItemID.length; i++)
			{
				if (createItemID[i] <= 0)
					continue;

				if (ItemTable.getInstance().getTemplate(createItemID[i]) == null)
				{
					_log.warn("createItemID " + createItemID[i] + " doesn't have template!");
					activeChar.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
					continue;
				}

				if (ItemTable.getInstance().getTemplate(createItemID[i]).isStackable())
				{
					activeChar.addItem("Extract", createItemID[i], createAmount[i], activeChar, false);
				}
				else
				{
					for (int j = 0; j < createAmount[i]; j++)
						activeChar.addItem("Extract", createItemID[i], 1, activeChar, false);
				}
				if (createItemID[i] == PcInventory.ADENA_ID)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S1_ADENA);
					sm.addNumber(createAmount[i]);
					activeChar.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
					sm.addItemName(createItemID[i]);
					sm.addNumber(createAmount[i]);
					activeChar.sendPacket(sm);
				}
			}
		}
		activeChar.destroyItemByItemId("Extract", itemID, 1, activeChar.getTarget(), true);
	}

	@Override
	public int[] getItemIds()
	{
		return ExtractableItemsData.getInstance().itemIDs();
	}
}
