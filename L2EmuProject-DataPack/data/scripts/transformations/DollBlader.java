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

import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.services.transformation.L2Transformation;
import net.l2emuproject.gameserver.services.transformation.TransformationService;

public class DollBlader extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public DollBlader()
	{
		// id, colRadius, colHeight
		super(7, 6, 12);
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
			addSkill(player, 752, level); // Doll Blader Sting (3 levels)
			addSkill(player, 753, level); // Doll Blader Throwing Knife (3 levels)
			addSkill(player, 754, 1);	  // Doll Blader Clairvoyance
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 752); // Doll Blader Sting (3 levels)
		removeSkill(player, 753); // Doll Blader Throwing Knife (3 levels)
		removeSkill(player, 754); // Doll Blader Clairvoyance
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new DollBlader());
	}
}
