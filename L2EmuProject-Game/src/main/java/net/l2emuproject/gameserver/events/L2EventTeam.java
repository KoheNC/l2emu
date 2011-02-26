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
package net.l2emuproject.gameserver.events;

import java.util.Map;

import net.l2emuproject.gameserver.world.object.L2Player;

import javolution.util.FastMap;

/**
 * @author lord_rex
 */
public final class L2EventTeam
{
	private final Map<Integer, L2Player>	_players;

	private final String						_teamName;
	private final int							_color;
	private int									_points;
	private final int[]							_coords;

	public L2EventTeam(String teamName, int color, int[] coords)
	{
		_players = new FastMap<Integer, L2Player>().shared();
		_teamName = teamName;
		_color = color;
		_coords = coords;
		_points = 0;
	}

	public final String getTeamName()
	{
		return _teamName;
	}

	public final void addPlayer(L2Player player)
	{
		if (player == null)
			return;

		_players.put(player.getObjectId(), player);
	}

	public final void removePlayer(L2Player player)
	{
		_players.remove(player.getObjectId());
	}

	public final boolean containsPlayer(L2Player player)
	{
		return _players.containsValue(player);
	}

	public final int getPlayersCount()
	{
		return _players.size();
	}

	public final void clear()
	{
		_players.clear();
	}

	public final int getColor()
	{
		return _color;
	}

	public int[] getCoords()
	{
		return _coords;
	}

	private final void setPoints(int points)
	{
		_points = points;
	}

	public final int getPoints()
	{
		return _points;
	}

	public final void addPoint()
	{
		setPoints(getPoints() + 1);
	}

	public final void removePoint()
	{
		setPoints(getPoints() - 1);
	}
}