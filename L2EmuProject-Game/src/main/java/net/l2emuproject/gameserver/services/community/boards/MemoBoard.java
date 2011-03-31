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

import net.l2emuproject.gameserver.services.community.CommunityBoard;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.services.community.models.Forum;
import net.l2emuproject.gameserver.services.community.models.Post;
import net.l2emuproject.gameserver.services.community.models.Topic;
import net.l2emuproject.gameserver.services.community.models.Topic.ConstructorType;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;

public final class MemoBoard extends CommunityBoard
{
	public MemoBoard(CommunityService service)
	{
		super(service);
	}

	@Override
	public final void parseCommand(final L2Player player, final String command)
	{
		final Forum playerForum = CommunityService.getInstance().getPlayerForum(player);
		if (command.equals("_bbsmemo"))
			showPage(player, playerForum, 1);
		else if (command.split(";")[1].equalsIgnoreCase("crea"))
			showWrite(player, null);
		else if (command.split(";")[1].equalsIgnoreCase("list"))
			showPage(player, playerForum, Integer.valueOf(command.split(";")[2]));
		else if (command.split(";")[1].equalsIgnoreCase("read"))
		{
			final Topic topic = playerForum.getTopic(Topic.MEMO);
			final Post post = topic.getPost(Integer.valueOf(command.split(";")[2]));
			if (post == null)
				_log.info("Memo read command: " + command.split(";")[2]);
			else
				showPost(player, post);
		}
		else if (command.split(";")[1].equalsIgnoreCase("del"))
		{
			playerForum.getTopic(Topic.MEMO).rmPostById(Integer.valueOf(command.split(";")[2]));
			showPage(player, playerForum, 1);
		}
		else if (command.split(";")[1].equalsIgnoreCase("edit"))
		{
			final Post post = playerForum.getTopic(Topic.MEMO).getPost(Integer.valueOf(command.split(";")[2]));
			showWrite(player, post);
		}
	}

	@Override
	public final void parseWrite(final L2Player player, final String ar1, final String ar2, final String ar3, final String ar4, final String ar5)
	{
		final Forum playerForum = CommunityService.getInstance().getPlayerForum(player);

		if (ar1.equalsIgnoreCase("new"))
		{
			final int postId = playerForum.getTopic(Topic.MEMO).getNewPostId();
			final Post post = new Post(ConstructorType.CREATE, postId, player.getObjectId(), player.getName(), "", System.currentTimeMillis(), Topic.MEMO,
					playerForum.getForumId(), editPlayerText(ar3), editPlayerText(ar4), 0, 0);
			playerForum.getTopic(Topic.MEMO).addPost(post);
		}
		else if (ar1.equalsIgnoreCase("edit"))
			playerForum.getTopic(Topic.MEMO).getPost(Integer.valueOf(ar2)).updatePost(editPlayerText(ar3), editPlayerText(ar4));

		showPage(player, playerForum, 1);
	}

	private void showPage(final L2Player player, final Forum forum, final int index)
	{
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "memo.htm");
		if (forum == null)
		{
			_log.warn("Forum is NULL!!!");
			showHTML(player, content);
			return;
		}

		final Topic t = forum.getTopic(Topic.MEMO);

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
				tb.append("<td FIXWIDTH=511><a action=\"bypass _bbsmemo;read;" + p.getPostId() + "\">" + p.getPostTitle() + "</a></td>");
				tb.append("<td FIXWIDTH=148 align=center></td>");
				tb.append("<td FIXWIDTH=86 align=center>" + DateFormat.getInstance().format(new Date(p.getPostDate())) + "</td>");
				tb.append("<td FIXWIDTH=5></td>");
				tb.append("</tr>");
				tb.append("<tr><td height=5></td></tr>");
				tb.append("</table>");
				tb.append("<img src=\"L2UI.SquareBlank\" width=\"750\" height=\"3\">");
				tb.append("<img src=\"L2UI.SquareGray\" width=\"750\" height=\"1\">");
			}
		}
		content = content.replaceAll("%memoList%", tb.moveToString());
		tb.clear();
		tb.clear();
		if (index == 1)
		{
			tb.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		else
		{
			tb.append("<td><button action=\"bypass _bbsmemo;list;" + (index - 1)
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
				tb.append("<td><a action=\"bypass _bbsmemo;list;" + i + "\"> " + i + " </a></td>");
			}
		}
		if (index == nbp)
		{
			tb.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		else
		{
			tb.append("<td><button action=\"bypass _bbsmemo;list;" + (index + 1)
					+ "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		content = content.replaceAll("%memoListLength%", tb.moveToString());

		showHTML(player, content);
	}

	private void showWrite(final L2Player player, final Post post)
	{
		String title = " ";
		String message = " ";
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "memo-write.htm");
		content = content.replaceAll("%playerObjId%", String.valueOf(player.getObjectId()));
		if (post == null)
			content = content.replaceAll("%job%", "new");
		else
		{
			content = content.replaceAll("%job%", "edit");
			content = content.replaceAll("%postId%", String.valueOf(post.getPostId()));
			title = post.getPostTitle();
			message = post.getPostText();
		}

		sendWrite(player, content, message, title, title);
	}

	private void showPost(final L2Player player, final Post post)
	{
		post.increaseReadCount();
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "memo-show.htm");
		content = content.replaceAll("%memoName%", post.getPostTitle());
		content = content.replaceAll("%postId%", String.valueOf(post.getPostId()));
		content = content.replaceAll("%memoOwnerName%", post.getOwnerName());
		content = content.replaceAll("%postDate%", DateFormat.getInstance().format(new Date(post.getPostDate())));
		content = content.replaceAll("%mes%", post.getPostText());

		showHTML(player, content);
	}
}
