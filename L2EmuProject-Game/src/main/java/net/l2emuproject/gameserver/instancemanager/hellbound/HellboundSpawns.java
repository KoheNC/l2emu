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
package net.l2emuproject.gameserver.instancemanager.hellbound;

import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.spawn.L2Spawn;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author Gigiikun, L2JNightFall Team
 *	<br>Remade for L2EmuProject by lord_rex
 */
public final class HellboundSpawns extends L2Spawn
{
	private int	_minLevel;
	private int	_maxLevel;
	private int	_trustPoints;

	public HellboundSpawns(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException
	{
		super(mobTemplate);
	}

	public final int getMinLevel()
	{
		return _minLevel;
	}

	public final void setMinLevel(int level)
	{
		_minLevel = level;
	}

	public final int getMaxLevel()
	{
		return _maxLevel;
	}

	public final void setMaxLevel(int level)
	{
		_maxLevel = level;
	}

	public final void setTrustPoints(int points)
	{
		_trustPoints = points;
	}

	@Override
	public final void decreaseCount(L2Npc oldNpc)
	{
		if (getRespawnMaxDelay() > getRespawnMinDelay())
			setRespawnDelay(Rnd.get(getRespawnMinDelay(), getRespawnMaxDelay()));

		super.decreaseCount(oldNpc);
		HellboundManager.getInstance().addTrustPoints(_trustPoints);
	}
}
