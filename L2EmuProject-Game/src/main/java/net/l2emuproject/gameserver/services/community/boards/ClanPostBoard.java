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
import java.util.Collection;
import java.util.Date;

import javolution.util.FastList;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.clan.L2ClanMember;
import net.l2emuproject.gameserver.services.community.CommunityBoard;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.services.community.models.Comment;
import net.l2emuproject.gameserver.services.community.models.Forum;
import net.l2emuproject.gameserver.services.community.models.Post;
import net.l2emuproject.gameserver.services.community.models.Topic;
import net.l2emuproject.gameserver.services.community.models.Topic.ConstructorType;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;

public final class ClanPostBoard extends CommunityBoard
{
	public ClanPostBoard(CommunityService service)
	{
		super(service);
	}

	@Override
	public final void parseCommand(final L2Player player, final String command)
	{
		final int clanId = Integer.valueOf(command.split(";")[3]);
		final Forum clanForum = CommunityService.getInstance().getClanForum(clanId);
		int type;
		if (command.split(";")[2].equalsIgnoreCase("announce"))
			type = Topic.ANNOUNCE;
		else if (command.split(";")[2].equalsIgnoreCase("cbb"))
			type = Topic.BULLETIN;
		else
		{
			_log.info("Clan Post Board command error: " + command);
			return;
		}
		int perNon = clanForum.getTopic(type).getPermissions();
		int perMem = perNon % 10;
		perNon = (perNon - perMem) / 10;
		boolean isPlayerMember = false;
		final L2Clan clan = ClanTable.getInstance().getClan(clanId);

		if (clan == null)
			return;

		for (L2ClanMember member : clan.getMembers())
			if (member.getObjectId() == player.getObjectId())
				isPlayerMember = true;

		final boolean isLeader = clan.getLeaderId() == player.getObjectId();
		if (!isLeader && ((isPlayerMember && perMem == 0) || (!isPlayerMember && perNon == 0)))
		{
			player.sendPacket(SystemMessageId.NO_WRITE_PERMISSION);
			return;
		}
		if (command.split(";")[1].equalsIgnoreCase("list"))
		{
			int index = 1;
			if (command.split(";").length == 5)
				index = Integer.valueOf(command.split(";")[4]);
			showPage(player, clanForum, type, index);
		}
		else if (command.split(";")[1].equalsIgnoreCase("read"))
		{
			Topic t = clanForum.getTopic(type);
			Post p = t.getPost(Integer.valueOf(command.split(";")[4]));
			if (p == null)
				_log.info("Missing post: " + command);
			else
			{
				if (command.split(";").length > 5)
				{
					_log.info("Index: " + command.split(";")[5] + ";" + command.split(";")[6]);
					showPost(player, t, p, clanId, type, Integer.valueOf(command.split(";")[5]), Integer.valueOf(command.split(";")[6]));
				}
				else
					showPost(player, t, p, clanId, type, 1, 1);
			}
		}
		else if (!isLeader && ((isPlayerMember && perMem == 1) || (!isPlayerMember && perNon == 1)))
		{
			player.sendPacket(SystemMessageId.NO_WRITE_PERMISSION);
			return;
		}
		else if (command.split(";")[1].equalsIgnoreCase("crea"))
		{
			showWrite(player, null, clanId, type);
		}
		else if (command.split(";")[1].equalsIgnoreCase("del"))
		{
			clanForum.getTopic(type).rmPostById(Integer.valueOf(command.split(";")[4]));
			showPage(player, clanForum, type, 1);
		}
		else if (command.split(";")[1].equalsIgnoreCase("delcom"))
		{
			Topic t = clanForum.getTopic(type);
			Post p = t.getPost(Integer.valueOf(command.split(";")[4]));
			p.rmCommentByID(Integer.valueOf(command.split(";")[5]));
			showPost(player, t, p, clanId, type, 1, 1);
		}
		else if (command.split(";")[1].equalsIgnoreCase("edit"))
		{
			Post p = clanForum.getTopic(type).getPost(Integer.valueOf(command.split(";")[4]));
			showWrite(player, p, clanId, type);
		}
		else if (command.split(";")[1].equalsIgnoreCase("reply"))
		{
			Post p = clanForum.getTopic(type).getPost(Integer.valueOf(command.split(";")[4]));
			showReply(player, p, clanId, type);
		}
	}

