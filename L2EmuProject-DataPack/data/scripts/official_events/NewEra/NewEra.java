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
package official_events.NewEra;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.GlobalDropTable;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.system.script.DateRange;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author Gnat
 */
public class NewEra extends QuestJython
{
	private final static String		QN				= "NewEra";

	/**
	 * Event beginning and end date.
	 */
	//change date as you want
	private static final DateRange	EVENT_DATES		= DateRange.parse(Config.NEW_ERA_DATE, new SimpleDateFormat("dd MM yyyy", Locale.US));
	private static final Date		_startDate		= EVENT_DATES.getStartDate();
	private static final Date		_endDate		= EVENT_DATES.getEndDate();
	private static final Date		_currentDate	= new Date();

	//Items
	private final static int		_letterL		= 3882;
	private final static int		_letterI		= 3881;
	private final static int		_letterN		= 3883;
	private final static int		_letterE		= 3877;
	private final static int		_letterA		= 3875;
	private final static int		_letterG		= 3879;
	private final static int		_letterII		= 3888;
	private final static int		_letterT		= 3887;
	private final static int		_letterH		= 3880;
	private final static int		_letterR		= 3885;
	private final static int		_letterO		= 3884;
	private final static int[]		GLOBAL_DROP		=
													{
			_letterL,
			_letterI,
			_letterN,
			_letterE,
			_letterA,
			_letterG,
			_letterII,
			_letterT,
			_letterH,
			_letterR,
			_letterO,
			_letterN								};

	private final static int[]		_eventSpawnX	=
													{
			147698,
			147443,
			81921,
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
			-117195								};
	private final static int[]		_eventSpawnY	=
													{
			-56025,
			26942,
			148638,
			53573,
			143254,
			218933,
			122914,
			-143166,
			150092,
			76642,
			-47758,
			49387,
			16753,
			244761,
			-178692,
			-113119,
			46837									};
	private final static int[]		_eventSpawnZ	=
													{
			-2775,
			-2205,
			-3473,
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
			367									};

	private final static int		NPC				= 31854;

	private static List<L2Npc>		_eventManagers	= new ArrayList<L2Npc>();

	private static boolean			_newEraEvent	= false;

	public NewEra(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int itemId : GLOBAL_DROP)
			GlobalDropTable.getInstance().addGlobalDrop(itemId, 1, 1, Config.NEW_ERA_DROP_CHANCE * 10000, EVENT_DATES.getStartDate(), EVENT_DATES.getEndDate());

		Announcements.getInstance().addAnnouncement(false, "New Era Event is currently active.", _startDate, _endDate);

		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
		startQuestTimer("EventCheck", 1800000, null, null);

		if (EVENT_DATES.isWithinRange(_currentDate))
		{
			_newEraEvent = true;
		}

