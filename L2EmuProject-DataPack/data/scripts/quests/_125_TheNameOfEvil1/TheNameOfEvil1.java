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
package quests._125_TheNameOfEvil1;

import org.apache.commons.lang.ArrayUtils;

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 ** @author Gnacik
 **
 ** 2010-08-22 Based on official server Franz
 ** 
 ** Remade for L2EmuProject in Java: lord_rex
 */
public final class TheNameOfEvil1 extends QuestJython
{
	private static final String	QN					= "_125_TheNameOfEvil1";

	// NPCs
	private static final int	MUSHIKA				= 32114;
	private static final int	KARAKAWEI			= 32117;
	private static final int	ULU_KAIMU			= 32119;
	private static final int	BALU_KAIMU			= 32120;
	private static final int	CHUTA_KAIMU			= 32121;

	// ITEMS
	private static final int	GAZKH_FRAGMENT		= 8782;
	private static final int	ORNITHOMIMUS_CLAW	= 8779;
	private static final int	DEINONYCHUS_BONE	= 8780;
	private static final int	EPITAPH_OF_WISDOM	= 8781;

	// MOBS
	private static final int[]	ORNITHOMIMUS		=
													{ 22200, 22201, 22202, 22219, 22224, 22742, 22744 };

	private static final int[]	DEINONYCHUS			=
													{ 16067, 22203, 22204, 22205, 22220, 22225, 22743, 22745 };

	// DROP
	private static final int	DROP_CHANCE			= 30;

