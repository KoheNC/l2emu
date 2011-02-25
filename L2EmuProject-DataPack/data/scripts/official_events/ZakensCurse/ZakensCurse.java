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
package official_events.ZakensCurse;

import gnu.trove.TIntObjectHashMap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import javolution.util.FastList;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.Announcements;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.model.actor.instance.L2FlyMonsterInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.script.DateRange;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.spawn.L2Spawn;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.lang.ArrayUtils;

/** 
 * Zaken's Curse by TheOne
 * Converted and Edited by Gigiikun
 */
public class ZakensCurse extends QuestJython
{
	private static final String							QN						= "ZakensCurse";

	private static final String							SPAWNLIST_FILE			= "/data/scripts/official_events/ZakensCurse/spawnlist.csv";

	private static FastList<L2Spawn>					_spawns					= new FastList<L2Spawn>();

	private static final int							BONNY					= 102890;
	private static boolean								_isEventTime			= false;

	private static final int[]							TRANSFORM_SCROLL_HIGH	=
																				{ 10302, 10303, 10304, 10305, 10306 };
	private static final int[]							TRANSFORM_SCROLL_BOSS	=
																				{ 10295, 10296, 10297, 10298, 10299, 10300, 10301 };
	private static final int[]							LIFECRYS				=
																				{ 8176, 9814, 9815, 9816, 9817, 9818 };
	private static final int[]							ELEMENTALSTONES			=
																				{ 9546, 9547, 9548, 9549, 9550, 9551 };
	private static final int[][]						SCROLLS					=
																				{
																				{ 956, 952, 955, 951, 12362, 12363, 12367, 12368 }, // D, C grade
			{ 948, 730, 947, 729, 12364, 12365, 12369, 12370 }, // B, A grade
			{ 960, 959 }, // S grade
			{ 6576, 6574, 6573, 6575 }, // Blessed D, C grade
			{ 6572, 6570, 6571, 6569 }, // Blessed B, A grade
			{ 6578, 6577, 12366, 12371 }										// Blessed S grade
																				};

	private static final int[]							MOB_X					=
																				{
			19964,
			21078,
			21858,
			-10308,
			-14383,
			-17070,
			-18697,
			-17816,
			50888,
			51280,
			51347,
			52934,
			-120692,
			-121463,
			-121537,
			-122877,
			-40762,
			-41603,
			-43096,
			-44279,
			21334,
			21955,
			22967,
			23045,
			21585,
			-16947,
			-16647,
			-17965,
			-18887,
			-18238,
			-17645,
			-17544,
			51226,
			51971,
			48859,
			48263,
			47888,
			47136,
			44815,
			43779																};
	private static final int[]							MOB_Y					=
																				{
			171049,
			172830,
			171427,
			126693,
			127592,
			127213,
			125278,
			122913,
			53808,
			52503,
			50916,
			51242,
			54605,
			55887,
			57613,
			57291,
			-123303,
			-122620,
			-121474,
			-120510,
			172100,
			173071,
			172515,
			171417,
			170361,
			122458,
			121417,
			121854,
			122899,
			123944,
			125383,
			126339,
			51733,
			50245,
			44750,
			43949,
			42795,
			42677,
			42305,
			42407																};
	private static final int[]							MOB_Z					=
																				{
			-3567,
			-3574,
			-3553,
			-3636,
			-3217,
			-3240,
			-3201,
			-3164,
			-3388,
			-3519,
			-3564,
			-3541,
			-1467,
			-1802,
			-2067,
			-2189,
			-462,
			-334,
			-270,
			-259,
			-3574,
			-3573,
			-3488,
			-3442,
			-3501,
			-3158,
			-3158,
			-3196,
			-3232,
			-3171,
			-3179,
			-3211,
			-3582,
			-3506,
			-3509,
			-3505,
			-3488,
			-3498,
			-3493,
			-3493																};
	private static final int[]							EVENT_NPC_SKILLS		=
																				{ 5206, 5207, 5218 };
	public static final TIntObjectHashMap<Integer[]>	EVENT_NPC_DROPS			= new TIntObjectHashMap<Integer[]>();

