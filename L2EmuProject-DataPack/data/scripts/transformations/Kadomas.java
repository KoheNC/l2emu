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

public class Kadomas extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public Kadomas()
	{
		// id, colRadius, colHeight
		super(20000, 24.5, 14);
	}

	@Override
	public void transformedSkills(L2Player player)
	{
		addSkill(player, 23154, 1); // Kadomas Special Skill - Fireworks

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2Player player)
	{
		removeSkill(player, 23154); // Kadomas Special Skill - Fireworks
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new Kadomas());
	}
}
