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

import net.l2emuproject.gameserver.services.transformation.L2Transformation;
import net.l2emuproject.gameserver.services.transformation.TransformationService;
import net.l2emuproject.gameserver.world.object.L2Player;

public class GolemGuardianStrong extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public GolemGuardianStrong()
	{
		// id, colRadius, colHeight
		super(210, 13, 25);
	}
	
	@Override
	public void transformedSkills(L2Player player)
	{
			int level = -1;
			if (player.getLevel() >= 60)
				level = 4;
			else if (player.getLevel() >= 1)
				level = 1;
		{
			addSkill(player, 572, level); // Double Slasher (4 levels)
			addSkill(player, 573, level); // Earthquake (4 levels)
			addSkill(player, 574, level); // Bomb Installation (4 levels)
			addSkill(player, 575, level); // Steel Cutter (4 levels)
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}
	
	@Override
	public void removeSkills(L2Player player)
	{
		removeSkill(player, 572); // Double Slasher (4 levels)
		removeSkill(player, 573); // Earthquake (4 levels)
		removeSkill(player, 574); // Bomb Installation (4 levels)
		removeSkill(player, 575); // Steel Cutter (4 levels)
	}
	
	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new GolemGuardianStrong());
	}
}