	private static final int[]							RANDOM_MICEID			=
																				{ 13120, 13121, 13122, 13120, 13121, 13122, 13124 };
	private static final int							BIG_MOUSE				= 13123;
	private static final int							EXTRA_MOUSE				= 13124;
	private static final String[]						MICE_TALKS				=
																				{
			"Squeak squeak! Nooo! I don't want to go back to normal! I look better with this body. What have you done?!",
			"You cured me! Thanks a lot! Squeak squeak!",
			"Squeak squeak! Rats unite! Transform into Mega Ultra Awesome Super Rat form! Let's show them our strength!",
			"Squeak squeak! Take that! Ultra Rat Fear! Even Valakas will flee from me! Ha ha ha!",
			"Squeak squeak! This hurts too much! Are you sure you're trying to cure me? You're not very good at this!" };

	private static final int[]							RANDOM_PIGID			=
																				{ 13031, 13032, 13033, 13031, 13032, 13033, 13035 };
	private static final int							BIG_PIG					= 13034;
	private static final int							EXTRA_PIG				= 13035;
	private static final String[]						PIG_TALKS				=
																				{
			"Oink oink! Nooo! I don't want to go back to normal! I look better with this body. What have you done?!",
			"You cured me! Thanks a lot! Oink oink!",
			"Oink oink! Pigs unite! Transform into Mega Ultra Awesome Super Pig form! Let's show them our strength!",
			"Oink oink! Take that! Ultra Pig Fear! Even Valakas will flee from me! Ha ha ha!",
			"Oink oink! This hurts too much! Are you sure you're trying to cure me? You're not very good at this!" };

	private FastList<L2Npc>								_liveNPCs				= new FastList<L2Npc>();
	private FastList<L2Npc>								_deadNPCs				= new FastList<L2Npc>();

	static
	{
		EVENT_NPC_DROPS.put(13031, new Integer[]
		{ 1, 9142, 1, 9144, 1 });
		EVENT_NPC_DROPS.put(13032, new Integer[]
		{ 1, 9142, 1, 9144, 1 });
		EVENT_NPC_DROPS.put(13033, new Integer[]
		{ 1, 9142, 1, 9144, 1 });
		EVENT_NPC_DROPS.put(13034, new Integer[]
		{ 3, 9142, 5, 9144, 2 });
		EVENT_NPC_DROPS.put(13035, new Integer[]
		{ 5, 9143, 1, 9144, 1 });
		EVENT_NPC_DROPS.put(13120, new Integer[]
		{ 3, -1, 0, 10639, 1 });
		EVENT_NPC_DROPS.put(13121, new Integer[]
		{ 3, -1, 0, 10639, 1 });
		EVENT_NPC_DROPS.put(13122, new Integer[]
		{ 3, -1, 0, 10639, 1 });
		EVENT_NPC_DROPS.put(13123, new Integer[]
		{ 7, -1, 0, 10639, 1 });
		EVENT_NPC_DROPS.put(13124, new Integer[]
		{ 7, -1, 0, 10639, 2 });
	}

	private static final String[]						EVENT_ANNOUNCE			=
																				{ "The Zaken's Curse Event is started. Go see Bonny in Floran Village for the details!" };

	private static final DateRange						EVENT_DATES				= DateRange.parse(Config.ZAKENS_CURSE_DATE, new SimpleDateFormat("dd MM yyyy",
																						Locale.US));

	public ZakensCurse(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(BONNY);
		addFirstTalkId(BONNY);
		addTalkId(BONNY);

		Announcements.getInstance().addEventAnnouncement(EVENT_DATES, EVENT_ANNOUNCE);

		final Date currentDate = new Date();

		if (EVENT_DATES.isWithinRange(currentDate))
		{
			_isEventTime = true;
			registerDrops();
			load();
			doSpawns();
		}
	}

	private boolean accCondition(L2Player player)
	{
		String time = (loadGlobalQuestVar(player.getAccountName()));
		if (time.isEmpty())
			return false;
		long remain = Long.parseLong(time) - System.currentTimeMillis();
		if (remain <= 0)
			return false;
		return true;
	}

	private String[] getEventTexts()
	{
		switch (Config.ZAKENS_CURSE_EVENT_TYPE)
		{
			case 0:
				return PIG_TALKS;
			case 1:
				return MICE_TALKS;
			default:
				return null;
		}
	}

	private void giveItemToPlayer(int npcId, QuestState st)
	{
		int rr = Rnd.get(10);
		if (rr > EVENT_NPC_DROPS.get(npcId)[0])
		{
			if (EVENT_NPC_DROPS.get(npcId)[1] > 0)
				st.giveItems(EVENT_NPC_DROPS.get(npcId)[1], EVENT_NPC_DROPS.get(npcId)[2]);
		}
		else
			st.giveItems(EVENT_NPC_DROPS.get(npcId)[3], EVENT_NPC_DROPS.get(npcId)[4]);
	}

