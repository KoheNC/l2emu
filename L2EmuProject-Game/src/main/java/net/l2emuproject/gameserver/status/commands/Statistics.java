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
package net.l2emuproject.gameserver.status.commands;

import net.l2emuproject.gameserver.L2GameServer;
import net.l2emuproject.gameserver.LoginServerThread;
import net.l2emuproject.gameserver.datatables.GmListTable;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.status.GameStatusCommand;
import net.l2emuproject.gameserver.system.time.GameTimeController;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;
import net.l2emuproject.gameserver.world.object.instance.L2DoorInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;

public final class Statistics extends GameStatusCommand
{
	public Statistics()
	{
		super("displays basic server statistics", "status", "stats");
	}
	
	@Override
	protected void useCommand(String command, String params)
	{
		int max = LoginServerThread.getInstance().getMaxPlayer();
		
		int playerCount = L2World.getInstance().getAllPlayersCount();
		int objectCount = L2World.getInstance().getAllVisibleObjectsCount();
		
		int itemCount = 0;
		int itemVoidCount = 0;
		int monsterCount = 0;
		int minionCount = 0;
		int minionsGroupCount = 0;
		int npcCount = 0;
		int charCount = 0;
		int pcCount = 0;
		int detachedCount = 0;
		int doorCount = 0;
		int summonCount = 0;
		int AICount = 0;
		
		for (L2Object obj : L2World.getInstance().getAllVisibleObjects())
		{
			if (obj == null)
				continue;
			if (obj instanceof L2Character)
				if (((L2Character)obj).hasAI())
					AICount++;
			
			if (obj instanceof L2ItemInstance)
			{
				if (((L2ItemInstance)obj).getLocation() == L2ItemInstance.ItemLocation.VOID)
					itemVoidCount++;
				else
					itemCount++;
			}
			else if (obj instanceof L2MonsterInstance)
			{
				monsterCount++;
				minionCount += ((L2MonsterInstance)obj).getTotalSpawnedMinionsInstances();
				minionsGroupCount += ((L2MonsterInstance)obj).getTotalSpawnedMinionsGroups();
			}
			else if (obj instanceof L2Npc)
				npcCount++;
			else if (obj instanceof L2Player)
			{
				pcCount++;
				
				if (((L2Player)obj).isInOfflineMode())
					detachedCount++;
			}
			else if (obj instanceof L2Summon)
				summonCount++;
			else if (obj instanceof L2DoorInstance)
				doorCount++;
			else if (obj instanceof L2Character)
				charCount++;
		}
		
		println("Server Status: ");
		println("  --->  Player Count: " + playerCount + "/" + max);
		println("  ---> Offline Count: " + detachedCount + "/" + playerCount);
		println("  +-->  Object Count: " + objectCount);
		println("  +-->      AI Count: " + AICount);
		println("  +.... L2Item(Void): " + itemVoidCount);
		println("  +.......... L2Item: " + itemCount);
		println("  +....... L2Monster: " + monsterCount);
		println("  +......... Minions: " + minionCount);
		println("  +.. Minions Groups: " + minionsGroupCount);
		println("  +........... L2Npc: " + npcCount);
		println("  +............ L2Pc: " + pcCount);
		println("  +........ L2Summon: " + summonCount);
		println("  +.......... L2Door: " + doorCount);
		println("  +.......... L2Char: " + charCount);
		println("  --->   Ingame Time: " + GameTimeController.getInstance().getFormattedGameTime());
		println("  ---> Server Uptime: " + getUptime(L2GameServer.getStartedTime().getTimeInMillis()));
		println("  --->      GM Count: " + GmListTable.getAllGms(true).size());
		println("  --->       Threads: " + Thread.activeCount());
		println("  RAM Used: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576));
	}
	
	private static String getUptime(long time)
	{
		long uptime = System.currentTimeMillis() - time;
		uptime = uptime / 1000;
		long h = uptime / 3600;
		long m = (uptime - (h * 3600)) / 60;
		long s = ((uptime - (h * 3600)) - (m * 60));
		return h + "hrs " + m + "mins " + s + "secs";
	}
}
