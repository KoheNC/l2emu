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
package net.l2emuproject.gameserver.services.community.boards;

import java.text.DateFormat;
import java.util.Date;

import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExMailArrived;
import net.l2emuproject.gameserver.services.community.CommunityBoard;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.services.community.models.Forum;
import net.l2emuproject.gameserver.services.community.models.Post;
import net.l2emuproject.gameserver.services.community.models.Topic;
import net.l2emuproject.gameserver.services.community.models.Topic.ConstructorType;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;

public final class MailBoard extends CommunityBoard
{
	public MailBoard(CommunityService service)
	{
		super(service);
	}

	@Override
	public final void parseCommand(final L2Player player, final String command)
	{
		final Forum playerForum = CommunityService.getInstance().getPlayerForum(player);

		if (playerForum == null)
			return;

		if (command.equalsIgnoreCase("_bbsmail") || command.equalsIgnoreCase("_maillist_0_1_0_"))
			showPage(player, playerForum, Topic.INBOX, 1);
		else if (command.split(";")[1].equalsIgnoreCase("sent"))
			showPage(player, playerForum, Topic.OUTBOX, 1);
		else if (command.split(";")[1].equalsIgnoreCase("archive"))
			showPage(player, playerForum, Topic.ARCHIVE, 1);
		else if (command.split(";")[1].equalsIgnoreCase("tarchive"))
			showPage(player, playerForum, Topic.TEMP_ARCHIVE, 1);
		else if (command.split(";")[1].equalsIgnoreCase("crea"))
			showWrite(player, null, 0);
		else if (command.split(";")[1].equalsIgnoreCase("reply"))
		{
			final Topic topic = playerForum.getTopic(Integer.valueOf(command.split(";")[2]));
			final Post post = topic.getPost(Integer.valueOf(command.split(";")[3]));
			showWrite(player, post, 1);
		}
		else if (command.split(";")[1].equalsIgnoreCase("forward"))
		{
			final Topic topic = playerForum.getTopic(Integer.valueOf(command.split(";")[2]));
			final Post post = topic.getPost(Integer.valueOf(command.split(";")[3]));
			showWrite(player, post, 2);
		}
		else if (command.split(";")[1].equalsIgnoreCase("store"))
		{
			final Topic topic = playerForum.getTopic(Integer.valueOf(command.split(";")[2]));
			final Post post = topic.getPost(Integer.valueOf(command.split(";")[3]));
			topic.rmPostById(post.getPostId());
			int postId = playerForum.getTopic(Topic.OUTBOX).getNewPostId();
			post.setTopic(Topic.ARCHIVE, postId);
			playerForum.getTopic(Topic.ARCHIVE).addPost(post);
			showPage(player, playerForum, Topic.INBOX, 1);
		}
		else if (command.split(";")[1].equalsIgnoreCase("del"))
		{
			playerForum.getTopic(Integer.valueOf(command.split(";")[2])).rmPostById(Integer.valueOf(command.split(";")[3]));
			showPage(player, playerForum, Topic.INBOX, 1);
		}
		else if (command.split(";")[1].equalsIgnoreCase("read"))
		{
			final Topic topic = playerForum.getTopic(Integer.valueOf(command.split(";")[2]));
			final Post post = topic.getPost(Integer.valueOf(command.split(";")[3]));
			showMail(player, playerForum, topic, post);
		}
	}

