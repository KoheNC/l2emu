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
package net.l2emuproject;

import net.l2emuproject.Config;
import junit.framework.TestCase;

public class TestConfig extends TestCase
{
	/**
	 * test the good loading
	 * @throws Exception 
	 */
	public void testLoadConfig() throws Exception
	{
		try
		{
			Config.load();
		}
		catch (Error e)
		{
			fail(e.getMessage());
		}
	}

	/**
	 * test that db properties are in system properties
	 * @throws Exception 
	 */
	public void testInitDbProperties() throws Exception
	{
		try
		{
			Config.load();
		}
		catch (Error e)
		{
			fail(e.getMessage());
		}
		assertNotNull(System.getProperty("net.l2emuproject.db.driverclass"));
		assertNotNull(System.getProperty("net.l2emuproject.db.urldb"));
		assertNotNull(System.getProperty("net.l2emuproject.db.user"));
		assertNotNull(System.getProperty("net.l2emuproject.db.password"));

	}

}
