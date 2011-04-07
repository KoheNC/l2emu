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
package quests._126_TheNameOfEvil2;


import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/** 
 * Remade for L2EmuProject by lewzer
 */
public final class TheNameOfEvil2 extends QuestJython
{
	public static final String	QN					= "_126_TheNameOfEvil2";

	// NPC
	private static final int	MUSHIKA				= 32114;
	private static final int	ASAMANAH			= 32115;
	private static final int	ULU_KAIMU			= 32119;
	private static final int	BALU_KAIMU			= 32120;
	private static final int	CHUTA_KAIMU			= 32121;
	private static final int	WARRIOR_GRAVE		= 32122;
	private static final int	SHILEN_STONE_STATUE	= 32109;

	// QUEST ITEMS
	private static final int	BONEPOWDER			= 8783;
	private static final int	EWA					= 729;
	private static final int	ADENA				= 57;

	public TheNameOfEvil2(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(ASAMANAH);
		addTalkId(ASAMANAH);
		addTalkId(MUSHIKA);
		addTalkId(ULU_KAIMU);
		addTalkId(BALU_KAIMU);
		addTalkId(CHUTA_KAIMU);
		addTalkId(WARRIOR_GRAVE);
		addTalkId(SHILEN_STONE_STATUE);
	}

	private String getSongOne32122(QuestState st)
	{
		String htmltext = "32122-24.htm";
		if (st.getInt(CONDITION) == 14 && st.getInt("DO") > 0 && st.getInt("MI") > 0 && st.getInt("FA") > 0 && st.getInt("SOL") > 0 && st.getInt("FA_2") > 0)
		{
			htmltext = "32122-42.htm";
			st.set(CONDITION, "15");
			st.unset("DO");
			st.unset("MI");
			st.unset("FA");
			st.unset("SOL");
			st.unset("FA_2");
		}
		return htmltext;
	}

	private String getSongTwo32122(QuestState st)
	{
		String htmltext = "32122-45.htm";
		if (st.getInt(CONDITION) == 15 && st.getInt("FA") > 0 && st.getInt("SOL") > 0 && st.getInt("TI") > 0 && st.getInt("SOL_2") > 0 && st.getInt("FA_2") > 0)
		{
			htmltext = "32122-63.htm";
			st.set(CONDITION, "16");
			st.unset("FA");
			st.unset("SOL");
			st.unset("TI");
			st.unset("SOL_2");
			st.unset("FA3_2");
		}
		return htmltext;
	}

	private String getSongTri32122(QuestState st)
	{
		String htmltext = "32122-66.htm";
		if (st.getInt(CONDITION) == 16 && st.getInt("SOL") > 0 && st.getInt("FA") > 0 && st.getInt("MI") > 0 && st.getInt("FA_2") > 0 && st.getInt("MI_2") > 0)
		{
			htmltext = "32122-84.htm";
			st.set(CONDITION, "17");
			st.unset("SOL");
			st.unset("FA");
			st.unset("MI");
			st.unset("FA_2");
			st.unset("MI_2");
		}
		return htmltext;
	}

