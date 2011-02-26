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
import net.l2emuproject.gameserver.entity.itemcontainer.ItemContainer;
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
public final class RequestCancelPost extends L2GameClientPacket
{
	private static final String	_C__D0_6F_REQUESTCANCELPOSTATTACHMENT	= "[C] D0:6F RequestCancelPostAttachment";
	
	private int					_msgId;
	
	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null || !Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
			return;
		
		if (!getClient().getFloodProtector().tryPerformAction(Protected.TRANSACTION))
		{
			activeChar.sendMessage("You are acting too fast.");
			return;
		}
		
		Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
			return;
		if (msg.getSenderId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to cancel not own post!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!activeChar.isInsideZone(L2Zone.FLAG_PEACE))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_CANCEL_NOT_IN_PEACE_ZONE));
			return;
		}
		
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_CANCEL_DURING_EXCHANGE));
			return;
		}
		
		if (activeChar.isEnchanting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_CANCEL_DURING_ENCHANT));
			return;
		}
		
		if (activeChar.getPrivateStoreType() > 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_CANCEL_PRIVATE_STORE));
			return;
		}
		
		if (!msg.hasAttachments())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANT_CANCEL_RECEIVED_MAIL));
			return;
		}
		
		final ItemContainer attachments = msg.getAttachments();
		if (attachments == null || attachments.getSize() == 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANT_CANCEL_RECEIVED_MAIL));
			return;
		}
		
		int weight = 0;
		int slots = 0;
		
		for (L2ItemInstance item : attachments.getItems())
		{
			if (item == null)
				continue;
			
			if (item.getOwnerId() != activeChar.getObjectId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get not own item from cancelled attachment!",
						Config.DEFAULT_PUNISH);
				return;
			}
			
			if (!item.getLocation().equals(ItemLocation.MAIL))
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to get items not from mail !", Config.DEFAULT_PUNISH);
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
		
		if (!activeChar.getInventory().validateCapacity(slots))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_CANCEL_INVENTORY_FULL));
			return;
		}
		
		if (!activeChar.getInventory().validateWeight(weight))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_CANCEL_INVENTORY_FULL));
			return;
		}
		
		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (L2ItemInstance item : attachments.getItems())
		{
			if (item == null)
				continue;
			
			long count = item.getCount();
			final L2ItemInstance newItem = attachments.transferItem(attachments.getName(), item.getObjectId(), count, activeChar.getInventory(), activeChar,
					null);
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
		
		msg.removeAttachments();
		
		// Send updated item list to the player
		if (playerIU != null)
			activeChar.sendPacket(playerIU);
		else
			activeChar.sendPacket(new ItemList(activeChar, false));
		
		// Update current load status on player
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
		
		final L2Player receiver = L2World.getInstance().getPlayer(msg.getReceiverId());
		if (receiver != null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANCELLED_MAIL);
			sm.addCharName(activeChar);
			receiver.sendPacket(sm);
			receiver.sendPacket(new ExChangePostState(true, _msgId, Message.DELETED));
		}
		
		MailManager.getInstance().deleteMessageInDb(_msgId);
		
		activeChar.sendPacket(new ExChangePostState(false, _msgId, Message.DELETED));
		activeChar.sendPacket(new SystemMessage(SystemMessageId.MAIL_SUCCESSFULLY_CANCELLED));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_6F_REQUESTCANCELPOSTATTACHMENT;
	}
}
