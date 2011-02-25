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

public class DivineHealer extends L2Transformation
{
	private static final int[] SKILLS = new int[]{648, 803, 1490};

	public DivineHealer()
	{
		// id, colRadius, colHeight
		super(255, 10, 25);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		{
			addSkill(player, 698, 1); // Divine Healer Major Heal
			addSkill(player, 699, 1); // Divine Healer Battle Heal
			addSkill(player, 700, 1); // Divine Healer Group Heal
			addSkill(player, 701, 1); // Divine Healer Resurrection
			addSkill(player, 702, 1); // Divine Healer Cleanse
			addSkill(player, 703, 1); // Sacrifice Healer
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 698); // Divine Healer Major Heal
		removeSkill(player, 699); // Divine Healer Battle Heal
		removeSkill(player, 700); // Divine Healer Group Heal
		removeSkill(player, 701); // Divine Healer Resurrection
		removeSkill(player, 702); // Divine Healer Cleanse
		removeSkill(player, 703); // Sacrifice Healer
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new DivineHealer());
	}
}
