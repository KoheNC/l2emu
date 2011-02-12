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
package net.l2emuproject.gameserver.templates;

import java.util.Map;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author NB4L1
 */
public class ValidatingStatsSet extends StatsSet
{
	private static final Log _log = LogFactory.getLog(ValidatingStatsSet.class);
	
	private final Map<String, Object> _nonRequestedStats = new FastMap<String, Object>();
	
	private String _description;
	private boolean _validating = true;
	
	public ValidatingStatsSet setDescription(String description)
	{
		_description = description;
		return this;
	}
	
	public ValidatingStatsSet setValidating(boolean validating)
	{
		_validating = validating;
		return this;
	}
	
	@Override
	protected Object get(String key)
	{
		_nonRequestedStats.remove(key);
		
		return super.get(key);
	}
	
	@Override
	protected Object put(String key, Object value)
	{
		_nonRequestedStats.put(key, value);
		
		final Object previousValue = super.put(key, value);
		
		// L2EMU_EDIT
		if (previousValue != null)
			if (_validating)
				_log.warn(_description + ": '" + key + "' has been replaced {" + previousValue + " -> " + value + "}!");
		// L2EMU_EDIT
		
		return previousValue;
	}
	
	@Override
	public void clear()
	{
		for (Map.Entry<String, Object> entry : _nonRequestedStats.entrySet())
		{
			// L2EMU_EDIT
			_log.warn(_description + ": non-requested stat {" + entry.getKey() + " => " + entry.getValue() + "}!");
			// L2EMU_EDIT
		}
		
		_nonRequestedStats.clear();
		
		super.clear();
	}
	
	@Override
	protected void finalize()
	{
		clear();
	}
}
