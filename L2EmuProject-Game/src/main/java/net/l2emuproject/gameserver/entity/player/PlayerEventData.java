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
package net.l2emuproject.gameserver.entity.player;

import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author lord_rex
 */
public final class PlayerEventData extends PlayerExtension
{
	private int	_points;
	private int	_deathPoints;

	public PlayerEventData(L2Player activeChar)
	{
		super(activeChar);
	}

	public final void setPoints(int points)
	{
		_points = points;
	}

	public final void givePoints(int points)
	{
		setPoints(getPoints() + points);
	}

	public final int getPoints()
	{
		return _points;
	}

	public final void setDeathPoints(int deathPoints)
	{
		_deathPoints = deathPoints;
	}

	public final void giveDeathPoints(int deathPoints)
	{
		setDeathPoints(getDeathPoints() + deathPoints);
	}

	public final int getDeathPoints()
	{
		return _deathPoints;
	}
}
