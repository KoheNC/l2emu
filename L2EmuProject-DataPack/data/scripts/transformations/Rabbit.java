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

public class Rabbit extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public Rabbit()
	{
		// id, colRadius, colHeight
		super(105, 5, 4.5);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		{
			addSkill(player, 629, 1); // Rabbit Magic Eye
			addSkill(player, 630, 1); // Rabbit Tornado
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 629); // Rabbit Magic Eye
		removeSkill(player, 630); // Rabbit Tornado
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new Rabbit());
	}
}
