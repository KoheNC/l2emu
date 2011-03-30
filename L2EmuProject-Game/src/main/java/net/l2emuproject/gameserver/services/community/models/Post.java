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
package net.l2emuproject.gameserver.services.community.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.services.community.models.Topic.ConstructorType;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Post
{
	private static Log				_log			= LogFactory.getLog(Post.class);

	public static final int			ADVERTISE		= 0;
	public static final int			MISCELLANEOUS	= 1;
	public static final int			INFORMATION		= 2;

	private int						_postId;
	private int						_postOwnerId;
	private String					_postOwnerName;
	private String					_postRecipientList;
	private int						_postParentId;
	private long					_postDate;
	private int						_postTopicId;
	private int						_postForumId;
	private String					_postTitle;
	private String					_postText;
	private int						_postType;
	private int						_lastCommentId;
	private Map<Integer, Comment>	_comments;
	private int						_readCount;

	public Post(final ConstructorType ct, final int postId, final int postOwnerId, final String postOwnerName, final String recipentList, final long postDate,
			final int postTopicId, final int postForumId, final String postTitle, final String postText, final int postType, final int readCount)
	{
		_postId = postId;
		_postOwnerId = postOwnerId;
		_postOwnerName = postOwnerName;
		_postRecipientList = recipentList;
		_postDate = postDate;
		_postTopicId = postTopicId;
		_postForumId = postForumId;
		_postTitle = postTitle;
		_postText = postText;
		_postType = postType;
		_postParentId = -1;
		_comments = new FastMap<Integer, Comment>();
		_readCount = readCount;
		if (ct == ConstructorType.CREATE)
			insertInToDB();
		else
			loadComments();
	}

	public Post(final ConstructorType ct, final int postId, final int postOwnerId, final String postOwnerName, final String recipentList,
			final int postParentId, final long postDate, final int postTopicId, final int postForumId, final String postTitle, final String postText,
			final int postType, final int readCount)
	{
		_postId = postId;
		_postOwnerId = postOwnerId;
		_postOwnerName = postOwnerName;
		_postRecipientList = recipentList;
		_postDate = postDate;
		_postTopicId = postTopicId;
		_postForumId = postForumId;
		_postTitle = postTitle;
		_postText = postText;
		_postType = postType;
		_postParentId = postParentId;
		_comments = new FastMap<Integer, Comment>();
		_readCount = readCount;
		if (ct == ConstructorType.CREATE)
			insertInToDB();
		else
			loadComments();
	}

	private void loadComments()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con
					.prepareStatement("SELECT * FROM community_comments WHERE comment_forum_id=? AND comment_topic_id=? AND comment_post_id=?");
			statement.setInt(1, _postForumId);
			statement.setInt(2, _postTopicId);
			statement.setInt(3, _postId);
			final ResultSet result = statement.executeQuery();

			while (result.next())
			{
				final int commentId = Integer.parseInt(result.getString("comment_id"));
				final int commentOwner = Integer.parseInt(result.getString("comment_ownerid"));
				final long date = Long.parseLong(result.getString("comment_date"));
				final String text = result.getString("comment_txt");
				final Comment c = new Comment(ConstructorType.RESTORE, commentId, commentOwner, date, _postId, _postTopicId, _postForumId, text);
				_comments.put(commentId, c);
				if (commentId > _lastCommentId)
					_lastCommentId = commentId;
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("data error on Forum " + _postForumId + " : ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public int getNewCommentId()
	{
		return ++_lastCommentId;
	}

	public void insertInToDB()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con
					.prepareStatement("INSERT INTO community_posts (post_id,post_ownerid,post_ownername,post_recipient_list,post_date,post_topic_id,post_forum_id,post_txt,post_title,post_type,post_parent_id,post_read_count) values (?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _postId);
			statement.setInt(2, _postOwnerId);
			statement.setString(3, _postOwnerName);
			statement.setString(4, _postRecipientList);
			statement.setLong(5, _postDate);
			statement.setInt(6, _postTopicId);
			statement.setInt(7, _postForumId);
			statement.setString(8, _postText);
			statement.setString(9, _postTitle);
			statement.setInt(10, _postType);
			statement.setInt(11, _postParentId);
			statement.setInt(12, _readCount);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("error while saving new Post to db " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void deleteFromDB()
	{
		for (Comment c : _comments.values())
			c.deleteFromDB();
		_comments.clear();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("DELETE FROM community_posts WHERE post_forum_id=? AND post_topic_id=? AND post_id=?");
			statement.setInt(1, _postForumId);
			statement.setInt(2, _postTopicId);
			statement.setInt(3, _postId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void updatePost()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con
					.prepareStatement("UPDATE community_posts SET post_txt=?,post_title=?,post_recipient_list=?,post_read_count=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?");
			statement.setString(1, _postText);
			statement.setString(2, _postTitle);
			statement.setString(3, _postRecipientList);
			statement.setInt(4, _readCount);
			statement.setInt(5, _postId);
			statement.setInt(6, _postTopicId);
			statement.setInt(7, _postForumId);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("error while saving new Post to db " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void clearComments()
	{
		_comments.clear();
	}

	public int getCommentsSize()
	{
		return _comments.size();
	}

	public Comment getComment(final int j)
	{
		return _comments.get(j);
	}

	public void addComment(final Comment c)
	{
		_comments.put(c.getCommentId(), c);
	}

	public void rmCommentByID(final int id)
	{
		_comments.get(id).deleteFromDB();
		_comments.remove(id);
	}

	public Collection<Comment> getAllComments()
	{
		return _comments.values();
	}

	public int getPostId()
	{
		return _postId;
	}

	public String getPostText()
	{
		return _postText;
	}

	public int getOwnerId()
	{
		return _postOwnerId;
	}

	public String getOwnerName()
	{
		return _postOwnerName;
	}

	public int getParentId()
	{
		return _postParentId;
	}

	public void updatePost(final String newTitle, final String newText)
	{
		_postTitle = newTitle;
		_postText = newText;
		updatePost();
	}

	public void updatePost(final String newTitle, final String newText, final int type)
	{
		_postTitle = newTitle;
		_postText = newText;
		_postType = type;
		updatePost();
	}

	public void setTopic(final int newTopicId, final int newPostId)
	{
		_postTopicId = newTopicId;
		_postId = newPostId;
		insertInToDB();
	}

	public String getPostRecipientList()
	{
		return _postRecipientList;
	}

	public String getPostTitle()
	{
		return _postTitle;
	}

	public Long getPostDate()
	{
		return _postDate;
	}

	public int getPostType()
	{
		return _postType;
	}

	public String getPostTypeName()
	{
		switch (_postType)
		{
			case ADVERTISE:
				return "[Advertise]";
			case MISCELLANEOUS:
				return "[Miscellaneous]";
			case INFORMATION:
				return "[Information]";
		}
		return "";
	}

	public int getReadCount()
	{
		return _readCount;
	}

	public void increaseReadCount()
	{
		_readCount++;
		updatePost();
	}
}