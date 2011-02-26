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
package quests._158_SeedOfEvil;

import net.l2emuproject.gameserver.model.itemcontainer.PcInventory;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class SeedOfEvil extends QuestJython
{
	private static final String	QN				= "_158_SeedOfEvil";

	// Quest NPCs
	private static final int	BIOTIN			= 30031;

	// Quest items
	private static final int	CLAY_TABLET		= 1025;

	// Quest monsters
	private static final int	NERKAS			= 27016;
	private static final String	NERKAS_ATTACKED	= "...How dare you challenge me!";
	private static final String	NERKAS_KILLED	= "May Beleth's power be spread on the whole world!";

	private SeedOfEvil(int questId, String name, String descr)
	{
		super(questId, name, descr);
		questItemIds = new int[]
		{ CLAY_TABLET };
		addStartNpc(BIOTIN);
		addTalkId(BIOTIN);
		addAttackId(NERKAS);
		addKillId(NERKAS);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		QuestState qs = player.getQuestState(QN);
		if (qs.isCompleted())
			return QUEST_DONE;
		else if (QUEST_START_EVT.equals(event))
		{
			qs.set(CONDITION, 1);
			qs.setState(State.STARTED);
			player.sendPacket(SND_ACCEPT);
			return "30031-04.htm";
		}
		else
			return event;
	}

	@Override
	public final String onAttack(L2Npc npc, L2Player attacker, int damage, boolean isPet, L2Skill skill)
	{
		switch (npc.getQuestAttackStatus())
		{
			case ATTACK_NOONE:
				npc.broadcastPacket(new NpcSay(npc, NERKAS_ATTACKED));
				npc.setQuestAttackStatus(ATTACK_SINGLE);
				npc.setQuestFirstAttacker(attacker);
				break;
			case ATTACK_SINGLE:
				//if (attacker != npc.getQuestFirstAttacker())
				//	npc.setQuestAttackStatus(ATTACK_MULTIPLE);
				break;
		}
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player killer, boolean isPet)
	{
		npc.broadcastPacket(new NpcSay(npc, NERKAS_KILLED));
		L2Player quester = killer/*npc.getQuestFirstAttacker()*/;
		if (quester == null)
			return null;
		QuestState qs = quester.getQuestState(QN);
		if (qs == null || !qs.isStarted() || qs.getInt(CONDITION) != 1 || npc.getQuestAttackStatus() != ATTACK_SINGLE)
			return null;

		if (qs.getQuestItemsCount(CLAY_TABLET) == 0)
		{
			qs.giveItems(CLAY_TABLET, 1);
			quester.sendPacket(SND_MIDDLE);
			qs.set(CONDITION, 2);
		}

		return null;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player talker)
	{
		QuestState qs = talker.getQuestState(QN);
		if (qs == null)
			return NO_QUEST;
		else if (qs.isCompleted())
			return QUEST_DONE;

		int cond = qs.getInt(CONDITION);
		if (cond == 0)
		{
			if (talker.getLevel() < 21)
			{
				qs.exitQuest(true);
				return "30031-02.htm";
			}
			else
				return "30031-03.htm";
		}
		else
		{
			if (qs.getQuestItemsCount(CLAY_TABLET) != 0)
			{
				qs.exitQuest(false);
				qs.rewardItems(PcInventory.ADENA_ID, 1495);
				qs.giveItems(956, 1);
				qs.addExpAndSp(17818, 927);
				talker.sendPacket(SND_FINISH);
				return "30031-06.htm";
			}
			else
				return "30031-05.htm";
		}
	}

	public static void main(String[] args)
	{
		new SeedOfEvil(158, QN, "Seed of Evil");
	}
}
