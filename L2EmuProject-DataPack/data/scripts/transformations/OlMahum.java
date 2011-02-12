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
package transformations;

import net.l2emuproject.gameserver.instancemanager.TransformationManager;
import net.l2emuproject.gameserver.model.L2Transformation;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

public class OlMahum extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public OlMahum()
	{
		// id, colRadius, colHeight
		super(6, 23, 61);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		int level = -1;
		if (player.getLevel() >= 76)
			level = 3;
		else if (player.getLevel() >= 73)
			level = 2;
		else if (player.getLevel() >= 70)
			level = 1;
		{
			addSkill(player, 749, level); // Oel Mahum Stun Attack (3 levels)
			addSkill(player, 750, 1);     // Oel Mahum Ultimate Defense
			addSkill(player, 751, level); // Oel Mahum Arm Flourish (3 levels)
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 749); // Oel Mahum Stun Attack (3 levels)
		removeSkill(player, 750); // Oel Mahum Ultimate Defense
		removeSkill(player, 751); // Oel Mahum Arm Flourish (3 levels)
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new OlMahum());
	}
}
