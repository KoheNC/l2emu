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
package net.l2emuproject.gameserver.services;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <b> This Class Returns The Following Infos About System: </b><br><br>
 * 
 * <li> CPU Info<br>
 * <li> System OS <br>
 * <li> System Architecture <br>
 * <li> System JRE <br>
 * <li> Mahcine Info <br>
 * <li> JVM Info <br><br>
 * 
 * @author Rayan 
 * @project L2Emu Project
 * @since 512
 */
public final class SystemService
{
	private static final Log	_log	= LogFactory.getLog(SystemService.class);

	private SystemService()
	{
	}

	/**
	 * Returns how many processors are installed on this system.
	 */
	public static final String[] getCPUInfo()
	{
		return new String[] {
				"Avaible CPU(s): " + Runtime.getRuntime().availableProcessors(),
				"Processor(s) Identifier: " + System.getenv("PROCESSOR_IDENTIFIER"),
				"..................................................",
				".................................................." 
		};
	}

	/**
	 * Returns the operational system server is running on it.
	 */
	public static final String[] getOSInfo()
	{
		return new String[] {
				"OS: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version"),
				"OS Arch: " + System.getProperty("os.arch"),
				"..................................................",
				".................................................." 
		};
	}

	/**
	 * Returns JAVA Runtime Enviroment properties
	 */
	public static final String[] getJREInfo()
	{
		return new String[] {
				"Java Platform Information",
				"Java Runtime  Name: " + System.getProperty("java.runtime.name"),
				"Java Version: " + System.getProperty("java.version"),
				"Java Class Version: " + System.getProperty("java.class.version"),
				"..................................................",
				".................................................." 
		};
	}

	/**
	 * Returns general infos related to machine
	 */
	public static final String[] getRuntimeInfo()
	{
		return new String[] {
				"Runtime Information",
				"Current Free Heap Size: " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + " mb",
				"Current Heap Size: " + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + " mb",
				"Maximum Heap Size: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " mb",
				"..................................................",
				".................................................." 
		};
	}

	/**
	 * Calls time service to get system time.
	 */
	public static final String[] getSystemTime()
	{
		// instanciates Date Objec
		Date dateInfo = new Date();

		//generates a simple date format
		SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa");

		//generates String that will get the formater info with values
		String dayInfo = df.format(dateInfo);

		return new String[] { 
				"..................................................", 
				"System Time: " + dayInfo, 
				".................................................." 
		};
	}

	/**
	 * Gets system JVM properties.
	 */
	public static final String[] getJVMInfo()
	{
		return new String[] {
				"Virtual Machine Information (JVM)",
				"JVM Name: " + System.getProperty("java.vm.name"),
				"JVM installation directory: " + System.getProperty("java.home"),
				"JVM version: " + System.getProperty("java.vm.version"),
				"JVM Vendor: " + System.getProperty("java.vm.vendor"),
				"JVM Info: " + System.getProperty("java.vm.info"),
				"..................................................",
				".................................................." 
		};
	}

	/**
	 * Prints all other methods.
	 */
	public static final void printGeneralSystemInfo()
	{
		for (String line : getSystemTime())
			_log.info(line);

		for (String line : getOSInfo())
			_log.info(line);

		for (String line : getCPUInfo())
			_log.info(line);

		for (String line : getRuntimeInfo())
			_log.info(line);

		for (String line : getJREInfo())
			_log.info(line);

		for (String line : getJVMInfo())
			_log.info(line);
	}
}
