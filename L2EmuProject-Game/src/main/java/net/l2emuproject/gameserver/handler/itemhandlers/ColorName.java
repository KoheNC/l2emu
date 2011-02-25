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
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.clientpackets.RequestChangeNicknameColor;
import net.l2emuproject.gameserver.network.serverpackets.ExChangeNicknameNColor;
import net.l2emuproject.gameserver.world.object.L2Playable;

public class ColorName implements IItemHandler
{
	private static final int[] ITEM_IDS = {
		RequestChangeNicknameColor.COLOR_NAME_1, RequestChangeNicknameColor.COLOR_NAME_2
	};

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		playable.sendPacket(ExChangeNicknameNColor.PACKET);
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
