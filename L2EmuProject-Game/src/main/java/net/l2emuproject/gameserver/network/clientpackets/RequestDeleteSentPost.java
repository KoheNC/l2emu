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
import net.l2emuproject.gameserver.manager.MailManager;
import net.l2emuproject.gameserver.model.entity.Message;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExChangePostState;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.zone.L2Zone;

/**
 * @author Migi, DS
 */
public final class RequestDeleteSentPost extends L2GameClientPacket
{
	private static final String	_C__D0_6C_REQUESTDELETESENTPOST	= "[C] D0:6D RequestDeleteSentPost";
	
	private static final int	BATCH_LENGTH					= 4;									// length of the one item
																										
	int[]						_msgIds							= null;
	
	@Override
	protected void readImpl()
	{
		int count = readD();
		
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != getByteBuffer().remaining())
			return;
		
		_msgIds = new int[count];
		for (int i = 0; i < count; i++)
			_msgIds[i] = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null || _msgIds == null || !Config.ALLOW_MAIL)
			return;
		
		if (!activeChar.isInsideZone(L2Zone.FLAG_PEACE))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE));
			return;
		}
		
		for (int msgId : _msgIds)
		{
			Message msg = MailManager.getInstance().getMessage(msgId);
			if (msg == null)
				continue;
			if (msg.getSenderId() != activeChar.getObjectId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to delete not own post!", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (msg.hasAttachments() || msg.isDeletedBySender())
				return;
			
			msg.setDeletedBySender();
		}
		activeChar.sendPacket(new ExChangePostState(false, _msgIds, Message.DELETED));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_6C_REQUESTDELETESENTPOST;
	}
}
