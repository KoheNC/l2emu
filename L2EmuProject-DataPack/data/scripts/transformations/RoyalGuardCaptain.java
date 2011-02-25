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

public class RoyalGuardCaptain extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public RoyalGuardCaptain()
	{
		// id, colRadius, colHeight
		super(16, 12, 24);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		{
			addSkill(player, 870, 1); // Royal Guard Captain Power Strike
			addSkill(player, 871, 1); // Royal Guard Captain Might
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 870); // Royal Guard Captain Power Strike
		removeSkill(player, 871); // Royal Guard Captain Might
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new RoyalGuardCaptain());
	}
}
