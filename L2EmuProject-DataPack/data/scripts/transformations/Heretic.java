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

public class Heretic extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public Heretic()
	{
		// id, colRadius, colHeight
		super(3, 7.7, 28.4);
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
			addSkill(player, 738, level); // Heretic Heal (3 levels)
			addSkill(player, 739, level); // Heretic Battle Heal (3 levels)
			addSkill(player, 740, level); // Heretic Resurrection (3 levels)
			addSkill(player, 741, level); // Heretic Heal Side Effect (3 levels)
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 738); // Heretic Heal (3 levels)
		removeSkill(player, 739); // Heretic Battle Heal (3 levels)
		removeSkill(player, 740); // Heretic Resurrection (3 levels)
		removeSkill(player, 741); // Heretic Heal Side Effect (3 levels)
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new Heretic());
	}
}
