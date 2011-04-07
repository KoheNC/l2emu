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

public final class VehiclePathPoint
{
	private final int	x;
	private final int	y;
	private final int	z;
	private final int	moveSpeed;
	private final int	rotationSpeed;

	public VehiclePathPoint(int _x, int _y, int _z)
	{
		x = _x;
		y = _y;
		z = _z;
		moveSpeed = 350;
		rotationSpeed = 4000;
	}

	public VehiclePathPoint(int _x, int _y, int _z, int _m, int _r)
	{
		x = _x;
		y = _y;
		z = _z;
		moveSpeed = _m;
		rotationSpeed = _r;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getZ()
	{
		return z;
	}

	public int getMoveSpeed()
	{
		return moveSpeed;
	}

	public int getRotationSpeed()
	{
		return rotationSpeed;
	}
}
