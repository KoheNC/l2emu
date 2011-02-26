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
package official_events.FifthAnniversary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.EventDroplist;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.system.script.DateRange;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author Gigiikun
 */
public class FifthAnniversary extends QuestJython
{
	private final static String		QN						= "FifthAnniversary";

	/**
	 * Event beginning and end date.
	 */
	private static final DateRange	EVENT_DATES				= DateRange.parse(Config.FIFTH_ANNIVERSARY_DATE, new SimpleDateFormat("dd MM yyyy", Locale.US));
	private static final String[]	EVENT_ANNOUNCE			=
															{ "5th Anniversary Event is currently active." };
	private static final Date		_endDate				= EVENT_DATES.getEndDate();
	private static final Date		_currentDate			= new Date();

	// Items
	private final static int		_letterL				= 3882;
	private final static int		_letterI				= 3881;
	private final static int		_letterN				= 3883;
	private final static int		_letterE				= 3877;
	private final static int		_letterA				= 3875;
	private final static int		_letterG				= 3879;
	private final static int		_letterII				= 3888;
	private final static int		_letter5				= 13418;
	private final static int		_letterY				= 13417;
	private final static int		_letterR				= 3885;
	private final static int		_letterS				= 3886;
	private final static int		_letterC				= 3876;
	private final static int[]		_dropList				=
															{
			_letterL,
			_letterI,
			_letterN,
			_letterE,
			_letterA,
			_letterG,
			_letterII,
			_letter5,
			_letterY,
			_letterR,
			_letterS,
			_letterC										};
	private final int[]				_dropCount				=
															{ 1, 1 };
	private final static int		_dropChance				= 25000;																							// actually 2.5%
																																								// Goddard,Aden,Giran, Oren,Dion,Heine,Gludio,Schuttgart,Gludin,Hunters,Rune,SoDA,Dark Elf,TI,Dwarf,Orc,Kamael
	private final static int[]		_eventSpawnX			=
															{
			147698,
			147443,
			82227,
			82754,
			15064,
			111067,
			-12965,
			87362,
			-81037,
			117412,
			43983,
			-45907,
			12153,
			-84458,
			114750,
			-45656,
			-117195										};
	private final static int[]		_eventSpawnY			=
															{
			-56025,
			26942,
			148609,
			53573,
			143254,
			218933,
			122914,
			143166,
			150092,
			76642,
			-47758,
			49387,
			16753,
			244761,
			-178692,
			-113119,
			46837											};
	private final static int[]		_eventSpawnZ			=
															{
			-2775,
			-2205,
			-3472,
			-1496,
			-2668,
			-3543,
			-3117,
			-1293,
			-3044,
			-2695,
			-797,
			-3060,
			-4584,
			-3730,
			-820,
			-240,
			367											};

	private final static int		_eventNPC				= 31854;

	private static List<L2Npc>		_eventManagers			= new ArrayList<L2Npc>();

	private static boolean			_fifthAnniversaryEvent	= false;

