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
package quests._999_T1Tutorial;

import javolution.util.FastMap;
import quests._255_Tutorial.Tutorial;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.services.quest.QuestState;
import net.l2emuproject.gameserver.services.quest.State;
import net.l2emuproject.gameserver.services.quest.jython.QuestJython;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;

/**
 * @author L0ngh0rn
 */
public final class T1Tutorial extends QuestJython
{
	public static final String	QN					= "_999_T1Tutorial";
	private static final String	QN_TUTORIAL			= Tutorial.QN;

	private static final int[]	NPCS				= new int[]
													{
			30008,
			30009,
			30017,
			30019,
			30129,
			30131,
			30573,
			30575,
			30370,
			30528,
			30530,
			30400,
			30401,
			30402,
			30403,
			30404,
			30600,
			30601,
			30602,
			30598,
			30599,
			32133,
			32134,
			32135									};
	private static final int[]	NPCS_KILL			= new int[]
													{ 18342, 20001 };

	// Quest Item
	private static final int	RECOMMENDATION_01	= 1067;
	private static final int	RECOMMENDATION_02	= 1068;
	private static final int	LEAF_OF_MOTHERTREE	= 1069;
	private static final int	BLOOD_OF_JUNDIN		= 1070;
	private static final int	LICENSE_OF_MINER	= 1498;
	private static final int	VOUCHER_OF_FLAME	= 1496;
	private static final int	SOULSHOT_NOVICE		= 5789;
	private static final int	SPIRITSHOT_NOVICE	= 5790;
	private static final int	BLUE_GEM			= 6353;
	private static final int	DIPLOMA				= 9881;

	private static class Event
	{
		public String	_htm;
		public Location	_loc;
		public int		_item;
		public int		_classId1;
		public int		_gift1;
		public int		_count1;
		public int		_classId2;
		public int		_gift2;
		public int		_count2;

		public Event(String htm, Location loc, int item, int classId1, int gift1, int count1, int classId2, int gift2, int count2)
		{
			this._htm = htm;
			this._loc = loc;
			this._item = item;
			this._classId1 = classId1;
			this._gift1 = gift1;
			this._count1 = count1;
			this._classId2 = classId2;
			this._gift2 = gift2;
			this._count2 = count2;
		}
	}

	private static class Talk
	{
		public int		_raceId;
		public String[]	_htm;
		public int		_npcType;
		public int		_item;

		public Talk(int raceId, String[] htm, int npcType, int item)
		{
			this._raceId = raceId;
			this._htm = htm;
			this._npcType = npcType;
			this._item = item;
		}
	}

	private static final FastMap<String, Event>	EVENTS	= new FastMap<String, Event>();
	private static final FastMap<Integer, Talk>	TALKS	= new FastMap<Integer, Talk>();