	public TheNameOfEvil1(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(MUSHIKA);
		addTalkId(MUSHIKA);
		addTalkId(KARAKAWEI);
		addTalkId(ULU_KAIMU);
		addTalkId(BALU_KAIMU);
		addTalkId(CHUTA_KAIMU);

		for (int i : ORNITHOMIMUS)
			addKillId(i);
		for (int i : DEINONYCHUS)
			addKillId(i);

		questItemIds = new int[]
		{ GAZKH_FRAGMENT, ORNITHOMIMUS_CLAW, DEINONYCHUS_BONE, EPITAPH_OF_WISDOM };
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		int cond = st.getInt(CONDITION);

		if (event.equalsIgnoreCase("32114-05.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, "1");
			st.sendPacket(SND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32114-09.htm") && cond == 1)
		{
			st.set(CONDITION, "2");
			st.giveItems(GAZKH_FRAGMENT, 1);
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32117-08.htm") && cond == 2)
		{
			st.set(CONDITION, "3");
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32117-14.htm") && cond == 4)
		{
			st.set(CONDITION, "5");
			st.sendPacket(SND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("32119-02.htm"))
			st.set("pilar1", "0");
		else if (cond == 5)
		{
			int correct = st.getInt("pilar1");
			st.set("pilar1", (correct + 1));
			htmltext = event;
			if (event.equalsIgnoreCase("32119-14.htm") && cond == 5)
		{
			st.set(CONDITION, "6");
			st.sendPacket(SND_MIDDLE);
		}
		}
		else if (event.equalsIgnoreCase("32120-02.htm"))
			st.set("pilar2", "0");
		else if (cond == 6)
		{
			int correct = st.getInt("pilar2");
			st.set("pilar2", (correct + 1));
			htmltext = event;
			if (event.equalsIgnoreCase("32120-15.htm") && cond == 6)
		{
			st.set(CONDITION, "7");
			st.sendPacket(SND_MIDDLE);
		}
		}
		else if (event.equalsIgnoreCase("32121-02.htm"))
			st.set("pilar3", "0");
		else if (cond == 7)
		{
			int correct = st.getInt("pilar3");
			st.set("pilar3", (correct + 1));
			htmltext = event;
			if (event.equalsIgnoreCase("32121-16.htm") && cond == 7)
		{
			st.set(CONDITION, "8");
			st.takeItems(GAZKH_FRAGMENT, -1);
			st.giveItems(EPITAPH_OF_WISDOM, 1);
			st.sendPacket(SND_MIDDLE);
		}
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
			case MUSHIKA:
				QuestState first = player.getQuestState("124_MeetingTheElroki");
				if (st.getState() == State.COMPLETED)
					htmltext = QUEST_DONE;
				else if (first != null && first.getState() == State.COMPLETED && st.getState() == State.CREATED && player.getLevel() >= 76)
					htmltext = "32114-01.htm";
				else switch (cond)
				{
					case 0:
						htmltext = "32114-00.htm";
						break;
					case 1:
						htmltext = "32114-07.htm";
						break;
					case 2:
						htmltext = "32114-10.htm";
						break;
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
						htmltext = "32114-11.htm";
						break;
					case 8:
						st.addExpAndSp(859195, 86603);
						st.unset(CONDITION);
						st.takeItems(EPITAPH_OF_WISDOM, -1);
						st.unset("pilar1");
						st.unset("pilar2");
						st.unset("pilar3");
						st.setState(State.COMPLETED);
						st.exitQuest(false);
						st.sendPacket(SND_ITEM_GET);
						htmltext = "32114-12.htm";
						break;
				}
				break;
			case KARAKAWEI:
				switch (cond)
				{
					case 2:
						htmltext = "32117-01.htm";
						break;
					case 3:
						htmltext = "32117-09.htm";
						break;
					case 4:
						st.takeItems(ORNITHOMIMUS_CLAW, -1);
						st.takeItems(DEINONYCHUS_BONE, -1);
						st.sendPacket(SND_MIDDLE);
						htmltext = "32117-10.htm";
						break;
					case 5:
						htmltext = "32117-15.htm";
						break;
					case 6:
					case 7:
						htmltext = "32117-16.htm";
						break;
					case 8:
						htmltext = "32117-17.htm";
						break;
				}
				break;
			case ULU_KAIMU:
				switch (cond)
				{
					case 5:
						npc.doCast(SkillTable.getInstance().getInfo(5089, 1));
						htmltext = "32119-01.htm";
						break;
					case 6:
						htmltext = "32119-14.htm";
						break;
				}
				break;
			case BALU_KAIMU:
				switch (cond)
				{
					case 6:
						npc.doCast(SkillTable.getInstance().getInfo(5089, 1));
						htmltext = "32120-01.htm";
						break;
					case 7:
						htmltext = "32120-16.htm";
						break;
				}
				break;
			case CHUTA_KAIMU:
				switch (cond)
				{
					case 7:
						npc.doCast(SkillTable.getInstance().getInfo(5089, 1));
						htmltext = "32121-01.htm";
						break;
					case 8:
						htmltext = "32121-17.htm";
						break;
				}
				break;
		}

		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(QN);

		if (st == null)
			return htmltext;

		if (st.getInt(CONDITION) == 3)
		{
			final int npcId = npc.getNpcId();

			if (ArrayUtils.contains(ORNITHOMIMUS, npcId) && st.getQuestItemsCount(ORNITHOMIMUS_CLAW) < 2)
			{
				if (st.getRandom(100) < DROP_CHANCE)
				{
					st.giveItems(ORNITHOMIMUS_CLAW, 1);
					st.sendPacket(SND_ITEM_GET);
				}
			}
			else if (ArrayUtils.contains(DEINONYCHUS, npcId) && st.getQuestItemsCount(DEINONYCHUS_BONE) < 2)
			{
				if (st.getRandom(100) < DROP_CHANCE)
				{
					st.giveItems(DEINONYCHUS_BONE, 1);
					st.sendPacket(SND_ITEM_GET);
				}
			}
			else if (st.getQuestItemsCount(ORNITHOMIMUS_CLAW) == 2 && st.getQuestItemsCount(DEINONYCHUS_BONE) == 2)
			{
				st.set(CONDITION, "4");
				st.sendPacket(SND_MIDDLE);
			}
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		new TheNameOfEvil1(125, QN, "The Name of Evil - 1");
	}
}
