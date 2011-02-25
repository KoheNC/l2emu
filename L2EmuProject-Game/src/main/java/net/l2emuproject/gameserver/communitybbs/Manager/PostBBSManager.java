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
package net.l2emuproject.gameserver.communitybbs.Manager;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.communitybbs.bb.Forum;
import net.l2emuproject.gameserver.communitybbs.bb.Post;
import net.l2emuproject.gameserver.communitybbs.bb.Post.CPost;
import net.l2emuproject.gameserver.communitybbs.bb.Topic;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;


public class PostBBSManager extends BaseBBSManager
{

	private final Map<Topic,Post> _postByTopic;

	public static PostBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private PostBBSManager()
	{
		_postByTopic = new FastMap<Topic,Post>();
	}

	public Post getGPosttByTopic(Topic t)
	{
		Post post = null;
		post = _postByTopic.get(t);
		if(post == null)
		{
			post = load(t);
			_postByTopic.put(t,post);
		}
		return post;
	}
	/**
	 * @param t
	 */
	public void delPostByTopic(Topic t)
	{
		_postByTopic.remove(t);
	}
	public void addPostByTopic(Post p,Topic t)
	{
		if(_postByTopic.get(t) == null)
		{
			_postByTopic.put(t,p);
		}
	}
	/**
	 * @param t
	 * @return
	 */
	private Post load(Topic t)
	{
		Post p;
		p = new Post(t);
		return p;
	}
	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, net.l2emuproject.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsecmd(String command, L2Player activeChar)
	{
		if(command.startsWith("_bbsposts;read;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			int idp = Integer.parseInt(st.nextToken());
			String index = null;
			if(st.hasMoreTokens())
			{
			 index = st.nextToken();
			}
			int ind = 0;
			if(index == null)
			{
				ind = 1;
			}
			else
			{
				ind = Integer.parseInt(index);
			}

			showPost((TopicBBSManager.getInstance().getTopicByID(idp)),ForumsBBSManager.getInstance().getForumByID(idf),activeChar,ind);
		}
		else if(command.startsWith("_bbsposts;edit;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			int idt = Integer.parseInt(st.nextToken());
			int idp = Integer.parseInt(st.nextToken());
			showEditPost((TopicBBSManager.getInstance().getTopicByID(idt)),ForumsBBSManager.getInstance().getForumByID(idf),activeChar,idp);
		}
		else
		{
			notImplementedYet(activeChar, command);
		}
	}
	/**
	 * @param topic
	 * @param forumByID
	 * @param activeChar
	 * @param idp
	 */
	private void showEditPost(Topic topic, Forum forum, L2Player activeChar, int idp)
	{
		Post p = getGPosttByTopic(topic);
		if((forum == null)||(topic == null)||(p == null))
		{
			separateAndSend("<html><body><br><br><center>Error, this forum, topic or post does not exit !</center><br><br></body></html>", activeChar);
		}
		else
		{
			showHtmlEditPost(topic,activeChar,forum,p);
		}
	}

	/**
	 * @param posttByTopic
	 * @param forumByID
	 * @param activeChar
	 * @param ind
	 * @param idf
	 */
	private void showPost(Topic topic, Forum forum, L2Player activeChar, int ind)
	{
		if((forum == null)||(topic == null))
		{
			separateAndSend("<html><body><br><br><center>Error, this forum is not implemented yet</center><br><br></body></html>", activeChar);
		}
		else if(forum.getType() == Forum.MEMO)
		{
			showMemoPost(topic,activeChar,forum);
		}
		else
		{
			separateAndSend("<html><body><br><br><center>the forum: "+forum.getName()+" is not implemented yet</center><br><br></body></html>", activeChar);
		}
	}
	/**
	 * @param topic
	 * @param activeChar
	 * @param forum
	 * @param p
	 */
	private void showHtmlEditPost(Topic topic, L2Player activeChar, Forum forum, Post p)
	{
		final L2TextBuilder html = L2TextBuilder.newInstance();
		html.append("<html>");
		html.append("<body><br><br>");
		html.append("<table border=0 width=610><tr><td width=10></td><td width=600 align=left>");
		html.append("<a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">").append(
			forum.getName()).append(" Form</a>");
		html.append("</td></tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"10\">");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br1><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td></tr>");
		html.append("</table>");
		html.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=20></td></tr>");
		html.append("<tr>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("<td align=center FIXWIDTH=60 height=29>&$413;</td>");
		html.append("<td FIXWIDTH=540>").append(topic.getName()).append("</td>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("</tr></table>");
		html.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr>");
		html.append("<tr>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("<td align=center FIXWIDTH=60 height=29 valign=top>&$427;</td>");
		html.append("<td align=center FIXWIDTH=540><MultiEdit var =\"Content\" width=535 height=313></td>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("</tr>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr>");
		html.append("</table>");
		html.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr>");
		html.append("<tr>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("<td align=center FIXWIDTH=60 height=29>&nbsp;</td>");
		html.append("<td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Post ").append(forum.getID()).append(";").append(topic.getID())
			.append(";0 _ Content Content Content\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>");
		html.append("<td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td>");
		html.append("<td align=center FIXWIDTH=400>&nbsp;</td>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("</tr></table>");
		html.append("</center>");
		html.append("</body>");
		html.append("</html>");
		
		send1001(html.moveToString(), activeChar);
		send1002(activeChar, p.getCPost(0).postTxt, topic.getName(), DateFormat.getInstance().format(
				new Date(topic.getDate())));
	}

	/**
	 * @param topic
	 * @param activeChar
	 * @param forum
	 */
	private void showMemoPost(Topic topic, L2Player activeChar, Forum forum)
	{
		//
		Post p = getGPosttByTopic(topic);
		Locale locale = Locale.getDefault();
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
		final L2TextBuilder html = L2TextBuilder.newInstance();
		html.append("<html><body><br><br>");
		html.append("<table border=0 width=610><tr><td width=10></td><td width=600 align=left>");
		html.append("<a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a>");
		html.append("</td></tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"10\">");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 bgcolor=333333>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td fixWIDTH=55 align=right valign=top>&$413; : &nbsp;</td>");
		html.append("<td fixWIDTH=380 valign=top>").append(topic.getName()).append("</td>");
		html.append("<td fixwidth=5></td>");
		html.append("<td fixwidth=50></td>");
		html.append("<td fixWIDTH=120></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td align=right><font color=\"AAAAAA\" >&$417; : &nbsp;</font></td>");
		html.append("<td><font color=\"AAAAAA\">").append(topic.getOwnerName()).append("</font></td>");
		html.append("<td></td>");
		html.append("<td><font color=\"AAAAAA\">&$418; :</font></td>");
		html.append("<td><font color=\"AAAAAA\">").append(dateFormat.format(p.getCPost(0).postDate)).append(
			"</font></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("</table>");
		html.append("<br>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td fixwidth=5></td>");
		String Mes = p.getCPost(0).postTxt.replace(">", "&gt;");
		Mes = Mes.replace("<", "&lt;");
		Mes = Mes.replace("\n", "<br1>");
		html.append("<td FIXWIDTH=600 align=left>").append(Mes).append("</td>");
		html.append("<td fixqqwidth=5></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"5\">");
		html.append("<img src=\"L2UI.squaregray\" width=\"610\" height=\"1\">");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"5\">");
		html.append("<table border=0 cellspacing=0 cellpadding=0 FIXWIDTH=610>");
		html.append("<tr>");
		html.append("<td width=50>");
		html.append("<button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">");
		html.append("</td>");
		html.append("<td width=560 align=right><table border=0 cellspacing=0><tr>");
		html.append("<td FIXWIDTH=300></td><td><button value = \"&$424;\" action=\"bypass _bbsposts;edit;").append(forum.getID()).append(";").append(topic.getID())
			.append(";0\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;");
		html.append("<td><button value = \"&$425;\" action=\"bypass _bbstopics;del;").append(forum.getID()).append(";").append(topic.getID())
			.append("\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;");
		html.append("<td><button value = \"&$421;\" action=\"bypass _bbstopics;crea;").append(forum.getID())
			.append("\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>&nbsp;");
		html.append("</tr></table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br>");
		html.append("<br>");
		html.append("<br></center>");
		html.append("</body>");
		html.append("</html>");
		separateAndSend(html, activeChar);
	}
	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, net.l2emuproject.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player activeChar)
	{

		StringTokenizer st = new StringTokenizer(ar1, ";");
		int idf = Integer.parseInt(st.nextToken());
		int idt = Integer.parseInt(st.nextToken());
		int idp = Integer.parseInt(st.nextToken());

		Forum f = ForumsBBSManager.getInstance().getForumByID(idf);
		if(f == null)
		{
			separateAndSend("<html><body><br><br><center>the forum: "+idf+" does not exist !</center><br><br></body></html>", activeChar);
		}
		else
		{
			Topic t = f.getTopic(idt);
			if(t == null)
			{
				separateAndSend("<html><body><br><br><center>the topic: "+idt+" does not exist !</center><br><br></body></html>", activeChar);
			}
			else
			{
				CPost cp = null;
				Post p = getGPosttByTopic(t);
				if(p != null)
				{
					cp = p.getCPost(idp);
				}
				if(cp == null)
				{
					separateAndSend("<html><body><br><br><center>the post: "+idp+" does not exist !</center><br><br></body></html>", activeChar);
				}
				else if(p != null)
				{
					p.getCPost(idp).postTxt = ar4;
					p.updatetxt(idp);
					parsecmd("_bbsposts;read;"+ f.getID() +";"+ t.getID(),activeChar);
				}
			}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PostBBSManager _instance = new PostBBSManager();
	}
}