	@Override
	public final String onEvent(String event, QuestState st)
	{
		String htmltext = event;

		if (event.equalsIgnoreCase("32115-05.htm"))
		{
			st.setState(State.STARTED);
			st.set(CONDITION, "1");
		}
		else if (event.equalsIgnoreCase("32115-10.htm"))
		{
			st.set(CONDITION, "2");
		}
		else if (event.equalsIgnoreCase("32119-02.htm"))
		{
			st.set(CONDITION, "3");
		}
		else if (event.equalsIgnoreCase("32119-09.htm"))
		{
			st.set(CONDITION, "4");
		}
		else if (event.equalsIgnoreCase("32119-11.htm"))
		{
			st.set(CONDITION, "5");
		}
		else if (event.equalsIgnoreCase("32120-07.htm"))
		{
			st.set(CONDITION, "6");
		}
		else if (event.equalsIgnoreCase("32120-09.htm"))
		{
			st.set(CONDITION, "7");
		}
		else if (event.equalsIgnoreCase("32120-11.htm"))
		{
			st.set(CONDITION, "8");
		}
		else if (event.equalsIgnoreCase("32121-07.htm"))
		{
			st.set(CONDITION, "9");
		}
		else if (event.equalsIgnoreCase("32121-10.htm"))
		{
			st.set(CONDITION, "10");
		}
		else if (event.equalsIgnoreCase("32121-15.htm"))
		{
			st.set(CONDITION, "11");
		}
		else if (event.equalsIgnoreCase("32122-03.htm"))
		{
			st.set(CONDITION, "12");
		}
		else if (event.equalsIgnoreCase("32122-15.htm"))
		{
			st.set(CONDITION, "13");
		}
		else if (event.equalsIgnoreCase("32122-18.htm"))
		{
			st.set(CONDITION, "14");
		}
		else if (event.equalsIgnoreCase("32122-87.htm"))
		{
			htmltext = "32122-87.htm";
			st.giveItems(BONEPOWDER, 1);
		}
		else if (event.equalsIgnoreCase("32122-90.htm"))
		{
			st.set(CONDITION, "18");
		}
		else if (event.equalsIgnoreCase("32109-02.htm"))
		{
			st.set(CONDITION, "19");
		}
		else if (event.equalsIgnoreCase("32109-19.htm"))
		{
			st.set(CONDITION, "20");
			st.takeItems(BONEPOWDER, 1);
		}
		else if (event.equalsIgnoreCase("32115-21.htm"))
		{
			st.set(CONDITION, "21");
		}
		else if (event.equalsIgnoreCase("32115-28.htm"))
		{
			st.set(CONDITION, "22");
		}
		else if (event.equalsIgnoreCase("32114-08.htm"))
		{
			st.set(CONDITION, "23");
		}
		else if (event.equalsIgnoreCase("32114-09.htm"))
		{
			st.giveItems(EWA, 1);
			st.giveItems(ADENA, 460483);
			st.addExpAndSp(1015973, 102802);
			st.unset(CONDITION);
			st.setState(State.COMPLETED);
			st.exitQuest(false);
		}
		else if (event.equalsIgnoreCase("DOOne"))
		{
			htmltext = "32122-26.htm";
			if (st.getInt("DO") < 1)
				st.set("DO", "1");
		}
		else if (event.equalsIgnoreCase("MIOne"))
		{
			htmltext = "32122-30.htm";
			if (st.getInt("MI") < 1)
				st.set("MI", "1");
		}
		else if (event.equalsIgnoreCase("FAOne"))
		{
			htmltext = "32122-34.htm";
			if (st.getInt("FA") < 1)
				st.set("FA", "1");
		}
		else if (event.equalsIgnoreCase("SOLOne"))
		{
			htmltext = "32122-38.htm";
			if (st.getInt("SOL") < 1)
				st.set("SOL", "1");
		}
		else if (event.equalsIgnoreCase("FA_2One"))
		{
			if (st.getInt("FA_2") < 1)
				st.set("FA_2", "1");
			htmltext = getSongOne32122(st);
		}
		else if (event.equalsIgnoreCase("FATwo"))
		{
			htmltext = "32122-47.htm";
			if (st.getInt("FA") < 1)
				st.set("FA", "1");
		}
		else if (event.equalsIgnoreCase("SOLTwo"))
		{
			htmltext = "32122-51.htm";
			if (st.getInt("SOL") < 1)
				st.set("SOL", "1");
		}
		else if (event.equalsIgnoreCase("TITwo"))
		{
			htmltext = "32122-55.htm";
			if (st.getInt("TI") < 1)
				st.set("TI", "1");
		}
		else if (event.equalsIgnoreCase("SOL_2Two"))
		{
			htmltext = "32122-59.htm";
			if (st.getInt("SOL_2") < 1)
				st.set("SOL_2", "1");
		}
		else if (event.equalsIgnoreCase("FA_2Two"))
		{
			if (st.getInt("FA_2") < 1)
				st.set("FA_2", "1");
			htmltext = getSongTwo32122(st);
		}
		else if (event.equalsIgnoreCase("SOLTri"))
		{
			htmltext = "32122-68.htm";
			if (st.getInt("SOL") < 1)
				st.set("SOL", "1");
		}
		else if (event.equalsIgnoreCase("FATri"))
		{
			htmltext = "32122-72.htm";
			if (st.getInt("FA") < 1)
				st.set("FA", "1");
		}
		else if (event.equalsIgnoreCase("MITri"))
		{
			htmltext = "32122-76.htm";
			if (st.getInt("MI") < 1)
				st.set("MI", "1");
		}
		else if (event.equalsIgnoreCase("FA_2Tri"))
		{
			htmltext = "32122-80.htm";
			if (st.getInt("FA_2") < 1)
				st.set("FA_2", "1");
		}
		else if (event.equalsIgnoreCase("MI_2Tri"))
		{
			if (st.getInt("MI_2") < 1)
				st.set("MI_2", "1");
			htmltext = getSongTri32122(st);
		}

		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		int npcId = npc.getNpcId();
		int cond = st.getInt(CONDITION);
		QuestState qs126 = st.getPlayer().getQuestState("_126_TheNameOfEvil2");
		if (qs126.isCompleted())
		{
			htmltext = QUEST_DONE;
		}
		else
		{
			switch (npcId)
			{
				case ASAMANAH:
					QuestState qs125 = st.getPlayer().getQuestState("_125_TheNameOfEvil1");
					if (cond == 0)
					{
						if ((qs125 != null && qs125.isCompleted()))
						{
							htmltext = "32115-01.htm";
							st.exitQuest(true);
						}
						else if (st.getPlayer().getLevel() < 77)
						{
							htmltext = "32115-02.htm";
							st.exitQuest(true);
						}
						else
						{
							htmltext = "32115-04.htm";
							st.exitQuest(true);
						}
					}
					else if (cond == 1)
						htmltext = "32115-11.htm";
					else if (cond > 1 && cond < 20)
						htmltext = "32115-12.htm";
					else if (cond == 20)
						htmltext = "32115-13.htm";
					else if (cond == 21)
						htmltext = "32115-22.htm";
					else if (cond == 22)
						htmltext = "32115-29.htm";
					break;
				case ULU_KAIMU:
					if (cond == 1)
						htmltext = "32119-01a.htm";
					else if (cond == 2)
						htmltext = "32119-02.htm";
					else if (cond == 3)
						htmltext = "32119-08.htm";
					else if (cond == 4)
						htmltext = "32119-09.htm";
					else if (cond >= 5)
						htmltext = "32119-12.htm";
					break;
				case BALU_KAIMU:
					if (cond < 5)
						htmltext = "32120-02.htm";
					else if (cond == 5)
						htmltext = "32120-01.htm";
					else if (cond == 6)
						htmltext = "32120-03.htm";
					else if (cond == 7)
						htmltext = "32120-08.htm";
					else if (cond >= 8)
						htmltext = "32120-12.htm";
					break;
				case CHUTA_KAIMU:
					if (cond < 8)
						htmltext = "32121-02.htm";
					else if (cond == 8)
						htmltext = "32121-01.htm";
					else if (cond == 9)
						htmltext = "32121-03.htm";
					else if (cond == 10)
						htmltext = "32121-10.htm";
					else if (cond >= 11)
						htmltext = "32121-16.htm";
					break;
				case WARRIOR_GRAVE:
					if (cond < 11)
						htmltext = "32122-02.htm";
					else if (cond == 11)
						htmltext = "32122-01.htm";
					else if (cond == 12)
						htmltext = "32122-15.htm";
					else if (cond == 13)
						htmltext = "32122-18.htm";
					else if (cond == 14)
						htmltext = "32122-24.htm";
					else if (cond == 15)
						htmltext = "32122-45.htm";
					else if (cond == 16)
						htmltext = "32122-66.htm";
					else if (cond == 17)
						htmltext = "32122-84.htm";
					else if (cond == 18)
						htmltext = "32122-91.htm";
					break;
				case SHILEN_STONE_STATUE:
					if (cond < 18)
						htmltext = "32109-03.htm";
					else if (cond == 18)
						htmltext = "32109-02.htm";
					else if (cond == 19)
						htmltext = "32109-05.htm";
					else if (cond > 19)
						htmltext = "32109-04.htm";
					break;
				case MUSHIKA:
					if (cond < 22)
						htmltext = "32114-02.htm";
					else if (cond == 22)
						htmltext = "32114-01.htm";
					else if (cond == 23)
						htmltext = "32114-04.htm";
					break;
				default:
					htmltext = QUEST_DONE;
					break;
			}
		}

		return htmltext;
	}

	public static void main(String[] args)
	{
		// Quest class and state definition
		new TheNameOfEvil2(126, QN, "The Name of Evil - 2");
	}
}
