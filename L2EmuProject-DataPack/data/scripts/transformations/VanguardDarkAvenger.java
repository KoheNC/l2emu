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

public class VanguardDarkAvenger extends L2Transformation
{
	private static final int[] SKILLS = new int[]{18, 28, 65, 86, 283, 401};

	public VanguardDarkAvenger()
	{
		// id
		super(313);
	}

	@Override
	public void transformedSkills(L2Player player)
	{
		if (player.getLevel() > 43)
		{
			int level = player.getLevel() - 43;
			addSkill(player, 144, level); // Dual Weapon Mastery
			addSkill(player, 815, level); // Blade Hurricane
			addSkill(player, 817, level); // Double Strike
		}

		if (player.getLevel() > 48)
		{
			int level = player.getLevel() - 48;
			addSkill(player, 958, level); // Triple Blade Slash
		}

		int level = -1;
		if (player.getLevel() >= 73)
			level = 3;
		else if (player.getLevel() >= 65)
			level = 2;
		else if (player.getLevel() >= 57)
			level = 1;
		{
			addSkill(player, 956, level); // Boost Morale
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2Player player)
	{
		removeSkill(player, 144); // Dual Weapon Mastery
		removeSkill(player, 815); // Blade Hurricane
		removeSkill(player, 817); // Double Strike
		removeSkill(player, 956); // Boost Morale
		removeSkill(player, 958); // Triple Blade Slash
	}
	
	@Override
	public boolean hidesActionButtons()
	{
		return false;
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new VanguardDarkAvenger());
	}
}
