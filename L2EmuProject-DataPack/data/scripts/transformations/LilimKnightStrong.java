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
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.services.transformation.L2Transformation;

public class LilimKnightStrong extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public LilimKnightStrong()
	{
		// id, colRadius, colHeight
		super(207, 12, 25.5);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
			int level = -1;
			if (player.getLevel() >= 60)
				level = 4;
			else if (player.getLevel() >= 1)
				level = 1;
		{
			addSkill(player, 568, level); // Attack Buster (4 levels)
			addSkill(player, 569, level); // Attack Storm (4 levels)
			addSkill(player, 570, level); // Attack Rage (4 levels)
			addSkill(player, 571, level); // Poison Dust (4 levels)

			player.clearCharges();
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 568); // Attack Buster (4 levels)
		removeSkill(player, 569); // Attack Storm (4 levels)
		removeSkill(player, 570); // Attack Rage (4 levels)
		removeSkill(player, 571); // Poison Dust (4 levels)
		
		player.clearCharges();
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new LilimKnightStrong());
	}
}
