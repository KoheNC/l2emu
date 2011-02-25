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

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.object.L2Playable;

/**
 * @author Charus
 * @changes Gnat / remake for L2Emu - Kai http://l2vault.ign.com/View.php?view=Guides.Detail&id=439 cubic cannot be used when sitting
 */
public final class WondrousCubic implements IItemHandler
{
	private static final int	WONDOROUS_CUBIC	= 10632;

	private static final int[]	ITEM_IDS		=
												{ WONDOROUS_CUBIC };

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance activeChar = (L2PcInstance) playable;
		if (activeChar.isSubClassActive())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return;
		}
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		if (item.getItemId() == WONDOROUS_CUBIC)
			activeChar.useMagic(SkillTable.getInstance().getInfo(2510, 1), true, false);
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
