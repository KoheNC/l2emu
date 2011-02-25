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

public class UnicornWeak extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public UnicornWeak()
	{
		// id, colRadius, colHeight
		super(206, 15, 28);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
			int level = -1;
			if (player.getLevel() >= 60)
				level = 2;
			else if (player.getLevel() >= 1)
				level = 1;
		{
			addSkill(player, 563, level); // Horn of Doom (4 levels)
			addSkill(player, 564, level); // Gravity Control (4 levels)
			addSkill(player, 565, level); // Horn Assault (4 levels)
			addSkill(player, 567, level); // Light of Heal (4 levels)
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 563); // Horn of Doom (4 levels)
		removeSkill(player, 564); // Gravity Control (4 levels)
		removeSkill(player, 565); // Horn Assault (4 levels)
		removeSkill(player, 567); // Light of Heal (4 levels)
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new UnicornWeak());
	}
}
