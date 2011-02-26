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
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.entity.itemcontainer.ItemContainer;
import net.l2emuproject.gameserver.entity.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.entity.player.mail.Message;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.items.L2ItemInstance.ItemLocation;
import net.l2emuproject.gameserver.manager.MailManager;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExChangePostState;
import net.l2emuproject.gameserver.network.serverpackets.InventoryUpdate;
import net.l2emuproject.gameserver.network.serverpackets.ItemList;
import net.l2emuproject.gameserver.network.serverpackets.StatusUpdate;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.system.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.zone.L2Zone;

/**
 * @author Migi, DS
 */
public final class RequestPostAttachment extends L2GameClientPacket
{
	private static final String	_C__D0_6A_REQUESTPOSTATTACHMENT	= "[C] D0:6A RequestPostAttachment";
	
	private int					_msgId;
	
	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}
	
	@Override
	public void runImpl()
	{
		if (!Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
			return;
		
		final L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (!getClient().getFloodProtector().tryPerformAction(Protected.TRANSACTION))
		{
			activeChar.sendMessage("You are acting too fast.");
			return;
		}
		
		if (Config.GM_DISABLE_TRANSACTION && activeChar.getAccessLevel() >= Config.GM_TRANSACTION_MIN
	            && activeChar.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
		{
			activeChar.sendMessage("Transactions are disable for your Access Level.");
			return;
		}
		
		if (!activeChar.isInsideZone(L2Zone.FLAG_PEACE))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_RECEIVE_NOT_IN_PEACE_ZONE));
			return;
		}
		
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_RECEIVE_DURING_EXCHANGE));
			return;
		}
		
		if (activeChar.isEnchanting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_RECEIVE_DURING_ENCHANT));
			return;
		}
		
		if (activeChar.getPrivateStoreType() > 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_RECEIVE_PRIVATE_STORE));
			return;
		}
		
		final Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
			return;
		
		if (msg.getReceiverId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get not own attachment!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!msg.hasAttachments())
			return;
		
		final ItemContainer attachments = msg.getAttachments();
		if (attachments == null)
			return;
		
		int weight = 0;
		int slots = 0;
		
		for (L2ItemInstance item : attachments.getItems())
		{
			if (item == null)
				continue;
			
			// Calculate needed slots
			if (item.getOwnerId() != msg.getSenderId())
			{
				Util.handleIllegalPlayerAction(activeChar,
						"Player " + activeChar.getName() + " tried to get wrong item (ownerId != senderId) from attachment!", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (!item.getLocation().equals(ItemLocation.MAIL))
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get wrong item (Location != MAIL) from attachment!",
						Config.DEFAULT_PUNISH);
				return;
			}
			
			if (item.getLocationSlot() != msg.getId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get items from different attachment!",
						Config.DEFAULT_PUNISH);
				return;
			}
			
			weight += item.getCount() * item.getItem().getWeight();
			if (!item.isStackable())
				slots += item.getCount();
			else if (activeChar.getInventory().getItemByItemId(item.getItemId()) == null)
				slots++;
		}
		
		// Item Max Limit Check
		if (!activeChar.getInventory().validateCapacity(slots))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_RECEIVE_INVENTORY_FULL));
			return;
		}
		
		// Weight limit Check
		if (!activeChar.getInventory().validateWeight(weight))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_RECEIVE_INVENTORY_FULL));
			return;
		}
		
		long adena = msg.getReqAdena();
		if (adena > 0 && !activeChar.reduceAdena("PayMail", adena, null, true))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_RECEIVE_NO_ADENA));
			return;
		}
		
		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (L2ItemInstance item : attachments.getItems())
		{
			if (item == null)
				continue;
			
			if (item.getOwnerId() != msg.getSenderId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get items with owner != sender !",
						Config.DEFAULT_PUNISH);
				return;
			}
			
			long count = item.getCount();
			final L2ItemInstance newItem = attachments.transferItem(attachments.getName(), item.getObjectId(), item.getCount(), activeChar.getInventory(),
					activeChar, null);
			if (newItem == null)
				return;
			
			if (playerIU != null)
			{
				if (newItem.getCount() > count)
					playerIU.addModifiedItem(newItem);
				else
					playerIU.addNewItem(newItem);
			}
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_ACQUIRED_S2_S1);
			sm.addItemName(item.getItemId());
			sm.addItemNumber(count);
			activeChar.sendPacket(sm);
		}
		
		// Send updated item list to the player
		if (playerIU != null)
			activeChar.sendPacket(playerIU);
		else
			activeChar.sendPacket(new ItemList(activeChar, false));
		
		msg.removeAttachments();
		
		// Update current load status on player
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
		
		SystemMessage sm;
		final L2Player sender = L2World.getInstance().getPlayer(msg.getSenderId());
		if (adena > 0)
		{
			if (sender != null)
			{
				sender.addAdena("PayMail", adena, activeChar, false);
				sm = new SystemMessage(SystemMessageId.PAYMENT_OF_S1_ADENA_COMPLETED_BY_S2);
				sm.addItemNumber(adena);
				sm.addCharName(activeChar);
				sender.sendPacket(sm);
			}
			else
			{
				L2ItemInstance paidAdena = ItemTable.getInstance().createItem("PayMail", PcInventory.ADENA_ID, adena, activeChar, null);
				paidAdena.setOwnerId(msg.getSenderId());
				paidAdena.setLocation(ItemLocation.INVENTORY);
				paidAdena.updateDatabase(true);
				L2World.getInstance().removeObject(paidAdena);
			}
		}
		else if (sender != null)
		{
			sm = new SystemMessage(SystemMessageId.S1_ACQUIRED_ATTACHED_ITEM);
			sm.addCharName(activeChar);
			sender.sendPacket(sm);
		}
		
		activeChar.sendPacket(new ExChangePostState(true, _msgId, Message.READED));
		activeChar.sendPacket(new SystemMessage(SystemMessageId.MAIL_SUCCESSFULLY_RECEIVED));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_6A_REQUESTPOSTATTACHMENT;
	}
}
