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

import ai.L2AttackableAIScript;
import javolution.util.FastMap;
import net.l2emuproject.gameserver.instancemanager.gracia.SeedOfDestructionManager;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.skill.L2Skill;
import net.l2emuproject.gameserver.model.world.L2Object;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author L0ngh0rn
 * @since Converted from python script. (Author: Psycho(killer1888) / L2jFree)
 */
public final class EnergySeeds extends L2AttackableAIScript
{
	private static final String						QN			= "EnergySeeds";

	// NPCs / Stones
	private static final FastMap<Integer, Integer>	NPCS_STONES	= new FastMap<Integer, Integer>();

	// SKILL
	private static final int						SKILL		= 5780;

	static
	{
		NPCS_STONES.put(18678, 14016);
		NPCS_STONES.put(18679, 14015);
		NPCS_STONES.put(18680, 14017);
		NPCS_STONES.put(18681, 14016);
		NPCS_STONES.put(18678, 14020);
		NPCS_STONES.put(18683, 14019);
	}

	public EnergySeeds(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (Integer npc : NPCS_STONES.keySet())
		{
			addSpawnId(npc);
			addSkillSeeId(npc);
		}
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return null;
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		Integer[] npcs = new Integer[NPCS_STONES.size()];
		NPCS_STONES.keySet().toArray(npcs);
		if (event.equals("respawn"))
			addSpawn(npcs[Rnd.get(NPCS_STONES.size() - 1)], npc.getX(), npc.getY(), npc.getZ(), 0, false, 0);
		return null;
	}

	@Override
	public final String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		final int skillId = skill.getId();

		if (NPCS_STONES.containsKey(npcId))
		{
			if (SeedOfDestructionManager.getInstance().getState() != 3)
				return null;
			if (!contains(targets, npc))
				return null;
			if (skillId == SKILL)
			{
				SystemMessage sm = null;
				int itemId = NPCS_STONES.get(npcId);
				int chance = Rnd.get(100);
				if (chance > 40)
				{
					caster.addItem("Energy Seed", itemId, Rnd.get(1, 2), caster, true, true);
					sm = new SystemMessage(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED);
				}
				else
					sm = new SystemMessage(SystemMessageId.THE_COLLECTION_HAS_FAILED);

				startQuestTimer("respawn", Rnd.get(60000, 7200000), npc, null);
				npc.decayMe();
				caster.sendPacket(sm);
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	public static void main(String[] args)
	{
		new EnergySeeds(-1, QN, "ai");
	}
}
