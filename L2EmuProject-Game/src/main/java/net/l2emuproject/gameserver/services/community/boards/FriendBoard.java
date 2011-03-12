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

import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.services.community.CommunityBoard;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;

public final class FriendBoard extends CommunityBoard
{
	private static final byte	MAIN_PAGE		= 0;
	private static final byte	CONFIRM_PAGE	= 1;

	public FriendBoard(CommunityService service)
	{
		super(service);
	}

	@Override
	public final void parseCommand(final L2Player player, final String command)
	{
		if (command.equals("_bbsfriend"))
			showMainPage(player, MAIN_PAGE);
		else if (command.split(";")[1].equalsIgnoreCase("select"))
		{
			final Integer friendId = Integer.valueOf(command.split(";")[2]);
			player.getFriendList().selectFriend(friendId);
			showMainPage(player, MAIN_PAGE);
		}
		else if (command.split(";")[1].equalsIgnoreCase("deselect"))
		{
			final Integer friendId = Integer.valueOf(command.split(";")[2]);
			player.getFriendList().deselectFriend(friendId);
			showMainPage(player, MAIN_PAGE);
		}
		else if (command.split(";")[1].equalsIgnoreCase("delete"))
		{
			for (int i : player.getFriendList().getSelectedFriends())
			{
				final String name = CharNameTable.getInstance().getNameByObjectId(i);
				player.getFriendList().remove(name);
				player.getFriendList().getSelectedFriends().clear();
			}
			showMainPage(player, MAIN_PAGE);
		}
		else if (command.split(";")[1].equalsIgnoreCase("delete_all"))
		{
			showMainPage(player, CONFIRM_PAGE);
		}
		else if (command.split(";")[1].equalsIgnoreCase("delete_all_confirm"))
		{
			for (int i : player.getFriendList().getFriendIds())
			{
				final String name = CharNameTable.getInstance().getNameByObjectId(i);
				player.getFriendList().remove(name);
				player.getFriendList().getSelectedFriends().clear();
			}
			showMainPage(player, MAIN_PAGE);
		}
	}

	@Override
	public final void parseWrite(final L2Player player, final String ar1, final String ar2, final String ar3, final String ar4, final String ar5)
	{
	}

	private final void showMainPage(final L2Player player, final byte pageId)
	{
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "friend.htm");

		final L2TextBuilder tb = L2TextBuilder.newInstance();

		for (int i : player.getFriendList().getFriendIds())
		{
			final L2Player friend = L2World.getInstance().getPlayer(i);

			if (friend == null)
				break;

			tb.append("<a action=\"bypass _bbsfriend;select;" + friend.getObjectId() + "\">" + friend.getName() + "</a> ("
					+ (friend.isOnline() == L2Player.ONLINE_STATE_ONLINE ? "On" : "Off") + ") &nbsp;");
		}
		content = content.replaceAll("%friendslist%", tb.moveToString());

		for (int i : player.getFriendList().getSelectedFriends())
		{
			final L2Player friend = L2World.getInstance().getPlayer(i);

			if (friend == null)
				break;

			tb.append("<a action=\"bypass _bbsfriend;deselect;" + friend.getObjectId() + "\">" + friend.getName() + "</a>;");
		}
		content = content.replaceAll("%selectedFriendsList%", tb.moveToString());

		switch (pageId)
		{
			case MAIN_PAGE:
				content = content.replaceAll("%deleteMSG%", "");
				break;
			case CONFIRM_PAGE:
				tb.append("Do you want to delete all of your friends?"); // In retail this message is bugged.			
				tb.append("<a action=\"bypass _bbsfriend;delete_all_confirm\">Confirm Delete</a>");
				content = content.replaceAll("%deleteMSG%", tb.moveToString());
				break;
		}

		showHTML(player, content);
	}
}