	static
	{
		EVENTS.put("30008_02", new Event("30008-03.htm", null, RECOMMENDATION_01, 0x00, SOULSHOT_NOVICE, 200, 0x00, 0, 0));
		EVENTS.put("30008_04", new Event("30008-04.htm", new Location(-84058, 243239, -3730), 0, 0x00, 0, 0, 0, 0, 0));
		EVENTS.put("30017_02", new Event("30017-03.htm", null, RECOMMENDATION_02, 0x0a, SPIRITSHOT_NOVICE, 100, 0x00, 0, 0));
		EVENTS.put("30017_04", new Event("30017-04.htm", new Location(-84058, 243239, -3730), 0, 0x0a, 0, 0, 0x00, 0, 0));
		EVENTS.put("30129_02", new Event("30129-03.htm", null, BLOOD_OF_JUNDIN, 0x26, SPIRITSHOT_NOVICE, 100, 0x1f, SOULSHOT_NOVICE, 200));
		EVENTS.put("30129_04", new Event("30129-04.htm", new Location(12116, 16666, -4610), 0, 0x26, 0, 0, 0x1f, 0, 0));
		EVENTS.put("30370_02", new Event("30370-03.htm", null, LEAF_OF_MOTHERTREE, 0x19, SPIRITSHOT_NOVICE, 100, 0x12, SOULSHOT_NOVICE, 200));
		EVENTS.put("30370_04", new Event("30370-04.htm", new Location(45491, 48359, -3086), 0, 0x19, 0, 0, 0x12, 0, 0));
		EVENTS.put("30528_02", new Event("30528-03.htm", null, LICENSE_OF_MINER, 0x35, SOULSHOT_NOVICE, 200, 0x00, 0, 0));
		EVENTS.put("30528_04", new Event("30528-04.htm", new Location(115642, -178046, -941), 0, 0x35, 0, 0, 0x00, 0, 0));
		EVENTS.put("30573_02", new Event("30573-03.htm", null, VOUCHER_OF_FLAME, 0x31, SPIRITSHOT_NOVICE, 100, 0x2c, SOULSHOT_NOVICE, 200));
		EVENTS.put("30573_04", new Event("30573-04.htm", new Location(-45067, -113549, -235), 0, 0x31, 0, 0, 0x2c, 0, 0));
		EVENTS.put("32133_02", new Event("32133-03.htm", new Location(-119692, 44504, 380), DIPLOMA, 0x7b, SOULSHOT_NOVICE, 200, 0x7c, SOULSHOT_NOVICE, 200));

		TALKS.put(30008, new Talk(0, new String[]
		{ "30008-01.htm", "30008-02.htm", "30008-04.htm" }, 0, 0));
		TALKS.put(30009, new Talk(0, new String[]
		{ "30530-01.htm", "30009-03.htm", "", "30009-04.htm", }, 1, RECOMMENDATION_01));
		TALKS.put(30011, new Talk(0, new String[]
		{ "30530-01.htm", "30009-03.htm", "", "30009-04.htm", }, 1, RECOMMENDATION_01));
		TALKS.put(30012, new Talk(0, new String[]
		{ "30530-01.htm", "30009-03.htm", "", "30009-04.htm", }, 1, RECOMMENDATION_01));
		TALKS.put(30017, new Talk(0, new String[]
		{ "30017-01.htm", "30017-02.htm", "30017-04.htm" }, 0, 0));
		TALKS.put(30018, new Talk(0, new String[]
		{ "30131-01.htm", "", "30019-03a.htm", "30019-04.htm", }, 1, RECOMMENDATION_02));
		TALKS.put(30019, new Talk(0, new String[]
		{ "30131-01.htm", "", "30019-03a.htm", "30019-04.htm", }, 1, RECOMMENDATION_02));
		TALKS.put(30020, new Talk(0, new String[]
		{ "30131-01.htm", "", "30019-03a.htm", "30019-04.htm", }, 1, RECOMMENDATION_02));
		TALKS.put(30021, new Talk(0, new String[]
		{ "30131-01.htm", "", "30019-03a.htm", "30019-04.htm", }, 1, RECOMMENDATION_02));
		TALKS.put(30056, new Talk(0, new String[]
		{ "30530-01.htm", "30009-03.htm", "", "30009-04.htm", }, 1, RECOMMENDATION_01));
		TALKS.put(30129, new Talk(2, new String[]
		{ "30129-01.htm", "30129-02.htm", "30129-04.htm" }, 0, 0));
		TALKS.put(30131, new Talk(2, new String[]
		{ "30131-01.htm", "30131-03.htm", "30131-03a.htm", "30131-04.htm", }, 1, BLOOD_OF_JUNDIN));
		TALKS.put(30370, new Talk(1, new String[]
		{ "30370-01.htm", "30370-02.htm", "30370-04.htm" }, 0, 0));
		TALKS.put(30400, new Talk(1, new String[]
		{ "30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm", }, 1, LEAF_OF_MOTHERTREE));
		TALKS.put(30401, new Talk(1, new String[]
		{ "30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm", }, 1, LEAF_OF_MOTHERTREE));
		TALKS.put(30402, new Talk(1, new String[]
		{ "30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm", }, 1, LEAF_OF_MOTHERTREE));
		TALKS.put(30403, new Talk(1, new String[]
		{ "30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm", }, 1, LEAF_OF_MOTHERTREE));
		TALKS.put(30404, new Talk(2, new String[]
		{ "30131-01.htm", "30131-03.htm", "30131-03a.htm", "30131-04.htm", }, 1, BLOOD_OF_JUNDIN));
		TALKS.put(30528, new Talk(4, new String[]
		{ "30528-01.htm", "30528-02.htm", "30528-04.htm" }, 0, 0));
		TALKS.put(30530, new Talk(4, new String[]
		{ "30530-01.htm", "30530-03.htm", "", "30530-04.htm", }, 1, LICENSE_OF_MINER));
		TALKS.put(30573, new Talk(3, new String[]
		{ "30573-01.htm", "30573-02.htm", "30573-04.htm" }, 0, 0));
		TALKS.put(30574, new Talk(3, new String[]
		{ "30575-01.htm", "30575-03.htm", "30575-03a.htm", "30575-04.htm", }, 1, VOUCHER_OF_FLAME));
		TALKS.put(30575, new Talk(3, new String[]
		{ "30575-01.htm", "30575-03.htm", "30575-03a.htm", "30575-04.htm", }, 1, VOUCHER_OF_FLAME));
		TALKS.put(30598, new Talk(0, null, 0, 0));
		TALKS.put(30599, new Talk(0, null, 0, 0));
		TALKS.put(30600, new Talk(0, null, 0, 0));
		TALKS.put(30601, new Talk(0, null, 0, 0));
		TALKS.put(30602, new Talk(0, null, 0, 0));
		TALKS.put(32133, new Talk(5, new String[]
		{ "32133-01.htm", "32133-02.htm", "32133-04.htm" }, 0, 0));
		TALKS.put(32134, new Talk(5, new String[]
		{ "32134-01.htm", "32134-03.htm", "", "32134-04.htm", }, 1, DIPLOMA));
		TALKS.put(32135, new Talk(0, null, 0, 0));
	}