	@Override
	public final void parseWrite(final L2Player player, final String ar1, final String ar2, final String ar3, final String ar4, final String ar5)
	{
		int postId = Integer.valueOf(ar2);
		final Forum senderForum = CommunityService.getInstance().getPlayerForum(player);
		String[] recipients = ar3.split(";");
		boolean isSended = false;
		Post post = null;

		for (String recipient : recipients)
		{
			final int receiverId = CharNameTable.getInstance().getObjectIdByName(recipient);

			if (ar1.equalsIgnoreCase("new"))
			{
				if (receiverId > 0)
				{
					final Forum receiverForum = CommunityService.getInstance().getPlayerForum(receiverId);
					postId = receiverForum.getTopic(Topic.INBOX).getNewPostId();
					post = new Post(ConstructorType.CREATE, postId, player.getObjectId(), player.getName(), ar3, System.currentTimeMillis(), Topic.INBOX,
							receiverForum.getForumId(), editPlayerText(ar4), editPlayerText(ar5), 0, 0);
					receiverForum.getTopic(Topic.INBOX).addPost(post);
					final L2Player reciever = L2World.getInstance().getPlayer(receiverId);
					if (reciever != null)
					{
						reciever.sendPacket(ExMailArrived.STATIC_PACKET);
						reciever.sendPacket(SystemMessageId.NEW_MAIL);
					}
					isSended = true;
				}
			}
			else if (ar1.equalsIgnoreCase("store"))
			{
				postId = senderForum.getTopic(Topic.TEMP_ARCHIVE).getNewPostId();
				post = new Post(ConstructorType.CREATE, postId, player.getObjectId(), player.getName(), ar3, System.currentTimeMillis(), Topic.TEMP_ARCHIVE,
						senderForum.getForumId(), editPlayerText(ar4), editPlayerText(ar5), 0, 0);
				senderForum.getTopic(Topic.TEMP_ARCHIVE).addPost(post);
				player.sendPacket(SystemMessageId.MAIL_STORED_IN_MAILBOX);
				showPage(player, senderForum, Topic.TEMP_ARCHIVE, 1);
				return;
			}
			else
				_log.info("Mail Write command missing: " + ar1);
		}
		if (isSended)
		{
			postId = senderForum.getTopic(Topic.OUTBOX).getNewPostId();
			post = new Post(ConstructorType.CREATE, postId, player.getObjectId(), player.getName(), ar3, System.currentTimeMillis(), Topic.OUTBOX,
					senderForum.getForumId(), editPlayerText(ar4), editPlayerText(ar5), 0, 0);
			senderForum.getTopic(Topic.OUTBOX).addPost(post);
		}
		showPage(player, senderForum, Topic.INBOX, 1);
	}

	private void showPage(final L2Player player, final Forum forum, final int topicType, final int index)
	{
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "mail.htm");
		switch (topicType)
		{
			case Topic.INBOX:
				content = content.replaceAll("%maillink%", "<a action=\"bypass _bbsmail\">Inbox</a>");
				content = content.replaceAll("%authrecive%", "Author");
				break;
			case Topic.OUTBOX:
				content = content.replaceAll("%maillink%", "<a action=\"bypass _bbsmail;sent\">Sent Box</a>");
				content = content.replaceAll("%authrecive%", "Recipient");
				break;
			case Topic.ARCHIVE:
				content = content.replaceAll("%maillink%", "<a action=\"bypass _bbsmail;archive\">Mail Archive</a>");
				content = content.replaceAll("%authrecive%", "Author");
				break;
			case Topic.TEMP_ARCHIVE:
				content = content.replaceAll("%maillink%", "<a action=\"bypass _bbsmail;tarchive\">Temporary Mail Archive</a>");
				content = content.replaceAll("%authrecive%", "Recipient");
				break;
		}
		content = content.replaceAll("%inbox%", String.valueOf(forum.getTopic(Topic.INBOX).getAllPosts().size()));
		content = content.replaceAll("%outbox%", String.valueOf(forum.getTopic(Topic.OUTBOX).getAllPosts().size()));
		content = content.replaceAll("%archive%", String.valueOf(forum.getTopic(Topic.ARCHIVE).getAllPosts().size()));
		content = content.replaceAll("%tarchive%", String.valueOf(forum.getTopic(Topic.TEMP_ARCHIVE).getAllPosts().size()));

