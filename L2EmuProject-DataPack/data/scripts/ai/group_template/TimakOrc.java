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
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author Unknown, translated by Intrepid
 */
public final class TimakOrc extends L2AttackableAIScript
{
	private static final String	QN							= "TimakOrc";

	private static final int	OVERLORD					= 20588;
	private static final int	TROOP_LEADER				= 20767;

	private boolean				_firstAttackedOverlord		= false;
	private boolean				_firstAttackedTroopLeader	= false;

	public TimakOrc(int questId, String name, String descr)
	{
		super(questId, name, descr);

		int[] mobs =
		{ OVERLORD, TROOP_LEADER };

		for (int i : mobs)
		{
			addAttackId(i);
			addKillId(i);
		}

		_firstAttackedOverlord = true;
		_firstAttackedTroopLeader = true;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case OVERLORD:
				if (_firstAttackedOverlord && Rnd.get(100) > 50)
					npc.broadcastPacket(new NpcSay(npc, "Dear ultimate power!!!"));
				else
					_firstAttackedOverlord = false;
				break;
			case TROOP_LEADER:
				if (_firstAttackedTroopLeader && Rnd.get(100) > 50)
					npc.broadcastPacket(new NpcSay(npc, "Destroy the enemy, my brothers!"));
				else
					_firstAttackedTroopLeader = false;
				break;
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();

		switch (npcId)
		{
			case OVERLORD:
				if (_firstAttackedOverlord)
					addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
				break;
			case TROOP_LEADER:
				if (_firstAttackedTroopLeader)
					addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
				break;
		}

		return "";
	}

	public static void main(String[] args)
	{
		new TimakOrc(-1, QN, "ai");
	}
}
