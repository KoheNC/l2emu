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
package net.l2emuproject.gameserver.world;

import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.tools.random.Rnd;

public final class Location
{
	public static final Location	EMPTY_LOCATION	= new Location(0, 0, 0, 0);

	private final int				_x;
	private final int				_y;
	private final int				_z;
	private final int				_rndX;
	private final int				_rndY;
	private final int				_heading;

	public Location(int x, int y, int z)
	{
		this(x, y, z, 0);
	}
	
	public Location(int x, int y, int z, int rndX, int rndY)
	{
		_x = x;
		_y = y;
		_z = z;
		_rndX = rndX;
		_rndY = rndY;
		_heading = 0;
	}

	public Location(int x, int y, int z, int heading)
	{
		_x = x;
		_y = y;
		_z = z;
		_rndX = 0;
		_rndY = 0;
		_heading = heading;
	}

	public Location(L2Character cha)
	{
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_rndX = 0;
		_rndY = 0;
		_heading = cha.getHeading();
	}

	public int getX()
	{
		if (_rndX == 0)
			return _x;
		return (_x + Rnd.get(_rndX));
	}

	public int getY()
	{
		if (_rndY == 0)
			return _y;
		return (_y + Rnd.get(_rndY));
	}

	public int getZ()
	{
		return _z;
	}

	public int getHeading()
	{
		return _heading;
	}
	
	public boolean isEmpty()
	{
		if (_x == 0 && _y == 0 && _z == 0 && _heading == 0)
			return true;
		return false;
	}
}
