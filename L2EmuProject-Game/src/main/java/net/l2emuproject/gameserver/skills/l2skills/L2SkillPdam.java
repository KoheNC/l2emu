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

import net.l2emuproject.gameserver.model.L2Skill;
import net.l2emuproject.gameserver.templates.StatsSet;

/**
 * @author NB4L1
 */
public final class L2SkillPdam extends L2Skill
{
	private final int _numberOfHits;
	
	public L2SkillPdam(StatsSet set)
	{
		super(set);
		
		_numberOfHits = set.getInteger("numberOfHits", 1);
	}
	
	public int getNumberOfHits()
	{
		return _numberOfHits;
	}
}
