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

public class Kiyachi extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public Kiyachi()
	{
		// id, colRadius, colHeight
		super(310, 12, 29);
	}

	@Override
	public void transformedSkills(L2Player player)
	{
		{
		addSkill(player, 733, 1); // Kechi Double Cutter
		addSkill(player, 734, 1); // Kechi Air Blade
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2Player player)
	{
		removeSkill(player, 733); // Kechi Double Cutter
		removeSkill(player, 734); // Kechi Air Blade
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new Kiyachi());
	}
}
