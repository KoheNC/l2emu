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
package net.l2emuproject.gameserver.network.serverpackets;

import net.l2emuproject.gameserver.entity.itemcontainer.ItemContainer;
import net.l2emuproject.gameserver.entity.player.mail.Message;
import net.l2emuproject.gameserver.items.L2ItemInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Migi, DS
 */
public class ExShowReceivedPost extends L2GameServerPacket
{
	private static final String	_S__FE_AB_EXSHOWRECEIVEDPOST	= "[S] FE:AB ExShowReceivedPost";
	private static final Log	_log							= LogFactory.getLog(ExShowReceivedPost.class);
	
	private Message				_msg;
	private L2ItemInstance[]	_items							= null;
	
	public ExShowReceivedPost(Message msg)
	{
		_msg = msg;
		if (msg.hasAttachments())
		{
			final ItemContainer attachments = msg.getAttachments();
			if (attachments != null && attachments.getSize() > 0)
				_items = attachments.getItems();
			else
				_log.warn("Message " + msg.getId() + " has attachments but itemcontainer is empty.");
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xab);
		writeD(_msg.getId());
		writeD(_msg.isLocked() ? 1 : 0);
		writeD(0x00); //Unknown
		writeS(_msg.getSenderName());
		writeS(_msg.getSubject());
		writeS(_msg.getContent());
		
		if (_items != null && _items.length > 0)
		{
			writeD(_items.length);
			for (L2ItemInstance item : _items)
			{
				writeD(0x00);
				writeD(item.getItemId());
				writeD(item.getLocationSlot());
				writeQ(item.getCount());
				writeH(item.getItem().getType2());
				writeH(item.getCustomType1());
				writeH(item.isEquipped() ? 0x01 : 0x00);
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel());
				writeH(item.getCustomType2());
				if (item.isAugmented())
					writeD(item.getAugmentation().getAugmentationId());
				else
					writeD(0x00);
				writeD(item.getMana());
				writeD(item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999);
				writeElementalInfo(item);
				// Enchant Effects
				writeEnchantEffectInfo();
				writeD(item.getObjectId());
			}
			_items = null;
		}
		else
			writeD(0x00);
		
		writeQ(_msg.getReqAdena());
		writeD(_msg.hasAttachments() ? 1 : 0);
		writeD(_msg.isFourStars() ? 1 : 0);
		
		_msg = null;
	}
	
	@Override
	public String getType()
	{
		return _S__FE_AB_EXSHOWRECEIVEDPOST;
	}
}
