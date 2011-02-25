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
package net.l2emuproject.gameserver.status.commands;

import net.l2emuproject.gameserver.status.GameStatusCommand;
import net.l2emuproject.gameserver.system.taskmanager.LeakTaskManager;

public final class Clean extends GameStatusCommand
{
	public Clean()
	{
		super("cleans leakmanager mapped objects", "clean");
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		println("================================================================");
		long begin = System.currentTimeMillis();
		LeakTaskManager.getInstance().clean();
		println("'clean' done in " + (System.currentTimeMillis() - begin) + "msec.");
		println("================================================================");
	}
}
