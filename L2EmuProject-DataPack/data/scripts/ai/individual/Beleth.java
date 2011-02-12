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
package ai.individual;

import net.l2emuproject.gameserver.instancemanager.QuestManager;
import net.l2emuproject.gameserver.instancemanager.grandbosses.BelethManager;
import net.l2emuproject.gameserver.model.L2Skill;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import ai.group_template.L2AttackableAIScript;

/**
 * @author lord_rex
 */
public class Beleth extends L2AttackableAIScript
{
	private static String		QN					= "Beleth";

	private static final int[]	EXIT_LOCATION		=
													{ -24095, 251617, -3374 };

	private static final int	BELETH_RING			= 10314;

	private static double		RELOCATE_PERCENT	= 0.8;

	public Beleth(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addFirstTalkId(BelethManager.PRESENTATION_ELF);
		addStartNpc(BelethManager.STONE_COFFIN);
		addTalkId(BelethManager.STONE_COFFIN);
		addKillId(BelethManager.BELETH_SLAVE);

		int[] mobs =
		{ BelethManager.BELETH, BelethManager.BELETH_CLONE };
		registerMobs(mobs);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		QuestState st = attacker.getQuestState(QN);
		if (st == null)
		{
			Quest quest = QuestManager.getInstance().getQuest(QN);
			st = quest.newQuestState(attacker);
		}
		switch (npc.getNpcId())
		{
			case BelethManager.BELETH_CLONE:
				BelethManager.getInstance().useSkill(npc);
				break;
			case BelethManager.BELETH:
				if (npc.getCurrentHp() <= (npc.getMaxHp() * RELOCATE_PERCENT) && RELOCATE_PERCENT > 0)
				{
					if (RELOCATE_PERCENT >= 0.6 && RELOCATE_PERCENT < 0.8)
					{
						for (int i = 32; i < 48; i++)
							BelethManager.getInstance().spawnClone(i);
						BelethManager.getInstance().respawnColnes();
					}
					else if (RELOCATE_PERCENT >= 0.2 && RELOCATE_PERCENT < 0.4)
					{
						for (int i = 48; i < 56; i++)
							BelethManager.getInstance().spawnClone(i);
						BelethManager.getInstance().respawnColnes();
					}

					BelethManager.getInstance().relocateBeleth(0);
					RELOCATE_PERCENT -= 0.2;

					return "";
				}
				else
					BelethManager.getInstance().useSkill(npc);
				break;
		}

		return "";
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case BelethManager.BELETH_SLAVE:
				BelethManager.getInstance().belethManagerTask(BelethManager.TASK_WAIT_ROOM, player);
				break;
			case BelethManager.BELETH_CLONE:
				BelethManager.getInstance().respawnColne();
				break;
			case BelethManager.BELETH:
				BelethManager.getInstance().belethManagerTask(BelethManager.TASK_BELETH_DEAD, player);
				QuestState st = player.getQuestState(QN);
				if (st != null)
					st.setState(State.STARTED);
				break;
		}
		return null;
	}

	@Override
	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		// Starts casting skills on a target if not already casting helps avoid "dead" moments.
		if (!npc.isCastingNow())
			BelethManager.getInstance().useSkill(npc);

		return "";
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";

		switch (npc.getNpcId())
		{
			case BelethManager.PRESENTATION_ELF:
				BelethManager.getInstance().teleport(player, EXIT_LOCATION[0], EXIT_LOCATION[1], EXIT_LOCATION[2]);
				break;
		}

		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(QN);

		switch (npc.getNpcId())
		{
			case BelethManager.STONE_COFFIN:
				switch (st.getState())
				{
					case State.STARTED:
						st.giveItems(BELETH_RING, 1);
						st.setState(State.COMPLETED);
						break;
					case State.COMPLETED:
						htmltext = "You already have Beleth's Ring!";
						break;
					default:
						htmltext = "You should kill Beleth to get Beleth's Ring!";
						break;
				}
				break;
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new Beleth(-1, QN, "ai");
	}
}
