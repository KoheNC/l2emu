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
package hellbound.Slaves;

import java.util.Set;

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.instancemanager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.model.L2CharPosition;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2MinionInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.taskmanager.DecayTaskManager;
import ai.group_template.L2AttackableAIScript;

/**
 * @author DS, based on theOne's work
 */
public final class Slaves extends L2AttackableAIScript
{
	private static final int[]			MASTERS	=
												{ 22320, 22321 };
	private static final int[]			SLAVES	=
												{ 22322, 22323 };

	private static final int			TRUST	= -10;

	private static final L2CharPosition	MOVE_TO	= new L2CharPosition(-25451, 252291, -3252, 3500);

	public Slaves(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int npcId : MASTERS)
		{
			addSpawnId(npcId);
			addKillId(npcId);
		}
		for (int npcId : SLAVES)
			addKillId(npcId);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		return super.onSpawn(npc);
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc instanceof L2MinionInstance)
		{
			// Slaves does not have spawns and trust can't be handled by manager
			HellboundManager.getInstance().updateTrustPoints(TRUST, true);
		}
		else
		{
			Set<L2MinionInstance> slaves = ((L2MonsterInstance) npc).getSpawnedMinions();
			if (slaves != null && !slaves.isEmpty())
			{
				for (L2MinionInstance slave : slaves)
				{
					if (slave == null || slave.isDead())
						continue;

					slave.clearAggroList();
					slave.abortAttack();
					slave.abortCast();
					slave.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO);
					DecayTaskManager.getInstance().addDecayTask(slave);
				}
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	public static void main(String[] args)
	{
		new Slaves(-1, Slaves.class.getSimpleName(), "ai");
	}
}