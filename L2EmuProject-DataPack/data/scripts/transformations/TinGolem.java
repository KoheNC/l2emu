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

public class TinGolem extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public TinGolem()
	{
		// id, colRadius, colHeight
		super(116, 13, 18.5);
	}

	@Override
	public void transformedSkills(L2Player player)
	{
		{
			addSkill(player, 940, 1); // Fake Attack
			addSkill(player, 942, 1); // Special Motion
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2Player player)
	{
		removeSkill(player, 940); // Fake Attack
		removeSkill(player, 941); // Special Motion
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new TinGolem());
	}
}
