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
package net.l2emuproject.gameserver.model.restriction.global;

import net.l2emuproject.gameserver.instancemanager.MercTicketManager;
import net.l2emuproject.gameserver.model.actor.instance.L2PetInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author savormix
 */
public final class MercenaryTicketRestriction extends AbstractRestriction
{
	@Override
	public boolean canPickUp(L2Player activeChar, L2ItemInstance item, L2PetInstance pet)
	{
		if (!MercTicketManager.getInstance().isTicket(item.getItemId()))
			return true;
		else if (!MercTicketManager.getInstance().canPickUp(activeChar, item))
			return false;

		activeChar.leaveParty();
		return true;
	}
}
