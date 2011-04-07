/* This program is free software: you can redistribute it and/or modify it under
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
package ai.zone.hellbound.Shadai;

import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.manager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.system.time.GameTimeController;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.spawn.L2Spawn;
import net.l2emuproject.tools.random.Rnd;

public final class Shadai extends QuestJython
{
	private static final int	SHADAI	= 32347;

	public Shadai(int questId, String name, String descr)
	{
		super(questId, name, descr);

		shadaiSpawn();
	}

	private void shadaiSpawn()
	{
		int initial = (1440 - GameTimeController.getInstance().getGameTime()) * 10000;

		startQuestTimer("shadai_spawn", initial, null, null);
	}

	private L2Npc findTemplate(int npcId)
	{
		L2Npc npc = null;
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
		{
			if (spawn != null && spawn.getNpcId() == npcId)
			{
				npc = spawn.getLastSpawn();
				break;
			}
		}
		return npc;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if (event.equalsIgnoreCase("shadai_spawn"))
		{
			if (HellboundManager.getInstance().getHellboundLevel() >= 1 && Rnd.get(100) <= 33)
			{
				L2Npc shadaiSpawn = findTemplate(SHADAI);
				if (shadaiSpawn == null)
				{
					if (HellboundManager.getInstance().getHellboundLevel() < 9)
						addSpawn(SHADAI, -5704, 256417, -3136, 0, false, 3600000);
					else
						addSpawn(SHADAI, 8962, 253278, -1932, 0, false, 3600000);
					Announcements.getInstance().announceToAll("The Legendary Blacksmith Shadai has just appeared.");
				}
			}
			startQuestTimer("shadai_spawn", 14400000, null, null);
		}

		return null;
	}

	public static void main(String[] args)
	{
		new Shadai(-1, "Shadai", "ai");
	}
}