		if (_newEraEvent)
		{
			System.out.println("New Era Event - ON");

			for (int i = 0; i < _eventSpawnX.length; i++)
			{
				L2Npc eventManager = addSpawn(NPC, _eventSpawnX[i], _eventSpawnY[i], _eventSpawnZ[i], 0, false, 0);
				_eventManagers.add(eventManager);
			}
		}
		else
		{
			System.out.println("New Era Event - OFF");

			Calendar endWeek = Calendar.getInstance();
			endWeek.setTime(_endDate);
			endWeek.add(Calendar.DATE, 7);

			if (_endDate.before(_currentDate) && endWeek.getTime().after(_currentDate))
			{
				for (int i = 0; i < _eventSpawnX.length; i++)
				{
					L2Npc eventManager = addSpawn(NPC, _eventSpawnX[i], _eventSpawnY[i], _eventSpawnZ[i], 0, false, 0);
					_eventManagers.add(eventManager);
				}
			}
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(QN);

		if (st == null)
			newQuestState(player);

		int prize, l2day;

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

				if (!_newEraEvent && Event1)
				{
					_newEraEvent = true;
					System.out.println("New Era Event - ON");
					Announcements.getInstance().announceToAll("New Era Event is currently active. See the Event NPCs to participate!");

					for (int i = 0; i < _eventSpawnX.length; i++)
					{
						L2Npc eventManager = addSpawn(NPC, _eventSpawnX[i], _eventSpawnY[i], _eventSpawnZ[i], 0, false, 0);
						_eventManagers.add(eventManager);
					}
				}
				else if (_newEraEvent && !Event1)
				{
					_newEraEvent = false;
					System.out.println("New Era Event - OFF");
					for (L2Npc eventManager : _eventManagers)
					{
						eventManager.deleteMe();
					}
				}
			}
		}
		else if (event.equalsIgnoreCase("LINEAGEII"))
		{
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
				l2day = Rnd.get(10);

				if (prize <= 5)
					st.giveItems(6660, 1); // 1 - Ring of Ant Queen
				else if (prize <= 10)
					st.giveItems(6662, 1); // 1 - Ring of Core
				else if (prize <= 25)
					st.giveItems(8949, 1); // 1 - Fairy Antennae
				else if (prize <= 50)
					st.giveItems(8950, 1); // 1 - Feathered Hat
				else if (prize <= 75)
					st.giveItems(8947, 1); // 1 - Rabbit Ears
				else if (prize <= 100)
					st.giveItems(729, 1); // 1 - Scroll Enchant Weapon A Grade
				else if (prize <= 200)
					st.giveItems(947, 2); // 2 - Scroll Enchant Weapon B Grade
				else if (prize <= 300)
					st.giveItems(951, 3); // 3 - Scroll Enchant Weapon C Grade
				else if (prize <= 400)
					st.giveItems(3936, 1); // 1 - Blessed Scroll of Resurrection
				else if (prize <= 500)
					st.giveItems(1538, 1); // 1 - Blessed Scroll of Escape
				else if (l2day == 1)
					st.giveItems(3926, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 2)
					st.giveItems(3927, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 3)
					st.giveItems(3928, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 4)
					st.giveItems(3929, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 5)
					st.giveItems(3930, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 6)
					st.giveItems(3931, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 7)
					st.giveItems(3932, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 8)
					st.giveItems(3933, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 9)
					st.giveItems(3934, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
				else
					st.giveItems(3935, 3); // 3 - Random L2 Day Buff Scrolls 2 of the same type
			}
			else
				htmltext = "31854-03.htm";
		}
		else if (event.equalsIgnoreCase("THRONE"))
		{
			if (st.getQuestItemsCount(_letterT) >= 1 && st.getQuestItemsCount(_letterH) >= 1 && st.getQuestItemsCount(_letterR) >= 1
					&& st.getQuestItemsCount(_letterO) >= 1 && st.getQuestItemsCount(_letterN) >= 1 && st.getQuestItemsCount(_letterE) >= 1)
			{
				st.takeItems(_letterT, 1);
				st.takeItems(_letterH, 1);
				st.takeItems(_letterR, 1);
				st.takeItems(_letterO, 1);
				st.takeItems(_letterN, 1);
				st.takeItems(_letterE, 1);

				prize = Rnd.get(1000);
				l2day = Rnd.get(10);

				if (prize <= 5)
					st.giveItems(6660, 1); // 1 - Ring of Ant Queen
				else if (prize <= 10)
					st.giveItems(6662, 1); // 1 - Ring of Core
				else if (prize <= 25)
					st.giveItems(8951, 1); // 1 - Artisans Goggles
				else if (prize <= 50)
					st.giveItems(8948, 1); // 1 - Little Angel Wings
				else if (prize <= 75)
					st.giveItems(947, 2); // 2 - Scroll Enchant Weapon B Grade
				else if (prize <= 100)
					st.giveItems(951, 3); // 3 - Scroll Enchant Weapon C Grade
				else if (prize <= 150)
					st.giveItems(955, 4); // 4 - Scroll Enchant Weapon D Grade
				else if (prize <= 200)
					st.giveItems(3936, 1); // 1 - Blessed Scroll of Resurrection
				else if (prize <= 300)
					st.giveItems(1538, 1); // 1 - Blessed Scroll of Escape
				else if (l2day == 1)
					st.giveItems(3926, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 2)
					st.giveItems(3927, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 3)
					st.giveItems(3928, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 4)
					st.giveItems(3929, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 5)
					st.giveItems(3930, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 6)
					st.giveItems(3931, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 7)
					st.giveItems(3932, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 8)
					st.giveItems(3933, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				else if (l2day == 9)
					st.giveItems(3934, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
				else
					st.giveItems(3935, 2); // 2 - Random L2 Day Buff Scrolls 2 of the same type
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
			st = this.newQuestState(player);

		return "31854.htm";
	}

	public static void main(String[] args)
	{
		if (Config.ALLOW_NEW_ERA)
		{
			new NewEra(-1, QN, "official_events");
			_log.info("Official Events: New Era is loaded.");
		}
		else
			_log.info("Official Events: New Era is disabled.");
	}
}