	@Override
	public final void parseWrite(final L2Player player, final String ar1, final String ar2, final String ar3, final String ar4, final String ar5)
	{
		final int clanId = Integer.valueOf(ar2.split(";")[0]);
		final int topicId = Integer.valueOf(ar2.split(";")[1]);
		int postId = Integer.valueOf(ar2.split(";")[2]);
		final Forum clanForum = CommunityService.getInstance().getClanForum(clanId);
		int perNon = clanForum.getTopic(topicId).getPermissions();
		int perMem = perNon % 10;
		perNon = (perNon - perMem) / 10;

		final L2Clan clan = ClanTable.getInstance().getClan(clanId);

		if (clan == null)
			return;

		final boolean isPlayerMember = player.getClan() != null && player.getClanId() == clanId;

		final boolean isLeader = clan.getLeaderId() == player.getObjectId();

		if (!isLeader && ((isPlayerMember && perMem != 2) || (!isPlayerMember && perNon != 2)))
		{
			player.sendPacket(SystemMessageId.NO_WRITE_PERMISSION);
			return;
		}

		int type = Post.ADVERTISE;
		if (ar5.equalsIgnoreCase("Information"))
			type = Post.INFORMATION;
		else if (ar5.equalsIgnoreCase("Miscellaneous"))
			type = Post.MISCELLANEOUS;

		if (ar1.equalsIgnoreCase("new"))
		{
			postId = clanForum.getTopic(topicId).getNewPostId();
			Post p = new Post(ConstructorType.CREATE, postId, player.getObjectId(), player.getName(), "", System.currentTimeMillis(), topicId,
					clanForum.getForumId(), editPlayerText(ar3), editPlayerText(ar4), type, 0);
			clanForum.getTopic(topicId).addPost(p);
		}
		else if (ar1.equalsIgnoreCase("reply"))
		{
			int parentId = postId;
			postId = clanForum.getTopic(topicId).getNewPostId();
			Post p = new Post(ConstructorType.CREATE, postId, player.getObjectId(), player.getName(), "", parentId, System.currentTimeMillis(), topicId,
					clanForum.getForumId(), editPlayerText(ar3), editPlayerText(ar4), type, 0);
			clanForum.getTopic(topicId).addPost(p);
		}
		else if (ar1.equalsIgnoreCase("edit"))
		{
			clanForum.getTopic(topicId).getPost(postId).updatePost(editPlayerText(ar3), editPlayerText(ar4), type);
		}
		else if (ar1.equalsIgnoreCase("com"))
		{
			Post p = clanForum.getTopic(topicId).getPost(postId);
			int comId = p.getNewCommentId();
			Comment c = new Comment(ConstructorType.CREATE, comId, player.getObjectId(), System.currentTimeMillis(), postId, topicId, clanForum.getForumId(),
					editPlayerText(ar3));
			p.addComment(c);
			showPost(player, clanForum.getTopic(topicId), p, clanId, topicId, 1, 1);
			return;
		}

		showPage(player, clanForum, topicId, 1);
	}

	private String replace(final String text, final int type)
	{
		String content = text;
		switch (type)
		{
			case Topic.ANNOUNCE:
				content = content.replaceAll("%link%", "<a action=\"bypass _bbscpost;list;announce;%clanid%\">Announcement</a>");
				content = content.replaceAll("%type%", "announce");
				content = content.replaceAll("%topicId%", String.valueOf(Topic.ANNOUNCE));
				content = content.replaceAll("%combobox%", "Advertise;Miscellaneous");
				break;
			case Topic.BULLETIN:
				content = content.replaceAll("%link%", "<a action=\"bypass _bbscpost;list;cbb;%clanid%\">Free Community</a>");
				content = content.replaceAll("%type%", "cbb");
				content = content.replaceAll("%topicId%", String.valueOf(Topic.BULLETIN));
				content = content.replaceAll("%combobox%", "Information;Miscellaneous");
				break;
		}

		return content;
	}