	private void registerDrops()
	{
		for (int level = 1; level < 100; level++)
		{
			L2NpcTemplate[] templates = NpcTable.getInstance().getAllOfLevel(level);
			if ((templates != null) && (templates.length > 0))
			{
				for (L2NpcTemplate t : templates)
				{
					try
					{
						if (t.isAssignableTo(L2Attackable.class))
							addEventId(t.getNpcId(), Quest.QuestEventType.ON_KILL);
					}
					catch (RuntimeException e)
					{
						_log.warn("", e);
					}
				}
			}
		}
	}

	private void doSpawns()
	{
		if (_spawns.isEmpty() || _spawns.size() == 0)
			return;
		for (L2Spawn spawn : _spawns)
		{
			if (spawn == null)
				continue;
			spawn.doSpawn();
		}

		// spawn Event NPCs
		int[] mob_Ids = (Config.ZAKENS_CURSE_EVENT_TYPE == 0 ? RANDOM_PIGID : RANDOM_MICEID);
		for (int mobId : EVENT_NPC_DROPS.keys())
		{
			addSkillSeeId(mobId);
			addAttackId(mobId);
			addKillId(mobId);
		}
		for (int i = 0; i < MOB_X.length; i++)
		{
			int r1 = Rnd.get(15);
			if (r1 <= 7)
				r1 = 8;
			for (int m = 0; m < r1; m++)
			{
				int x1 = MOB_X[i] + (Rnd.get(800) - 400);
				int y1 = MOB_Y[i] + (Rnd.get(800) - 400);
				int z1 = MOB_Z[i] + 10;
				_liveNPCs.add(addSpawn(mob_Ids[Rnd.get(mob_Ids.length)], x1, y1, z1, 0, false, 0));
			}
		}
		startQuestTimer("NPCsRespawn", 60000, null, null);
	}

	private void deleteSpawns()
	{
		if (_spawns.isEmpty() || _spawns.size() == 0)
			return;
		for (L2Spawn spawn : _spawns)
		{
			if (spawn == null)
				continue;
			spawn.stopRespawn();
			spawn.getLastSpawn().doDie(spawn.getLastSpawn());
		}
		for (L2Npc npc : _liveNPCs)
			npc.deleteMe();
	}

