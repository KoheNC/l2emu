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

public final class DarkElfMercenary extends L2Transformation
{
	public DarkElfMercenary()
	{
		// id, colRadius, colHeight
		super(12, 8, 25);
	}

	@Override
	public final void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 869, 1); // Mercenary Power Strike
		addSkill(player, 936, 1); // Ordinary Mercenary TODO
	}

	@Override
	public final void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 869); // Mercenary Power Strike
		removeSkill(player, 936); // Ordinary Mercenary
	}

	@Override
	public final void onTransform(L2PcInstance player)
	{
		player.setCanGainExpSp(false);
		super.onTransform(player);
	}

	@Override
	public final void onUntransform(L2PcInstance player)
	{
		player.setCanGainExpSp(true);
		super.onUntransform(player);
	}

	public static void main(String[] args)
	{
		TransformationService.getInstance().registerTransformation(new DarkElfMercenary());
	}
}
