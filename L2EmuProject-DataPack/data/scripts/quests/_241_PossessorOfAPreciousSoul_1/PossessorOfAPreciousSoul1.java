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
package quests._241_PossessorOfAPreciousSoul_1;

import net.l2emuproject.gameserver.services.quest.Quest;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author Intrepid
 *
 */
public class PossessorOfAPreciousSoul1 extends Quest
{
	private static final String	QN							= "_241_PossessorOfAPreciousSoul_1";

	// NPC List
	private final int			STEDMIEL					= 30692;
	private final int			GABRIELLE					= 30753;
	private final int			GILMORE						= 30754;
	private final int			KANTABILON					= 31042;
	private final int			RAHORAKTI					= 31336;
	private final int			TALIEN						= 31739;
	private final int			CARADINE					= 31740;
	private final int			VIRGIL						= 31742;
	private final int			KASSANDRA					= 31743;
	private final int			OGMAR						= 31744;

	// Mob List
	private final int			BARAHAM						= 27113;
	private final int			TAIK_ORC_SUPPLY_LEADER		= 20669;
	private final int			MALRUK_SUCCUBUS				= 20244;
	private final int			MALRUK_SUCCUBUS_TUREN		= 20245;
	private final int			SPLINTER_STAKATO			= 21508;
	private final int			SPLINTER_STAKATO_WORKER		= 21509;
	private final int			SPLINTER_STAKATO_SOLDIER	= 21510;
	private final int			SPLINTER_STAKATO_DRONE_69	= 21511;
	private final int			SPLINTER_STAKATO_DRONE_70	= 21512;

	// Quest Item List
	private final int			LEGEND_OF_SEVENTEEN			= 7587;
	private final int			MALRUK_SUCCUBUS_CLAW		= 7597;
	private final int			ECHO_CRYSTAL				= 7589;
	private final int			POETRY_BOOK					= 7588;
	private final int			CRIMSON_MOSS				= 7598;
	private final int			RAHORAKTIS_MEDICINE			= 7599;
	private final int			VIRGILS_LETTER				= 7677;

	// Drop Chance - In high5 drop chances increased need confirmed chance
	private final int			CRIMSON_MOSS_CHANCE			= 8;
	private final int			MALRUK_SUCCUBUS_CLAW_CHANCE	= 15;

