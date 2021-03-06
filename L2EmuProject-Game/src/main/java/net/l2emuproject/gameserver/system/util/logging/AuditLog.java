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
package net.l2emuproject.gameserver.system.util.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;

import javolution.text.TextBuilder;
import net.l2emuproject.util.logging.L2LogFilter;
import net.l2emuproject.util.logging.L2LogFormatter;


/**
 * @author NB4L1
 */
public final class AuditLog
{
	private AuditLog()
	{
	}
	
	public static final class Handler extends FileHandler
	{
		static
		{
			new File("log/audit").mkdirs();
		}
		
		public Handler() throws IOException, SecurityException
		{
			super();
		}
	}
	
	public static final class Filter extends L2LogFilter
	{
		@Override
		protected String getLoggerName()
		{
			return "audit";
		}
	}
	
	public static final class Formatter extends L2LogFormatter
	{
		@Override
		protected void format0(LogRecord record, TextBuilder tb)
		{
			appendDate(record, tb);
			appendMessage(record, tb);
			appendParameters(record, tb, ", ", true);
		}
	}
}
