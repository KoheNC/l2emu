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
package net.l2emuproject.gameserver.network.clientpackets;

import net.l2emuproject.gameserver.model.actor.instance.L2PetInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.system.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.world.object.L2Player;

public class RequestGetItemFromPet extends L2GameClientPacket
{
	private static final String	REQUESTGETITEMFROMPET__C__8C	= "[C] 8C RequestGetItemFromPet";

	private int					_objectId;
	private long				_amount;
	@SuppressWarnings("unused")
	private int					_unknown;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readQ();
		_unknown = readD();// = 0 for most trades
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if (player == null)
			return;
		else if (!getClient().getFloodProtector().tryPerformAction(Protected.TRANSACTION))
			return;

		if (!(player.getPet() instanceof L2PetInstance))
		{
			requestFailed(SystemMessageId.DONT_HAVE_PET);
			return;
		}
		else if (player.getActiveEnchantItem() != null)
		{
			requestFailed(SystemMessageId.TRY_AGAIN_LATER);
			return;
		}

		L2PetInstance pet = (L2PetInstance) player.getPet();

		if (_amount > 0 && pet.transferItem("Transfer", _objectId, _amount, player.getInventory(), player, pet) == null)
			_log.warn("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());

		sendAF();
	}

	@Override
	public String getType()
	{
		return REQUESTGETITEMFROMPET__C__8C;
	}
}
