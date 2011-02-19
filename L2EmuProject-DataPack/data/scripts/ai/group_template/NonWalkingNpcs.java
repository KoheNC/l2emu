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
package ai.group_template;

import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.spawn.L2Spawn;
import ai.group_template.L2AttackableAIScript;

public final class NonWalkingNpcs extends L2AttackableAIScript
{
	private final static int[]	NO_RND_WALK_MOBS	=
													{ 
			18343, // Gatekeeper Zombie
			18345, // Sprigant
			18367, // Prison Guard
			18368, // Prison Guard
			22138, // Chapel Guard
			27384, // 5th epic quest Seal devices
													};

	public NonWalkingNpcs(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (L2Spawn npc : SpawnTable.getInstance().getSpawnTable().values())
		{
			if (contains(NO_RND_WALK_MOBS, npc.getNpcId()))
			{
				npc.getLastSpawn().setIsNoRndWalk(true);
				npc.getLastSpawn().setIsImmobilized(true);
			}
		}

		for (int npcid : NO_RND_WALK_MOBS)
		{
			addSpawnId(npcid);
		}
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (contains(NO_RND_WALK_MOBS, npc.getNpcId()))
		{
			npc.setIsNoRndWalk(true);
			npc.setIsImmobilized(true);
		}

		return "";
	}

	public static void main(String[] args)
	{
		new NonWalkingNpcs(-1, "NonWalkingNpcs", "ai");
	}
}