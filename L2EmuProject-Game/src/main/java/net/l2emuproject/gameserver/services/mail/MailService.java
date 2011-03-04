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
package net.l2emuproject.gameserver.services.mail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExNoticePostArrived;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.system.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.idfactory.IdFactory;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.SingletonList;
import net.l2emuproject.util.SingletonMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Migi, DS
 */
public final class MailService
{
	private static final Log			_log		= LogFactory.getLog(MailService.class);

	private final Map<Integer, Message>	_messages	= new SingletonMap<Integer, Message>();

	private static final class SingletonHolder
	{
		private static final MailService	INSTANCE	= new MailService();
	}

	public static MailService getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private MailService()
	{
		load();
	}

	private final void load()
	{
		int readed = 0;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement stmt1 = con.prepareStatement("SELECT * FROM messages ORDER BY expiration");
			final PreparedStatement stmt2 = con.prepareStatement("SELECT * FROM attachments WHERE messageId = ?");
			final ResultSet rset1 = stmt1.executeQuery();

			while (rset1.next())
			{
				final Message msg = new Message(rset1);

				final int msgId = msg.getId();
				_messages.put(msgId, msg);

				readed++;

				final long expiration = msg.getExpiration();

				if (expiration < System.currentTimeMillis())
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), 10000);
				else
					ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msgId), expiration - System.currentTimeMillis());
			}

			stmt1.close();
			stmt2.close();
		}
		catch (SQLException e)
		{
			_log.warn("Mail Manager: Error loading from database:" + e.toString());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		_log.info(getClass().getSimpleName() + " : Loaded " + readed + " message(s).");
	}

	public final Message getMessage(int msgId)
	{
		return _messages.get(msgId);
	}

	public final boolean hasUnreadPost(L2Player player)
	{
		final int objectId = player.getObjectId();
		for (Message msg : _messages.values())
			if (msg != null && msg.getReceiverId() == objectId && msg.isUnread())
				return true;

		return false;
	}

	public final int getInboxSize(int objectId)
	{
		int size = 0;
		for (Message msg : _messages.values())
			if (msg != null && msg.getReceiverId() == objectId && !msg.isDeletedByReceiver())
				size++;

		return size;
	}

	public final int getOutboxSize(int objectId)
	{
		int size = 0;
		for (Message msg : _messages.values())
			if (msg != null && msg.getSenderId() == objectId && !msg.isDeletedBySender())
				size++;

		return size;
	}

	public final List<Message> getInbox(int objectId)
	{
		final List<Message> inbox = new SingletonList<Message>();
		for (Message msg : _messages.values())
			if (msg != null && msg.getReceiverId() == objectId && !msg.isDeletedByReceiver())
				inbox.add(msg);

		return inbox;
	}

	public final List<Message> getOutbox(int objectId)
	{
		final List<Message> outbox = new SingletonList<Message>();
		for (Message msg : _messages.values())
			if (msg != null && msg.getSenderId() == objectId && !msg.isDeletedBySender())
				outbox.add(msg);

		return outbox;
	}

	public void sendMessage(Message msg)
	{
		_messages.put(msg.getId(), msg);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement stmt = Message.getStatement(msg, con);
			stmt.execute();
			stmt.close();
		}
		catch (SQLException e)
		{
			_log.warn("Mail Manager: Error saving message:" + e.toString());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		final L2Player receiver = L2World.getInstance().getPlayer(msg.getReceiverId());
		if (receiver != null)
			receiver.sendPacket(new ExNoticePostArrived(true));

		ThreadPoolManager.getInstance().scheduleGeneral(new MessageDeletionTask(msg.getId()), msg.getExpiration() - System.currentTimeMillis());
	}

	private final class MessageDeletionTask implements Runnable
	{
		private final int	_msgId;

		public MessageDeletionTask(int msgId)
		{
			_msgId = msgId;
		}

		@Override
		public final void run()
		{
			final Message msg = getMessage(_msgId);
			if (msg == null)
				return;

			if (msg.hasAttachments())
			{
				try
				{
					final L2Player sender = L2World.getInstance().getPlayer(msg.getSenderId());
					if (sender != null)
					{
						msg.getAttachments().returnToWh(sender.getWarehouse());
						sender.sendPacket(new SystemMessage(SystemMessageId.MAIL_RETURNED));
					}
					else
						msg.getAttachments().returnToWh(null);

					msg.getAttachments().deleteMe();
					msg.removeAttachments();

					final L2Player receiver = L2World.getInstance().getPlayer(msg.getReceiverId());
					if (receiver != null)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.MAIL_RETURNED);
						sm.addString(msg.getReceiverName());
						receiver.sendPacket(sm);
					}
				}
				catch (Exception e)
				{
					_log.warn("Mail Manager: Error returning items:" + e.toString());
				}
			}
			deleteMessageInDb(msg.getId());
		}
	}

	public final void markAsReadInDb(int msgId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement stmt = con.prepareStatement("UPDATE messages SET isUnread = 'false' WHERE messageId = ?");

			stmt.setInt(1, msgId);

			stmt.execute();
			stmt.close();
		}
		catch (SQLException e)
		{
			_log.warn("Mail Manager: Error marking as read message:" + e.toString());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final void markAsDeletedBySenderInDb(int msgId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement stmt = con.prepareStatement("UPDATE messages SET isDeletedBySender = 'true' WHERE messageId = ?");

			stmt.setInt(1, msgId);

			stmt.execute();
			stmt.close();
		}
		catch (SQLException e)
		{
			_log.warn("Mail Manager: Error marking as deleted by sender message:" + e.toString());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final void markAsDeletedByReceiverInDb(int msgId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement stmt = con.prepareStatement("UPDATE messages SET isDeletedByReceiver = 'true' WHERE messageId = ?");

			stmt.setInt(1, msgId);

			stmt.execute();
			stmt.close();
		}
		catch (SQLException e)
		{
			_log.warn("Mail Manager: Error marking as deleted by receiver message:" + e.toString());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final void removeAttachmentsInDb(int msgId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement stmt = con.prepareStatement("UPDATE messages SET hasAttachments = 'false' WHERE messageId = ?");

			stmt.setInt(1, msgId);

			stmt.execute();
			stmt.close();
		}
		catch (SQLException e)
		{
			_log.warn("Mail Manager: Error removing attachments in message:" + e.toString());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final void deleteMessageInDb(int msgId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement stmt = con.prepareStatement("DELETE FROM messages WHERE messageId = ?");

			stmt.setInt(1, msgId);

			stmt.execute();
			stmt.close();
		}
		catch (SQLException e)
		{
			_log.warn("Mail Manager: Error deleting message:" + e.toString());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		_messages.remove(msgId);
		IdFactory.getInstance().releaseId(msgId);
	}
}
