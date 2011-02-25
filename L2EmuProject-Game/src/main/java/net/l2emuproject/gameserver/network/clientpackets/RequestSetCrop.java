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

import static net.l2emuproject.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;
import static net.l2emuproject.gameserver.world.object.L2Npc.INTERACTION_DISTANCE;

import java.util.ArrayList;
import java.util.List;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.instancemanager.CastleManager;
import net.l2emuproject.gameserver.model.actor.instance.L2CastleChamberlainInstance;
import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.model.entity.Castle;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.services.manor.CastleManorService;
import net.l2emuproject.gameserver.services.manor.CastleManorService.CropProcure;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;


/**
 * Format: (ch) dd [dddc]
 * 
 * @author -Wooden-
 * 
 *         d - manor id d - size [ d - crop id d - sales d - price c - reward
 *         type ]
 * @author l3x
 * 
 */
public class RequestSetCrop extends L2GameClientPacket
{
	private static final String	_C__D0_0B_REQUESTSETCROP	= "[C] D0:0B RequestSetCrop";

	private static final int	BATCH_LENGTH				= 21;							// length of the one item

	private int					_manorId;

	private Crop[]				_items						= null;

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != getByteBuffer().remaining())
		{
			return;
		}

		_items = new Crop[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			long sales = readQ();
			long price = readQ();
			int type = readC();
			if (itemId < 1 || sales < 0 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new Crop(itemId, sales, price, type);
		}
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if (player == null)
			return;

		if (_items == null || player.getClan() == null)
		{
			sendAF();
			return;
		}

		// check player privileges
		if (!L2Clan.checkPrivileges(player, L2Clan.CP_CS_MANOR_ADMIN))
		{
			requestFailed(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		// check castle owner
		Castle currentCastle = CastleManager.getInstance().getCastleById(_manorId);
		if (currentCastle.getOwnerId() != player.getClanId())
		{
			sendAF();
			return;
		}

		L2Object manager = player.getTarget();

		if (!(manager instanceof L2CastleChamberlainInstance))
			manager = player.getLastFolkNPC();

		if (!(manager instanceof L2CastleChamberlainInstance))
		{
			sendAF();
			return;
		}

		if (((L2CastleChamberlainInstance) manager).getCastle() != currentCastle)
		{
			sendAF();
			return;
		}

		if (!player.isInsideRadius(manager, INTERACTION_DISTANCE, true, false))
		{
			requestFailed(SystemMessageId.TOO_FAR_FROM_NPC);
			return;
		}

		List<CropProcure> crops = new ArrayList<CropProcure>(_items.length);
		for (Crop i : _items)
		{
			CropProcure s = i.getCrop();
			if (s == null)
			{
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			crops.add(s);
		}

		currentCastle.setCropProcure(crops, CastleManorService.PERIOD_NEXT);
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			currentCastle.saveCropData(CastleManorService.PERIOD_NEXT);

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__D0_0B_REQUESTSETCROP;
	}

	private class Crop
	{
		private final int	_itemId;
		private final long	_sales;
		private final long	_price;
		private final int	_type;

		public Crop(int id, long s, long p, int t)
		{
			_itemId = id;
			_sales = s;
			_price = p;
			_type = t;
		}

		public CropProcure getCrop()
		{
			if (_sales != 0 && (MAX_ADENA / _sales) < _price)
				return null;

			return CastleManorService.getInstance().getNewCropProcure(_itemId, _sales, _type, _price, _sales);
		}
	}
}
