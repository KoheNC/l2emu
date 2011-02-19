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
package hellbound.Remnants;

import net.l2emuproject.gameserver.instancemanager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.model.L2Object;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.skill.L2Skill;
import ai.group_template.L2AttackableAIScript;

public final class Remnants extends L2AttackableAIScript
{
	private static final int[]	NPCS		=
											{ 18463, 18464, 18465 };

	private static final int	DEREK		= 18465;

	private static final int	HOLY_WATER	= 2358;

	private static final String	MSG			= "The holy water affects Remnants Ghost. You have freed his soul.";
	private static final String	MSG_DEREK	= "The holy water affects Derek. You have freed his soul.";

	public Remnants(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int npcId : NPCS)
		{
			addSpawnId(npcId);
			addSkillSeeId(npcId);
		}
		addKillId(DEREK);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setIsMortal(false);
		return super.onSpawn(npc);
	}

	@Override
	public final String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.getId() == HOLY_WATER)
		{
			if (!npc.isDead())
			{
				if (targets.length > 0 && targets[0] == npc)
				{
					if (npc.getCurrentHp() < npc.getMaxHp() * 0.1)
					{
						npc.doDie(caster);
						if (npc.getNpcId() == DEREK)
							caster.sendMessage(MSG_DEREK);
						else
							caster.sendMessage(MSG);
					}
				}
			}
		}

		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		// Updating level in separate thread.
		HellboundManager.getInstance().setHellboundLevel(5);

		return super.onKill(npc, killer, isPet);
	}

	public static void main(String[] args)
	{
		new Remnants(-1, Remnants.class.getSimpleName(), "ai");
	}
}