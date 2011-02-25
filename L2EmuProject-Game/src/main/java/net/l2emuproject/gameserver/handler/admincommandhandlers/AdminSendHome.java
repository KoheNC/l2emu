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
package net.l2emuproject.gameserver.handler.admincommandhandlers;

import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.mapregion.TeleportWhereType;
import net.l2emuproject.gameserver.world.object.L2Object;

public class AdminSendHome implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_sendhome" };
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_sendhome"))
		{
			L2PcInstance targetPlayer;
			if (command.split(" ").length > 1)
			{
				targetPlayer = L2World.getInstance().getPlayer(command.split(" ")[1]);
			}
			else
			{
				L2Object target = activeChar.getTarget();
				if (target == null)
					targetPlayer = activeChar;
				else
					targetPlayer = L2Object.getActingPlayer(target);
			}
			
			if (targetPlayer != null)
				targetPlayer.teleToLocation(TeleportWhereType.Town);
			else
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
