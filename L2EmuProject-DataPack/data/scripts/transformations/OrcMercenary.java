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

public class OrcMercenary extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public OrcMercenary()
	{
		// id, colRadius, colHeight
		super(13, 8, 27);
	}

	@Override
	public void transformedSkills(L2Player player)
	{
		{
			addSkill(player, 869, 1); // Mercenary Power Strike
			addSkill(player, 936, 1); // Ordinary Mercenary TODO
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2Player player)
	{
		removeSkill(player, 869); // Mercenary Power Strike
		removeSkill(player, 936); // Ordinary Mercenary
	}
	
	@Override
	public void onTransform(L2Player player)
	{
		player.setCanGainExpSp(false);
		super.onTransform(player);
	}
	
	@Override
	public void onUntransform(L2Player player)
	{
		player.setCanGainExpSp(true);
		super.onUntransform(player);
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new OrcMercenary());
	}
}
