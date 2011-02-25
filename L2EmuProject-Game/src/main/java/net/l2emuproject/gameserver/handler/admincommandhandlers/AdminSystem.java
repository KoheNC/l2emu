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
import net.l2emuproject.gameserver.services.SystemService;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.lang.L2Thread;

/**
 * @author lord_rex
 */
public class AdminSystem implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{
			"admin_memory",
			"admin_gc",
			"admin_system_time",
			"admin_os_info",
			"admin_cpu_info",
			"admin_runtime_info",
			"admin_jre_info",
			"admin_jvm_info"						};

	@Override
	public boolean useAdminCommand(String command, L2Player activeChar)
	{
		if (command.equalsIgnoreCase("admin_memory"))
		{
			for (String line : L2Thread.getMemoryUsageStatistics())
				activeChar.sendMessage(line);
		}
		else if (command.equalsIgnoreCase("admin_gc"))
		{
			activeChar.sendMessage("Before GC and Finalization:");
			for (String line : L2Thread.getMemoryUsageStatistics())
				activeChar.sendMessage(line);

			System.gc();
			System.runFinalization();

			activeChar.sendMessage("After GC and Finalization:");
			for (String line : L2Thread.getMemoryUsageStatistics())
				activeChar.sendMessage(line);
		}
		else if (command.equalsIgnoreCase("admin_system_time"))
		{
			for (String line : SystemService.getSystemTime())
				activeChar.sendMessage(line);
		}
		else if (command.equalsIgnoreCase("admin_os_info"))
		{
			for (String line : SystemService.getOSInfo())
				activeChar.sendMessage(line);
		}
		else if (command.equalsIgnoreCase("admin_cpu_info"))
		{
			for (String line : SystemService.getCPUInfo())
				activeChar.sendMessage(line);
		}
		else if (command.equalsIgnoreCase("admin_runtime_info"))
		{
			for (String line : SystemService.getRuntimeInfo())
				activeChar.sendMessage(line);
		}
		else if (command.equalsIgnoreCase("admin_jre_info"))
		{
			for (String line : SystemService.getJREInfo())
				activeChar.sendMessage(line);
		}
		else if (command.equalsIgnoreCase("admin_jvm_info"))
		{
			for (String line : SystemService.getJVMInfo())
				activeChar.sendMessage(line);
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
