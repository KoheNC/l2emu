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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.events.global.sevensigns.SevenSigns;
import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.manager.AutoSpawnManager;
import net.l2emuproject.gameserver.manager.AutoSpawnManager.AutoSpawnInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;



/**
 * Admin Command Handler for Mammon NPCs
 * 
 * @author Tempy
 */
public class AdminMammon implements IAdminCommandHandler
{

	private static final String[]	ADMIN_COMMANDS		=
														{ "admin_mammon_find", "admin_mammon_respawn", "admin_list_spawns", "admin_msg" };

	private final boolean					_isSealValidation	= SevenSigns.getInstance().isSealValidationPeriod();

	@Override
	public boolean useAdminCommand(String command, L2Player activeChar)
	{
		int npcId = 0;
		int teleportIndex = -1;
		AutoSpawnInstance blackSpawnInst = AutoSpawnManager.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_BLACKSMITH_ID, false);
		AutoSpawnInstance merchSpawnInst = AutoSpawnManager.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_MERCHANT_ID, false);

		if (command.startsWith("admin_mammon_find"))
		{
			try
			{
				if (command.length() > 17)
					teleportIndex = Integer.parseInt(command.substring(18));
			}
			catch (Exception NumberFormatException)
			{
				activeChar.sendMessage("Usage: //mammon_find [teleportIndex] (where 1 = Blacksmith, 2 = Merchant)");
			}

			if (!_isSealValidation)
			{
				activeChar.sendMessage("The competition period is currently in effect.");
				return true;
			}
			if (blackSpawnInst != null)
			{
				L2Npc[] blackInst = blackSpawnInst.getNPCInstanceList();
				if (blackInst.length > 0)
				{
					int x1 = blackInst[0].getX(), y1 = blackInst[0].getY(), z1 = blackInst[0].getZ();
					activeChar.sendMessage("Blacksmith of Mammon: " + x1 + " " + y1 + " " + z1);
					if (teleportIndex == 1)
						activeChar.teleToLocation(x1, y1, z1, true);
				}
			}
			else
				activeChar.sendMessage("Blacksmith of Mammon isn't registered for spawn.");
			if (merchSpawnInst != null)
			{
				L2Npc[] merchInst = merchSpawnInst.getNPCInstanceList();
				if (merchInst.length > 0)
				{
					int x2 = merchInst[0].getX(), y2 = merchInst[0].getY(), z2 = merchInst[0].getZ();
					activeChar.sendMessage("Merchant of Mammon: " + x2 + " " + y2 + " " + z2);
					if (teleportIndex == 2)
						activeChar.teleToLocation(x2, y2, z2, true);
				}
			}
			else
				activeChar.sendMessage("Merchant of Mammon isn't registered for spawn.");
		}

		else if (command.startsWith("admin_mammon_respawn"))
		{
			if (!_isSealValidation)
			{
				activeChar.sendMessage("The competition period is currently in effect.");
				return true;
			}
			if (merchSpawnInst != null)
			{
				long merchRespawn = AutoSpawnManager.getInstance().getTimeToNextSpawn(merchSpawnInst);
				activeChar.sendMessage("The Merchant of Mammon will respawn in " + (merchRespawn / 60000) + " minute(s).");
			}
			else
				activeChar.sendMessage("Merchant of Mammon isn't registered for spawn.");
			if (blackSpawnInst != null)
			{
				long blackRespawn = AutoSpawnManager.getInstance().getTimeToNextSpawn(blackSpawnInst);
				activeChar.sendMessage("The Blacksmith of Mammon will respawn in " + (blackRespawn / 60000) + " minute(s).");
			}
			else
				activeChar.sendMessage("Blacksmith of Mammon isn't registered for spawn.");
		}

		else if (command.startsWith("admin_list_spawns"))
		{
			try
			{ // admin_list_spawns x[xxxx] x[xx]
				String[] params = command.split(" ");
				Pattern pattern = Pattern.compile("[0-9]*");
				Matcher regexp = pattern.matcher(params[1]);
				if (regexp.matches())
					npcId = Integer.parseInt(params[1]);
				else
				{
					params[1] = params[1].replace('_', ' ');
					npcId = NpcTable.getInstance().getTemplateByName(params[1]).getNpcId();
				}
				if (params.length > 2)
					teleportIndex = Integer.parseInt(params[2]);
			}
			catch (Exception e)
			{
				activeChar.sendPacket(SystemMessage.sendString("Command format is //list_spawns <npcId|npc_name> [tele_index]"));
			}

			SpawnTable.getInstance().findNPCInstances(activeChar, npcId, teleportIndex);
		}

		// Used for testing SystemMessage IDs	- Use //msg <ID>
		else if (command.startsWith("admin_msg"))
		{
			int msgId = -1;

			try
			{
				msgId = Integer.parseInt(command.substring(10).trim());
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Command format: //msg <SYSTEM_MSG_ID>");
				return true;
			}
			activeChar.sendPacket(SystemMessageId.getSystemMessageId(msgId));
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
