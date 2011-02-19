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
package net.l2emuproject.gameserver.skills.l2skills;

import net.l2emuproject.gameserver.model.skill.L2Skill;
import net.l2emuproject.gameserver.templates.StatsSet;

public class L2SkillSweep extends L2Skill
{
	private final int	_absorbAbs;

	public L2SkillSweep(StatsSet set)
	{
		super(set);

		_absorbAbs = set.getInteger("absorbAbs", 0);
	}

	public int getAbsorbAbs()
	{
		return _absorbAbs;
	}
}
