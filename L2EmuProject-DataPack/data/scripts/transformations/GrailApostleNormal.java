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

public class GrailApostleNormal extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public GrailApostleNormal()
	{
		// id, colRadius, colHeight
		super(202, 8, 30);
	}

	@Override
	public void transformedSkills(L2Player player)
	{
			int level = -1;
			if (player.getLevel() >= 60)
				level = 3;
			else if (player.getLevel() >= 1)
				level = 1;
		{
			addSkill(player, 559, level); // Spear
			addSkill(player, 560, level); // Power Slash
			addSkill(player, 561, level); // Bless of Angel
			addSkill(player, 562, level); // Wind of Angel
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2Player player)
	{
		removeSkill(player, 559); // Spear
		removeSkill(player, 560); // Power Slash
		removeSkill(player, 561); // Bless of Angel
		removeSkill(player, 562); // Wind of Angel
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new GrailApostleNormal());
	}
}
