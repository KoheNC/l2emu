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
package net.l2emuproject.gameserver.world.object.instance;

import net.l2emuproject.gameserver.events.global.krateicube.KrateiCube;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author lord_rex
 */
public final class L2KrateiCubeManagerInstance extends L2TeleporterInstance
{
	public L2KrateiCubeManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public final void onBypassFeedback(L2Player player, String command)
	{
		if (command.equalsIgnoreCase("Register"))
		{
			KrateiCube.getInstance().registerPlayer(player);
		}
		else if (command.equalsIgnoreCase("Cancel"))
		{
			KrateiCube.getInstance().cancelRegistration(player);
			player.showHTMLFile(KrateiCube.HTML_PATH + "32503-6.htm");
		}
		else if (command.equalsIgnoreCase("BackToRooms"))
		{
			KrateiCube.getInstance().giveBuffs(player);
			KrateiCube.getInstance().randomTeleport(player);
		}
		else if (command.equalsIgnoreCase("LeaveKrateiCube"))
		{
			KrateiCube.getInstance().leaveKrateiCube(player);
		}
		else
			super.onBypassFeedback(player, command);

	}

	@Override
	public final String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
			pom = String.valueOf(npcId);
		else
			pom = npcId + "-" + val;

		return KrateiCube.HTML_PATH + pom + ".htm";
	}
}
