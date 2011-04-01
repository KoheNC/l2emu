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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExMailArrived;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.services.clan.L2ClanMember;
import net.l2emuproject.gameserver.services.community.CommunityBoard;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.services.community.models.Forum;
import net.l2emuproject.gameserver.services.community.models.Post;
import net.l2emuproject.gameserver.services.community.models.Topic;
import net.l2emuproject.gameserver.services.community.models.Topic.ConstructorType;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;

public final class ClanBoard extends CommunityBoard
{
	public ClanBoard(CommunityService service)
	{
		super(service);
	}

	@Override
	public final void parseCommand(final L2Player player, final String command)
	{
		if (command.equals("_bbsclan") || command.equals("_bbsclan;clan;0"))
			showMainPage(player, 1);
		else if (command.split(";")[1].equalsIgnoreCase("list"))
			showMainPage(player, Integer.valueOf(command.split(";")[2]));
		else if (command.split(";")[1].equalsIgnoreCase("clan"))
		{
			final int clanId = Integer.valueOf(command.split(";")[2]);
			final L2Clan clan = ClanTable.getInstance().getClan(clanId);

			if (clan == null || clan.getLevel() < Config.MIN_CLAN_LVL_FOR_FORUM)
				player.sendPacket(SystemMessageId.NO_CB_IN_MY_CLAN);

			showClanPage(player, clanId);
		}
		else if (command.split(";")[1].equalsIgnoreCase("notice"))
		{
			final L2Clan clan = player.getClan();

			if (command.split(";").length == 3)
			{
				if (clan != null)
				{
					final boolean val = command.split(";")[2].equalsIgnoreCase("true");
					clan.setNoticeEnabled(val);
					CommunityService.getInstance().clanNotice(2, player.getClanId(), null, val);
				}
			}
			showNoticePage(player, clan);
		}
		else if (command.split(";")[1].equalsIgnoreCase("management"))
		{
			if (player.getClan() != null && player.getClan().getLeaderId() != player.getObjectId())
			{
				player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
				showClanPage(player, player.getClanId());
			}
			else
				showClanManagementPage(player, Integer.valueOf(command.split(";")[2]));
		}
		else if (command.split(";")[1].equalsIgnoreCase("mail"))
			showClanMailPage(player, Integer.valueOf(command.split(";")[2]));
		else if (command.split(";")[1].equalsIgnoreCase("permission"))
		{
			final int topicId = (command.split(";")[2].equalsIgnoreCase("cbb") ? Topic.BULLETIN : Topic.ANNOUNCE);
			final Forum clanForum = CommunityService.getInstance().getClanForum(player.getClanId());
			int perNon = clanForum.getTopic(topicId).getPermissions();
			int perMem = perNon % 10;
			perNon = (perNon - perMem) / 10;
			if (command.split(";")[3].equalsIgnoreCase("non"))
				perNon = (perNon + 1) % 3;
			else
				perMem = (perMem + 1) % 3;
			clanForum.getTopic(topicId).setPermissions(perNon * 10 + perMem);
			showClanManagementPage(player, player.getClanId());
		}
	}

	@Override
	public final void parseWrite(final L2Player player, final String ar1, final String ar2, final String ar3, final String ar4, final String ar5)
	{
		if (ar1.equalsIgnoreCase("intro"))
		{
			if (Integer.valueOf(ar2) != player.getClanId())
				return;

			final L2Clan clan = ClanTable.getInstance().getClan(player.getClanId());
			String intro = editPlayerText(ar3);
			clan.setIntroduction(intro);
			CommunityService.getInstance().storeClanIntroduction(player.getClanId(), intro);
			showClanManagementPage(player, Integer.valueOf(ar2));
		}
		else if (ar1.equalsIgnoreCase("notice"))
		{
			final L2Clan clan = player.getClan();

			if (clan == null)
				return;

			String notice = editPlayerText(ar3);

			if (notice.length() > 4096)
				notice = notice.substring(0, 4096);

			clan.setNotice(notice);
			CommunityService.getInstance().clanNotice(2, player.getClanId(), notice, clan.isNoticeEnabled());
			showNoticePage(player, clan);
		}
		else if (ar1.equalsIgnoreCase("mail"))
		{
			final L2Clan clan = player.getClan();

			if (clan == null)
				return;

			for (L2ClanMember member : ClanTable.getInstance().getClan(clan.getClanId()).getMembers())
			{
				final Forum receiverForum = CommunityService.getInstance().getPlayerForum(member.getObjectId());
				final int postId = receiverForum.getTopic(Topic.INBOX).getNewPostId();
				final Post post = new Post(ConstructorType.CREATE, postId, player.getObjectId(), player.getName(), clan.getName(), System.currentTimeMillis(),
						Topic.INBOX, receiverForum.getForumId(), editPlayerText(ar4), editPlayerText(ar5), 0, 0);
				receiverForum.getTopic(Topic.INBOX).addPost(post);
				final L2Player reciever = member.getPlayerInstance();
				if (reciever != null)
				{
					reciever.sendPacket(ExMailArrived.STATIC_PACKET);
					reciever.sendPacket(SystemMessageId.NEW_MAIL);
				}
			}
			showClanPage(player, clan.getClanId());
		}
	}

