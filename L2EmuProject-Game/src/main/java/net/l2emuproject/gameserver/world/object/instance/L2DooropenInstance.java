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

import java.util.StringTokenizer;

import net.l2emuproject.gameserver.datatables.DoorTable;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Player;

public class L2DooropenInstance extends L2NpcInstance
{
    /**
     * @param template
     */
    public L2DooropenInstance(int objectID, L2NpcTemplate template)
    {
        super(objectID, template);
    }

    @Override
    public void onBypassFeedback(L2Player player, String command)
    {
        if (command.startsWith("Chat"))
        {
            showChatWindow(player);
            return;
        }
        else if (command.startsWith("open_doors"))
        {
            DoorTable doorTable = DoorTable.getInstance();
            StringTokenizer st = new StringTokenizer(command.substring(10), ", ");

            while (st.hasMoreTokens())
            {
                int _doorid = Integer.parseInt(st.nextToken());
                doorTable.getDoor(_doorid).openMe();
            }
            return;

        }
        else
            super.onBypassFeedback(player, command);
    }

	@Override
    public final void showChatWindow(L2Player player)
    {
        //player.sendPacket(new ActionFailed());
        String filename = "data/npc_data/html/dooropen/" + getTemplate().getNpcId() + ".htm";

        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);

        html.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(html);
    }
}
