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

import org.apache.commons.lang.ArrayUtils;

import ai.L2AttackableAIScript;

import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.model.actor.L2Attackable;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.spawn.L2Spawn;

public class SeeThroughSilentMove extends L2AttackableAIScript
{
	private static final int[]	MOBIDS	=
										{
			18001,
			18002,
			18329,
			18330,
			18331,
			18332,
			18333,
			18334,
			18335,
			18336,
			18337,
			18338,
			18339,
			18340,
			18341,
			22199,
			22215,
			22216,
			22217,
			22327,
			22360,
			22536,
			22537,
			22538,
			22539,
			22540,
			22541,
			22542,
			22543,
			22544,
			22546,
			22547,
			22548,
			22549,
			22596,
			22708,
			22709,
			22710,
			22711,
			22712,
			22713,
			22714,
			22715,
			22716,
			22717,
			22718,
			22719,
			22720,
			22721,
			22722,
			22723,
			22724,
			22725,
			22726,
			22727,
			22728,
			22729,
			22730,
			22731,
			22732,
			22733,
			22734,
			22735,
			22736,
			22737,
			22738,
			22739,
			22740,
			29009,
			29010,
			29011,
			29012,
			29013,
			29162						};

	public SeeThroughSilentMove(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (L2Spawn npc : SpawnTable.getInstance().getSpawnTable().values())
			if (ArrayUtils.contains(MOBIDS, npc.getNpcId()) && npc.getLastSpawn() != null && npc.getLastSpawn() instanceof L2Attackable)
				((L2Attackable) npc.getLastSpawn()).setSeeThroughSilentMove(true);
		for (int npcId : MOBIDS)
			addSpawnId(npcId);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc instanceof L2Attackable)
			((L2Attackable) npc).setSeeThroughSilentMove(true);

		return "";
	}

	public static void main(String[] args)
	{
		new SeeThroughSilentMove(-1, "SeeThroughSilentMove", "ai");
	}
}
