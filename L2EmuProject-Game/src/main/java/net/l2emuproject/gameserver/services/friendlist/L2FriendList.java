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
package net.l2emuproject.gameserver.services.friendlist;

import java.util.List;
import java.util.Set;

import javolution.util.FastList;
import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.FriendPacket;
import net.l2emuproject.gameserver.network.serverpackets.FriendPacket.FriendAction;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.blocklist.BlockList;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author G1ta0
 */
public final class L2FriendList
{
	private final L2Player		_owner;
	private final Set<Integer>	_set;

	public L2FriendList(L2Player owner)
	{
		_owner = owner;
		_set = FriendListService.getInstance().getFriendList(owner.getObjectId());
	}

	public final boolean contains(Integer objectId)
	{
		return _set.contains(objectId);
	}

	public final boolean contains(L2Player player)
	{
		return player != null && contains(player.getObjectId());
	}

	public final Iterable<Integer> getFriendIds()
	{
		return _set;
	}

	public final boolean canAddAsFriend(L2Player friend)
	{
		if (friend == null || friend.isOnline() == 0 || friend.getAppearance().isInvisible())
			_owner.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
		else if (friend == _owner)
			_owner.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
		else if (BlockList.isBlocked(_owner, friend))
			_owner.sendMessage(friend.getName() + " is already on your Ignore List.");
		else if (BlockList.isBlocked(friend, _owner))
			_owner.sendMessage("You are in target's Ignore List.");
		else if (contains(friend))
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_ALREADY_ON_FRIENDS_LIST).addPcName(friend));
		else
			return true;

		return false;
	}

	public final void add(L2Player friend)
	{
		if (!canAddAsFriend(friend))
			return;

		if (FriendListService.getInstance().insert(_owner.getObjectId(), friend.getObjectId()))
		{
			_owner.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);

			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_ADDED_TO_FRIENDS).addPcName(friend));
			_owner.sendPacket(new FriendPacket(FriendAction.ADD_FRIEND, friend.getObjectId()));

			friend.sendPacket(new SystemMessage(SystemMessageId.C1_JOINED_AS_FRIEND).addPcName(_owner));
			friend.sendPacket(new FriendPacket(FriendAction.ADD_FRIEND, _owner.getObjectId()));
		}
		else
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_ALREADY_ON_FRIENDS_LIST).addPcName(friend));
	}

	public final void remove(String name)
	{
		final Integer objId = CharNameTable.getInstance().getObjectIdByName(name);

		if (objId != null && FriendListService.getInstance().remove(_owner.getObjectId(), objId))
		{
			name = CharNameTable.getInstance().getNameByObjectId(objId);

			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(name));
			_owner.sendPacket(new FriendPacket(FriendAction.REMOVE_FRIEND, objId));

			final L2Player friend = L2World.getInstance().findPlayer(objId);
			if (friend != null)
			{
				friend.sendPacket(new SystemMessage(SystemMessageId.C1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addPcName(_owner));
				friend.sendPacket(new FriendPacket(FriendAction.REMOVE_FRIEND, _owner.getObjectId()));
			}
		}
		else
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_NOT_ON_YOUR_FRIENDS_LIST).addString(name));
	}

	private final List<Integer> _selectedFriends = new FastList<Integer>();
	
	public final void selectFriend(Integer friendId)
	{
		if (_selectedFriends.contains(friendId))
			return;
		
		_selectedFriends.add(friendId);
	}
	
	public final void deselectFriend(Integer friendId)
	{
		if (!_selectedFriends.contains(friendId))
			return;
		
		_selectedFriends.remove(friendId);
	}
	
	public final List<Integer> getSelectedFriends()
	{
		return _selectedFriends;
	}
}
