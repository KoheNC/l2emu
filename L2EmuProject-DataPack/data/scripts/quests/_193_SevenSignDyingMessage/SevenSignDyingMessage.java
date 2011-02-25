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
package quests._193_SevenSignDyingMessage;

import quests._192_SevenSignSeriesOfDoubt.SevenSignSeriesOfDoubt;
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
 * @since 2010-06-26 by Gnacik<br>Based on official server Franz
 */
public final class SevenSignDyingMessage extends QuestJython
{
	public static final String	QN				= "_193_SevenSignDyingMessage";

	// NPCs
	private static final int	HOLLINT			= 30191;
	private static final int	CAIN			= 32569;
	private static final int	ERIC			= 32570;
	private static final int	ATHEBALDT		= 30760;

	// MOBs
	private static final int	SHILENSEVIL		= 27343;

	// Quest Item
	private static final int	JACOB_NECK		= 13814;
	private static final int	DEADMANS_HERB	= 13816;
	private static final int	SCULPTURE		= 14353;

	public SevenSignDyingMessage(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(HOLLINT);

		addTalkId(HOLLINT);
		addTalkId(CAIN);
		addTalkId(ERIC);
		addTalkId(ATHEBALDT);

		addKillId(SHILENSEVIL);

		questItemIds = new int[]
		{ JACOB_NECK, DEADMANS_HERB, SCULPTURE };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("30191-02.htm"))
		{
			st.set(CONDITION, 1);
			st.setState(State.STARTED);
			st.giveItems(JACOB_NECK, 1);
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32569-05.htm"))
		{
			st.set(CONDITION, 2);
			st.takeItems(JACOB_NECK, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32570-02.htm"))
		{
			st.set(CONDITION, 3);
			st.giveItems(DEADMANS_HERB, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("9"))
		{
			st.takeItems(DEADMANS_HERB, 1);
			st.set(CONDITION, 4);
			st.sendPacket(SND_MIDDLE);
			player.showQuestMovie(Integer.parseInt(event));
			return "";
		}
		else if (event.equalsIgnoreCase("32569-09.htm"))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), player.getName() + "! That stranger must be defeated!"));
			L2Attackable monster = (L2Attackable) addSpawn(SHILENSEVIL, 82624, 47422, -3220, 0, false, 60000, true);
			monster.broadcastPacket(new NpcSay(monster.getObjectId(), 0, monster.getNpcId(), "You are not the owner of that item!"));
			monster.setRunning();
			monster.addDamageHate(player, 0, 999);
			monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st.getPlayer());
		}
		else if (event.equalsIgnoreCase("32569-13.htm"))
		{
			st.set(CONDITION, 6);
			st.takeItems(SCULPTURE, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30760-02.htm"))
		{
			st.addExpAndSp(52518015, 5817677);
			st.unset(CONDITION);
			st.setState(State.COMPLETED);
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
			case HOLLINT:
				QuestState first = player.getQuestState(SevenSignSeriesOfDoubt.QN);
				if (st.getState() == State.COMPLETED)
					htmltext = QUEST_DONE;
				else if (first != null && first.getState() == State.COMPLETED && st.getState() == State.CREATED && player.getLevel() >= 79)
					htmltext = "30191-01.htm";
				else if (cond == 1)
					htmltext = "30191-03.htm";
				else
				{
					htmltext = "30191-00.htm";
					st.exitQuest(true);
				}
				break;
			case CAIN:
				switch (cond)
				{
					case 1:
						htmltext = "32569-01.htm";
						break;
					case 2:
						htmltext = "32569-06.htm";
						break;
					case 3:
						htmltext = "32569-07.htm";
						break;
					case 4:
						htmltext = "32569-08.htm";
						break;
					case 5:
						htmltext = "32569-10.htm";
						break;
				}
				break;
			case ERIC:
				switch (cond)
				{
					case 2:
						htmltext = "32570-01.htm";
						break;

					case 3:
						htmltext = "32570-03.htm";
						break;
				}
				break;
			case ATHEBALDT:
				if (cond == 6)
					htmltext = "30760-01.htm";
				break;
		}
		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (npc.getNpcId() == SHILENSEVIL && st.getInt(CONDITION) == 4)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), player.getName()
					+ "... You may have won this time... But next time, I will surely capture you!"));
			st.giveItems(SCULPTURE, 1);
			st.set(CONDITION, 5);
		}

		return null;
	}

	public static void main(String[] args)
	{
		new SevenSignDyingMessage(193, QN, "Seven Sign Dying Message");
	}
}
