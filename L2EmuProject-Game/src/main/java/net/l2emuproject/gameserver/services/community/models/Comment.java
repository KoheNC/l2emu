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

import net.l2emuproject.gameserver.services.community.models.Topic.ConstructorType;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Comment
{
	private static final Log	_log	= LogFactory.getLog(Comment.class);

	private final int			_commentId;
	private final int			_commentOwnerId;
	private final long			_commentDate;
	private final int			_commentPostId;
	private final int			_commentTopicId;
	private final int			_commentForumId;
	private final String		_commentText;

	public Comment(final ConstructorType ct, final int commentId, final int commentOwnerId, final long commentDate, final int commentPostId,
			final int commentTopicId, final int commentForumId, final String commentText)
	{
		_commentId = commentId;
		_commentOwnerId = commentOwnerId;
		_commentDate = commentDate;
		_commentPostId = commentPostId;
		_commentTopicId = commentTopicId;
		_commentForumId = commentForumId;
		_commentText = commentText;
		if (ct == ConstructorType.CREATE)
		{
			insertInToDB();
		}
	}

	public void insertInToDB()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con
					.prepareStatement("INSERT INTO community_comments (comment_id,comment_ownerid,comment_date,comment_post_id,comment_topic_id,comment_forum_id,comment_txt) values (?,?,?,?,?,?,?)");
			statement.setInt(1, _commentId);
			statement.setInt(2, _commentOwnerId);
			statement.setLong(3, _commentDate);
			statement.setInt(4, _commentPostId);
			statement.setInt(5, _commentTopicId);
			statement.setInt(6, _commentForumId);
			statement.setString(7, _commentText);
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
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con
					.prepareStatement("DELETE FROM community_comments WHERE comment_forum_id=? AND comment_topic_id=? AND comment_Post_id=? AND comment_id=?");
			statement.setInt(1, _commentForumId);
			statement.setInt(2, _commentTopicId);
			statement.setInt(3, _commentPostId);
			statement.setInt(4, _commentId);
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

	public int getCommentId()
	{
		return _commentId;
	}

	public String getCommentText()
	{
		return _commentText;
	}

	public int getOwnerId()
	{
		return _commentOwnerId;
	}

	public Long getCommentDate()
	{
		return _commentDate;
	}
}