	private void showPage(final L2Player player, final Forum f, final int type, final int index)
	{
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanpost.htm");
		if (f == null)
		{
			_log.info("Forum is NULL!!!");
			showHTML(player, content);
			return;
		}

		final Topic t = f.getTopic(type);
		final L2TextBuilder tb = L2TextBuilder.newInstance();
		int i = 0;
		for (Post p : t.getAllPosts())
		{
			if (i > ((index - 1) * 10 + 9))
			{
				break;
			}
			if (i++ >= ((index - 1) * 10))
			{
				tb.append("<img src=\"L2UI.SquareBlank\" width=\"750\" height=\"3\">");
				tb.append("<table border=0 cellspacing=0 cellpadding=0 width=750>");
				tb.append("<tr> ");
				tb.append("<td FIXWIDTH=5></td>");
				tb.append("<td FIXWIDTH=80 align=center>" + p.getPostId() + "</td>");
				tb.append("<td FIXWIDTH=340><a action=\"bypass _bbscpost;read;%type%;" + f.getOwner() + ";" + p.getPostId() + "\">" + p.getPostTypeName()
						+ p.getPostTitle() + "</a></td>");
				tb.append("<td FIXWIDTH=120 align=center>" + p.getOwnerName() + "</td>");
				tb.append("<td FIXWIDTH=120 align=center>" + DateFormat.getInstance().format(new Date(p.getPostDate())) + "</td>");
				tb.append("<td FIXWIDTH=80 align=center>" + p.getReadCount() + "</td>");
				tb.append("<td FIXWIDTH=5></td>");
				tb.append("</tr>");
				tb.append("<tr><td height=5></td></tr>");
				tb.append("</table>");
				tb.append("<img src=\"L2UI.SquareBlank\" width=\"750\" height=\"3\">");
				tb.append("<img src=\"L2UI.SquareGray\" width=\"750\" height=\"1\">");
			}
		}
		content = content.replaceAll("%postList%", tb.toString());
		tb.clear();
		if (index == 1)
		{
			tb.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		else
		{
			tb.append("<td><button action=\"bypass _bbscpost;list;%type%;%clanid%;" + (index - 1)
					+ "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}

		int nbp;
		nbp = t.getAllPosts().size() / 10;
		if (nbp * 10 != t.getAllPosts().size())
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
				tb.append("<td><a action=\"bypass _bbscpost;list;%type%;%clanid%;" + i + "\"> " + i + " </a></td>");
			}
		}
		if (index == nbp)
		{
			tb.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		else
		{
			tb.append("<td><button action=\"bypass _bbscpost;list;%type%;%clanid%;" + (index + 1)
					+ "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		content = content.replaceAll("%postListLength%", tb.toString());
		content = replace(content, type);
		content = content.replaceAll("%clanid%", String.valueOf(f.getOwner()));

		showHTML(player, content);
	}

	private void showWrite(final L2Player player, final Post p, final int clanId, final int type)
	{
		String title = " ";
		String message = " ";
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanpost-write.htm");
		content = replace(content, type);
		content = content.replaceAll("%clanid%", String.valueOf(clanId));
		if (p == null)
		{
			content = content.replaceAll("%job%", "new");
			content = content.replaceAll("%postId%", "-1");
		}
		else
		{
			content = content.replaceAll("%job%", "edit");
			content = content.replaceAll("%postId%", String.valueOf(p.getPostId()));
			title = p.getPostTitle();
			message = p.getPostText();
		}

		sendWrite(player, content, message, title, title);
	}

	private void showReply(final L2Player player, final Post p, final int clanId, final int type)
	{
		if (p == null)
		{
			showWrite(player, p, clanId, type);
			return;
		}
		String title = " ";
		String message = " ";
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanpost-write.htm");
		content = replace(content, type);
		content = content.replaceAll("%clanid%", String.valueOf(clanId));
		content = content.replaceAll("%job%", "reply");
		content = content.replaceAll("%postId%", String.valueOf(p.getPostId()));

		sendWrite(player, content, message, title, " ");
	}

	private void showPost(final L2Player player, final Topic t, final Post p, final int clanId, final int type, final int indexR, final int indexC)
	{
		p.increaseReadCount();
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanpost-show.htm");
		content = content.replaceAll("%postTitle%", p.getPostTitle());
		content = content.replaceAll("%postId%", String.valueOf(p.getPostId()));
		Post parent = p;

		if (p.getParentId() != -1)
		{
			content = content.replaceAll("%postParentId%", String.valueOf(p.getParentId()));
			parent = t.getPost(p.getParentId());
		}
		else
			content = content.replaceAll("%postParentId%", String.valueOf(p.getPostId()));

		content = content.replaceAll("%postOwnerName%", p.getOwnerName());
		content = content.replaceAll("%postReadCount%", String.valueOf(p.getReadCount()));
		content = content.replaceAll("%postDate%", DateFormat.getInstance().format(new Date(p.getPostDate())));
		content = content.replaceAll("%mes%", p.getPostText());

		// reply list
		final L2TextBuilder mList = L2TextBuilder.newInstance();
		int i = 1;
		final FastList<Post> childrenList = t.getChildrenPosts(parent);
		for (Post child : childrenList)
		{
			if (i++ == indexR)
			{
				mList.append("<table border=0 cellspacing=5 cellpadding=0 WIDTH=750>");
				mList.append("<tr>");
				mList.append("<td WIDTH=60 align=center>" + child.getPostId() + "</td>");
				if (child != p)
					mList.append("<td width=415><a action=\"bypass _bbscpost;read;%type%;" + clanId + ";" + child.getPostId() + "\">" + child.getPostTypeName()
							+ child.getPostTitle() + "</a></td>");
				else
					mList.append("<td width=415>" + child.getPostTypeName() + child.getPostTitle() + "</td>");
				mList.append("<td WIDTH=130 align=center>" + child.getOwnerName() + "</td>");
				mList.append("<td WIDTH=80 align=center>" + DateFormat.getInstance().format(new Date(child.getPostDate())) + "</td>");
				mList.append("<td WIDTH=65 align=center>" + child.getReadCount() + "</td>");
				mList.append("</tr>");
				mList.append("</table>");
			}
		}
		content = content.replaceAll("%replyList%", mList.moveToString());
		if (indexR == 1)
			content = content.replaceAll("%prevReply%", "[Previous Reply]");
		else
			content = content.replaceAll("%prevReply%", "<a action=\"bypass _bbscpost;read;%type%;" + clanId + ";" + p.getPostId() + ";" + (indexR - 1) + ";"
					+ indexC + "\">[Previous Reply]</a>");
		content = content.replaceAll("%replyCount%", indexR + "/" + childrenList.size());
		if (indexR == childrenList.size())
			content = content.replaceAll("%nextReply%", "[Next Reply]");
		else
			content = content.replaceAll("%nextReply%", "<a action=\"bypass _bbscpost;read;%type%;" + clanId + ";" + p.getPostId() + ";" + (indexR + 1) + ";"
					+ indexC + "\">[Next Reply]</a>");

		// comment list
		mList.clear();
		i = 1;
		Collection<Comment> commentsList = p.getAllComments();
		int csize = commentsList.size();
		if (csize == 0)
			csize = 1;
		else
		{
			for (Comment c : commentsList)
			{
				if (i++ == indexC)
				{
					mList.append("<tr><td><img src=\"L2UI.squaregray\" width=\"750\" height=\"1\"></td></tr>");
					mList.append("<tr><td>");
					mList.append("<table>");
					mList.append("<tr>");
					mList.append("<td WIDTH=100 valign=top>" + p.getOwnerName() + "</td>");
					mList.append("<td width=10 valign=top><img src=\"L2UI.squaregray\" width=\"5\" height=\"28\"></td>");
					mList.append("<td FIXWIDTH=560 valign=top><font color=\"AAAAAA\">" + c.getCommentText() + "</font></td>");
					mList.append("<td WIDTH=20 valign=top><a action=\"bypass _bbscpost;delcom;%type%;" + clanId + ";" + p.getPostId() + ";" + c.getCommentId()
							+ "\">&\\$425;</a></td>");
					mList.append("<td WIDTH=60 valign=top>" + DateFormat.getInstance().format(new Date(c.getCommentDate())) + "</td>");
					mList.append("</tr>");
					mList.append("</table>");
					mList.append("</td></tr>");
				}
			}
		}
		content = content.replaceAll("%commentList%", mList.toString());
		if (indexC == 1)
			content = content.replaceAll("%prevCom%", "[Previous Comment]");
		else
			content = content.replaceAll("%prevCom%", "<a action=\"bypass _bbscpost;read;%type%;" + clanId + ";" + p.getPostId() + ";" + indexR + ";"
					+ (indexC - 1) + "\">[Previous Comment]</a>");
		content = content.replaceAll("%comCount%", indexC + "/" + csize);
		if (indexC == csize)
			content = content.replaceAll("%nextCom%", "[Next Comment]");
		else
			content = content.replaceAll("%nextCom%", "<a action=\"bypass _bbscpost;read;%type%;" + clanId + ";" + p.getPostId() + ";" + indexR + ";"
					+ (indexC + 1) + "\">[Next Comment]</a>");

		content = replace(content, type);
		content = content.replaceAll("%clanid%", String.valueOf(clanId));

		showHTML(player, content);
	}
}