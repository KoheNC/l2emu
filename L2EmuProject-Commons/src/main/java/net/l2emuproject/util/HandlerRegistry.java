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
package net.l2emuproject.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author NB4L1
 */
public class HandlerRegistry<K, V>
{
	protected static final Log _log = LogFactory.getLog(HandlerRegistry.class);
	
	private final Map<K, V> _map;
	
	protected HandlerRegistry(Map<K, V> map)
	{
		_map = map;
	}
	
	public HandlerRegistry(boolean sorted)
	{
		this(sorted ? new TreeMap<K, V>() : new HashMap<K, V>());
	}
	
	public HandlerRegistry()
	{
		this(false);
	}
	
	protected K standardizeKey(K key)
	{
		return key;
	}
	
	public final void register(K key, V handler)
	{
		key = standardizeKey(key);
		V old = _map.put(key, handler);
		
		if (old != null && !old.equals(handler))
			_log.warn(getName() + ": Replaced type(" + key + "), " + old + " -> " + handler + ".");
	}
	
	public final void registerAll(V handler, K... keys)
	{
		for (K key : keys)
			register(key, handler);
	}
	
	public final V get(K key)
	{
		key = standardizeKey(key);
		return _map.get(key);
	}
	
	public final int size()
	{
		return _map.size();
	}
	
	public final Map<K, V> getHandlers()
	{
		return Collections.unmodifiableMap(_map);
	}
	
	protected String getName()
	{
		return getClass().getSimpleName();
	}
	
	public final void clear()
	{
		_map.clear();
	}
}
