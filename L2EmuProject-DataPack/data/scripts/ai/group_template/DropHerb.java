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
package ai.group_template;

/**
 * @author L0ngh0rn
 */
public final class DropHerb
{
	private final int	_itemId;
	private final int	_chance;
	private final int	_min;
	private final int	_max;

	public DropHerb(int itemId, int chance, int min, int max)
	{
		_itemId = itemId;
		_chance = chance;
		_min = min;
		_max = max;
	}

	public final int getItemId()
	{
		return _itemId;
	}

	public final int getChance()
	{
		return _chance;
	}

	public final int getMin()
	{
		return _min;
	}

	public final int getMax()
	{
		return _max;
	}
}
