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

public class GatekeeperAlternate extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public GatekeeperAlternate()
	{
		// id, colRadius, colHeight
		super(107, 8, 25);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		{
		addSkill(player, 5656, player.getLevel()); // Gatekeeper Aura Flare
		addSkill(player, 5657, player.getLevel()); // Gatekeeper Prominence
		addSkill(player, 5658, player.getLevel()); // Gatekeeper Flame Strike
		addSkill(player, 5659, 2); // Gatekeeper Berserker Spirit
		}

		player.getPlayerTransformation().addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 5656); // Gatekeeper Aura Flare
		removeSkill(player, 5657); // Gatekeeper Prominence
		removeSkill(player, 5658); // Gatekeeper Flame Strike
		removeSkill(player, 5659); // Gatekeeper Berserker Spirit
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new GatekeeperAlternate());
	}
}
