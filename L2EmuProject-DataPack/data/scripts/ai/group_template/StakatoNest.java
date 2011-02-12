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

import java.util.List;

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.model.L2Object;
import net.l2emuproject.gameserver.model.L2Skill;
import net.l2emuproject.gameserver.model.actor.L2Attackable;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.util.Broadcast;
import net.l2emuproject.gameserver.util.Util;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.lang.ArrayUtils;

public final class StakatoNest extends L2AttackableAIScript
{
	private static final String	QN					= "StakatoNestAI";

	// List of all mobs just for register
	private static final int[]	STAKATO_MONSTERS	=
													{
			18793,
			18794,
			18795,
			18796,
			18797,
			18798,
			22617,
			22618,
			22619,
			22620,
			22621,
			22622,
			22623,
			22624,
			22625,
			22626,
			22627,
			22628,
			22629,
			22630,
			22631,
			22632,
			22633,
			25667									};
	// Coocons
	private static final int[]	COCOONS				=
													{ 18793, 18794, 18795, 18796, 18797, 18798 };

	// Cannibalistic Stakato Leader
	private static final int	STAKATO_LEADER		= 22625;

	// Spike Stakato Nurse
	private static final int	STAKATO_NURSE		= 22630;
	// Spike Stakato Nurse (Changed)
	private static final int	STAKATO_NURSE2		= 22631;
	// Spiked Stakato Baby
	private static final int	STAKATO_BABY		= 22632;
	// Spiked Stakato Captain
	private static final int	STAKATO_CAPTAIN		= 22629;

	// Female Spiked Stakato
	private static final int	STAKATO_FEMALE		= 22620;
	// Male Spiked Stakato
	private static final int	STAKATO_MALE		= 22621;
	// Male Spiked Stakato (Changed)
	private static final int	STAKATO_MALE2		= 22622;
	// Spiked Stakato Guard
	private static final int	STAKATO_GUARD		= 22619;

	// Cannibalistic Stakato Chief
	private static final int	STAKATO_CHIEF		= 25667;
	// Growth Accelerator
	private static final int	GROWTH_ACCELERATOR	= 2905;
	// Small Stakato Cocoon
	private static final int	SMALL_COCOON		= 14833;
	// Large Stakato Cocoon
	private static final int	LARGE_COCOON		= 14834;

	public StakatoNest(int questId, String name, String descr)
	{
		super(questId, name, descr);

		registerMobs(STAKATO_MONSTERS);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		L2MonsterInstance mob = (L2MonsterInstance) npc;

		if ((mob.getNpcId() == STAKATO_LEADER) && (Rnd.get(1000) < 100) && (mob.getCurrentHp() < (mob.getMaxHp() * 0.3)))
		{
			L2MonsterInstance follower = (L2MonsterInstance) npc;

			if (follower != null)
			{
				double hp = follower.getCurrentHp();

				if (hp > (follower.getMaxHp() * 0.3))
				{
					mob.abortAttack();
					mob.abortCast();
					mob.setHeading(Util.calculateHeadingFrom(mob, follower));
					mob.doCast(SkillTable.getInstance().getInfo(4484, 1));
					mob.getStatus().setCurrentHp(mob.getCurrentHp() + hp);
					follower.doDie(follower);
					follower.deleteMe();
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		L2MonsterInstance monster = (L2MonsterInstance) npc;

		switch (npc.getNpcId())
		{
			case STAKATO_NURSE:
				if (monster != null)
				{
					Broadcast.toSelfAndKnownPlayers(npc, new MagicSkillUse(npc, 2046, 1, 1000, 0));
					for (int i = 0; i < 3; i++)
					{
						L2Npc _spawned = addSpawn(STAKATO_CAPTAIN, monster, true);
						attackPlayer(killer, _spawned);
					}
				}
				break;
			case STAKATO_BABY:
				if (monster != null && !monster.isDead())
				{
					startQuestTimer("nurse_change", 5000, monster, killer);
				}
				break;
			case STAKATO_MALE:
				if (monster != null)
				{
					Broadcast.toSelfAndKnownPlayers(npc, new MagicSkillUse(npc, 2046, 1, 1000, 0));
					for (int i = 0; i < 3; i++)
					{
						L2Npc spawned = addSpawn(STAKATO_GUARD, monster, true);
						attackPlayer(killer, spawned);
					}
				}
				break;
			case STAKATO_FEMALE:
				if (monster != null && !monster.isDead())
				{
					startQuestTimer("male_change", 5000, monster, killer);
				}
				break;
			case STAKATO_CHIEF:
				if (killer.isInParty())
				{
					List<L2PcInstance> party = killer.getParty().getPartyMembers();
					for (L2PcInstance member : party)
						giveCocoon(member, npc);
				}
				else
					giveCocoon(killer, npc);
				break;
		}

		return "";
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (ArrayUtils.contains(COCOONS, npc.getNpcId()) && ArrayUtils.contains(targets, npc) && skill.getId() == GROWTH_ACCELERATOR)
		{
			npc.doDie(caster);
			L2Npc _spawned = addSpawn(STAKATO_CHIEF, npc.getX(), npc.getY(), npc.getZ(), Util.calculateHeadingFrom(npc, caster), false, 0, true);
			attackPlayer(caster, _spawned);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ((npc == null) || (player == null))
			return null;
		if (npc.isDead())
			return null;

		if (event.equalsIgnoreCase("nurse_change"))
		{
			npc.getSpawn().decreaseCount(npc);
			npc.deleteMe();
			L2Npc _spawned = addSpawn(STAKATO_NURSE2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, true);
			attackPlayer(player, _spawned);
		}
		else if (event.equalsIgnoreCase("male_change"))
		{
			npc.getSpawn().decreaseCount(npc);
			npc.deleteMe();
			L2Npc _spawned = addSpawn(STAKATO_MALE2, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, true);
			attackPlayer(player, _spawned);
		}
		return null;
	}

	private void attackPlayer(L2PcInstance player, L2Npc npc)
	{
		if (npc != null && player != null)
		{
			((L2Attackable) npc).setIsRunning(true);
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			((L2Attackable) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
	}

	private void giveCocoon(L2PcInstance player, L2Npc npc)
	{
		if (Rnd.get(100) > 80)
			player.addItem("StakatoCocoon", LARGE_COCOON, 1, npc, true);
		else
			player.addItem("StakatoCocoon", SMALL_COCOON, 1, npc, true);
	}

	public static void main(String[] args)
	{
		new StakatoNest(-1, QN, "ai");
	}
}