	public FifthAnniversary(int questId, String name, String descr)
	{
		super(questId, name, descr);

		EventDroplist.getInstance().addGlobalDrop(_dropList, _dropCount, _dropChance, EVENT_DATES);

		Announcements.getInstance().addEventAnnouncement(EVENT_DATES, EVENT_ANNOUNCE);

		addStartNpc(_eventNPC);
		addFirstTalkId(_eventNPC);
		addTalkId(_eventNPC);
		startQuestTimer("EventCheck", 1800000, null, null);

		if (EVENT_DATES.isWithinRange(_currentDate))
		{
			_fifthAnniversaryEvent = true;
		}

		if (_fifthAnniversaryEvent)
		{
			_log.info("5th Anniversary Event - ON");

			for (int i = 0; i < _eventSpawnX.length; i++)
			{
				L2Npc eventManager = addSpawn(_eventNPC, _eventSpawnX[i], _eventSpawnY[i], _eventSpawnZ[i], 0, false, 0);
				_eventManagers.add(eventManager);
			}
		}
		else
		{
			_log.info("5th Anniversary Event - OFF");

			Calendar endWeek = Calendar.getInstance();
			endWeek.setTime(_endDate);
			endWeek.add(Calendar.DATE, 7);

			if (_endDate.before(_currentDate) && endWeek.getTime().after(_currentDate))
			{
				for (int i = 0; i < _eventSpawnX.length; i++)
				{
					L2Npc eventManager = addSpawn(_eventNPC, _eventSpawnX[i], _eventSpawnY[i], _eventSpawnZ[i], 0, false, 0);
					_eventManagers.add(eventManager);
				}
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = "";
		QuestState st;
		int prize;

		if (npc == null)
		{
			if (event.equalsIgnoreCase("EventCheck"))
			{
				this.startQuestTimer("EventCheck", 1800000, null, null);
				boolean Event1 = false;

				if (EVENT_DATES.isWithinRange(_currentDate))
				{
					Event1 = true;
				}

				if (!_fifthAnniversaryEvent && Event1)
				{
					_fifthAnniversaryEvent = true;
					_log.info("5th Anniversary Event - ON");
					Announcements.getInstance().announceToAll("5th Anniversary Event is currently active. See the Event NPCs to participate!");

					for (int i = 0; i < _eventSpawnX.length; i++)
					{
						L2Npc eventManager = addSpawn(_eventNPC, _eventSpawnX[i], _eventSpawnY[i], _eventSpawnZ[i], 0, false, 0);
						_eventManagers.add(eventManager);
					}
				}
				else if (_fifthAnniversaryEvent && !Event1)
				{
					_fifthAnniversaryEvent = false;
					_log.info("5th Anniversary Event - OFF");
					for (L2Npc eventManager : _eventManagers)
					{
						eventManager.deleteMe();
					}
				}
			}
		}
		else if (player != null && event.equalsIgnoreCase("LINEAGEII"))
		{
			st = player.getQuestState(QN);

			if (st.getQuestItemsCount(_letterL) >= 1 && st.getQuestItemsCount(_letterI) >= 1 && st.getQuestItemsCount(_letterN) >= 1
					&& st.getQuestItemsCount(_letterE) >= 2 && st.getQuestItemsCount(_letterA) >= 1 && st.getQuestItemsCount(_letterG) >= 1
					&& st.getQuestItemsCount(_letterII) >= 1)
			{
				st.takeItems(_letterL, 1);
				st.takeItems(_letterI, 1);
				st.takeItems(_letterN, 1);
				st.takeItems(_letterE, 2);
				st.takeItems(_letterA, 1);
				st.takeItems(_letterG, 1);
				st.takeItems(_letterII, 1);

				prize = Rnd.get(1000);

				if (prize <= 5)
					st.giveItems(6662, 1); // 1 - Ring of Core
				else if (prize <= 10)
					st.giveItems(8752, 1); // 1 - High grade Life Stone 76
				else if (prize <= 25)
					st.giveItems(8742, 1); // 1 - Mid grade Life Stone 76
				else if (prize <= 50)
					st.giveItems(9157, 1); // 1 - L2day Blessed SoR
				else if (prize <= 75)
					st.giveItems(9156, 1); // 1 - L2day Blessed SoE
				else if (prize <= 100)
					st.giveItems(13429, 1); // 1 - Teddy Bear Hat
				else if (prize <= 200)
					st.giveItems(13430, 1); // 1 - Piggy Hat
				else if (prize <= 300)
					st.giveItems(13431, 1); // 1 - Jester Hat
				else if (prize <= 400)
					st.giveItems(13425, 1); // 1 - Small Parchement Box (1x Village Soe)
				else if (prize <= 500)
					st.giveItems(13426, 1); // 1 - Small Mineral Box (1x Elemental Stone)
				else
					st.giveItems(13428, 1); // 1 - Small Libation Box (1x L2Day Juice)
			}
			else
				htmltext = "31854-03.htm";
		}
		else if (player != null && event.equalsIgnoreCase("5YEARS"))
		{
			st = player.getQuestState(QN);

			if (st.getQuestItemsCount(_letter5) >= 1 && st.getQuestItemsCount(_letterY) >= 1 && st.getQuestItemsCount(_letterE) >= 1
					&& st.getQuestItemsCount(_letterA) >= 1 && st.getQuestItemsCount(_letterR) >= 1 && st.getQuestItemsCount(_letterS) >= 1)
			{
				st.takeItems(_letter5, 1);
				st.takeItems(_letterY, 1);
				st.takeItems(_letterE, 1);
				st.takeItems(_letterA, 1);
				st.takeItems(_letterR, 1);
				st.takeItems(_letterS, 1);

				prize = Rnd.get(1000);

				if (prize <= 5)
					st.giveItems(6660, 1); // 1 - Ring of Queen Ant
				else if (prize <= 10)
					st.giveItems(8762, 1); // 1 - Top grade Life Stone 76
				else if (prize <= 25)
					st.giveItems(8752, 2); // 2 - High grade Life Stones 76
				else if (prize <= 50)
					st.giveItems(9157, 2); // 2 - L2day Blessed SoRs
				else if (prize <= 75)
					st.giveItems(9156, 2); // 2 - L2day Blessed SoEs
				else if (prize <= 100)
					st.giveItems(13429, 1); // 1 - Teddy Bear Hat
				else if (prize <= 150)
					st.giveItems(13430, 1); // 1 - Piggy Hat
				else if (prize <= 200)
					st.giveItems(13431, 1); // 1 - Jester Hat
				else if (prize <= 300)
					st.giveItems(13422, 2); // 1 - Medium Parchement Box (2x Village Soes)
				else if (prize <= 400)
					st.giveItems(13423, 2); // 1 - Medium Mineral Box (2x Elemental Stones)
				else
					st.giveItems(13424, 3); // 1 - Large Libation Box (3x L2Day Juices)
			}
			else
				htmltext = "31854-03.htm";
		}
		else if (player != null && event.equalsIgnoreCase("GRACIA"))
		{
			st = player.getQuestState(QN);

			if (st.getQuestItemsCount(_letterG) >= 1 && st.getQuestItemsCount(_letterR) >= 1 && st.getQuestItemsCount(_letterA) >= 2
					&& st.getQuestItemsCount(_letterC) >= 1 && st.getQuestItemsCount(_letterI) >= 1)
			{
				st.takeItems(_letterG, 1);
				st.takeItems(_letterR, 1);
				st.takeItems(_letterA, 2);
				st.takeItems(_letterC, 1);
				st.takeItems(_letterI, 1);

				prize = Rnd.get(1000);

				if (prize <= 5)
					st.giveItems(6661, 1); // 1 - Earring of Orfen
				else if (prize <= 10)
					st.giveItems(8752, 1); // 1 - High grade Life Stone 76
				else if (prize <= 25)
					st.giveItems(8742, 2); // 2 - Mid grade Life Stones 76
				else if (prize <= 50)
					st.giveItems(9157, 1); // 1 - L2day Blessed SoR
				else if (prize <= 75)
					st.giveItems(9156, 1); // 1 - L2day Blessed SoE
				else if (prize <= 100)
					st.giveItems(13429, 1); // 1 - Teddy Bear Hat
				else if (prize <= 150)
					st.giveItems(13430, 1); // 1 - Piggy Hat
				else if (prize <= 200)
					st.giveItems(13431, 1); // 1 - Jester Hat
				else if (prize <= 300)
					st.giveItems(13425, 1); // 1 - Small Parchement Box (1x Village Soe)
				else if (prize <= 400)
					st.giveItems(13426, 1); // 1 - Small Mineral Box (1x Elemental Stone)
				else
					st.giveItems(13424, 2); // 1 - Medium Libation Box (2x L2Day Juices)
			}
			else
				htmltext = "31854-03.htm";
		}
		else if (event.equalsIgnoreCase("chat0"))
			htmltext = "31854.htm";
		else if (event.equalsIgnoreCase("chat1"))
			htmltext = "31854-02.htm";

		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);

		return "31854.htm";
	}

	public static void main(String[] args)
	{
		if (Config.ALLOW_FIFTH_ANNIVERSARY)
		{
			new FifthAnniversary(-1, QN, "official_events");
			_log.info("Official Events: Fifth Anniversary is loaded.");
		}
		else
			_log.info("Official Events: Fifth Anniversary is disabled.");
	}
}
