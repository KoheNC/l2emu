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

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Topic
{
	private static final Log	_log			= LogFactory.getLog(Topic.class);

	public static final int		SERVER			= 0;
	public static final int		INBOX			= 1;
	public static final int		OUTBOX			= 2;
	public static final int		ARCHIVE			= 3;
	public static final int		TEMP_ARCHIVE	= 4;
	public static final int		MEMO			= 5;
	public static final int		ANNOUNCE		= 6;
	public static final int		BULLETIN		= 7;

	public static final int		NONE			= 0;
	public static final int		ALL				= 1;
	public static final int		READ			= 2;

	private int					_topicId;
	private int					_forumId;
	private String				_topicName;
	private int					_ownerId;
	private int					_lastPostId;
	private int					_permissions;
	private Map<Integer, Post>	_posts;

	public Topic(final ConstructorType ct, final int topicId, final int forumId, final String forumName, final int ownerId, final int permissions)
	{
		_topicId = topicId;
		_forumId = forumId;
		_topicName = forumName;
		_ownerId = ownerId;
		_lastPostId = 0;
		_permissions = permissions;
		_posts = new FastMap<Integer, Post>();

		if (ct == ConstructorType.CREATE)
			insertInToDB();
		else
			loadPosts();
	}

	private void loadPosts()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM community_posts WHERE post_forum_id=? AND post_topic_id=?");
			statement.setInt(1, _forumId);
			statement.setInt(2, _topicId);
			final ResultSet result = statement.executeQuery();

			while (result.next())
			{
				final int postId = Integer.parseInt(result.getString("post_id"));
				final int postOwner = Integer.parseInt(result.getString("post_ownerid"));
				final String postOwnerName = result.getString("post_ownername");
				final String recipientList = result.getString("post_recipient_list");
				final long date = Long.parseLong(result.getString("post_date"));
				final String title = result.getString("post_title");
				final String text = result.getString("post_txt");
				final int type = Integer.parseInt(result.getString("post_type"));
				final int parentId = Integer.parseInt(result.getString("post_parent_id"));
				final int readCount = Integer.parseInt(result.getString("post_read_count"));
				final Post p = new Post(ConstructorType.RESTORE, postId, postOwner, postOwnerName, recipientList, parentId, date, _topicId, _forumId, title,
						text, type, readCount);
				_posts.put(postId, p);
				if (postId > _lastPostId)
					_lastPostId = postId;
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("data error on Forum " + _forumId + " : ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public int getNewPostId()
	{
		return ++_lastPostId;
	}

	public void insertInToDB()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con
					.prepareStatement("INSERT INTO community_topics (topic_id,topic_forum_id,topic_name,topic_ownerid,topic_permissions) values (?,?,?,?,?)");
			statement.setInt(1, _topicId);
			statement.setInt(2, _forumId);
			statement.setString(3, _topicName);
			statement.setInt(4, _ownerId);
			statement.setInt(5, _permissions);
			statement.execute();
			statement.close();

		}
		catch (Exception e)
		{
			_log.warn("error while saving new Topic to db " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public enum ConstructorType
	{
		RESTORE, CREATE
	}

	public void clearPosts()
	{
		_posts.clear();
	}

	public int getPostsSize()
	{
		return _posts.size();
	}

	public Post getPost(final int j)
	{
		return _posts.get(j);
	}

	public void addPost(final Post p)
	{
		_posts.put(p.getPostId(), p);
	}

	public void rmPostById(final int id)
	{
		_posts.get(id).deleteFromDB();
		_posts.remove(id);
	}

	public Collection<Post> getAllPosts()
	{
		return _posts.values();
	}

	public Post[] getLastTwoPosts()
	{
		// if the Topic type is Announce then only Advertise Posts count
		Post[] ret = new Post[2];
		for (Post p : _posts.values())
		{
			if (_topicId == ANNOUNCE && p.getPostType() != Post.ADVERTISE)
				continue;
			if (ret[0] == null || ret[0].getPostDate() < p.getPostDate())
			{
				ret[1] = ret[0];
				ret[0] = p;
			}
		}
		return ret;
	}

	public FastList<Post> getChildrenPosts(final Post parent)
	{
		final FastList<Post> ret = new FastList<Post>();
		if (parent == null)
			return ret;
		// parent post always the first
		ret.add(parent);
		for (Post p : _posts.values())
			if (p.getParentId() == parent.getPostId())
				ret.add(p);

		return ret;
	}

	public int getTopicId()
	{
		return _topicId;
	}

	public int getForumId()
	{
		return _forumId;
	}

	public String getTopicName()
	{
		return _topicName;
	}

	public void deleteFromDB(final Forum f)
	{
		f.rmTopicById(getTopicId());
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("DELETE FROM community_topics WHERE topic_id=? AND topic_forum_id=?");
			statement.setInt(1, getTopicId());
			statement.setInt(2, f.getForumId());
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

	public int getPermissions()
	{
		return _permissions;
	}

	public void setPermissions(final int val)
	{
		_permissions = val;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("UPDATE community_topics SET topic_permissions=? WHERE topic_id=? AND topic_forum_id=?");
			statement.setInt(1, _permissions);
			statement.setInt(2, _topicId);
			statement.setInt(3, _forumId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warn("error while saving new permissions to db " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
