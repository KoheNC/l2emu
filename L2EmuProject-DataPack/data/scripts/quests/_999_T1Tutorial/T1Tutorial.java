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
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.model.world.Location;
import quests._255_Tutorial.Tutorial;

/**
 * @author L0ngh0rn
 */
public final class T1Tutorial extends QuestJython
{
	public static final String						QN					= "_999_T1Tutorial";
	private static final String						QN_TUTORIAL			= Tutorial.QN;

	private static final int[]						NPCS				= new int[]
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
			32135														};
	private static final int[]						NPCS_KILL			= new int[]
																		{ 18342, 20001 };

	// Quest Item
	private static final int						RECOMMENDATION_01	= 1067;
	private static final int						RECOMMENDATION_02	= 1068;
	private static final int						LEAF_OF_MOTHERTREE	= 1069;
	private static final int						BLOOD_OF_JUNDIN		= 1070;
	private static final int						LICENSE_OF_MINER	= 1498;
	private static final int						VOUCHER_OF_FLAME	= 1496;
	private static final int						SOULSHOT_NOVICE		= 5789;
	private static final int						SPIRITSHOT_NOVICE	= 5790;
	private static final int						BLUE_GEM			= 6353;
	private static final int						TOKEN				= 8542;
	private static final int						SCROLL				= 8594;
	private static final int						DIPLOMA				= 9881;

	// Events
	private static final FastMap<String, String>	EVENTS_HTML			= new FastMap<String, String>();
	private static final FastMap<String, Location>	EVENTS_RADAR		= new FastMap<String, Location>();
	private static final FastMap<String, int[]>		EVENTS_OTHERS		= new FastMap<String, int[]>();

	// Talks
	private static final FastMap<Integer, String[]>	TALKS_HTML			= new FastMap<Integer, String[]>();
	private static final FastMap<Integer, int[]>	TALKS_OTHERS		= new FastMap<Integer, int[]>();

	static
	{
		// Events
		EVENTS_HTML.put("30008_02", "30008-03.htm");
		EVENTS_HTML.put("30008_04", "30008-04.htm");
		EVENTS_HTML.put("30017_02", "30017-03.htm");
		EVENTS_HTML.put("30017_04", "30017-04.htm");
		EVENTS_HTML.put("30129_02", "30129-03.htm");
		EVENTS_HTML.put("30129_04", "30129-04.htm");
		EVENTS_HTML.put("30370_02", "30370-03.htm");
		EVENTS_HTML.put("30370_04", "30370-04.htm");
		EVENTS_HTML.put("30528_02", "30528-03.htm");
		EVENTS_HTML.put("30528_04", "30528-04.htm");
		EVENTS_HTML.put("30573_02", "30573-03.htm");
		EVENTS_HTML.put("30573_04", "30573-04.htm");
		EVENTS_HTML.put("32133_02", "32133-03.htm");

		EVENTS_RADAR.put("30008_02", new Location(0, 0, 0));
		EVENTS_RADAR.put("30008_04", new Location(-84058, 243239, -3730));
		EVENTS_RADAR.put("30017_02", new Location(0, 0, 0));
		EVENTS_RADAR.put("30017_04", new Location(-84058, 243239, -3730));
		EVENTS_RADAR.put("30129_02", new Location(0, 0, 0));
		EVENTS_RADAR.put("30129_04", new Location(12116, 16666, -4610));
		EVENTS_RADAR.put("30370_02", new Location(0, 0, 0));
		EVENTS_RADAR.put("30370_04", new Location(45491, 48359, -3086));
		EVENTS_RADAR.put("30528_02", new Location(0, 0, 0));
		EVENTS_RADAR.put("30528_04", new Location(115642, -178046, -941));
		EVENTS_RADAR.put("30573_02", new Location(0, 0, 0));
		EVENTS_RADAR.put("30573_04", new Location(-45067, -113549, -235));
		EVENTS_RADAR.put("32133_02", new Location(-119692, 44504, 380));

		// Model: item,classId1,gift1,count1,classId2,gift2,count2
		EVENTS_OTHERS.put("30008_02", new int[]
		{ RECOMMENDATION_01, 0x00, SOULSHOT_NOVICE, 200, 0x00, 0, 0 });
		EVENTS_OTHERS.put("30008_04", new int[]
		{ 0, 0x00, 0, 0, 0, 0, 0 });
		EVENTS_OTHERS.put("30017_02", new int[]
		{ RECOMMENDATION_02, 0x0a, SPIRITSHOT_NOVICE, 100, 0x00, 0, 0 });
		EVENTS_OTHERS.put("30017_04", new int[]
		{ 0, 0x0a, 0, 0, 0x00, 0, 0 });
		EVENTS_OTHERS.put("30129_02", new int[]
		{ BLOOD_OF_JUNDIN, 0x26, SPIRITSHOT_NOVICE, 100, 0x1f, SOULSHOT_NOVICE, 200 });
		EVENTS_OTHERS.put("30129_04", new int[]
		{ 0, 0x26, 0, 0, 0x1f, 0, 0 });
		EVENTS_OTHERS.put("30370_02", new int[]
		{ LEAF_OF_MOTHERTREE, 0x19, SPIRITSHOT_NOVICE, 100, 0x12, SOULSHOT_NOVICE, 200 });
		EVENTS_OTHERS.put("30370_04", new int[]
		{ 0, 0x19, 0, 0, 0x12, 0, 0 });
		EVENTS_OTHERS.put("30528_02", new int[]
		{ LICENSE_OF_MINER, 0x35, SOULSHOT_NOVICE, 200, 0x00, 0, 0 });
		EVENTS_OTHERS.put("30528_04", new int[]
		{ 0, 0x35, 0, 0, 0x00, 0, 0 });
		EVENTS_OTHERS.put("30573_02", new int[]
		{ VOUCHER_OF_FLAME, 0x31, SPIRITSHOT_NOVICE, 100, 0x2c, SOULSHOT_NOVICE, 200 });
		EVENTS_OTHERS.put("30573_04", new int[]
		{ 0, 0x31, 0, 0, 0x2c, 0, 0 });
		EVENTS_OTHERS.put("32133_02", new int[]
		{ DIPLOMA, 0x7b, SOULSHOT_NOVICE, 200, 0x7c, SOULSHOT_NOVICE, 200 });

		// Talks
		TALKS_HTML.put(30008, new String[]
		{ "30008-01.htm", "30008-02.htm", "30008-04.htm", "0" });
		TALKS_HTML.put(30009, new String[]
		{ "30530-01.htm", "30009-03.htm", "0", "30009-04.htm" });
		TALKS_HTML.put(30011, new String[]
		{ "30530-01.htm", "30009-03.htm", "0", "30009-04.htm" });
		TALKS_HTML.put(30012, new String[]
		{ "30530-01.htm", "30009-03.htm", "0", "30009-04.htm" });
		TALKS_HTML.put(30017, new String[]
		{ "30017-01.htm", "30017-02.htm", "30017-04.htm", "0" });
		TALKS_HTML.put(30018, new String[]
		{ "30131-01.htm", "0", "30019-03a.htm", "30019-04.htm" });
		TALKS_HTML.put(30019, new String[]
		{ "30131-01.htm", "0", "30019-03a.htm", "30019-04.htm" });
		TALKS_HTML.put(30020, new String[]
		{ "30131-01.htm", "0", "30019-03a.htm", "30019-04.htm" });
		TALKS_HTML.put(30021, new String[]
		{ "30131-01.htm", "0", "30019-03a.htm", "30019-04.htm" });
		TALKS_HTML.put(30056, new String[]
		{ "30530-01.htm", "30009-03.htm", "0", "30009-04.htm" });
		TALKS_HTML.put(30129, new String[]
		{ "30129-01.htm", "30129-02.htm", "30129-04.htm", "0" });
		TALKS_HTML.put(30131, new String[]
		{ "30131-01.htm", "30131-03.htm", "30131-03a.htm", "30131-04.htm" });
		TALKS_HTML.put(30370, new String[]
		{ "30370-01.htm", "30370-02.htm", "30370-04.htm", "0" });
		TALKS_HTML.put(30400, new String[]
		{ "30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm" });
		TALKS_HTML.put(30401, new String[]
		{ "30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm" });
		TALKS_HTML.put(30402, new String[]
		{ "30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm" });
		TALKS_HTML.put(30403, new String[]
		{ "30131-01.htm", "30400-03.htm", "30400-03a.htm", "30400-04.htm" });
		TALKS_HTML.put(30404, new String[]
		{ "30131-01.htm", "30131-03.htm", "30131-03a.htm", "30131-04.htm" });
		TALKS_HTML.put(30528, new String[]
		{ "30528-01.htm", "30528-02.htm", "30528-04.htm", "0" });
		TALKS_HTML.put(30530, new String[]
		{ "30530-01.htm", "30530-03.htm", "0", "30530-04.htm" });
		TALKS_HTML.put(30573, new String[]
		{ "30573-01.htm", "30573-02.htm", "30573-04.htm", "0" });
		TALKS_HTML.put(30574, new String[]
		{ "30575-01.htm", "30575-03.htm", "30575-03a.htm", "30575-04.htm" });
		TALKS_HTML.put(30575, new String[]
		{ "30575-01.htm", "30575-03.htm", "30575-03a.htm", "30575-04.htm" });
		TALKS_HTML.put(32133, new String[]
		{ "32133-01.htm", "32133-02.htm", "32133-04.htm", "0" });
		TALKS_HTML.put(32134, new String[]
		{ "32134-01.htm", "32134-03.htm", "0", "32134-04.htm" });

		// Model: raceId,npcTyp,item
		TALKS_OTHERS.put(30008, new int[]
		{ 0, 0, 0 });
		TALKS_OTHERS.put(30009, new int[]
		{ 0, 1, RECOMMENDATION_01 });
		TALKS_OTHERS.put(30011, new int[]
		{ 0, 1, RECOMMENDATION_01 });
		TALKS_OTHERS.put(30012, new int[]
		{ 0, 1, RECOMMENDATION_01 });
		TALKS_OTHERS.put(30017, new int[]
		{ 0, 0, 0 });
		TALKS_OTHERS.put(30018, new int[]
		{ 0, 1, RECOMMENDATION_02 });
		TALKS_OTHERS.put(30019, new int[]
		{ 0, 1, RECOMMENDATION_02 });
		TALKS_OTHERS.put(30020, new int[]
		{ 0, 1, RECOMMENDATION_02 });
		TALKS_OTHERS.put(30021, new int[]
		{ 0, 1, RECOMMENDATION_02 });
		TALKS_OTHERS.put(30056, new int[]
		{ 0, 1, RECOMMENDATION_01 });
		TALKS_OTHERS.put(30129, new int[]
		{ 2, 0, 0 });
		TALKS_OTHERS.put(30131, new int[]
		{ 2, 1, BLOOD_OF_JUNDIN });
		TALKS_OTHERS.put(30370, new int[]
		{ 1, 0, 0 });
		TALKS_OTHERS.put(30400, new int[]
		{ 1, 1, LEAF_OF_MOTHERTREE });
		TALKS_OTHERS.put(30401, new int[]
		{ 1, 1, LEAF_OF_MOTHERTREE });
		TALKS_OTHERS.put(30402, new int[]
		{ 1, 1, LEAF_OF_MOTHERTREE });
		TALKS_OTHERS.put(30403, new int[]
		{ 1, 1, LEAF_OF_MOTHERTREE });
		TALKS_OTHERS.put(30404, new int[]
		{ 2, 1, BLOOD_OF_JUNDIN });
		TALKS_OTHERS.put(30528, new int[]
		{ 4, 0, 0 });
		TALKS_OTHERS.put(30530, new int[]
		{ 4, 1, LICENSE_OF_MINER });
		TALKS_OTHERS.put(30573, new int[]
		{ 3, 0, 0 });
		TALKS_OTHERS.put(30574, new int[]
		{ 3, 1, VOUCHER_OF_FLAME });
		TALKS_OTHERS.put(30575, new int[]
		{ 3, 1, VOUCHER_OF_FLAME });
		TALKS_OTHERS.put(32133, new int[]
		{ 5, 0, 0 });
		TALKS_OTHERS.put(32134, new int[]
		{ 5, 1, DIPLOMA });
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
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (Config.ALT_DISABLE_TUTORIAL)
			return null;

		QuestState st = player.getQuestState(QN);
		if (st == null)
			return null;

		QuestState qs = st.getPlayer().getQuestState(QN_TUTORIAL);
		if (qs == null)
			return null;

		final int ex = qs.getInt("Ex");
		String htmltext = null;

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
		else if (EVENTS_HTML.containsKey(event))
		{
			if (!EVENTS_RADAR.get(event).isEmpty())
				st.addRadar(EVENTS_RADAR.get(event));

			final int item = EVENTS_OTHERS.get(event)[0];
			final int classId1 = EVENTS_OTHERS.get(event)[1];
			final int gift1 = EVENTS_OTHERS.get(event)[2];
			final int count1 = EVENTS_OTHERS.get(event)[3];
			final int classId2 = EVENTS_OTHERS.get(event)[4];
			final int gift2 = EVENTS_OTHERS.get(event)[5];
			final int count2 = EVENTS_OTHERS.get(event)[6];

			if (st.getQuestItemsCount(item) == 0 && st.getInt("onlyone") == 0)
			{
				st.addExpAndSp(0, 50);
				st.startQuestTimer("TimerEx_GrandMaster", 60000);
				st.takeItems(item, 1);
				if (ex <= 3)
					qs.set("Ex", 4);

				if (st.getPlayer().getClassId().getId() == classId1)
				{
					st.giveItems(gift1, count1);
					if (gift1 == SPIRITSHOT_NOVICE)
						st.playTutorialVoice("tutorial_voice_027");
					else
						st.playTutorialVoice("tutorial_voice_026");
				}
				else if (st.getPlayer().getClassId().getId() == classId2)
				{
					if (gift2 != 0)
					{
						st.giveItems(gift2, count2);
						st.playTutorialVoice("tutorial_voice_026");
					}
				}
				st.unset("step");
				st.set("onlyone", 1);
			}
			htmltext = EVENTS_HTML.get(event);
		}

		return htmltext;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(QN_TUTORIAL);
		if (qs == null)
		{
			npc.showChatWindow(player);
			return null;
		}

		String htmltext = NO_QUEST;
		QuestState st = player.getQuestState(QN);

		if (st == null)
			st = newQuestState(player);

		final int ex = qs.getInt("Ex");
		final int npcId = npc.getNpcId();
		final byte state = st.getState();
		final int step = st.getInt("step");
		final int onlyone = st.getInt("onlyone");
		final int level = player.getLevel();
		final boolean isMage = player.getClassId().isMage();

		if (TALKS_HTML.containsKey(npcId))
		{
			final int raceId = TALKS_OTHERS.get(npcId)[0];
			final int npcTyp = TALKS_OTHERS.get(npcId)[1];
			final int item = TALKS_OTHERS.get(npcId)[2];

			if ((level >= 10 || onlyone != 0) && npcTyp == 1)
				htmltext = "30575-05.htm";
			else if (onlyone == 0 && level < 10)
			{
				if (player.getRace().ordinal() == raceId)
				{
					htmltext = TALKS_HTML.get(npcId)[0];
					switch (npcTyp)
					{
						case 0:
							switch (step)
							{
								case 1:
									htmltext = TALKS_HTML.get(npcId)[0];
									break;
								case 2:
									htmltext = TALKS_HTML.get(npcId)[1];
									break;
								case 3:
									htmltext = TALKS_HTML.get(npcId)[2];
									break;
							}
							break;
						case 1:
							switch (step)
							{
								case 0:
									if (ex < 0)
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
									break;
								case 1:
									if (st.getQuestItemsCount(item) == 0)
									{
										switch (ex)
										{
											case 0:
											case 1:
											case 2:
												if (st.getQuestItemsCount(BLUE_GEM) > 0)
												{
													st.takeItems(BLUE_GEM, st.getQuestItemsCount(BLUE_GEM));
													st.giveItems(item, 1);
													st.set("step", 2);
													qs.set("Ex", 3);
													st.startQuestTimer("TimerEx_NewbieHelper", 30000);
													qs.set("ucMemo", 3);
													if (isMage)
													{
														st.playTutorialVoice("tutorial_voice_027");
														st.rewardItems(SPIRITSHOT_NOVICE, 100);
														htmltext = TALKS_HTML.get(npcId)[2];
														if (htmltext.equalsIgnoreCase("0"))
															htmltext = "<html><body>I am sorry.  I only help warriors.  Please go to another Newbie Helper who may assist you.</body></html>";
													}
													else
													{
														st.playTutorialVoice("tutorial_voice_026");
														st.rewardItems(SOULSHOT_NOVICE, 200);
														htmltext = TALKS_HTML.get(npcId)[1];
														if (htmltext.equalsIgnoreCase("0"))
															htmltext = "<html><body>I am sorry.  I only help mystics.  Please go to another Newbie Helper who may assist you.</body></html>";
													}
												}
												else
												{
													if (isMage)
													{
														htmltext = "30131-02.htm";
														if (player.getRace().ordinal() == 3)
															htmltext = "30575-02.htm";
													}
													else
														htmltext = "30530-02.htm";
												}
												break;
										}
									}
									break;
								case 2:
									htmltext = TALKS_HTML.get(npcId)[3];
									break;
							}
							break;
					}
				}
			}
			else if (state == State.COMPLETED && npcTyp == 0)
				htmltext = npcId + "-04.htm";
			else
			{
				switch (npcId)
				{
					case 30600:
					case 30601:
					case 30602:
					case 30598:
					case 30599:
					case 32135:
						final int reward = qs.getInt("reward");
						if (reward == 0)
						{
							if (isMage)
							{
								st.playTutorialVoice("tutorial_voice_027");
								st.rewardItems(SPIRITSHOT_NOVICE, 100);
							}
							else
							{
								st.playTutorialVoice("tutorial_voice_026");
								st.rewardItems(SOULSHOT_NOVICE, 200);
							}
							st.giveItems(TOKEN, 12);
							st.giveItems(SCROLL, 2);
							qs.set("reward", 1);
							st.exitQuest(false);
						}
						npc.showChatWindow(player);
						return null;
				}
			}
		}

		if (htmltext == null || htmltext.equalsIgnoreCase("") || htmltext.equalsIgnoreCase("0"))
			npc.showChatWindow(player);
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			return null;

		QuestState qs = st.getPlayer().getQuestState(QN_TUTORIAL);
		if (qs == null)
			return null;
		final int ex = qs.getInt("Ex");

		if (ex >= 0 && ex <= 1)
		{
			st.playTutorialVoice("tutorial_voice_011");
			st.showQuestionMark(3);
			qs.set("Ex", 2);
		}

		if ((ex >= 0 && ex <= 2) && st.getQuestItemsCount(BLUE_GEM) < 1)
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
