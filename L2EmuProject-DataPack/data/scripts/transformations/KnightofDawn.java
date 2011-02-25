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

public class KnightofDawn extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public KnightofDawn()
	{
		// id, colRadius, colHeight
		super(20, 12, 24.5);
	}

	@Override
	public void transformedSkills(L2Player player)
	{
		{
		addSkill(player, 878, 1); // Knight of Dawn Power Strike
		addSkill(player, 879, 1); // Knight of Dawn Curse Fear
		addSkill(player, 880, 1); // Knight of Dawn Ultimate Defense
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2Player player)
	{
		removeSkill(player, 878); // Knight of Dawn Power Strike
		removeSkill(player, 879); // Knight of Dawn Curse Fear
		removeSkill(player, 880); // Knight of Dawn Ultimate Defense
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new KnightofDawn());
	}
}
