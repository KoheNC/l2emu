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
package net.l2emuproject.gameserver.world.zone;

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.object.L2Character;

public class L2PaganZone extends L2Zone
{
	private static final int MARK = 8064;
	private static final int FADED_MARK = 8065;

	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			if (character.destroyItemByItemId("Pagan Zone", MARK, 1, character, false))
			{
				character.getInventory().addItem("Pagan Zone", FADED_MARK, 1, null, character);
				character.getActingPlayer().sendPacket(new SystemMessage(SystemMessageId.EARNED_S1).addItemName(FADED_MARK));
			}
		}
	}
}
