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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.Shutdown;
import net.l2emuproject.gameserver.Shutdown.DisableType;
import net.l2emuproject.gameserver.datatables.GmListTable;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.manager.MercTicketManager;
import net.l2emuproject.gameserver.model.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.UserInfo;
import net.l2emuproject.gameserver.system.util.IllegalPlayerAction;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.system.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.templates.item.L2EtcItemType;
import net.l2emuproject.gameserver.templates.item.L2Item;
import net.l2emuproject.gameserver.world.object.L2Player;

public class RequestDropItem extends L2GameClientPacket
{
	private static final String	_C__REQUESTDROPITEM	= "[C] 17 RequestDropItem c[dqddd]";

	private int					_objectId;
	private long				_count;
	private int					_x;
	private int					_y;
	private int					_z;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
		_x = readD();
		_y = readD();
		_z = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getActiveChar();
		if (activeChar == null || activeChar.isDead())
			return;

		// Flood protect drop to avoid packet lag, do not add any messages here
		if (!getClient().getFloodProtector().tryPerformAction(Protected.DROPITEM))
			return;
		else if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
		{
			requestFailed(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			return;
		}
		else if (Config.GM_DISABLE_TRANSACTION && activeChar.getAccessLevel() >= Config.GM_TRANSACTION_MIN && activeChar.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
		{
			requestFailed(SystemMessageId.ACCOUNT_CANT_DROP_ITEMS);
			return;
		}

		L2ItemInstance item = activeChar.checkItemManipulation(_objectId, _count, "Drop");
		if (item == null || _count == 0)
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		if (_count > item.getCount() || _count < 1)
		{
			sendAF();
			return;
		}
		else if (!item.isStackable() && _count > 1)
		{
			sendAF();
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] count > 1 but item is not stackable! ban! oid: " + _objectId + " owner: " + activeChar.getName(), IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		else if (!canDrop(item)) // TODO: merge to restriction
			return;

		if (_log.isDebugEnabled())
			_log.debug("requested drop item " + _objectId + "(" + item.getCount() + ") at " + _x + "/" + _y + "/" + _z);

		if (item.isEquipped())
		{
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequiped)
				iu.addModifiedItem(element);
			sendPacket(iu);
			
			// must be sent explicitly after IU
			sendPacket(new UserInfo(activeChar));
			
			activeChar.broadcastUserInfo();
		}

		if (MercTicketManager.getInstance().isTicket(item.getItemId()))
		{
			MercTicketManager.getInstance().reqPosition(activeChar, item);
			sendAF();
			return;
		}

		L2ItemInstance dropedItem = activeChar.dropItem("Drop", _objectId, _count, _x, _y, _z, activeChar, false);

		sendAF();

		if (_log.isDebugEnabled())
			_log.debug("dropping " + _objectId + " item(" + _count + ") at: " + _x + " " + _y + " " + _z);
		if (dropedItem != null && dropedItem.getItemId() == PcInventory.ADENA_ID && dropedItem.getCount() >= 1000000)
		{
			String msg = "Character (" + activeChar.getName() + ") has dropped (" + dropedItem.getCount() + ")adena at (" + _x + "," + _y + "," + _z + ")";
			_log.warn(msg);
			GmListTable.broadcastMessageToGMs(msg);
		}
	}

	private final boolean canDrop(L2ItemInstance item)
	{
		L2Player activeChar = getActiveChar();

		if (activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != 0)
		{
			requestFailed(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return false;
		}
		else if (activeChar.getPlayerFish().isFishing())
		{
			requestFailed(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
			return false;
		}
		// Cannot discard item that the skill is consuming
		else if (activeChar.isCastingNow() && activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getItemId())
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return false;
		}
		else if (activeChar.isFlying())
		{
			sendAF();
			return false;
		}
		else if (activeChar.isCastingSimultaneouslyNow() && activeChar.getLastSimultaneousSkillCast() != null && activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == item.getItemId())
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return false;
		}
		else if (!activeChar.isInsideRadius(_x, _y, 150, false) || Math.abs(_z - activeChar.getZ()) > 50)
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR);
			return false;
		}

		else if (item == null)
		{
			_log.warn("Error while droping item for char " + activeChar.getName() + " (validity check).");
			sendAF();
			return false;
		}
		else if (!activeChar.isGM() && !Config.ALLOW_DISCARDITEM)
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return false;
		}
		else if (!(activeChar.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS) && !item.isDropable())
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return false;
		}
		else if (!(activeChar.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS) && item.getItemType() == L2EtcItemType.QUEST)
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return false;
		}
		else if (!activeChar.isGM() && activeChar.isInvul() && Config.PLAYER_SPAWN_PROTECTION > 0)
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return false;
		}
		else if (Config.ALT_STRICT_HERO_SYSTEM && item.isHeroItem())
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return false;
		}
		else if (L2Item.TYPE2_QUEST == item.getItem().getType2())
		{
			requestFailed(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM);
			return false;
		}

		return true;
	}

	@Override
	public String getType()
	{
		return _C__REQUESTDROPITEM;
	}
}
