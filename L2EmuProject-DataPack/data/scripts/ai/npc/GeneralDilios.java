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
package ai.npc;

import java.util.ArrayList;
import java.util.List;

import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.gameserver.network.serverpackets.SocialAction;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.spawn.L2Spawn;
import net.l2emuproject.tools.random.Rnd;
import ai.L2AttackableAIScript;

/**
 * Dilios AI
 * @author JIV, Sephiroth, Apocalipce
 */
public class GeneralDilios extends L2AttackableAIScript
{
	private static final String		QN			= "GeneralDilios";

	private static final int		GENERAL_ID	= 32549;
	private static final int		GUARD_ID	= 32619;

	private static final String[]	DILIOS_TEXT	=
												{
			"Messenger, inform the patrons of the Keucereus Alliance Base! The Seed of Infinity is currently secured under the flag of the Keucereus Alliance!",
			"Messenger, inform the patrons of the Keucereus Alliance Base! We're gathering brave adventurers to attack Tiat's Mounted Troop that's rooted in the Seed of Destruction.",
			"Messenger, inform the brothers in Kucereu's clan outpost! Brave adventurers are currently eradicating Undead that are widespread in Seed of Immortality's Hall of Suffering and Hall of Erosion!" };

	private L2Npc					_general;
	private List<L2Npc>				_guards		= new ArrayList<L2Npc>();

	public GeneralDilios(int questId, String name, String descr)
	{
		super(questId, name, descr);
		findNpcs();
		if (_general == null || _guards.isEmpty())
			throw new NullPointerException("Cannot find npcs!");
		startQuestTimer("command_0", 60000, null, null);
	}

	public void findNpcs()
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
			if (spawn != null)
				if (spawn.getNpcId() == GENERAL_ID)
					_general = spawn.getLastSpawn();
				else if (spawn.getNpcId() == GUARD_ID)
					_guards.add(spawn.getLastSpawn());
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if (event.startsWith("command_"))
		{
			int value = Integer.parseInt(event.substring(8));
			if (value < 6)
			{
				_general.broadcastPacket(new NpcSay(_general.getObjectId(), 0, GENERAL_ID, "Stabbing three times!"));
				startQuestTimer("guard_animation_0", 3400, null, null);
			}
			else
			{
				value = -1;
				_general.broadcastPacket(new NpcSay(_general.getObjectId(), 1, GENERAL_ID, DILIOS_TEXT[Rnd.get(DILIOS_TEXT.length)]));
			}
			startQuestTimer("command_" + (value + 1), 60000, null, null);
		}
		else if (event.startsWith("guard_animation_"))
		{
			int value = Integer.parseInt(event.substring(16));
			for (L2Npc guard : _guards)
			{
				guard.broadcastPacket(new SocialAction(guard.getObjectId(), 4));
			}
			if (value < 2)
				startQuestTimer("guard_animation_" + (value + 1), 1500, null, null);
		}
		return "";
	}

	public static void main(String[] args)
	{
		new GeneralDilios(-1, QN, "ai");
	}
}
