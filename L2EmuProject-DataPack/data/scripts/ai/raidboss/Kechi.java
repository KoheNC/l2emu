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
package ai.raidboss;

import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author L0ngh0rn
 * @since 30/03/2011
 */
public final class Kechi extends QuestJython
{
	public static final String	QN		= "Kechi";

	private static final int	KECHI	= 25532;
	private static final int	GUARD1	= 22309;
	private static final int	GUARD2	= 22310;
	private static final int	GUARD3	= 22417;

	public int					KECHI_STATUS;

	public Kechi(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		addKillId(KECHI);
		addAttackId(KECHI);
		addSpawnId(KECHI);
	}

	@Override
	public String onAttack(L2Npc npc, L2Player attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (npc.getNpcId() == KECHI)
		{
			final int maxHp = npc.getMaxHp();
			final double nowHp = npc.getStatus().getCurrentHp();

			switch (KECHI_STATUS)
			{
				case 0:
					if (nowHp < maxHp * 0.8)
					{
						KECHI_STATUS = 1;
						spawnMobs(npc);
					}
					break;
				case 1:
					if (nowHp < maxHp * 0.6)
					{
						KECHI_STATUS = 2;
						spawnMobs(npc);
					}
					break;
				case 2:
					if (nowHp < maxHp * 0.4)
					{
						KECHI_STATUS = 3;
						spawnMobs(npc);
					}
					break;
				case 3:
					if (nowHp < maxHp * 0.3)
					{
						KECHI_STATUS = 4;
						spawnMobs(npc);
					}
					break;
				case 4:
					if (nowHp < maxHp * 0.2)
					{
						KECHI_STATUS = 5;
						spawnMobs(npc);
					}
					break;
				case 5:
					if (nowHp < maxHp * 0.1)
					{
						KECHI_STATUS = 6;
						spawnMobs(npc);
					}
					break;
				case 6:
					if (nowHp < maxHp * 0.05)
					{
						KECHI_STATUS = 7;
						spawnMobs(npc);
					}
					break;
			}
		}

		return super.onAttack(npc, attacker, damage, isPet, skill);
	}

	@Override
	public String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		if (npc.getNpcId() == KECHI)
			addSpawn(32279, 154077, 149527, -12159, 0, false, 0, false, player.getInstanceId());
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getNpcId() == KECHI)
			KECHI_STATUS = 0;
		return super.onSpawn(npc);
	}

	private void spawnMobs(L2Npc npc)
	{
		addSpawn(GUARD1, 154184, 149230, -12151, 0, false, 0, false, npc.getInstanceId());
		addSpawn(GUARD1, 153975, 149823, -12152, 0, false, 0, false, npc.getInstanceId());
		addSpawn(GUARD1, 154364, 149665, -12151, 0, false, 0, false, npc.getInstanceId());
		addSpawn(GUARD1, 153786, 149367, -12151, 0, false, 0, false, npc.getInstanceId());
		addSpawn(GUARD2, 154188, 149825, -12152, 0, false, 0, false, npc.getInstanceId());
		addSpawn(GUARD2, 153945, 149224, -12151, 0, false, 0, false, npc.getInstanceId());
		addSpawn(GUARD3, 154374, 149399, -12152, 0, false, 0, false, npc.getInstanceId());
		addSpawn(GUARD3, 153796, 149646, -12159, 0, false, 0, false, npc.getInstanceId());
	}

	public static void main(String[] args)
	{
		new Kechi(25532, QN, "Kechi", "ai");
	}
}
