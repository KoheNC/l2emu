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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Stake
 */
@SuppressWarnings("unchecked")
public final class L2ArrayUtil<T> extends ArrayList<T>
{
	private static final long serialVersionUID = 1L;
	
	public L2ArrayUtil()
	{
		super();
	}
	
	public L2ArrayUtil(final Collection<T> c)
	{
		super(c);
	}
	
	public L2ArrayUtil(final int cap)
	{
		super(cap);
	}
	
	public L2ArrayUtil(final T[] array)
	{
		for(int i=0;i<array.length;i++)
			add(array[i]);
	}
	
	public L2ArrayUtil(final T[][] array)
	{
		final int dimensions_length = Array.getLength(array[0]);
		final Object[] holder = new Object[dimensions_length];
		for(int i=0;i<array.length;i++)
		{
			for(int j=0;j<dimensions_length;j++)
			{
				holder[j] = array[i][j];
			}
			add((T) holder[i%dimensions_length]);
		}
	}
}
