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

import java.util.Collection;

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.geodata.GeoData;
import net.l2emuproject.gameserver.model.actor.L2Attackable;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.world.L2Object;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;

public final class GiantScouts extends L2AttackableAIScript
{
	private static final String	QN			= "GiantScouts";

	private static final int	SCHOUTS[]	=
											{ 22668, 22669 };

	public GiantScouts(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int id : SCHOUTS)
			addAggroRangeEnterId(id);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2Character target = isPet ? player.getPet() : player;

		if (GeoData.getInstance().canSeeTarget(npc, target))
		{
			if (!npc.isInCombat() && npc.getTarget() == null)
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(),
						"Oh Giants, an intruder has been discovered."));

			npc.setTarget(target);
			npc.setRunning();
			((L2Attackable) npc).addDamageHate(target, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

			// Notify clan
			Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
			for (L2Object obj : objs)
			{
				if (obj != null)
				{
					if (obj instanceof L2MonsterInstance)
					{
						L2MonsterInstance monster = (L2MonsterInstance) obj;
						if ((npc.getFactionId() != null && monster.getFactionId() != null) && monster.getFactionId().equals(npc.getFactionId())
								&& GeoData.getInstance().canSeeTarget(npc, monster))
						{
							monster.setTarget(target);
							monster.setRunning();
							monster.addDamageHate(target, 0, 999);
							monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						}
					}

				}
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	public static void main(String[] args)
	{
		new GiantScouts(-1, QN, "ai");
	}
}