	private final void showMainPage(final L2Player player, final int index)
	{
		final L2Clan clan = player.getClan();
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanlist.htm");

		content = content.replaceAll("%clanid%", String.valueOf(clan != null ? clan.getClanId() : 0));
		content = content.replaceAll("%clanhomename%", clan != null ? clan.getName() : "");

		final L2TextBuilder tb = L2TextBuilder.newInstance();
		int i = 0;
		for (L2Clan c : ClanTable.getInstance().getClans())
		{
			if (c == null)
				continue;
			if (i > ((index - 1) * 10 + 9))
				break;
			if (i++ >= ((index - 1) * 10))
			{
				tb.append("<img src=\"L2UI.SquareBlank\" width=\"750\" height=\"3\">");
				tb.append("<table border=0 cellspacing=0 cellpadding=0 width=755>");
				tb.append("<tr>");
				tb.append("<td FIXWIDTH=5></td>");
				tb.append("<td FIXWIDTH=200 align=center><a action=\"bypass _bbsclan;clan;").append(c.getClanId()).append("\">").append(c.getName())
						.append("</a></td>");
				tb.append("<td FIXWIDTH=200 align=center>").append(c.getLeaderName()).append("</td>");
				tb.append("<td FIXWIDTH=100 align=center>").append(c.getLevel()).append("</td>");
				tb.append("<td FIXWIDTH=200 align=center>").append(c.getMembersCount()).append("</td>");
				tb.append("<td FIXWIDTH=5></td>");
				tb.append("</tr>");
				tb.append("<tr><td height=5></td></tr>");
				tb.append("</table>");
				tb.append("<img src=\"L2UI.SquareBlank\" width=\"750\" height=\"3\">");
				tb.append("<img src=\"L2UI.SquareGray\" width=\"750\" height=\"1\">");
			}
		}
		content = content.replaceAll("%clanlist%", tb.moveToString());
		tb.clear();
		if (index == 1)
			tb.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		else
			tb.append("<td><button action=\"bypass _bbsclan;list;").append((index - 1))
					.append("\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");

		int nbp;
		nbp = ClanTable.getInstance().getClans().length / 10;
		if (nbp * 10 != ClanTable.getInstance().getClans().length)
			nbp++;
		for (i = 1; i <= nbp; i++)
		{
			if (i == index)
				tb.append("<td> ").append(i).append(" </td>");
			else
				tb.append("<td><a action=\"bypass _bbsclan;list;").append(i).append("\"> ").append(i).append(" </a></td>");
		}
		if (index == nbp)
			tb.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		else
			tb.append("<td><button action=\"bypass _bbsclan;list;").append((index + 1))
					.append("\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");

		content = content.replaceAll("%clanlistlength%", tb.moveToString());

		showHTML(player, content);
	}

	private final String getAnnounceTemplate(final Post post, final int clanId)
	{
		final L2TextBuilder tb = L2TextBuilder.newInstance();

		tb.append("<tr><td height=10></td></tr>");
		tb.append("<tr>");
		tb.append("<td fixWIDTH=100 align=center valign=top>[&\\$429;]</td>");
		tb.append("<td fixWIDTH=460 align=left valign=top><a action=\"bypass _bbscpost;read;announce;" + clanId + ";" + post.getPostId() + "\">"
				+ post.getPostTypeName() + post.getPostTitle() + "</a></td>");
		tb.append("<td fixWIDTH=80 align=right valign=top>&\\$418; :</td>");
		tb.append("<td fixWIDTH=100 align=right valign=top>" + DateFormat.getInstance().format(new Date(post.getPostDate())) + "</td>");
		tb.append("<td FIXWIDTH=10></td>");
		tb.append("</tr>");
		tb.append("<tr><td height=2></td></tr>");

		return tb.moveToString();
	}

	private final String getCommunityBoardTemplate(final Post post, final int clanId)
	{
		final L2TextBuilder tb = L2TextBuilder.newInstance();

		tb.append("<table border=0 cellspacing=0 cellpadding=0 width=750>");
		tb.append("<tr><td height=8></td></tr>");
		tb.append("<tr>");
		tb.append("<td FIXWIDTH=45 align=center>[New]</td>");
		tb.append("<td FIXWIDTH=400><a action=\"bypass _bbscpost;read;cbb;" + clanId + ";" + post.getPostId() + "\">" + post.getPostTypeName()
				+ post.getPostTitle() + "</a></td>");
		tb.append("<td FIXWIDTH=100 align=center>" + post.getOwnerName() + "</td>");
		tb.append("<td FIXWIDTH=100 align=center>" + DateFormat.getInstance().format(new Date(post.getPostDate())) + "</td>");
		tb.append("<td FIXWIDTH=100 align=center>" + post.getReadCount() + "</td>");
		tb.append("<td FIXWIDTH=5></td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"5\">");
		tb.append("<img src=\"L2UI.squaregray\" width=\"750\" height=\"1\">");

		return tb.moveToString();
	}

