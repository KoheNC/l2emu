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
package net.l2emuproject.gameserver.taskmanager.tasks;

import java.io.File;
import java.io.FileNotFoundException;

import javax.script.ScriptException;

import net.l2emuproject.gameserver.scripting.L2ScriptEngineManager;
import net.l2emuproject.gameserver.taskmanager.tasks.TaskManager.ExecutedTask;

/**
 * @author Layane
 */
class TaskCron extends TaskHandler
{	
	@Override
	void onTimeElapsed(ExecutedTask task, String[] params)
	{
		File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, "cron/" + params[2]);

		try 
		{
			L2ScriptEngineManager.getInstance().executeScript(file);
			_log.info("executing cron: data/scripts/cron/" + params[2]);
		} 
		catch (FileNotFoundException e) 
		{
			_log.warn("File Not Found: " + params[2], e);
		}
		catch (ScriptException e) 
		{
			_log.warn("Failed loading: " + params[2], e);
		}
	}
}