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
package net.l2emuproject.gameserver.datatables;

import net.l2emuproject.gameserver.model.L2Skill;

/**
 * @author Gnacik
 */
public final class GMSkillTable
{
	private static final L2Skill[]	GM_SKILLS	= new L2Skill[34];
	private static final int[]		GM_SKILL_IDS	=
												{
			7029,
			7041,
			7042,
			7043,
			7044,
			7045,
			7046,
			7047,
			7048,
			7049,
			7050,
			7051,
			7052,
			7053,
			7054,
			7055,
			7056,
			7057,
			7058,
			7059,
			7060,
			7061,
			7062,
			7063,
			7064,
			7088,
			7089,
			7090,
			7091,
			7092,
			7093,
			7094,
			7095,
			7096								};

	private GMSkillTable()
	{
		for (int i = 0; i < GM_SKILL_IDS.length; i++)
			GM_SKILLS[i] = SkillTable.getInstance().getInfo(GM_SKILL_IDS[i], 1);
	}

	public static L2Skill[] getGMSkills()
	{
		return GM_SKILLS;
	}

	public static boolean isGMSkill(int skillid)
	{
		for (int id : GM_SKILL_IDS)
			if (id == skillid)
				return true;

		return false;
	}
}