	private final void showClanPage(final L2Player player, final int clanId)
	{
		final L2Clan clan = ClanTable.getInstance().getClan(clanId);
		String content;
		if (player.getClanId() != clanId)
			content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanhome.htm");
		else if (clan.getLeaderId() == player.getObjectId())
			content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanhome-leader.htm");
		else
			content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanhome-member.htm");

		if (content == null)
		{
			_log.warn("ClanBoard: Missing Content!");
			return;
		}

		final Forum clanForum = CommunityService.getInstance().getClanForum(clanId);
		Post[] post = clanForum.getTopic(Topic.ANNOUNCE).getLastTwoPosts();
		if (post[0] != null)
		{
			String cbb = getAnnounceTemplate(post[0], clanId);
			if (post[1] != null)
				cbb += getAnnounceTemplate(post[1], clanId);
			content = content.replaceAll("%advert%", cbb);
		}
		else
			content = content.replaceAll("%advert%", "");

		post = clanForum.getTopic(Topic.BULLETIN).getLastTwoPosts();
		if (post[0] != null)
		{
			String cbb = getCommunityBoardTemplate(post[0], clanId);
			if (post[1] != null)
				cbb += getCommunityBoardTemplate(post[1], clanId);
			content = content.replaceAll("%clanbbs%", cbb);
		}
		else
			content = content.replaceAll("%clanbbs%", "");

		final String introduction = clan.getIndtroduction();
		content = content.replaceAll("%clanIntro%", introduction != null ? introduction : "");
		content = content.replaceAll("%clanid%", String.valueOf(clanId));
		content = content.replaceAll("%clanName%", clan.getName());
		content = content.replaceAll("%clanLvL%", String.valueOf(clan.getLevel()));
		content = content.replaceAll("%clanMembers%", String.valueOf(clan.getMembersCount()));
		content = content.replaceAll("%clanLeader%", clan.getLeaderName());
		String ally = "";
		if (clan.getAllyId() != 0)
		{
			for (L2Clan c : ClanTable.getInstance().getClans())
			{
				if (clan.getAllyId() == c.getAllyId() && c != clan)
				{
					if (ally.equalsIgnoreCase(""))
						ally += c.getName();
					else
						ally += ", " + c.getName();
				}
			}
		}
		content = content.replaceAll("%allyName%", ally);

		showHTML(player, content);
	}

	private final void showClanManagementPage(final L2Player player, final int clanId)
	{
		final L2Clan clan = player.getClan();

		if (clan == null)
			return;

		if (clan.getClanId() != clanId)
			return;

		final Forum clanForum = CommunityService.getInstance().getClanForum(clanId);
		String content;
		content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanhome-management.htm");

		content = content.replaceAll("%clanid%", String.valueOf(clanId));
		content = content.replaceAll("%clanName%", clan.getName());
		String[] perString =
		{ "No Access", "Read Access", "Write Access", "No Access" };
		int perNon = clanForum.getTopic(Topic.ANNOUNCE).getPermissions();
		int perMem = perNon % 10;
		perNon = (perNon - perMem) / 10;
		content = content.replaceAll("%curAnnoNonPer%", perString[perNon]);
		content = content.replaceAll("%curAnnoMemPer%", perString[perMem]);
		content = content.replaceAll("%nextAnnoNonPer%", perString[perNon + 1]);
		content = content.replaceAll("%nextAnnoMemPer%", perString[perMem + 1]);
		perNon = clanForum.getTopic(Topic.BULLETIN).getPermissions();
		perMem = perNon % 10;
		perNon = (perNon - perMem) / 10;
		content = content.replaceAll("%curBullNonPer%", perString[perNon]);
		content = content.replaceAll("%curBullMemPer%", perString[perMem]);
		content = content.replaceAll("%nextBullNonPer%", perString[perNon + 1]);
		content = content.replaceAll("%nextBullMemPer%", perString[perMem + 1]);

		sendWrite(player, content, clan.getIndtroduction(), "", "");
	}

	private final void showNoticePage(final L2Player player, final L2Clan clan)
	{
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanhome-notice.htm");
		content = content.replaceAll("%clanid%", String.valueOf(clan.getClanId()));
		content = content.replaceAll("%enabled%", (clan.isNoticeEnabled() ? "True" : "False"));
		content = content.replaceAll("%flag%", (clan.isNoticeEnabled() ? "False" : "True"));

		sendWrite(player, content, clan.getNotice(), "", "");
	}

	private final void showClanMailPage(final L2Player player, final int clanId)
	{
		final L2Clan clan = player.getClan();

		if (clan == null)
			return;

		if (clan.getClanId() != clanId)
			return;

		String content;
		content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "clanhome-mail.htm");

		content = content.replaceAll("%clanid%", String.valueOf(clanId));
		content = content.replaceAll("%clanName%", clan.getName());

		showHTML(player, content);
	}
}
