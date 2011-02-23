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

import ai.L2AttackableAIScript;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author Unknown, translated by Intrepid
 */
public final class DeluLizardman extends L2AttackableAIScript
{
	private static final String	QN								= "DeluLizardman";

	private static final int	SPECIAL_AGENT					= 21105;
	private static final int	SPECIAL_COMMANDER				= 21107;

	private boolean				_firstAttackedSecialAgent		= false;
	private boolean				_firstAttackedSecialCommander	= false;

	public DeluLizardman(int questId, String name, String descr)
	{
		super(questId, name, descr);

		final int[] mobs =
		{ SPECIAL_AGENT, SPECIAL_COMMANDER };
		for (int i : mobs)
		{
			addAttackId(i);
			addKillId(i);
		}

		_firstAttackedSecialAgent = true;
		_firstAttackedSecialCommander = true;
	}

	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		final int npcId = npc.getNpcId();

		switch (npcId)
		{
			case SPECIAL_AGENT:
				if (_firstAttackedSecialAgent && Rnd.get(100) > 40)
					npc.broadcastPacket(new NpcSay(npc, "Hey! Were having a duel here!"));
				else
				{
					npc.broadcastPacket(new NpcSay(npc, "How dare you interrupt our fight! Hey guys, help!"));
					_firstAttackedSecialAgent = false;
				}
				break;
			case SPECIAL_COMMANDER:
				if (_firstAttackedSecialCommander && Rnd.get(100) > 40)
					npc.broadcastPacket(new NpcSay(npc, "Come on, I'll take you on!"));
				else
				{
					npc.broadcastPacket(new NpcSay(npc, "How dare you interrupt a sacred duel! You must be taught a lesson!"));
					_firstAttackedSecialCommander = false;
				}
				break;
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final int npcId = npc.getNpcId();

		switch (npcId)
		{
			case SPECIAL_AGENT:
				if (_firstAttackedSecialAgent)
					addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
				break;
			case SPECIAL_COMMANDER:
				if (_firstAttackedSecialCommander)
					addSpawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
				break;
		}

		return "";
	}

	public static void main(String[] args)
	{
		new DeluLizardman(-1, QN, "ai");
	}
}