		final Topic topic = forum.getTopic(topicType);
		final L2TextBuilder tb = L2TextBuilder.newInstance();
		int i = 0;
		for (Post post : topic.getAllPosts())
		{
			if (i > ((index - 1) * 10 + 9))
			{
				break;
			}
			if (i++ >= ((index - 1) * 10))
			{
				tb.append("<table border=0 cellspacing=0 cellpadding=5 width=755>");
				tb.append("<tr> ");
				tb.append("<td FIXWIDTH=5 align=center></td>");
				tb.append("<td FIXWIDTH=150 align=center>" + post.getOwnerName() + "</td>");
				tb.append("<td FIXWIDTH=440><a action=\"bypass _bbsmail;read;" + topic.getTopicId() + ";" + post.getPostId() + "\">" + post.getPostTitle()
						+ "</a></td>");
				tb.append("<td FIXWIDTH=150>" + DateFormat.getInstance().format(new Date(post.getPostDate())) + "</td>");
				tb.append("<td FIXWIDTH=5 align=center></td>");
				tb.append("</tr>");
				tb.append("</table>");
				tb.append("<img src=\"L2UI.Squaregray\" width=\"755\" height=\"1\">");
			}
		}
		content = content.replaceAll("%maillist%", tb.moveToString());
		tb.clear();
		if (index == 1)
		{
			tb.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		else
		{
			tb.append("<td><button action=\"bypass _bbspost;list;" + (index - 1)
					+ "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}

		int nbp;
		nbp = topic.getAllPosts().size() / 10;
		if (nbp * 10 != topic.getAllPosts().size())
		{
			nbp++;
		}
		for (i = 1; i <= nbp; i++)
		{
			if (i == index)
			{
				tb.append("<td> " + i + " </td>");
			}
			else
			{
				tb.append("<td><a action=\"bypass _bbspost;list;" + i + "\"> " + i + " </a></td>");
			}
		}
		if (index == nbp)
		{
			tb.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		else
		{
			tb.append("<td><button action=\"bypass _bbspost;list;" + (index + 1)
					+ "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		content = content.replaceAll("%maillistlength%", tb.moveToString());
		showHTML(player, content);
	}

	private void showMail(final L2Player player, final Forum forum, final Topic topic, final Post post)
	{
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "mail-show.htm");
		switch (topic.getTopicId())
		{
			case Topic.INBOX:
				content = content.replaceAll("%writer%", post.getOwnerName());
				content = content.replaceAll("%receiver%", post.getPostRecipientList());
				content = content.replaceAll("%maillink%", "<a action=\"bypass _bbsmail\">Inbox</a>");
				break;
			case Topic.OUTBOX:
				content = content.replaceAll("%maillink%", "<a action=\"bypass _bbsmail;sent\">Sent Box</a>");
				content = content.replaceAll("%writer%", post.getOwnerName());
				content = content.replaceAll("%receiver%", post.getPostRecipientList());
				break;
			case Topic.ARCHIVE:
				content = content.replaceAll("%maillink%", "<a action=\"bypass _bbsmail;archive\">Mail Archive</a>");
				content = content.replaceAll("%writer%", post.getOwnerName());
				content = content.replaceAll("%receiver%", post.getPostRecipientList());
				break;
			case Topic.TEMP_ARCHIVE:
				content = content.replaceAll("%maillink%", "<a action=\"bypass _bbsmail;tarchive\">Temporary Mail Archive</a>");
				content = content.replaceAll("%writer%", post.getOwnerName());
				content = content.replaceAll("%receiver%", post.getPostRecipientList());
				break;
		}
		content = content.replaceAll("%sentDate%", DateFormat.getInstance().format(new Date(post.getPostDate())));
		content = content.replaceAll("%delDate%", DateFormat.getInstance().format(new Date(post.getPostDate() + 90 * 86400000)));
		content = content.replaceAll("%title%", post.getPostTitle());
		content = content.replaceAll("%mes%", post.getPostText());
		content = content.replaceAll("%topicId%", String.valueOf(topic.getTopicId()));
		content = content.replaceAll("%postId%", String.valueOf(post.getPostId()));
		post.increaseReadCount();

		showHTML(player, content);
	}

	private void showWrite(final L2Player player, final Post post, final int type)
	{
		String title = " ";
		String message = " ";
		String toList = " ";
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "mail-write.htm");
		content = content.replaceAll("%maillink%", "<a action=\"bypass _bbsmail\">Inbox</a>");

		content = content.replaceAll("%playerObjId%", String.valueOf(player.getObjectId()));
		if (post == null)
		{
			content = content.replaceAll("%postId%", "-1");
		}
		else
		{
			content = content.replaceAll("%postId%", String.valueOf(post.getPostId()));
			title = post.getPostTitle();
			message = post.getPostText();
			if (type == 1)
				toList = post.getPostRecipientList();
		}

		sendWrite(player, content, message, title, toList);
	}
}