	/**
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public PossessorOfAPreciousSoul1(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(TALIEN);

		addTalkId(TALIEN);
		addTalkId(STEDMIEL);
		addTalkId(GABRIELLE);
		addTalkId(GILMORE);
		addTalkId(KANTABILON);
		addTalkId(RAHORAKTI);
		addTalkId(CARADINE);
		addTalkId(VIRGIL);
		addTalkId(KASSANDRA);
		addTalkId(OGMAR);

		addKillId(BARAHAM);
		addKillId(MALRUK_SUCCUBUS);
		addKillId(MALRUK_SUCCUBUS_TUREN);
		addKillId(SPLINTER_STAKATO);
		addKillId(SPLINTER_STAKATO_WORKER);
		addKillId(SPLINTER_STAKATO_SOLDIER);
		addKillId(SPLINTER_STAKATO_DRONE_69);
		addKillId(SPLINTER_STAKATO_DRONE_70);
		addKillId(TAIK_ORC_SUPPLY_LEADER);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		int cond = st.getInt(CONDITION);

		if (st.getPlayer().isSubClassActive())
		{
			if (event.equalsIgnoreCase("31739-4.htm"))
			{
				if (cond == 0)
				{
					st.setState(State.STARTED);
					st.set(CONDITION, "1");
					st.playSound("ItemSound.quest_accept");
				}
			}
			else if (event.equalsIgnoreCase("30753-2.htm"))
			{
				if (cond == 1)
				{
					st.set(CONDITION, "2");
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if (event.equalsIgnoreCase("30754-2.htm"))
			{
				if (cond == 2)
				{
					st.set(CONDITION, "3");
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if (event.equalsIgnoreCase("31739-8.htm"))
			{
				if (cond == 4 && st.hasQuestItems(LEGEND_OF_SEVENTEEN))
				{
					st.set(CONDITION, "5");
					st.takeItems(LEGEND_OF_SEVENTEEN, 1);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if (event.equalsIgnoreCase("31042-2.htm"))
			{
				if (cond == 5)
				{
					st.set(CONDITION, "6");
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if (event.equalsIgnoreCase("31042-5.htm"))
			{
				if (cond == 7 && st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW) >= 10)
				{
					st.set(CONDITION, "8");
					st.takeItems(MALRUK_SUCCUBUS_CLAW, 10);
					st.giveItems(ECHO_CRYSTAL, 1);
					st.playSound("ItemSound.quest_middle");
				}
			}
			else if (event.equalsIgnoreCase("31739-12.htm"))
			{
				if (cond == 8 && st.hasQuestItems(ECHO_CRYSTAL))
				{
					st.set(CONDITION, "9");
					st.takeItems(ECHO_CRYSTAL, 1);
					st.playSound("ItemSound.quest_accept");
				}
			}
			else if (event.equalsIgnoreCase("30692-2.htm"))
			{
				if (cond == 9 && !st.hasQuestItems(POETRY_BOOK))
				{
					st.set(CONDITION, "10");
					st.giveItems(POETRY_BOOK, 1);
					st.playSound("ItemSound.quest_accept");
				}
			}
			else if (event.equalsIgnoreCase("31739-15.htm"))
			{
				if (cond == 10 && st.hasQuestItems(POETRY_BOOK))
				{
					st.set(CONDITION, "11");
					st.takeItems(POETRY_BOOK, 1);
					st.playSound("ItemSound.quest_accept");
				}
			}
			else if (event.equalsIgnoreCase("31742-2.htm"))
			{
				if (cond == 11)
				{
					st.set(CONDITION, "12");
					st.playSound("ItemSound.quest_accept");
				}
			}
			else if (event.equalsIgnoreCase("31744-2.htm"))
			{
				if (cond == 12)
				{
					st.set(CONDITION, "13");
					st.playSound("ItemSound.quest_accept");
				}
			}
			else if (event.equalsIgnoreCase("31336-2.htm"))
			{
				if (cond == 13)
				{
					st.set(CONDITION, "14");
					st.playSound("ItemSound.quest_accept");
				}
			}
			else if (event.equalsIgnoreCase("31336-5.htm"))
			{
				if (cond == 15 && st.getQuestItemsCount(CRIMSON_MOSS) >= 10)
				{
					st.set(CONDITION, "16");
					st.takeItems(CRIMSON_MOSS, 5);
					st.giveItems(RAHORAKTIS_MEDICINE, 1);
					st.playSound("ItemSound.quest_accept");
				}
			}
			else if (event.equalsIgnoreCase("31743-2.htm"))
			{
				if (cond == 16 && st.hasQuestItems(RAHORAKTIS_MEDICINE))
				{
					st.set(CONDITION, "17");
					st.takeItems(RAHORAKTIS_MEDICINE, 1);
					st.playSound("ItemSound.quest_accept");
				}
			}
			else if (event.equalsIgnoreCase("31740-2.htm"))
			{
				if (cond == 18)
				{
					st.giveItems(VIRGILS_LETTER, 1);
					st.addExpAndSp(263043, 0);
					st.set(CONDITION, "0");
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
				}
			}
		}

		return htmltext;
	}

	@Override
	public final String onTalk(L2Npc npc, L2Player player)
	{
		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);
		int cond = st.getInt(CONDITION);
		int npcId = npc.getNpcId();

		switch (npcId)
		{
			case TALIEN:
			{
				switch (cond)
				{
					case 0:
					{
						if (st.getState() == State.COMPLETED)
							htmltext = "<html><body>This quest has already been completed.</body></html>";
						else if (player.getLevel() >= 50 && player.isSubClassActive())
							htmltext = "31739-1.htm";
						else
							htmltext = "31739-2.htm";
					}
					case 1:
						if (st.getPlayer().isSubClassActive())
							htmltext = "31739-5.htm";
						else
							htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
					case 4:
						if (st.getPlayer().isSubClassActive())
						{
							if (st.hasQuestItems(LEGEND_OF_SEVENTEEN))
								htmltext = "31739-6.htm";
						}
						else
							htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
					case 5:
						if (st.getPlayer().isSubClassActive())
							htmltext = "31739-9.htm";
						else
							htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
					case 8:
						if (st.getPlayer().isSubClassActive())
						{
							if (st.hasQuestItems(ECHO_CRYSTAL))
								htmltext = "31739-11.htm";
						}
						else
							htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
					case 9:
						if (st.getPlayer().isSubClassActive())
							htmltext = "31739-13.htm";
						else
							htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
					case 10:
						if (st.getPlayer().isSubClassActive())
						{
							if (st.hasQuestItems(POETRY_BOOK))
								htmltext = "31739-14.htm";
						}
						else
							htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
					case 11:
						if (st.getPlayer().isSubClassActive())
							htmltext = "31739-16.htm";
						else
							htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
				}
			}
			case GABRIELLE:
				if (st.getPlayer().isSubClassActive())
				{
					if (cond == 1)
						htmltext = "30753-1.htm";
					else if (cond == 2)
						htmltext = "30753-3.htm";
				}
				else
					htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
			case GILMORE:
				if (st.getPlayer().isSubClassActive())
				{
					if (cond == 2)
						htmltext = "30754-1.htm";
					else if (cond == 3)
						htmltext = "30754-3.htm";
				}
				else
					htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
			case KANTABILON:
				if (st.getPlayer().isSubClassActive())
				{
					if (cond == 5)
						htmltext = "31042-1.htm";
					else if (cond == 6)
						htmltext = "31042-4.htm";
					else if (cond == 7 && st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW) == 10)
						htmltext = "31042-3.htm";
					else if (cond == 8)
						htmltext = "31042-6.htm";
				}
				else
					htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
			case STEDMIEL:
				if (st.getPlayer().isSubClassActive())
				{
					if (cond == 9)
						htmltext = "30692-1.htm";
					else if (cond == 10)
						htmltext = "30692-3.htm";
				}
				else
					htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
			case VIRGIL:
				if (st.getPlayer().isSubClassActive())
				{
					if (cond == 11)
						htmltext = "31742-1.htm";
					else if (cond == 12)
						htmltext = "31742-3.htm";
					else if (cond == 17)
						htmltext = "31742-4.htm";
					else if (cond == 18)
						htmltext = "31742-6.htm";
				}
				else
					htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
			case OGMAR:
				if (st.getPlayer().isSubClassActive())
				{
					if (cond == 12)
						htmltext = "31744-1.htm";
					else if (cond == 13)
						htmltext = "31744-3.htm";
				}
				else
					htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
			case RAHORAKTI:
				if (st.getPlayer().isSubClassActive())
				{
					if (cond == 13)
						htmltext = "31336-1.htm";
					else if (cond == 14)
						htmltext = "31336-4.htm";
					else if (cond == 15 && st.getQuestItemsCount(CRIMSON_MOSS) == 5)
						htmltext = "31336-3.htm";
					else if (cond == 16)
						htmltext = "31336-6.htm";
				}
				else
					htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
			case KASSANDRA:
				if (st.getPlayer().isSubClassActive())
				{
					if (cond == 16 && st.getQuestItemsCount(RAHORAKTIS_MEDICINE) == 1)
						htmltext = "31743-1.htm";
					else if (cond == 17)
						htmltext = "31743-3.htm";
				}
				else
					htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
			case CARADINE:
				if (st.getPlayer().isSubClassActive())
				{
					if (cond == 18)
						htmltext = "31740-4.htm";
				}
				else
					htmltext = "<html><body>This quest may only be undertaken by sub-class characters of level 50 or above.</body></html>";
		}

		return htmltext;
	}

	@Override
	public final String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		int npcId = npc.getNpcId();
		int cond = st.getInt(CONDITION);

		switch (npcId)
		{
			case BARAHAM:
			{
				if (cond == 3)
				{
					st.set(CONDITION, "4");
					st.giveItems(LEGEND_OF_SEVENTEEN, 1);
					st.playSound("ItemSound.quest_itemget");
				}
			}
			case MALRUK_SUCCUBUS:
			case MALRUK_SUCCUBUS_TUREN:
			{
				if (cond == 6)
				{
					int chance = st.getRandom(100);
					if (MALRUK_SUCCUBUS_CLAW_CHANCE >= chance && st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW) < 10)
					{
						st.giveItems(MALRUK_SUCCUBUS_CLAW, 1);
						st.playSound("ItemSound.quest_itemget");
						if (st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW) == 10)
						{
							st.set("cond", "7");
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
			}
			case SPLINTER_STAKATO:
			case SPLINTER_STAKATO_WORKER:
			case SPLINTER_STAKATO_SOLDIER:
			case SPLINTER_STAKATO_DRONE_69:
			case SPLINTER_STAKATO_DRONE_70:
			{
				if (cond == 14)
				{
					int chance = st.getRandom(100);
					if (CRIMSON_MOSS_CHANCE >= chance && st.getQuestItemsCount(CRIMSON_MOSS_CHANCE) < 5)
					{
						st.giveItems(CRIMSON_MOSS_CHANCE, 1);
						st.playSound("ItemSound.quest_itemget");
						if (st.getQuestItemsCount(CRIMSON_MOSS_CHANCE) == 5)
						{
							st.set("cond", "15");
							st.playSound("ItemSound.quest_middle");
						}
					}
				}
			}
		}

		return "";
	}

	public static void main(String[] args)
	{
		new PossessorOfAPreciousSoul1(237, QN, "Possessor Of A Precious Soul 1");
	}
}
