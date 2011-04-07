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
import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.services.community.models.Topic.ConstructorType;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Forum
{
	private static final Log			_log	= LogFactory.getLog(Forum.class);

	public static final int				ROOT	= 0;
	public static final int				NORMAL	= 1;
	public static final int				CLAN	= 2;
	public static final int				PLAYER	= 3;

	private final Map<Integer, Topic>	_topic;
	private final int					_forumId;
	private String						_forumName;
	private int							_forumType;
	private int							_ownerId;
	private boolean						_loaded	= false;

	public Forum(final int forumId)
	{
		_forumId = forumId;
		_topic = new FastMap<Integer, Topic>();
	}

	public Forum(final int forumId, final String forumName, final int forumType, final int ownerId)
	{
		_forumName = forumName;
		_forumId = forumId;
		_forumType = forumType;
		_ownerId = ownerId;
		_topic = new FastMap<Integer, Topic>();
		_loaded = true;
		insertInToDB();
		if (forumType == Forum.PLAYER)
		{
			_topic.put(Topic.INBOX, new Topic(ConstructorType.CREATE, Topic.INBOX, forumId, forumName, ownerId, 0));
			_topic.put(Topic.OUTBOX, new Topic(ConstructorType.CREATE, Topic.OUTBOX, forumId, forumName, ownerId, 0));
			_topic.put(Topic.ARCHIVE, new Topic(ConstructorType.CREATE, Topic.ARCHIVE, forumId, forumName, ownerId, 0));
			_topic.put(Topic.TEMP_ARCHIVE, new Topic(ConstructorType.CREATE, Topic.TEMP_ARCHIVE, forumId, forumName, ownerId, 0));
			_topic.put(Topic.MEMO, new Topic(ConstructorType.CREATE, Topic.MEMO, forumId, forumName, ownerId, 0));
		}
		else if (forumType == Forum.CLAN)
		{
			_topic.put(Topic.ANNOUNCE, new Topic(ConstructorType.CREATE, Topic.ANNOUNCE, forumId, forumName, ownerId, 0));
			_topic.put(Topic.BULLETIN, new Topic(ConstructorType.CREATE, Topic.BULLETIN, forumId, forumName, ownerId, 0));
		}
	}

	private void load()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM community_forums WHERE forum_id=?");
			statement.setInt(1, _forumId);
			final ResultSet result = statement.executeQuery();

			if (result.next())
			{
				_forumName = result.getString("forum_name");
				_forumType = Integer.parseInt(result.getString("forum_type"));
				_ownerId = Integer.parseInt(result.getString("forum_owner_id"));
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
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM community_topics WHERE topic_forum_id=? ORDER BY topic_id DESC");
			statement.setInt(1, _forumId);
			ResultSet result = statement.executeQuery();
			while (result.next())
			{
				final Topic t = new Topic(Topic.ConstructorType.RESTORE, Integer.parseInt(result.getString("topic_id")), Integer.parseInt(result
						.getString("topic_forum_id")), result.getString("topic_name"), Integer.parseInt(result.getString("topic_ownerid")),
						Integer.parseInt(result.getString("topic_permissions")));
				_topic.put(t.getTopicId(), t);
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

	public int getTopicSize()
	{
		if (_loaded == false)
		{
			load();
			_loaded = true;
		}
		return _topic.size();
	}

	public Topic getTopic(final int id)
	{
		if (_loaded == false)
		{
			load();
			_loaded = true;
		}
		return _topic.get(id);
	}

	public void addTopic(final Topic t)
	{
		if (_loaded == false)
		{
			load();
			_loaded = true;
		}
		_topic.put(t.getTopicId(), t);
	}

	public int getForumId()
	{
		return _forumId;
	}

	public int getOwner()
	{
		return _ownerId;
	}

	public String getForumName()
	{
		if (_loaded == false)
		{
			load();
			_loaded = true;
		}
		return _forumName;
	}

	public int getForumType()
	{
		if (_loaded == false)
		{
			load();
			_loaded = true;
		}
		return _forumType;
	}

	public void rmTopicById(final int id)
	{
		_topic.remove(id);
	}

	public void insertInToDB()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con
					.prepareStatement("INSERT INTO community_forums (forum_id,forum_name,forum_type,forum_owner_id) values (?,?,?,?)");
			statement.setInt(1, _forumId);
			statement.setString(2, _forumName);
			statement.setInt(3, _forumType);
			statement.setInt(4, _ownerId);
			statement.execute();
			statement.close();

		}
		catch (Exception e)
		{
			_log.warn("error while saving new Forum to db " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void vload()
	{
		if (_loaded == false)
		{
			load();
			_loaded = true;
		}
	}
}
