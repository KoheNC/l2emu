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

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.model.L2Object;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PetInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2SummonInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.model.restriction.global.GlobalRestrictions;
import net.l2emuproject.gameserver.model.world.L2World;

public class RequestPetGetItem extends L2GameClientPacket
{
	private static final String _C__8f_REQUESTPETGETITEM= "[C] 8F RequestPetGetItem";

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		L2Object obj = player.getKnownList().getKnownObject(_objectId);
		if (obj == null)
		{
			obj = L2World.getInstance().findObject(_objectId);
			//_log.warn("Player "+player.getName()+" requested pet to pickup item from outside of his knownlist.");
		}
		if (!(obj instanceof L2ItemInstance))
		{
			sendAF();
			return;
		}

		L2ItemInstance item = (L2ItemInstance) obj;

		if (player.getPet() == null || player.getPet() instanceof L2SummonInstance)
		{
			sendAF();
			return;
		}

		L2PetInstance pet = (L2PetInstance) player.getPet();
		if (pet.isDead() || pet.isOutOfControl())
		{
			sendAF();
			return;
		}

		if (!GlobalRestrictions.canPickUp(player, item, pet))
		{
			sendAF();
			return;
		}

		pet.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, item);
	}

	@Override
	public String getType()
	{
		return _C__8f_REQUESTPETGETITEM;
	}
}
