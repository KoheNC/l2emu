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

import net.l2emuproject.gameserver.instancemanager.TransformationManager;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.services.transformation.L2Transformation;

public class InfernoDrakeWeak extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public InfernoDrakeWeak()
	{
		// id, colRadius, colHeight
		super(215, 15, 24);
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
			addSkill(player, 576, level); // Paw Strike (4 levels)
			addSkill(player, 577, level); // Fire Breath (4 levels)
			addSkill(player, 578, level); // Blaze Quake (4 levels)
			addSkill(player, 579, level); // Fire Armor (4 levels)
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 576); // Paw Strike (4 levels)
		removeSkill(player, 577); // Fire Breath (4 levels)
		removeSkill(player, 578); // Blaze Quake (4 levels)
		removeSkill(player, 579); // Fire Armor (4 levels)
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new InfernoDrakeWeak());
	}
}
