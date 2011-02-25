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

import net.l2emuproject.gameserver.model.actor.instance.L2AirShipInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2ControllableAirShipInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.object.L2Playable;

public final class EnergyStarStone extends ItemSkills
{
	@Override
	public final void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		final L2AirShipInstance ship = ((L2PcInstance) playable).getAirShip();
		if (ship == null || !(ship instanceof L2ControllableAirShipInstance) || ship.getFuel() >= ship.getMaxFuel())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			playable.getActingPlayer().sendPacket(sm);
			sm.addItemName(item);
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		super.useItem(playable, item);
	}
}