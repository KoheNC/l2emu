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

public final class Treykan extends L2Transformation
{
	private static final int[]	SKILLS	= new int[]
										{ 619 };

	public Treykan()
	{
		// id, colRadius, colHeight
		super(126, 25, 27.00);
	}

	@Override
	protected final void transformedSkills(L2PcInstance player)
	{
		// Transform Dispel
		addSkill(player, 619, 1);

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	protected final void removeSkills(L2PcInstance player)
	{
		// Transform Dispel
		removeSkill(player, 619);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Treykan());
	}
}
