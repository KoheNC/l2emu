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

import static net.l2emuproject.gameserver.world.object.L2Npc.INTERACTION_DISTANCE;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.Shutdown;
import net.l2emuproject.gameserver.Shutdown.DisableType;
import net.l2emuproject.gameserver.items.ItemRequest;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.services.transactions.TradeList;
import net.l2emuproject.gameserver.system.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;

public class RequestPrivateStoreSell extends L2GameClientPacket
{
	private static final String	_C__96_REQUESTPRIVATESTORESELL	= "[C] 96 RequestPrivateStoreSell";

	private static final int	BATCH_LENGTH					= 28;								// length of the one item

	private int					_storePlayerId;
	private ItemRequest[]		_items							= null;

	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != getByteBuffer().remaining())
		{
			return;
		}
		_items = new ItemRequest[count];

		for (int i = 0; i < count; i++)
		{
			int objectId = readD();
			int itemId = readD();
			readH(); //TODO: analyse this
			readH(); //TODO: analyse this
			long cnt = readQ();
			long price = readQ();

			if (objectId < 1 || itemId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new ItemRequest(objectId, itemId, cnt, price);
		}
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if (player == null)
			return;
		else if (!getClient().getFloodProtector().tryPerformAction(Protected.TRANSACTION))
			return;

		if (_items == null)
		{
			sendAF();
			return;
		}

		if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
		{
			requestFailed(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			return;
		}

		L2Object object = null;

		// Get object from target
		if (player.getTargetId() == _storePlayerId)
			object = player.getTarget();

		// Get object from world
		if (object == null)
			object = L2World.getInstance().getPlayer(_storePlayerId);

		if (!(object instanceof L2Player))
		{
			requestFailed(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		L2Player storePlayer = (L2Player) object;

		if (!player.isInsideRadius(storePlayer, INTERACTION_DISTANCE, true, false))
		{
			sendAF();
			return;
		}

		if (storePlayer.getPrivateStoreType() != L2Player.STORE_PRIVATE_BUY)
		{
			sendAF();
			return;
		}

		TradeList storeList = storePlayer.getBuyList();
		if (storeList == null)
		{
			sendAF();
			return;
		}

		if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
		{
			requestFailed(SystemMessageId.ACCOUNT_CANT_TRADE_ITEMS);
			return;
		}

		if (!storeList.privateStoreSell(player, _items))
		{
			sendAF();
			return;
		}

		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__96_REQUESTPRIVATESTORESELL;
	}
}
