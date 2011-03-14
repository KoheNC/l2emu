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
package net.l2emuproject.gameserver.services.blocklist;

import java.util.Set;

import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author luisantonioa
 */
public final class BlockList
{
	private final L2Player		_owner;
	private final Set<String>	_set;

	private boolean				_blockingAll	= false;

	public BlockList(L2Player owner)
	{
		_owner = owner;
		_set = BlockListService.getInstance().getBlockList(_owner.getObjectId());
	}
	
	public final Iterable<String> getBlocks()
	{
		return _set;
	}

	public final void add(String name)
	{
		final L2Player player = L2World.getInstance().getPlayer(name);
		if (player == null)
		{
			_owner.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}

		if (player.isGM())
		{
			if (player.getAppearance().isInvisible())
				_owner.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			else
				_owner.sendPacket(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM);
			return;
		}

		if (_set.add(player.getName()))
		{
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_WAS_ADDED_TO_YOUR_IGNORE_LIST).addPcName(player));
			player.sendPacket(new SystemMessage(SystemMessageId.C1_HAS_ADDED_YOU_TO_IGNORE_LIST).addPcName(_owner));
			BlockListService.getInstance().insert(_owner, player);
		}
		else
			_owner.sendMessage(player.getName() + " was already on your Ignore List.");
	}

	public final void remove(String name)
	{
		if (_set.remove(name))
		{
			_owner.sendPacket(new SystemMessage(SystemMessageId.C1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST).addString(name));

			BlockListService.getInstance().remove(_owner, name);
		}
		else
			_owner.sendMessage(name + " wasn't on your Ignore List.");
	}

	private boolean contains(L2Player player)
	{
		if (player == null || player.isGM())
			return false;

		return _blockingAll || _set.contains(player.getName());
	}

	public static boolean isBlocked(L2Player listOwner, L2Player player)
	{
		return listOwner.getBlockList().contains(player);
	}

	public void setBlockingAll(boolean blockingAll)
	{
		_blockingAll = blockingAll;

		if (_blockingAll)
			_owner.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
		else
			_owner.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
	}

	public void sendListToOwner()
	{
		_owner.sendPacket(SystemMessageId.BLOCK_LIST_HEADER);

		for (String name : _set)
			_owner.sendMessage(name);

		_owner.sendMessage("");
	}

}
