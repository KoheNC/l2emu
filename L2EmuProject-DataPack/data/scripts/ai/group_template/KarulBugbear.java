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
import net.l2emuproject.gameserver.model.skill.L2Skill;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author L0ngh0rn
 * @since Author Maxi (L2jEurope)
 */
public final class KarulBugbear extends L2AttackableAIScript
{
	private static final String	QN				= "KarulBugbear";

	// NPCs
	private static final int	KARUL			= 20600;

	// Flag
	private boolean				_firstAttacked	= false;

	public KarulBugbear(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addAttackId(KARUL);
		addKillId(KARUL);

		_firstAttacked = false;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		int objId = npc.getObjectId();
		if (_firstAttacked)
		{
			if (Rnd.get(100) <= 25)
				npc.broadcastPacket(new NpcSay(objId, 0, npc.getNpcId(), "Your rear is practically unguarded!"));
		}
		else
		{
			_firstAttacked = true;
			if (Rnd.get(100) <= 25)
				npc.broadcastPacket(new NpcSay(objId, 0, npc.getNpcId(), "Watch your back!"));
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == KARUL)
			_firstAttacked = false;

		return "";
	}

	public static void main(String[] args)
	{
		new KarulBugbear(-1, QN, "ai");
	}
}
