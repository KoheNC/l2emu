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
package quests._197_SevenSignTheSacredBookOfSeal;

import quests._196_SevenSignSealOfTheEmperor.SevenSignSealOfTheEmperor;
import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.serverpackets.NpcSay;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * @author L0ngh0rn
 */
public final class SevenSignTheSacredBookOfSeal extends QuestJython
{
	public static final String	QN			= "_197_SevenSignTheSacredBookOfSeal";

	// NPCs
	private static final int	WOOD		= 32593;
	private static final int	ORVEN		= 30857;
	private static final int	LEOPARD		= 32594;
	private static final int	LAWRENCE	= 32595;
	private static final int	SOFIA		= 32596;

	// Mobs
	private static final int	SHILENSEVIL	= 27343;

	private static final int[]	NPCS		=
											{ WOOD, ORVEN, LEOPARD, LAWRENCE, SOFIA };

	// Quest Item
	private static final int	TEXT		= 13829;
	private static final int	SCULPTURE	= 14356;

	public SevenSignTheSacredBookOfSeal(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(WOOD);

		for (int i : NPCS)
			addTalkId(i);

		addKillId(SHILENSEVIL);

		questItemIds = new int[]
		{ TEXT, SCULPTURE };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("32593-04.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30857-04.htm"))
		{
			st.set(CONDITION, 2);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32594-03.htm"))
		{
			st.set(CONDITION, 3);
			st.sendPacket(SND_MIDDLE);

		}
		else if (event.equalsIgnoreCase("32595-04.htm"))
		{
			L2Attackable monster = (L2Attackable) addSpawn(SHILENSEVIL, 152520, -57685, -3438, 0, false, 60000, true);
			monster.broadcastPacket(new NpcSay(monster.getObjectId(), 0, monster.getNpcId(), "You are not the owner of that item!"));
			monster.setRunning();
			monster.addDamageHate(player, 0, 999);
			monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		else if (event.equalsIgnoreCase("32595-08.htm"))
		{
			st.sendPacket(SND_MIDDLE);
			st.set(CONDITION, 5);
		}
		else if (event.equalsIgnoreCase("32596-04.htm"))
		{
			st.sendPacket(SND_MIDDLE);
			st.set(CONDITION, 6);
			st.giveItems(TEXT, 1);
		}
		else if (event.equalsIgnoreCase("32593-08.htm"))
		{
			st.addExpAndSp(52518015, 5817676);
			st.setState(State.COMPLETED);
			st.unset(CONDITION);
			st.takeItems(TEXT, 1);
			st.takeItems(SCULPTURE, 1);
			st.exitQuest(false);
			st.sendPacket(SND_FINISH);
		}
		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		int cond = st.getInt(CONDITION);

		switch (npc.getNpcId())
		{
			case WOOD:
				QuestState qs = player.getQuestState(SevenSignSealOfTheEmperor.QN);
				if (player.getLevel() < 79 || qs == null)
				{
					st.exitQuest(true);
					htmltext = "32593-00.htm";
				}
				else if (st.getState() == State.CREATED && qs.getState() == State.COMPLETED)
					htmltext = "32593-01.htm";
				else
				{
					switch (cond)
					{
						case 0:
							st.exitQuest(true);
							htmltext = "32593-00.htm";
							break;
						case 1:
							htmltext = "32593-05.htm";
							break;
						case 6:
							htmltext = "32593-06.htm";
							break;
					}
				}
				break;
			case ORVEN:
				switch (cond)
				{
					case 1:
						htmltext = "30857-01.htm";
						break;
					case 2:
						htmltext = "30857-05.htm";
						break;
				}
				break;
			case LEOPARD:
				switch (cond)
				{
					case 2:
						htmltext = "32594-01.htm";
						break;
					case 3:
						htmltext = "32594-04.htm";
						break;
				}
				break;
			case LAWRENCE:
				switch (cond)
				{
					case 3:
						htmltext = "32595-01.htm";
						break;
					case 4:
						htmltext = "32595-05.htm";
						break;
					case 5:
						htmltext = "32595-09.htm";
						break;
				}
				break;
			case SOFIA:
				switch (cond)
				{
					case 5:
						htmltext = "32596-01.htm";
						break;
					case 6:
						htmltext = "32596-05.htm";
						break;
				}
				break;
		}
		if (st.getState() == State.COMPLETED)
			htmltext = QUEST_DONE;
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return null;

		int npcId = npc.getNpcId();

		if (npcId == SHILENSEVIL && st.getInt(CONDITION) == 3)
		{
			st.giveItems(SCULPTURE, 1);
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "... You may have won this time... But next time, I will surely capture you!"));
			st.set(CONDITION, 4);
		}
		return null;
	}

	public static void main(String[] args)
	{
		new SevenSignTheSacredBookOfSeal(197, QN, "Seven Sign The Sacred Book Of Seal");
	}
}