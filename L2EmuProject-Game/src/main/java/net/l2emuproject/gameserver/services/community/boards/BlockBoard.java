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

import net.l2emuproject.gameserver.services.community.CommunityBoard;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2TextBuilder;

/**
 * @author lord_rex
 */
public final class BlockBoard extends CommunityBoard
{
	private static final byte	MAIN_PAGE		= 0;
	private static final byte	CONFIRM_PAGE	= 1;

	public BlockBoard(CommunityService service)
	{
		super(service);
	}

	@Override
	public final void parseCommand(final L2Player player, final String command)
	{
		if (command.equals("_bbsblock"))
			showMainPage(player, MAIN_PAGE);
		else if (command.split(";")[1].equalsIgnoreCase("delete"))
		{
			final String name = String.valueOf(command.split(";")[2]);
			player.getBlockList().remove(name);
			showMainPage(player, MAIN_PAGE);
		}
		else if (command.split(";")[1].equalsIgnoreCase("delete_all"))
		{
			showMainPage(player, CONFIRM_PAGE);
		}
		else if (command.split(";")[1].equalsIgnoreCase("delete_all_confirm"))
		{
			for (String name : player.getBlockList().getBlocks())
				player.getBlockList().remove(name);

			showMainPage(player, MAIN_PAGE);
		}
	}

	@Override
	public final void parseWrite(final L2Player player, final String ar1, final String ar2, final String ar3, final String ar4, final String ar5)
	{
		if (ar1.equals("Block"))
			player.getBlockList().add(ar3);
	}

	private final void showMainPage(final L2Player player, final byte pageId)
	{
		String content = HtmCache.getInstance().getHtm(STATICFILES_PATH + "block.htm");

		final L2TextBuilder tb = L2TextBuilder.newInstance();

		for (String name : player.getBlockList().getBlocks())
		{
			final L2Player block = L2World.getInstance().getPlayer(name);

			if (block == null)
				break;

			tb.append(block.getName() + "&nbsp;<a action=\"bypass _bbsblock;delete;" + block.getName() + "\">Delete</a>");
		}
		content = content.replaceAll("%blockslist%", tb.moveToString());

		switch (pageId)
		{
			case MAIN_PAGE:
				content = content.replaceAll("%deleteMSG%", "");
				break;
			case CONFIRM_PAGE:
				tb.append("Do you want to delete all characters from the block list?<br>");
				tb.append("<button value=\"OK\" action=\"bypass _bbsblock;delete_all_confirm\" width=70 height=25 back=\"l2ui_ct1.button.button_df_small_down\" fore=\"l2ui_ct1.button.button_df_small\">");
				content = content.replaceAll("%deleteMSG%", tb.moveToString());
				break;
		}

		showHTML(player, content);
	}
}
