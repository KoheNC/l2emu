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
package net.l2emuproject.gameserver.model.actor.instance;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Npc;

public class L2WyvernManagerInstance extends L2Npc
{
    public L2WyvernManagerInstance (int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		String filename = "data/html/wyvernmanager/fortress-wyvernmanager-no.htm";
		
		if (isCastleManager())
			filename = "data/html/wyvernmanager/castle-wyvernmanager-no.htm";

		if (isOwnerClan(player))
		{
			if (isCastleManager())
				filename = "data/html/wyvernmanager/castle-wyvernmanager.htm";    // Castle Owner message window
			else
				filename = "data/html/wyvernmanager/fortress-wyvernmanager.htm";  // Fort Owner message window
		}
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%count%", String.valueOf(Config.ALT_MANAGER_CRYSTAL_COUNT));
		player.sendPacket(html);
	}

	public boolean isOwnerClan(L2PcInstance player)
	{
		return true;
	}
	
	public boolean isCastleManager()
	{
		int npcId = getNpcId();
		
		if (npcId >= 36457 && npcId <= 36477)
			return false;

		return true;
	}
}