	private void load()
	{
		Scanner s;
		try
		{
			s = new Scanner(new File(Config.DATAPACK_ROOT + SPAWNLIST_FILE));
		}
		catch (Exception e)
		{
			_log.warn("Zaken's Curse Event: Can not find '" + Config.DATAPACK_ROOT + SPAWNLIST_FILE);
			return;
		}
		int lineCount = 0;
		_spawns.clear();
		while (s.hasNextLine())
		{
			lineCount++;
			String line = s.nextLine();

			if (line.startsWith("#"))
				continue;
			else if (line.equals(""))
				continue;

			String[] lineSplit = line.split(";");

			boolean ok = true;
			int npcID = 0;

			try
			{
				npcID = Integer.parseInt(lineSplit[0]);
			}
			catch (Exception e)
			{
				_log.warn("Zaken's Curse Event: Error in line " + lineCount + " -> invalid npc id or wrong seperator after npc id!");
				_log.warn("		" + line);
				ok = false;
			}
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcID);
			if (template == null)
			{
				_log.warn("Zaken's Curse Event: NPC Id " + npcID + " not found!");
				continue;
			}
			if (!ok)
				continue;

			String[] lineSplit2 = lineSplit[1].split(",");

			int x = 0, y = 0, z = 0, heading = 0, respawn = 0;

			try
			{
				x = Integer.parseInt(lineSplit2[0]);
				y = Integer.parseInt(lineSplit2[1]);
				z = Integer.parseInt(lineSplit2[2]);
				heading = Integer.parseInt(lineSplit2[3]);
				respawn = Integer.parseInt(lineSplit2[4]);
			}

			catch (Exception e)
			{
				_log.warn("Zaken's Curse Event: Error in line " + lineCount + " -> incomplete/invalid data or wrong seperator!");
				_log.warn("		" + line);
				ok = false;
			}

			if (!ok)
				continue;
			try
			{
				L2Spawn spawnDat = new L2Spawn(template);
				spawnDat.setAmount(1);
				spawnDat.setLocx(x);
				spawnDat.setLocy(y);
				spawnDat.setLocz(z);
				spawnDat.setHeading(heading);
				spawnDat.setRespawnDelay(respawn);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				_spawns.add(spawnDat);
				//spawnDat.doSpawn();
				//spawnDat.startRespawn();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		s.close();
	}

	private void giveRewards(QuestState st, boolean isExtra)
	{
		switch (Config.ZAKENS_CURSE_EVENT_TYPE)
		{
			case 0:
				// TODO: make rewards for Pig type event
				if (isExtra)
					st.giveAdena(1000);
				else
					st.giveAdena(1000);
				break;
			case 1:
				int rr = Rnd.get(1000);
				if (rr < 334)
					// Gold Einhasad
					st.giveItems(4356, 1);
				else if (rr < 500)
					st.giveItems(LIFECRYS[Rnd.get(LIFECRYS.length)], 1);
				else if (rr < 510)
					st.giveItems(ELEMENTALSTONES[Rnd.get(ELEMENTALSTONES.length)], 1);
				else if (rr < 610)
					st.giveItems(SCROLLS[0][Rnd.get(SCROLLS[0].length)], 1);
				else if (rr < 700)
					st.giveItems(SCROLLS[1][Rnd.get(SCROLLS[1].length)], 1);
				else if (rr < 760)
					st.giveItems(SCROLLS[3][Rnd.get(SCROLLS[3].length)], 1);
				else if (rr < 800)
					st.giveItems(SCROLLS[4][Rnd.get(SCROLLS[4].length)], 1);
				else if (rr < 808)
					st.giveItems(SCROLLS[2][Rnd.get(SCROLLS[2].length)], 1);
				else if (rr < 810)
					st.giveItems(SCROLLS[5][Rnd.get(SCROLLS[5].length)], 1);
				else if (rr < 812)
					// Bloody Pa'agrio
					st.giveItems(4358, 1);
				else if (rr < 824)
					st.giveItems(TRANSFORM_SCROLL_HIGH[Rnd.get(TRANSFORM_SCROLL_HIGH.length)], 1);
				else if (rr < 825)
					st.giveItems(TRANSFORM_SCROLL_BOSS[Rnd.get(TRANSFORM_SCROLL_BOSS.length)], 1);
				else if (rr < 880)
					st.giveItems(13016, 1);
				else if (rr < 900)
					st.giveItems(20033, 1);
				else if (rr < 902)
					st.giveItems(13015, 1);
				else if (rr < 920)
					st.giveItems(20391, 1);
				else if (rr < 938)
					st.giveItems(20392, 1);
				else if (rr < 965)
					st.giveItems(6622, 1);
				else if (rr < 975)
					st.giveItems(9625, 1);
				else if (rr < 985)
					st.giveItems(9626, 1);
				else if (rr < 990)
					st.giveItems(9627, 1);
				else
					st.giveAdena(20000);
		}
	}

	@Override
	public String onSkillSee(L2Npc npc, L2Player caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (targets[0] != npc || npc.isDead())
			return super.onSkillSee(npc, caster, skill, targets, isPet);
		int npcId = npc.getNpcId();
		int skillId = skill.getId();
		QuestState st = caster.getQuestState(QN);
		if (st == null)
			return super.onSkillSee(npc, caster, skill, targets, isPet);
		if (_isEventTime)
		{
			int rr = Rnd.get(10);
			if ((skillId == 3261 && rr <= 1) || (skillId == 3262 && rr <= 2))
			{
				if (Rnd.get(10) < 5)
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), getEventTexts()[0]));
				else
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), getEventTexts()[1]));
				npc.reduceCurrentHp(npc.getMaxHp(), caster, false, false, false, skill);
				giveItemToPlayer(npcId, st);
			}
			else if (npc instanceof L2Attackable)
			{
				((L2Attackable) npc).getAggroList().remove(caster);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				boolean doTalk = false;
				rr = Rnd.get(100);
				if (npc.isCastingNow())
					doTalk = false;
				else if (npcId != EXTRA_PIG && npcId != EXTRA_MOUSE && npcId != BIG_PIG && npcId != BIG_MOUSE && rr <= 15)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), getEventTexts()[2]));
					npc.getSpawn().decreaseCount(npc);
					npc.deleteMe();
					L2Npc spawn = addSpawn((Config.ZAKENS_CURSE_EVENT_TYPE == 0 ? BIG_PIG : BIG_MOUSE), npc.getX(), npc.getY(), npc.getZ() + 10,
							npc.getHeading(), false, 0);
					spawn.doCast(SkillTable.getInstance().getInfo(5204, 1));
					if (_liveNPCs.contains(npc))
						_liveNPCs.remove(npc);
					_liveNPCs.add(spawn);
				}
				else if ((npcId == BIG_PIG || npcId == BIG_MOUSE) && rr <= 40)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), getEventTexts()[3]));
					npc.setTarget(caster);
					npc.doCast(SkillTable.getInstance().getInfo(5220, 1));
				}
				else if ((npcId == BIG_PIG || npcId == BIG_MOUSE) && rr <= 50)
				{
					npc.setTarget(caster);
					npc.doCast(SkillTable.getInstance().getInfo(5219, 1));
					doTalk = true;
				}
				else if ((npcId == EXTRA_PIG || npcId == RANDOM_PIGID[0] || npcId == EXTRA_MOUSE || npcId == RANDOM_MICEID[0]) && rr >= 95)
				{
					npc.setTarget(caster);
					npc.doCast(SkillTable.getInstance().getInfo(4075, 10));
					doTalk = true;
				}
				else if ((npcId == EXTRA_PIG || npcId == BIG_PIG || npcId == RANDOM_PIGID[0] || npcId == EXTRA_MOUSE || npcId == BIG_MOUSE || npcId == RANDOM_MICEID[0])
						&& rr >= 70)
				{
					npc.setTarget(caster);
					npc.doCast(SkillTable.getInstance().getInfo(EVENT_NPC_SKILLS[Rnd.get(EVENT_NPC_SKILLS.length)], 1));
					doTalk = true;
				}
				if (doTalk)
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), getEventTexts()[4]));
			}
		}
		return "";
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		if (event.equalsIgnoreCase("ZakensCurse"))
		{
			QuestState st = player.getQuestState(QN);
			if (npc.getNpcId() == BONNY)
			{
				if (_isEventTime)
				{
					int cond = st.getInt(CONDITION);
					if (cond == 0)
						return "102890-" + Config.ZAKENS_CURSE_EVENT_TYPE + "-4.htm";
					else if (cond == 1)
						return "102890-" + Config.ZAKENS_CURSE_EVENT_TYPE + "-5.htm";
				}
				else
					return "102890-" + Config.ZAKENS_CURSE_EVENT_TYPE + "-3.htm";
			}
		}
		else if (event.equalsIgnoreCase("bow"))
		{
			if (_isEventTime)
			{
				QuestState st = player.getQuestState(QN);
				if (st.getQuestItemsCount(9141) > 0 || player.getWarehouse().getItemByItemId(9141) != null)
					return "<html><body>Bonny:<br><br>I am terribly sorry but you already have a bow!</body></html>";
				else if (accCondition(player))
					return "<html><body>Bonny:<br><br>I am terribly sorry but I cant give you a bow yet!</body></html>";
				else if (st.getQuestItemsCount(57) >= 10000)
				{
					saveGlobalQuestVar(player.getAccountName(), String.valueOf(System.currentTimeMillis() + 28800000));
					st.takeAdena(10000);
					st.giveItems(9141, 1);
					st.set(CONDITION, 1);
					st.sendPacket(SND_MIDDLE);
					return "102890-" + Config.ZAKENS_CURSE_EVENT_TYPE + ".htm";
				}
				else
					return "<html><body>Bonny:<br><br>I am terribly sorry but you do not have enough Adena!</body></html>";
			}
		}
		else if (event.equalsIgnoreCase("rewardMouse"))
		{
			QuestState st = player.getQuestState(QN);
			if (st.getQuestItemsCount(10639) >= 20)
			{
				st.takeItems(10639, 20);
				giveRewards(st, false);
				st.sendPacket(SND_MIDDLE);
				return "102890-" + Config.ZAKENS_CURSE_EVENT_TYPE + ".htm";
			}
			else
				return "<html><body>You do not have enough Mouse Coins to get your prize. Go free more souls and come back to me after.</body></html>";
		}
		else if (event.equalsIgnoreCase("rewardApiga"))
		{
			QuestState st = player.getQuestState(QN);
			if (st.getQuestItemsCount(9142) >= 50)
			{
				st.takeItems(9142, 50);
				giveRewards(st, false);
				st.sendPacket(SND_MIDDLE);
				return "102890-" + Config.ZAKENS_CURSE_EVENT_TYPE + ".htm";
			}
			else
				return "<html><body>You do not have enough Apiga to get your prize. Go free more souls and come back to me after.</body></html>";
		}
		else if (event.equalsIgnoreCase("rewardGold"))
		{
			QuestState st = player.getQuestState(QN);
			if (st.getQuestItemsCount(9143) >= 30)
			{
				st.takeItems(9143, 30);
				giveRewards(st, true);
				st.sendPacket(SND_MIDDLE);
				return "102890-" + Config.ZAKENS_CURSE_EVENT_TYPE + ".htm";
			}
			else
				return "<html><body>You do not have enough Golden Apiga to get your prize. Go free more souls and come back to me after.</body></html>";
		}
		else if (event.equalsIgnoreCase("NPCsRespawn"))
		{
			if (_isEventTime)
			{
				startQuestTimer("NPCsRespawn", 60000, null, null);
				int deadNPCs = _deadNPCs.size();
				int rr = 0;
				if (deadNPCs >= 1)
					rr = Rnd.get(deadNPCs);
				if (rr > 0)
				{
					if (rr < (deadNPCs / 2))
						rr = (deadNPCs / 2);
				}
				if (rr >= deadNPCs)
					rr = deadNPCs - 1;
				int[] npcIds = (Config.ZAKENS_CURSE_EVENT_TYPE == 0 ? RANDOM_PIGID : RANDOM_MICEID);
				for (int i = 0; i <= rr; i++)
				{
					L2Npc deadNPC = _deadNPCs.remove(Rnd.get(deadNPCs - 1));
					int npcId = deadNPC.getNpcId();
					if (npcId == BIG_PIG || npcId == BIG_MOUSE || npcId == EXTRA_PIG || npcId == EXTRA_MOUSE)
						npcId = npcIds[Rnd.get(npcIds.length)];
					_liveNPCs.add(addSpawn(npcId, deadNPC.getX(), deadNPC.getY(), deadNPC.getZ() + 10, 0, false, 0));
					deadNPCs--;
				}
			}
		}
		else
			return event;
		return "";
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2Player player)
	{
		QuestState st = player.getQuestState(QN);
		if (st == null)
			st = newQuestState(player);
		if (npc.getNpcId() == BONNY)
			return "102890-" + Config.ZAKENS_CURSE_EVENT_TYPE + ".htm";
		return null;
	}

	@Override
	public String onAttack(L2Npc npc, L2Player attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (_isEventTime && attacker != null)
		{
			if (npc instanceof L2Attackable)
			{
				((L2Attackable) npc).getAggroList().remove(attacker);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				if (skill == null && Rnd.get(5) == 0)
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Normal, npc.getName(), getEventTexts()[4]));
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2Player killer, boolean isPet)
	{
		if (_isEventTime && ArrayUtils.contains(EVENT_NPC_DROPS.keys(), npc.getNpcId()))
		{
			if (_liveNPCs.contains(npc))
				_liveNPCs.remove(npc);
			_deadNPCs.add(npc);
		}
		else if (_isEventTime && Config.ZAKENS_CURSE_EVENT_TYPE == 1)
		{
			if ((killer.getLevel() - npc.getLevel()) >= 8)
				return super.onKill(npc, killer, isPet);
			if (npc instanceof L2FlyMonsterInstance && Rnd.get(100) < Config.ZAKENS_CURSE_DROP_FLY_CHANCE)
			{
				killer.addItem("ZakensCurse Event", 10639, 1, killer, true, false);
			}
			else if (npc instanceof L2MonsterInstance && Rnd.get(100) < Config.ZAKENS_CURSE_DROP_CHANCE)
			{
				L2ItemInstance item = ((L2MonsterInstance) npc).dropItem(killer, 10639, 1);
				if (item.getOwnerId() != killer.getObjectId() || item.getItemLootShedule() == null)
				{
					item.setOwnerId(killer.getObjectId());
					item.setItemLootShedule(ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), 5000));
				}
			}
		}
		return "";
	}

	private final class ResetOwner implements Runnable
	{
		L2ItemInstance	_item;

		public ResetOwner(L2ItemInstance item)
		{
			_item = item;
		}

		@Override
		public void run()
		{
			_item.setOwnerId(0);
			_item.setItemLootShedule(null);
		}
	}

	@Override
	public boolean unload()
	{
		deleteSpawns();
		return super.unload();
	}

	public static void main(String[] args)
	{
		if (Config.ALLOW_ZAKENS_CURSE)
		{
			new ZakensCurse(-1, QN, "official_events");
			_log.info("Official Events: Zaken's Curse is loaded.");
		}
		else
			_log.info("Official Events: Zaken's Curse is disabled.");
	}
}