	public T1Tutorial(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);

		for (int npcId : NPCS)
		{
			addStartNpc(npcId);
			addFirstTalkId(npcId);
			addTalkId(npcId);
		}

		for (int npcId : NPCS_KILL)
			addKillId(npcId);
	}

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if (Config.ALT_DISABLE_TUTORIAL)
			return null;

		QuestState st = player.getQuestState(QN);
		if (st == null)
			return event;

		QuestState qs = st.getPlayer().getQuestState(QN_TUTORIAL);
		if (qs == null)
			return event;

		final int ex = qs.getInt("Ex");
		String htmltext = event;

		if (event.equalsIgnoreCase("TimerEx_NewbieHelper"))
		{
			switch (ex)
			{
				case 0:
					if (player.getClassId().isMage())
						st.playTutorialVoice("tutorial_voice_009b");
					else
						st.playTutorialVoice("tutorial_voice_009a");
					qs.set("Ex", 1);
					break;
				case 3:
					st.playTutorialVoice("tutorial_voice_010a");
					qs.set("Ex", 4);
					break;
			}
			return null;
		}
		else if (event.equalsIgnoreCase("TimerEx_GrandMaster"))
		{
			if (ex >= 4)
			{
				st.showQuestionMark(7);
				st.playSound("ItemSound.quest_tutorial");
				st.playTutorialVoice("tutorial_voice_025");
			}
			return null;
		}
		else if (event.equalsIgnoreCase("isle"))
		{
			st.addRadar(-119692, 44504, 380);
			st.getPlayer().teleToLocation(-120050, 44500, 360);
			htmltext = "<html><body>"
					+ npc.getName()
					+ ":<br>Go to the <font color=\"LEVEL\">Isle of Souls</font> and meet the <font color=\"LEVEL\">Newbie Guide</font> there to learn a number of important tips. He will also give you an item to assist your development. <br>Follow the direction arrow above your head and it will lead you to the Newbie Guide. Good luck!</body></html>";
		}
		else if (EVENTS.containsKey(event))
		{
			Event e = EVENTS.get(event);

			if (e._loc != null)
				st.addRadar(e._loc);

			htmltext = e._htm;

			if (st.getQuestItemsCount(e._item) > 0 && st.getInt("onlyone") == 0)
			{
				st.addExpAndSp(0, 50);
				st.startQuestTimer("TimerEx_GrandMaster", 60000);
				st.takeItems(e._item, 1);
				if (ex <= 3)
					qs.set("Ex", 4);
				final int classId = player.getClassId().getId();
				if (classId == e._classId1)
				{
					st.giveItems(e._gift1, e._count1);
					if (e._gift1 == SPIRITSHOT_NOVICE)
						st.playTutorialVoice("tutorial_voice_027");
					else
						st.playTutorialVoice("tutorial_voice_026");
				}
				else if (classId == e._classId2)
				{
					if (e._gift2 != 0)
					{
						st.giveItems(e._gift2, e._count2);
						st.playTutorialVoice("tutorial_voice_026");
					}
				}
			}
			st.unset("step");
			st.set("onlyone", 1);
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2Player player)
	{
		if (Config.ALT_DISABLE_TUTORIAL)
			return null;

		QuestState qs = player.getQuestState(QN_TUTORIAL);
		if (qs == null)
		{
			npc.showChatWindow(player);
			return null;
		}

		QuestState st = player.getQuestState(QN);
		if (st == null)
			st = newQuestState(player);

		String htmltext = "";

		final int ex = qs.getInt("Ex");
		final int npcId = npc.getNpcId();
		final byte state = st.getState();
		final int step = st.getInt("step");
		final int onlyone = st.getInt("onlyone");
		final int level = player.getLevel();
		final boolean isMage = player.getClassId().isMage();

		Talk t = TALKS.get(npcId);
		if (t == null)
			return null;
		else if (level >= 10 && t._npcType == 1)
			htmltext = "30575-05.htm";
		else if (level < 10 && onlyone == 0)
		{
			if (player.getRace().ordinal() == t._raceId)
			{
				htmltext = t._htm[0];
				switch (t._npcType)
				{
					case 0:
						switch (step)
						{
							case 1:
								htmltext = t._htm[0];
								break;
							case 2:
								htmltext = t._htm[1];
								break;
							case 3:
								htmltext = t._htm[2];
								break;
						}
						break;
					case 1:
						if (step == 0 && ex < 0)
						{
							qs.set("Ex", 0);
							st.startQuestTimer("TimerEx_NewbieHelper", 30000);
							if (isMage)
							{
								st.set("step", 1);
								st.setState(State.STARTED);
							}
							else
							{
								htmltext = "30530-01.htm";
								st.set("step", 1);
								st.setState(State.STARTED);
							}
						}
						else if (step == 1 && st.getQuestItemsCount(t._item) == 0 && ex <= 2)
						{
							if (st.getQuestItemsCount(BLUE_GEM) > 0)
							{
								st.takeItems(BLUE_GEM, st.getQuestItemsCount(BLUE_GEM));
								st.giveItems(t._item, 1);
								st.set("step", 2);
								qs.set("Ex", 3);
								st.startQuestTimer("TimerEx_NewbieHelper", 30000);
								qs.set("ucMemo", 3);
								if (isMage)
								{
									st.playTutorialVoice("tutorial_voice_027");
									st.giveItems(SPIRITSHOT_NOVICE, 100);
									htmltext = t._htm[2];
									if (htmltext.isEmpty())
										htmltext = "<html><body>" + npc.getName()
												+ ":<br>I am sorry. I only help warriors. Please go to another Newbie Helper who may assist you.</body></html>";
								}
								else
								{
									st.playTutorialVoice("tutorial_voice_026");
									st.giveItems(SOULSHOT_NOVICE, 200);
									htmltext = t._htm[1];
									if (htmltext.isEmpty())
										htmltext = "<html><body>" + npc.getName()
												+ ":<br>I am sorry. I only help mystics. Please go to another Newbie Helper who may assist you.</body></html>";
								}
							}
							else if (isMage)
							{
								htmltext = "30131-02.htm";
								if (player.getRace().ordinal() == 3)
									htmltext = "30575-02.htm";
							}
							else
								htmltext = "30530-02.htm";
						}
						else if (step == 2)
							htmltext = t._htm[3];
						break;
				}
			}
		}
		else if (state == State.COMPLETED && t._npcType == 0)
			htmltext = (npc.getNpcId()) + "-04.htm";

		if (htmltext == null || htmltext == "")
			npc.showChatWindow(player);

		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2Player player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return null;

		QuestState qs = st.getPlayer().getQuestState(QN_TUTORIAL);
		if (qs == null)
			return null;

		final int ex = qs.getInt("Ex");

		if (ex <= 1)
		{
			st.playTutorialVoice("tutorial_voice_011");
			st.showQuestionMark(3);
			qs.set("Ex", 2);
		}

		if (ex <= 2 && st.getQuestItemsCount(BLUE_GEM) < 1)
		{
			if (st.getRandom(100) < 50)
			{
				st.dropItem((L2MonsterInstance) npc, player, BLUE_GEM, 1);
				st.playSound("ItemSound.quest_tutorial");
			}
		}
		return null;
	}

	public static void main(String[] args)
	{
		new T1Tutorial(999, QN, "Kamael Tutorial", QN);
	}
}
