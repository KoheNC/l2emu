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
package instances.CrystalCaverns;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.geodata.GeoData;
import net.l2emuproject.gameserver.instancemanager.InstanceManager;
import net.l2emuproject.gameserver.instancemanager.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.model.L2CharPosition;
import net.l2emuproject.gameserver.model.actor.L2Attackable;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.L2Summon;
import net.l2emuproject.gameserver.model.actor.instance.L2DoorInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2MonsterInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.entity.Instance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.model.party.L2Party;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.State;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.model.zone.L2Zone;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.network.serverpackets.FlyToLocation;
import net.l2emuproject.gameserver.network.serverpackets.FlyToLocation.FlyType;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.PlaySound;
import net.l2emuproject.gameserver.network.serverpackets.SocialAction;
import net.l2emuproject.gameserver.network.serverpackets.SpecialCamera;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.network.serverpackets.ValidateLocation;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.L2Skill.SkillTargetType;
import net.l2emuproject.gameserver.util.Util;
import net.l2emuproject.gameserver.world.L2Object;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.tools.random.Rnd;

/**
 * TODO:
 * 1. Kechi's Henchmans spawn animation is missing.
 * 2. NPC related Traps are not supported by core, so Darnels and Lahm door trap is not working.
 * 3. Need retail spawn for Coral Garden (EmeraldSteam/Square - done).
 * 4. Baylor Raid is missing a lot of things
 * 5. Add Trap support.
 * This script takes the best elements of different versions and combines them into one script to get the most optimal and retail-like experience.
 * Original sources: theone, L2EmuProject, L2JOfficial, L2JFree
 * Contributing authors: TGS, Lantoc, Janiii, Gigiikun, RosT
 * Please maintain consistency between the Crystal Caverns scripts.
 * 
 * Rework by lewzer
 * WORK IN PROGRESS
 */
public final class CrystalCaverns extends QuestJython
{
	private static final class CrystalGolem
	{
		private L2ItemInstance	foodItem		= null;
		private boolean			isAtDestination	= false;
		private L2CharPosition	oldpos			= null;
	}

	private final class CCWorld extends InstanceWorld
	{
		private Map<L2Npc, Boolean>					npcList1			= new FastMap<L2Npc, Boolean>();
		private L2Npc								tears;
		private boolean								isUsedInvulSkill	= false;
		private long								dragonScaleStart	= 0;
		private int									emelardMiniRbKilled	= 0;
		private int									dragonScaleNeed		= 0;
		private int									cleanedRooms		= 0;
		private long								endTime				= 0;
		private List<L2Npc>							copys				= new FastList<L2Npc>();
		private Map<L2Npc, CrystalGolem>			crystalGolems		= new FastMap<L2Npc, CrystalGolem>();
		private int									correctGolems		= 0;
		private boolean[]							OracleTriggered		=
																		{ false, false, false };
		private int[]								roomsStatus			=
																		{ 0, 0, 0, 0 };								// 0: not spawned, 1: spawned, 2: cleared
		private Map<L2DoorInstance, L2PcInstance>	openedDoors			= new FastMap<L2DoorInstance, L2PcInstance>();
		private Map<Integer, Map<L2Npc, Boolean>>	npcList2			= new FastMap<Integer, Map<L2Npc, Boolean>>();
		private Map<L2Npc, L2Npc>					oracles				= new FastMap<L2Npc, L2Npc>();
		private List<L2Npc>							keyKeepers			= new FastList<L2Npc>();
		private List<L2Npc>							oracle				= new FastList<L2Npc>();
		// baylor variables
		private List<L2PcInstance>					_raiders			= new FastList<L2PcInstance>();
		private int									_raidStatus			= 0;
		private long								_dragonClawStart	= 0;
		private int									_dragonClawNeed		= 0;
		private List<L2Npc>							_animationMobs		= new FastList<L2Npc>();
		private L2Npc								_camera				= null;
		private L2Npc								_baylor				= null;
		private L2Npc								_alarm				= null;

		private CCWorld(Long time)
		{
			InstanceManager.getInstance().super();
			endTime = time;
		}
	}

	private static final String		QN				= "CrystalCaverns";
	private static final int		INSTANCE_ID		= 10;													// this is the client number

	//Items
	private static final int		WHITE_SEED		= 9597;
	private static final int		BLACK_SEED		= 9598;
	private static final int		CONT_CRYSTAL	= 9690;												//Contaminated Crystal
	private static final int		RED_CORAL		= 9692;												//Red Coral
	private static final int		CRYSTALFOOD		= 9693;												//Food item for Crystal Golems
	private static final int		RACE_KEY		= 9694;												// Race Key for Emerald doors
	private static final int		BOSS_CRYSTAL_1	= 9695;												//Clear Crystal
	private static final int		BOSS_CRYSTAL_2	= 9696;												//Clear Crystal
	private static final int		BOSS_CRYSTAL_3	= 9697;												//Clear Crystal

	//NPCs
	private static final int		ORACLE_GUIDE_1	= 32281;
	private static final int		ORACLE_GUIDE_2	= 32278;
	private static final int		ORACLE_GUIDE_3	= 32280;
	private static final int		ORACLE_GUIDE_4	= 32279;
	private static final int		CRYSTAL_GOLEM	= 32328;

	//mobs
	private static final int		GK1				= 22275;
	private static final int		GK2				= 22277;
	private static final int		BAYLOR_CHEST1	= 29116;
	private static final int		BAYLOR_CHEST2	= 29117;

	private static final int		TOURMALINE		= 22292;
	private static final int		TEROD			= 22301;
	private static final int		DOLPH			= 22299;
	private static final int		WEYLIN			= 22298;
	private static final int		GUARDIAN		= 22303;
	private static final int		MINI_RB2		= 22302;
	private static final int		GUARDIAN2		= 22304;

	private static final int		TEARS			= 25534;
	private static final int		TEARS_COPY		= 25535;
	private static final int		KECHI			= 25532;
	private static final int		BAYLOR			= 29099;
	private static final int		DARNEL			= 25531;
	private static final int		ALARMID			= 18474;

	private static final int[]		CGMOBS			=
													{ 22311, 22312, 22313, 22314, 22315, 22316, 22317 };
	private static final int[]		MOBLIST			=
													{
			22279,
			22280,
			22281,
			22282,
			22283,
			22285,
			22286,
			22287,
			22288,
			22289,
			22293,
			22294,
			22295,
			22296,
			22297,
			22305,
			22306,
			22307,
			22416,
			22418,
			22419,
			22420									};

	// Doors/Walls/Zones
	private static final int		DOOR1			= 24220021;
	private static final int		DOOR2			= 24220024;
	private static final int		DOOR3			= 24220023;
	private static final int		DOOR4			= 24220061;
	private static final int		DOOR5			= 24220025;
	private static final int		DOOR6			= 24220022;
	private static final int[]		ZONES			=
													{ 100001, 100002, 100003 };

	// Baylor alarm spawns
	private final static int[][]	ALARMSPAWN		=
													{
													{ 153572, 141277, -12738 },
													{ 153572, 142852, -12738 },
													{ 154358, 142075, -12738 },
													{ 152788, 142075, -12738 } };

	// Oracle order
	private static final int[][]	ORDER_ORACLE1	=
													{
													{ 32274, 147090, 152505, -12169, 31613 },
													{ 32275, 147090, 152575, -12169, 31613 },
													{ 32274, 147090, 152645, -12169, 31613 },
													{ 32274, 147090, 152715, -12169, 31613 } };

	private static final int[][]	ORDER_ORACLE2	=
													{
													{ 32274, 149783, 152505, -12169, 31613 },
													// {32274, 149783, 152575, -12169, 31613},
			{ 32274, 149783, 152645, -12169, 31613 },
			{ 32276, 149783, 152715, -12169, 31613 } };

	private static final int[][]	ORDER_ORACLE3	=
													{
													{ 32274, 152461, 152505, -12169, 31613 },
													// {32274, 152461, 152575, -12169, 31613},
			{ 32277, 152461, 152645, -12169, 31613 }
													// {32274, 152461, 152715, -12169, 31613}
													};

	//Hall spawns
	private static int[][]			SPAWNS			=
													{
													{ 141842, 152556, -11814, 50449 },
													{ 141503, 153395, -11814, 40738 },
													{ 141070, 153201, -11814, 39292 },
													{ 141371, 152986, -11814, 35575 },
													{ 141602, 154188, -11814, 24575 },
													{ 141382, 154719, -11814, 37640 },
													{ 141376, 154359, -11814, 12054 },
													{ 140895, 154383, -11814, 37508 },
													{ 140972, 154740, -11814, 52690 },
													{ 141045, 154504, -11814, 50674 },
													{ 140757, 152740, -11814, 39463 },
													{ 140406, 152376, -11814, 16599 },
													{ 140268, 152007, -11817, 45316 },
													{ 139996, 151485, -11814, 47403 },
													{ 140378, 151190, -11814, 58116 },
													{ 140521, 150711, -11815, 55997 },
													{ 140816, 150215, -11814, 53682 },
													{ 141528, 149909, -11814, 22020 },
													{ 141644, 150360, -11817, 13283 },
													{ 142048, 150695, -11815, 5929 },
													{ 141852, 151065, -11817, 27071 },
													{ 142408, 151211, -11815, 2402 },
													{ 142481, 151762, -11815, 12876 },
													{ 141929, 152193, -11815, 27511 },
													{ 142083, 151791, -11814, 47176 },
													{ 141435, 150402, -11814, 41798 },
													{ 140390, 151199, -11814, 50069 },
													{ 140557, 151849, -11814, 45293 },
													{ 140964, 153445, -11814, 56672 },
													{ 142851, 154109, -11814, 24920 },
													{ 142379, 154725, -11814, 30342 },
													{ 142816, 154712, -11814, 33193 },
													{ 142276, 154223, -11814, 33922 },
													{ 142459, 154490, -11814, 33184 },
													{ 142819, 154372, -11814, 21318 },
													{ 141157, 154541, -11814, 27090 },
													{ 141095, 150281, -11814, 55186 } };

	//first spawns
	private static int[][]			FIRST_SPAWNS	=
													{
													{ 22276, 148109, 149601, -12132, 34490 },
													{ 22276, 148017, 149529, -12132, 33689 },
													{ 22278, 148065, 151202, -12132, 35323 },
													{ 22278, 147966, 151117, -12132, 33234 },
													{ 22279, 144063, 150238, -12132, 29654 },
													{ 22279, 144300, 149118, -12135, 5520 },
													{ 22279, 144397, 149337, -12132, 644 },
													{ 22279, 144426, 150639, -12132, 50655 },
													{ 22282, 145841, 151097, -12132, 31810 },
													{ 22282, 144387, 149958, -12132, 61173 },
													{ 22282, 145821, 149498, -12132, 31490 },
													{ 22282, 146619, 149694, -12132, 33374 },
													{ 22282, 146669, 149244, -12132, 31360 },
													{ 22284, 144147, 151375, -12132, 58395 },
													{ 22284, 144485, 151067, -12132, 64786 },
													{ 22284, 144356, 149571, -12132, 63516 },
													{ 22285, 144151, 150962, -12132, 664 },
													{ 22285, 146657, 151365, -12132, 33154 },
													{ 22285, 146623, 150857, -12132, 28034 },
													{ 22285, 147046, 151089, -12132, 32941 },
													{ 22285, 145704, 151255, -12132, 32523 },
													{ 22285, 145359, 151101, -12132, 32767 },
													{ 22285, 147785, 150817, -12132, 27423 },
													{ 22285, 147727, 151375, -12132, 37117 },
													{ 22285, 145428, 149494, -12132, 890 },
													{ 22285, 145601, 149682, -12132, 32442 },
													{ 22285, 147003, 149476, -12132, 31554 },
													{ 22285, 147738, 149210, -12132, 20971 },
													{ 22285, 147769, 149757, -12132, 34980 } };

	// Emerald Square
	private static int[][]			EMERALD_SPAWNS	=
													{
													{ 22280, 144437, 143395, -11969, 34248 },
													{ 22281, 149241, 143735, -12230, 24575 },
													{ 22281, 147917, 146861, -12289, 60306 },
													{ 22281, 144406, 147782, -12133, 14349 },
													{ 22281, 144960, 146881, -12039, 23881 },
													{ 22281, 144985, 147679, -12135, 27594 },
													{ 22283, 147784, 143540, -12222, 2058 },
													{ 22283, 149091, 143491, -12230, 24836 },
													{ 22287, 144479, 147569, -12133, 20723 },
													{ 22287, 145158, 146986, -12058, 21970 },
													{ 22287, 145142, 147175, -12092, 24420 },
													{ 22287, 145110, 147133, -12088, 22465 },
													{ 22287, 144664, 146604, -12028, 14861 },
													{ 22287, 144596, 146600, -12028, 14461 },
													{ 22288, 143925, 146773, -12037, 10813 },
													{ 22288, 144415, 147070, -12069, 8568 },
													{ 22288, 143794, 145584, -12027, 14849 },
													{ 22288, 143429, 146166, -12030, 4078 },
													{ 22288, 144477, 147009, -12056, 8752 },
													{ 22289, 142577, 145319, -12029, 5403 },
													{ 22289, 143831, 146902, -12051, 9717 },
													{ 22289, 143714, 146705, -12028, 10044 },
													{ 22289, 143937, 147134, -12078, 7517 },
													{ 22293, 143356, 145287, -12027, 8126 },
													{ 22293, 143462, 144352, -12008, 25905 },
													{ 22293, 143745, 142529, -11882, 17102 },
													{ 22293, 144574, 144032, -12005, 34668 },
													{ 22295, 143992, 142419, -11884, 19697 },
													{ 22295, 144671, 143966, -12004, 32088 },
													{ 22295, 144440, 143269, -11957, 34169 },
													{ 22295, 142642, 146362, -12028, 281 },
													{ 22295, 143865, 142707, -11881, 21326 },
													{ 22295, 143573, 142530, -11879, 16141 },
													{ 22295, 143148, 146039, -12031, 65014 },
													{ 22295, 143001, 144853, -12014, 0 },
													{ 22296, 147505, 146580, -12260, 59041 },
													{ 22296, 149366, 146932, -12358, 39407 },
													{ 22296, 149284, 147029, -12352, 41120 },
													{ 22296, 149439, 143940, -12230, 23189 },
													{ 22296, 147698, 143995, -12220, 27028 },
													{ 22296, 141885, 144969, -12007, 2526 },
													{ 22296, 147843, 143763, -12220, 28386 },
													{ 22296, 144753, 143650, -11982, 35429 },
													{ 22296, 147613, 146760, -12271, 56296 } };

	private static int[][]			ROOM1_SPAWNS	=
													{
													{ 22288, 143114, 140027, -11888, 15025 },
													{ 22288, 142173, 140973, -11888, 55698 },
													{ 22289, 143210, 140577, -11888, 17164 },
													{ 22289, 142638, 140107, -11888, 6571 },
													{ 22297, 142547, 140938, -11888, 48556 },
													{ 22298, 142690, 140479, -11887, 7663 } };

	private static int[][]			ROOM2_SPAWNS	=
													{
													{ 22303, 146276, 141483, -11880, 34643 },
													{ 22287, 145707, 142161, -11880, 28799 },
													{ 22288, 146857, 142129, -11880, 33647 },
													{ 22288, 146869, 142000, -11880, 31215 },
													{ 22289, 146897, 140880, -11880, 19210 } };

	private static int[][]			ROOM3_SPAWNS	=
													{
													{ 22302, 145123, 143713, -12808, 65323 },
													{ 22294, 145188, 143331, -12808, 496 },
													{ 22294, 145181, 144104, -12808, 64415 },
													{ 22293, 144994, 143431, -12808, 65431 },
													{ 22293, 144976, 143915, -12808, 61461 } };

	private static int[][]			ROOM4_SPAWNS	=
													{
													{ 22304, 150563, 142240, -12108, 16454 },
													{ 22294, 150769, 142495, -12108, 16870 },
													{ 22281, 150783, 141995, -12108, 20033 },
													{ 22283, 150273, 141983, -12108, 16043 },
													{ 22294, 150276, 142492, -12108, 13540 } };

	// Steam Corridor
	private static int[][]			STEAM1_SPAWNS	=
													{
													{ 22305, 145260, 152387, -12165, 32767 },
													{ 22305, 144967, 152390, -12165, 30464 },
													{ 22305, 145610, 152586, -12165, 17107 },
													{ 22305, 145620, 152397, -12165, 8191 },
													{ 22418, 146081, 152847, -12165, 31396 },
													{ 22418, 146795, 152641, -12165, 33850 }
													// {22308, 145093, 152502, -12165, 31841},{22308, 146158, 152776, -12165, 30810},
													// {22308, 146116, 152976, -12133, 32571},

													};
	private static int[][]			STEAM2_SPAWNS	=
													{
													{ 22306, 147740, 152767, -12165, 65043 },
													{ 22306, 148215, 152828, -12165, 970 },
													{ 22306, 147743, 152846, -12165, 64147 },// {22308, 147849, 152854, -12165, 60534},
			// {22308, 147754, 152908, -12141, 59827},{22308, 148194, 152681, -12165, 63620},
			// {22308, 147767, 152939, -12133, 63381},{22309, 147737, 152671, -12165, 65320},
			{ 22418, 148207, 152725, -12165, 61801 },
			{ 22419, 149058, 152828, -12165, 64564 } };

	private static int[][]			STEAM3_SPAWNS	=
													{
													{ 22307, 150735, 152316, -12145, 31930 },
													{ 22307, 150725, 152467, -12165, 33635 },
													{ 22307, 151058, 152316, -12146, 65342 },
													{ 22307, 151057, 152461, -12165, 2171 }
													/*{22308, 150794, 152455, -12165, 31613},{22308, 150665, 152383, -12165, 32767},
													{22308, 151697, 152621, -12167, 31423},{22309, 151061, 152581, -12165, 6228},
													{22309, 150653, 152253, -12132, 31343},{22309, 150628, 152431, -12165, 33022},
													{22309, 151620, 152487, -12165, 30114},{22309, 151672, 152544, -12165, 31846},
													{22309, 150488, 152350, -12165, 29072},{22310, 151139, 152238, -12132, 1069}*/
													};

	private static int[][]			STEAM4_SPAWNS	=
													{//{22308, 151707, 150199, -12165, 32859},{22308, 152091, 150140, -12165, 32938},
														// {22308, 149757, 150204, -12138, 65331},{22308, 149950, 150307, -12132, 62437},
														//{22308, 149901, 150322, -12132, 62136},{22309, 150071, 150173, -12165, 64943},
			{ 22416, 151636, 150280, -12142, 36869 },
			{ 22416, 149893, 150232, -12165, 64258 },
			{ 22416, 149864, 150110, -12165, 65054 },
			{ 22416, 151926, 150218, -12165, 31613 },
			{ 22420, 149986, 150051, -12165, 105 },
			{ 22420, 151970, 149997, -12165, 32170 },
			{ 22420, 150744, 150006, -12165, 63 }	// ,{22417, 149782, 150188, -12151, 64001}
													};

	// Instance reenter time
	// default: 86400000ms(24h)
	private static final int		INSTANCEPENALTY	= 86400000;
	private static final int		DRAGONSCALETIME	= 3000;
	private static final int		DRAGONCLAWTIME	= 3000;

	public CrystalCaverns(int questId, String name, String descr)
	{
		super(questId, name, descr);

		addStartNpc(ORACLE_GUIDE_1);
		addTalkId(ORACLE_GUIDE_1);
		addStartNpc(ORACLE_GUIDE_4);
		addFirstTalkId(ORACLE_GUIDE_4);
		addFirstTalkId(ORACLE_GUIDE_3);
		addTalkId(ORACLE_GUIDE_4);
		addFirstTalkId(CRYSTAL_GOLEM);
		addAttackId(TEARS);
		addKillId(TEARS);
		addKillId(GK1);
		addKillId(GK2);
		addKillId(TEROD);
		addKillId(WEYLIN);
		addKillId(DOLPH);
		addKillId(DARNEL);
		addKillId(KECHI);
		addKillId(MINI_RB2);
		addKillId(GUARDIAN);
		addKillId(GUARDIAN2);
		addKillId(TOURMALINE);
		addKillId(BAYLOR);
		addSpellFinishedId(BAYLOR);
		addKillId(ALARMID);
		int[] Talk =
		{ 32275, 32276, 32277 };
		for (int npc : Talk)
			addTalkId(npc);
		int[] firstTalk =
		{ 32274, 32275, 32276, 32277, ORACLE_GUIDE_1, ORACLE_GUIDE_2 };
		for (int npc : firstTalk)
			addFirstTalkId(npc);
		int[] skillSee =
		{ 25534, 32275, 32276, 32277, BAYLOR };
		for (int npc : skillSee)
			addSkillSeeId(npc);
		for (int mob : MOBLIST)
			addKillId(mob);
		for (int mob : CGMOBS)
			addKillId(mob);
		for (int zones : ZONES)
		{
			addEnterZoneId(zones);
			addExitZoneId(zones);
		}
	}

	private static class teleCoord
	{
		int	instanceId;
		int	x;
		int	y;
		int	z;
	}

	private void openDoor(int doorId, int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			if (door.getDoorId() == doorId)
				door.openMe();
	}

	private void closeDoor(int doorId, int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			if (door.getDoorId() == doorId)
				if (door.isOpen())
					door.closeMe();
	}

	private boolean checkConditions(L2PcInstance player)
	{
		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
			return false;
		}
		for (L2PcInstance partyMember : party.getPartyMembers())
		{
			if (partyMember.getLevel() < 78)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
			L2ItemInstance item = partyMember.getInventory().getItemByItemId(CONT_CRYSTAL);
			if (item == null)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCE_ID);
			if (System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.NO_RE_ENTER_TIME_FOR_C1);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
		}
		return true;
	}

	private boolean checkOracleConditions(L2PcInstance player)
	{
		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
			return false;
		}
		for (L2PcInstance partyMember : party.getPartyMembers())
		{
			L2ItemInstance item = partyMember.getInventory().getItemByItemId(RED_CORAL);
			if (item == null)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
		}
		return true;
	}

	private boolean checkBaylorConditions(L2PcInstance player)
	{
		L2Party party = player.getParty();
		if (party == null)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			return false;
		}
		if (party.getLeader() != player)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
			return false;
		}
		for (L2PcInstance partyMember : party.getPartyMembers())
		{
			L2ItemInstance item1 = partyMember.getInventory().getItemByItemId(BOSS_CRYSTAL_1);
			L2ItemInstance item2 = partyMember.getInventory().getItemByItemId(BOSS_CRYSTAL_2);
			L2ItemInstance item3 = partyMember.getInventory().getItemByItemId(BOSS_CRYSTAL_3);
			if (item1 == null || item2 == null || item3 == null)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_ITEM_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastToPartyMembers(sm);
				return false;
			}
		}
		return true;
	}

	// this should be handled from skill effect
	private void Throw(L2Character effector, L2Character effected)
	{
		// Get current position of the L2Character
		final int curX = effected.getX();
		final int curY = effected.getY();
		final int curZ = effected.getZ();

		// Calculate distance between effector and effected current position
		double dx = effector.getX() - curX;
		double dy = effector.getY() - curY;
		double dz = effector.getZ() - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);
		int offset = Math.min((int) distance + 300, 1400);

		double cos;
		double sin;

		// approximation for moving futher when z coordinates are different
		// TODO: handle Z axis movement better
		offset += Math.abs(dz);
		if (offset < 5)
			offset = 5;

		if (distance < 1)
			return;
		// Calculate movement angles needed
		sin = dy / distance;
		cos = dx / distance;

		// Calculate the new destination with offset included
		int _x = effector.getX() - (int) (offset * cos);
		int _y = effector.getY() - (int) (offset * sin);
		int _z = effected.getZ();

		if (Config.GEODATA > 0)
		{
			Location destiny = GeoData.getInstance().moveCheck(effected.getX(), effected.getY(), effected.getZ(), _x, _y, _z, effected.getInstanceId());
			_x = destiny.getX();
			_y = destiny.getY();
		}
		effected.broadcastPacket(new FlyToLocation(effected, _x, _y, _z, FlyType.THROW_UP));

		// maybe is need force set X,Y,Z
		effected.getPosition().setXYZ(_x, _y, _z);
		effected.broadcastPacket(new ValidateLocation(effected));
	}

	private void teleportplayer(L2PcInstance player, teleCoord teleto)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(teleto.instanceId);
		player.teleToLocation(teleto.x, teleto.y, teleto.z);
		return;
	}

	private int enterInstance(L2PcInstance player, String template, teleCoord teleto)
	{
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof CCWorld))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleto.instanceId = world.instanceId;
			teleportplayer(player, teleto);
			return world.instanceId;
		}
		//New instance
		else
		{
			if (!checkConditions(player))
				return 0;
			L2Party party = player.getParty();
			int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new CCWorld(System.currentTimeMillis() + 5400000);
			world.instanceId = instanceId;
			world.templateId = INSTANCE_ID;
			InstanceManager.getInstance().addWorld(world);
			_log.info("Crystal Caverns started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
			runOracle((CCWorld) world);
			// teleport players
			teleto.instanceId = instanceId;
			if (player.getParty() == null)
			{
				// this can happen only if debug is true
				player.sendMessage("Welcome to Crystal Caverns.");
				InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCE_ID, ((System.currentTimeMillis() + INSTANCEPENALTY)));
				teleportplayer(player, teleto);
				world.allowed.add(player.getObjectId());
			}
			else
			{
				for (L2PcInstance partyMember : party.getPartyMembers())
				{
					partyMember.sendMessage("Welcome to Crystal Caverns.");
					InstanceManager.getInstance().setInstanceTime(partyMember.getObjectId(), INSTANCE_ID, ((System.currentTimeMillis() + INSTANCEPENALTY)));
					teleportplayer(partyMember, teleto);
					world.allowed.add(partyMember.getObjectId());
				}
			}
			return instanceId;
		}
	}

	private void exitInstance(L2PcInstance player, teleCoord tele)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(0);
		player.teleToLocation(tele.x, tele.y, tele.z);
	}

	private void stopAttack(L2PcInstance player)
	{
		player.setTarget(null);
		player.abortAttack();
		player.abortCast();
		player.breakAttack();
		player.breakCast();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		L2Summon pet = player.getPet();
		if (pet != null)
		{
			pet.setTarget(null);
			pet.abortAttack();
			pet.abortCast();
			pet.breakAttack();
			pet.breakCast();
			pet.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}

	private void runOracle(CCWorld world)
	{
		world.status = 0;

		world.oracle.add(addSpawn(ORACLE_GUIDE_1, 143172, 148894, -11975, 0, false, 0, false, world.instanceId));
	}

	private void runEmerald(CCWorld world)
	{
		world.status = 1;
		runFirst(world);
		openDoor(DOOR1, world.instanceId);
	}

	private void runCoral(CCWorld world)
	{
		world.status = 1;
		runHall(world);
		openDoor(DOOR2, world.instanceId);
		openDoor(DOOR5, world.instanceId);
	}

	private void runHall(CCWorld world)
	{
		world.status = 2;

		for (int[] spawn : SPAWNS)
		{
			L2Npc mob = addSpawn(CGMOBS[Rnd.get(CGMOBS.length)], spawn[0], spawn[1], spawn[2], spawn[3], false, 0, false, world.instanceId);
			world.npcList1.put(mob, false);
		}
	}

	private void runFirst(CCWorld world)
	{
		world.status = 2;

		world.keyKeepers.add(addSpawn(GK1, 148206, 149486, -12140, 32308, false, 0, false, world.instanceId));
		world.keyKeepers.add(addSpawn(GK2, 148203, 151093, -12140, 31100, false, 0, false, world.instanceId));

		for (int[] spawn : FIRST_SPAWNS)
		{
			addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
		}
	}

	private void runEmeraldSquare(CCWorld world)
	{
		world.status = 3;

		Map<L2Npc, Boolean> spawnList = new FastMap<L2Npc, Boolean>();
		for (int[] spawn : EMERALD_SPAWNS)
		{
			L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
			spawnList.put(mob, false);
		}
		world.npcList2.put(0, spawnList);
	}

	private void runEmeraldRooms(CCWorld world, int[][] spawnList, int room)
	{
		Map<L2Npc, Boolean> spawned = new FastMap<L2Npc, Boolean>();
		for (int[] spawn : spawnList)
		{
			L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
			spawned.put(mob, false);
		}
		if (room == 1) // spawn Lahm
		{
			addSpawn(32359, 142110, 139896, -11888, 8033, false, 0, false, world.instanceId);
			openDoor(24220001, world.instanceId);
		}
		world.npcList2.put(room, spawned);
		world.roomsStatus[room - 1] = 1;
	}

	private void runDarnel(CCWorld world)
	{
		world.status = 9;

		addSpawn(DARNEL, 152759, 145949, -12588, 21592, false, 0, false, world.instanceId);
		openDoor(24220005, world.instanceId);
		openDoor(24220006, world.instanceId);
	}

	private void runSteamRooms(CCWorld world, int[][] spawnList, int status)
	{
		world.status = status;

		Map<L2Npc, Boolean> spawned = new FastMap<L2Npc, Boolean>();
		for (int[] spawn : spawnList)
		{
			L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
			spawned.put(mob, false);
		}
		world.npcList2.put(0, spawned);
	}

	private void runSteamOracles(CCWorld world, int[][] oracleOrder)
	{
		world.oracles.clear();
		for (int[] oracle : oracleOrder)
		{
			world.oracles.put(addSpawn(oracle[0], oracle[1], oracle[2], oracle[3], oracle[4], false, 0, false, world.instanceId), null);
		}
	}

	private boolean checkKillProgress(int room, L2Npc mob, CCWorld world)
	{
		if (world.npcList2.get(room).containsKey(mob))
			world.npcList2.get(room).put(mob, true);
		for (boolean isDead : world.npcList2.get(room).values())
			if (!isDead)
				return false;
		return true;
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == ORACLE_GUIDE_1)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof CCWorld)
			{
				CCWorld world = (CCWorld) tmpworld;
				if (world.status == 0 && world.oracle.contains(npc))
				{
					String htmltext = "32281.htm";
					return htmltext;
				}
			}
			npc.showChatWindow(player);
			return null;
		}
		else if (npc.getNpcId() >= 32275 && npc.getNpcId() <= 32277)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof CCWorld)
			{
				CCWorld world = (CCWorld) tmpworld;
				if (!world.OracleTriggered[npc.getNpcId() - 32275])
				{
					String htmltext = "no.htm";
					return htmltext;
				}
				else
				{
					// oracle guides are handled here
					L2Party party = player.getParty();
					teleCoord tele = new teleCoord();
					switch (npc.getNpcId())
					{
						case 32275:
							if (world.status == 22)
								runSteamRooms(world, STEAM2_SPAWNS, 23);
							tele.x = 147529;
							tele.y = 152587;
							tele.z = -12169;
							tele.instanceId = world.instanceId;
							cancelQuestTimers("Timer2");
							cancelQuestTimers("Timer21");
							if (checkOracleConditions(player))
							{
								for (L2PcInstance partyMember : party.getPartyMembers())
								{
									partyMember.stopSkillEffects(5239);
									SkillTable.getInstance().getInfo(5239, 2).getEffects(partyMember, partyMember);
									startQuestTimer("Timer3", 600000, npc, partyMember);
									teleportplayer(partyMember, tele);
									partyMember.destroyItemByItemId("Quest", RED_CORAL, 1, player, true);
								}
							}
							startQuestTimer("Timer31", 600000, npc, null);
							break;
						case 32276:
							if (world.status == 23)
								runSteamRooms(world, STEAM3_SPAWNS, 24);
							tele.x = 150194;
							tele.y = 152610;
							tele.z = -12169;
							tele.instanceId = world.instanceId;
							cancelQuestTimers("Timer3");
							cancelQuestTimers("Timer31");
							if (checkOracleConditions(player))
							{
								for (L2PcInstance partyMember : party.getPartyMembers())
								{
									partyMember.stopSkillEffects(5239);
									SkillTable.getInstance().getInfo(5239, 4).getEffects(partyMember, partyMember);
									startQuestTimer("Timer4", 1200000, npc, partyMember);
									teleportplayer(partyMember, tele);
									partyMember.destroyItemByItemId("Quest", RED_CORAL, 1, player, true);
								}
							}
							startQuestTimer("Timer41", 1200000, npc, null);
							break;
						case 32277:
							if (world.status == 24)
								runSteamRooms(world, STEAM4_SPAWNS, 25);
							tele.x = 149743;
							tele.y = 149986;
							tele.z = -12141;
							tele.instanceId = world.instanceId;
							cancelQuestTimers("Timer4");
							cancelQuestTimers("Timer41");
							if (checkOracleConditions(player))
							{
								for (L2PcInstance partyMember : party.getPartyMembers())
								{
									partyMember.stopSkillEffects(5239);
									SkillTable.getInstance().getInfo(5239, 3).getEffects(partyMember, partyMember);
									startQuestTimer("Timer5", 900000, npc, partyMember);
									teleportplayer(partyMember, tele);
									partyMember.destroyItemByItemId("Quest", RED_CORAL, 1, player, true);
								}
							}
							startQuestTimer("Timer51", 900000, npc, null);
							break;
					}
					npc.showChatWindow(player);
					return null;
				}
			}
		}
		else if (npc.getNpcId() == ORACLE_GUIDE_3)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof CCWorld)
			{
				CCWorld world = (CCWorld) tmpworld;
				if (world.status < 30 && checkBaylorConditions(player))
				{
					world._raiders.clear();
					L2Party party = player.getParty();
					if (party == null)
						world._raiders.add(player);
					else
					{
						for (L2PcInstance partyMember : party.getPartyMembers())
						{
							int rnd = Rnd.get(100);
							partyMember.destroyItemByItemId("Quest", (rnd < 33 ? BOSS_CRYSTAL_1 : (rnd < 67 ? BOSS_CRYSTAL_2 : BOSS_CRYSTAL_3)), 1,
									partyMember, true);
							world._raiders.add(partyMember);
						}
					}
					world.status = 30;
					long time = world.endTime - System.currentTimeMillis();
					Instance baylorInstance = InstanceManager.getInstance().getInstance(world.instanceId);
					baylorInstance.setDuration((int) time);

					int radius = 150;
					int i = 0;
					int members = world._raiders.size();
					for (L2PcInstance p : world._raiders)
					{
						int x = (int) (radius * Math.cos(i * 2 * Math.PI / members));
						int y = (int) (radius * Math.sin(i++ * 2 * Math.PI / members));
						p.teleToLocation(153571 + x, 142075 + y, -12737);
						L2Summon pet = p.getPet();
						if (pet != null)
						{
							pet.teleToLocation(153571 + x, 142075 + y, -12737, true);
							pet.broadcastPacket(new ValidateLocation(pet));
						}
						p.setIsParalyzed(true);
						p.broadcastPacket(new ValidateLocation(p));
					}
					startQuestTimer("Baylor", 30000, npc, null);
				}
				else
					return "";
			}
		}
		else if (npc.getNpcId() == 32274)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof CCWorld)
			{
				String htmltext = "no.htm";
				return htmltext;
			}
		}
		else if (npc.getNpcId() == 32279)
		{
			QuestState st = player.getQuestState("131_BirdInACage");
			String htmltext = "32279.htm";
			if (st != null && st.getState() != State.COMPLETED)
				htmltext = "32279-01.htm";
			return htmltext;
		}
		else if (npc.getNpcId() == CRYSTAL_GOLEM)
			player.sendPacket(ActionFailed.STATIC_PACKET);
		return "";
	}

	@Override
	public final String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{

		boolean doReturn = true;
		for (L2Object obj : targets)
			if (obj == npc)
				doReturn = false;
		if (doReturn)
			return super.onSkillSee(npc, caster, skill, targets, isPet);

		switch (skill.getId())
		{
			case 1011:
			case 1015:
			case 1217:
			case 1218:
			case 1401:
			case 2360:
			case 2369:
			case 5146:
				doReturn = false;
				break;
			default:
				doReturn = true;
		}
		if (doReturn)
			return super.onSkillSee(npc, caster, skill, targets, isPet);

		if (npc.getNpcId() >= 32275 && npc.getNpcId() <= 32277 && skill.getId() != 2360 && skill.getId() != 2369)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof CCWorld && Rnd.get(100) < 15)
			{
				for (L2Npc oracle : ((CCWorld) tmpworld).oracles.keySet())
					if (oracle != npc)
						oracle.decayMe();
				((CCWorld) tmpworld).OracleTriggered[npc.getNpcId() - 32275] = true;
			}
		}
		else if (npc.isInvul() && npc.getNpcId() == BAYLOR && skill.getId() == 2360 && caster != null)
		{
			if (caster.getParty() == null)
			{
				return super.onSkillSee(npc, caster, skill, targets, isPet);
			}
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof CCWorld)
			{
				CCWorld world = (CCWorld) tmpworld;

				if ((world._dragonClawStart + DRAGONCLAWTIME) <= System.currentTimeMillis() || world._dragonClawNeed <= 0)
				{
					world._dragonClawStart = System.currentTimeMillis();
					world._dragonClawNeed = caster.getParty().getMemberCount() - 1;
				}
				else
				{
					world._dragonClawNeed--;
				}
				if (world._dragonClawNeed == 0)
				{
					npc.stopSkillEffects(5225);
					npc.broadcastPacket(new MagicSkillUse(npc, npc, 5480, 1, 4000, 0));
					if (world._raidStatus == 3)
						world._raidStatus++;
				}
			}
		}
		else if (npc.isInvul() && npc.getNpcId() == TEARS && skill.getId() == 2369 && caster != null)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof CCWorld)
			{
				CCWorld world = (CCWorld) tmpworld;
				if (caster.getParty() == null)
				{
					return super.onSkillSee(npc, caster, skill, targets, isPet);
				}
				else if ((world.dragonScaleStart + DRAGONSCALETIME) <= System.currentTimeMillis() || world.dragonScaleNeed <= 0)
				{
					world.dragonScaleStart = System.currentTimeMillis();
					world.dragonScaleNeed = caster.getParty().getMemberCount() - 1;
				}
				else
				{
					world.dragonScaleNeed--;
				}
				if (world.dragonScaleNeed == 0 && Rnd.get(100) < 80)
					npc.setIsInvul(false);
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public final String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		if (npc.getNpcId() == TEARS)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof CCWorld)
			{
				CCWorld world = (CCWorld) tmpworld;
				if (world.status != 4 && attacker != null)
				{
					// Lucky cheater, the code only kicks his/her ass out of the dungeon
					teleCoord tele = new teleCoord();
					tele.x = 149361;
					tele.y = 172327;
					tele.z = -945;
					exitInstance(attacker, tele);
					world.allowed.remove(attacker.getObjectId());
				}
				else if (world.tears != npc)
					return "";
				else if (!world.copys.isEmpty())
				{
					boolean notAOE = true;
					if (skill != null
							&& (skill.getTargetType() == SkillTargetType.TARGET_AREA || skill.getTargetType() == SkillTargetType.TARGET_FRONT_AREA
									|| skill.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA || skill.getTargetType() == SkillTargetType.TARGET_AURA
									|| skill.getTargetType() == SkillTargetType.TARGET_FRONT_AURA || skill.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA))
						notAOE = false;
					if (notAOE)
					{
						for (L2Npc copy : world.copys)
							copy.onDecay();
						world.copys.clear();
					}
					return "";
				}

				int maxHp = npc.getMaxHp();
				double nowHp = npc.getStatus().getCurrentHp();
				int rand = Rnd.get(1000);

				if (nowHp < maxHp * 0.4 && rand < 5)
				{
					L2Party party = attacker.getParty();
					if (party != null)
						for (L2PcInstance partyMember : party.getPartyMembers())
							stopAttack(partyMember);
					else
						stopAttack(attacker);
					L2Character target = npc.getAI().getAttackTarget();
					for (int i = 0; i < 10; i++)
					{
						L2Npc copy = addSpawn(TEARS_COPY, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false, attacker.getInstanceId());
						copy.setRunning();
						((L2Attackable) copy).addDamageHate(target, 0, 99999);
						copy.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						copy.getStatus().setCurrentHp(nowHp);
						world.copys.add(copy);
					}
				}
				else if (nowHp < maxHp * 0.15 && !world.isUsedInvulSkill)
				{
					if (rand > 994 || nowHp < maxHp * 0.1)
					{
						world.isUsedInvulSkill = true;
						npc.setIsInvul(true);
					}
				}
			}
		}
		return null;
	}

	@Override
	public final String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (npc.getNpcId() == BAYLOR && skill.getId() == 5225)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if (tmpworld instanceof CCWorld)
				((CCWorld) tmpworld)._raidStatus++;
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@SuppressWarnings("deprecation")
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof CCWorld)
		{
			CCWorld world = (CCWorld) tmpworld;
			teleCoord teleto = new teleCoord();
			teleto.instanceId = world.instanceId;
			if (event.equalsIgnoreCase("TeleportOut"))
			{
				teleCoord tele = new teleCoord();
				tele.x = 149413;
				tele.y = 173078;
				tele.z = -5014;
				exitInstance(player, tele);
			}
			else if (event.equalsIgnoreCase("TeleportParme"))
			{
				teleCoord tele = new teleCoord();
				tele.x = 153689;
				tele.y = 142226;
				tele.z = -9750;
				tele.instanceId = world.instanceId;
				teleportplayer(player, tele);
			}
			else if (event.equalsIgnoreCase("Timer2") || event.equalsIgnoreCase("Timer3") || event.equalsIgnoreCase("Timer4")
					|| event.equalsIgnoreCase("Timer5"))
			{
				teleto.x = 144653;
				teleto.y = 152606;
				teleto.z = -12126;
				if (player.getInstanceId() == world.instanceId)
				{
					teleportplayer(player, teleto);
					player.stopSkillEffects(5239);
					SkillTable.getInstance().getInfo(5239, 1).getEffects(player, player);
					startQuestTimer("Timer2", 300000, npc, player);
				}
			}
			else if (event.equalsIgnoreCase("Timer21") || event.equalsIgnoreCase("Timer31") || event.equalsIgnoreCase("Timer41")
					|| event.equalsIgnoreCase("Timer51"))
			{
				InstanceManager.getInstance().getInstance(world.instanceId).removeNpcs();
				world.npcList2.clear();
				runSteamRooms(world, STEAM1_SPAWNS, 22);
				startQuestTimer("Timer21", 300000, npc, null);
			}

			else if (event.equalsIgnoreCase("checkKechiAttack"))
			{
				if (npc.isInCombat())
				{
					cancelQuestTimers("checkKechiAttack");
					closeDoor(DOOR4, npc.getInstanceId());
					closeDoor(DOOR3, npc.getInstanceId());
				}
				else
					startQuestTimer("checkKechiAttack", 10000, npc, null);
			}
			else if (event.equalsIgnoreCase("EmeraldSteam"))
			{
				runEmerald(world);
				for (L2Npc oracle : world.oracle)
					oracle.decayMe();
			}
			else if (event.equalsIgnoreCase("CoralGarden"))
			{
				runCoral(world);
				for (L2Npc oracle : world.oracle)
					oracle.decayMe();
			}
			else if (event.equalsIgnoreCase("spawn_oracle"))
			{
				addSpawn(32271, 153572, 142075, -9728, 10800, false, 0, false, world.instanceId);
				addSpawn((Rnd.get(10) < 5 ? BAYLOR_CHEST1 : BAYLOR_CHEST2), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0, false,
						world.instanceId); // Baylor's Chest
				addSpawn(ORACLE_GUIDE_4, 153572, 142075, -12738, 10800, false, 0, false, world.instanceId);
				this.cancelQuestTimer("baylor_despawn", npc, null);
				this.cancelQuestTimers("baylor_skill");
			}
			else if (event.equalsIgnoreCase("baylorEffect0"))
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
				startQuestTimer("baylorCamera0", 11000, npc, null);
				startQuestTimer("baylorEffect1", 19000, npc, null);
			}
			else if (event.equalsIgnoreCase("baylorCamera0"))
			{
				npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 500, -45, 170, 5000, 9000, 0, 0, 1, 0));
			}
			else if (event.equalsIgnoreCase("baylorEffect1"))
			{
				npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 300, 0, 120, 2000, 5000, 0, 0, 1, 0));
				npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
				startQuestTimer("baylorEffect2", 4000, npc, null);
			}
			else if (event.equalsIgnoreCase("baylorEffect2"))
			{
				npc.broadcastPacket(new SpecialCamera(npc.getObjectId(), 747, 0, 160, 2000, 3000, 0, 0, 1, 0));
				npc.broadcastPacket(new MagicSkillUse(npc, npc, 5402, 1, 2000, 0));
				startQuestTimer("RaidStart", 2000, npc, null);
			}
			else if (event.equalsIgnoreCase("BaylorMinions"))
			{
				for (int i = 0; i < 10; i++)
				{
					int radius = 300;
					int x = (int) (radius * Math.cos(i * 0.618));
					int y = (int) (radius * Math.sin(i * 0.618));
					L2Npc mob = addSpawn(29104, 153571 + x, 142075 + y, -12737, 0, false, 0, false, world.instanceId);
					mob.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					world._animationMobs.add(mob);
				}
				startQuestTimer("baylorEffect0", 200, npc, null);
			}
			else if (event.equalsIgnoreCase("RaidStart"))
			{
				world._camera.decayMe();
				world._camera = null;
				npc.setIsParalyzed(false);
				for (L2PcInstance p : world._raiders)
				{
					p.setIsParalyzed(false);
					Throw(npc, p);
					if (p.getPet() != null)
						Throw(npc, p.getPet());
				}
				world._raidStatus = 0;
				for (L2Npc mob : world._animationMobs)
				{
					mob.doDie(mob);
				}
				world._animationMobs.clear();
				startQuestTimer("baylor_despawn", 60000, npc, null, true);
				startQuestTimer("checkBaylorAttack", 1000, npc, null);
			}
			else if (event.equalsIgnoreCase("checkBaylorAttack"))
			{
				if (npc.isInCombat())
				{
					cancelQuestTimers("checkBaylorAttack");
					startQuestTimer("baylor_alarm", 40000, npc, null);
					startQuestTimer("baylor_skill", 5000, npc, null, true);
					world._raidStatus++;
				}
				else
					startQuestTimer("checkBaylorAttack", 1000, npc, null);
			}
			else if (event.equalsIgnoreCase("baylor_alarm"))
			{
				if (world._alarm == null)
				{
					int[] spawnLoc = ALARMSPAWN[Rnd.get(ALARMSPAWN.length)];
					npc.addSkill(SkillTable.getInstance().getInfo(5244, 1));
					npc.addSkill(SkillTable.getInstance().getInfo(5245, 1));
					world._alarm = addSpawn(ALARMID, spawnLoc[0], spawnLoc[1], spawnLoc[2], 10800, false, 0, false, world.instanceId);
					world._alarm.disableCoreAI(true);
					world._alarm.setIsImmobilized(true);
					world._alarm.broadcastPacket(new CreatureSay(world._alarm.getObjectId(), SystemChatChannelId.Chat_Shout, world._alarm.getName(),
							"Alarm signal was switched off! All will in the danger, if we do not take measures immediately!"));
				}
			}
			else if (event.equalsIgnoreCase("baylor_skill"))
			{
				if (world._baylor == null)
				{
					cancelQuestTimers("baylor_skill");
				}
				else
				{
					int maxHp = npc.getMaxHp();
					double nowHp = npc.getStatus().getCurrentHp();
					int rand = Rnd.get(100);

					if (nowHp < maxHp * 0.2 && world._raidStatus < 3 && npc.getFirstEffect(5224) == null && npc.getFirstEffect(5225) == null)
					{
						if (nowHp < maxHp * 0.15 && world._raidStatus == 2)
						{
							npc.doCast(SkillTable.getInstance().getInfo(5225, 1));
							npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(),
									"Demons King Beleth, give me power! Aaaaa!!!"));
						}
						else if (rand < 10 || nowHp < maxHp * 0.15)
						{
							npc.doCast(SkillTable.getInstance().getInfo(5225, 1));
							npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(),
									"Demons King Beleth, give me power! Aaaaa!!!"));
							startQuestTimer("baylor_remove_invul", 30000, world._baylor, null);
						}
					}
					else if (nowHp < maxHp * 0.3 && rand > 50 && npc.getFirstEffect(5225) == null && npc.getFirstEffect(5224) == null)
					{
						npc.doCast(SkillTable.getInstance().getInfo(5224, 1));
					}
					else if (rand < 33)
					{
						npc.setTarget(world._raiders.get(Rnd.get(world._raiders.size())));
						npc.doCast(SkillTable.getInstance().getInfo(5229, 1));
					}
				}
			}
			else if (event.equalsIgnoreCase("baylor_remove_invul"))
			{
				npc.stopSkillEffects(5225);
			}
			else if (event.equalsIgnoreCase("Baylor"))
			{
				world._baylor = addSpawn(29099, 153572, 142075, -12738, 10800, false, 0, false, world.instanceId);
				world._baylor.setIsParalyzed(true);
				world._camera = addSpawn(29120, 153273, 141400, -12738, 10800, false, 0, false, world.instanceId);
				world._camera.broadcastPacket(new SpecialCamera(world._camera.getObjectId(), 700, -45, 160, 500, 15200, 0, 0, 1, 0));
				startQuestTimer("baylorMinions", 2000, world._baylor, null);
			}
			else if (!event.endsWith("Food"))
				return "";
			else if (event.equalsIgnoreCase("autoFood"))
			{
				if (!world.crystalGolems.containsKey(npc))
					world.crystalGolems.put(npc, new CrystalGolem());
				if (world.status != 3 || !world.crystalGolems.containsKey(npc) || world.crystalGolems.get(npc).foodItem != null
						|| world.crystalGolems.get(npc).isAtDestination)
					return "";
				CrystalGolem cryGolem = world.crystalGolems.get(npc);
				List<L2Object> crystals = new FastList<L2Object>();
				for (L2Object object : L2World.getInstance().getVisibleObjects(npc, 300))
				{
					if (object instanceof L2ItemInstance && ((L2ItemInstance) object).getItemId() == CRYSTALFOOD)
						crystals.add(object);
				}
				int minDist = 300000;
				for (L2Object crystal : crystals)
				{
					int dx = npc.getX() - crystal.getX();
					int dy = npc.getY() - crystal.getY();
					int d = dx * dx + dy * dy;
					if (d < minDist)
					{
						minDist = d;
						cryGolem.foodItem = (L2ItemInstance) crystal;
					}
				}
				if (minDist != 300000)
					startQuestTimer("getFood", 2000, npc, null);
				else
				{
					if (Rnd.get(100) < 5)
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), SystemChatChannelId.Chat_Shout, npc.getName(), "ah ... I am so hungry ..."));
					startQuestTimer("autoFood", 2000, npc, null);
				}
				return "";
			}
			else if (!world.crystalGolems.containsKey(npc) || world.crystalGolems.get(npc).isAtDestination)
				return "";
			else if (event.equalsIgnoreCase("backFood"))
			{
				if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
				{
					cancelQuestTimers("backFood");
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
					world.crystalGolems.get(npc).foodItem = null;
					startQuestTimer("autoFood", 2000, npc, null);
				}
			}
			else if (event.equalsIgnoreCase("reachFood"))
			{
				CrystalGolem cryGolem = world.crystalGolems.get(npc);
				int dx;
				int dy;
				if (cryGolem.foodItem == null || !cryGolem.foodItem.isVisible())
				{
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, cryGolem.oldpos);
					cancelQuestTimers("reachFood");
					startQuestTimer("backFood", 2000, npc, null, true);
					return "";
				}
				else if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
				{
					L2World.getInstance().removeVisibleObject(cryGolem.foodItem, cryGolem.foodItem.getWorldRegion());
					L2World.getInstance().removeObject(cryGolem.foodItem);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
					cryGolem.foodItem = null;
					dx = npc.getX() - 142999;
					dy = npc.getY() - 151671;
					int d1 = dx * dx + dy * dy;
					dx = npc.getX() - 139494;
					dy = npc.getY() - 151668;
					int d2 = dx * dx + dy * dy;
					if (d1 < 10000 || d2 < 10000)
					{
						npc.broadcastPacket(new MagicSkillUse(npc, npc, 5441, 1, 1, 0));
						cryGolem.isAtDestination = true;
						world.correctGolems++;
						if (world.correctGolems >= 2)
						{
							openDoor(24220026, world.instanceId);
							world.status = 4;
						}
					}
					else
						startQuestTimer("autoFood", 2000, npc, null);
					cancelQuestTimers("reachFood");
				}
				return "";
			}
			else if (event.equalsIgnoreCase("getFood"))
			{
				CrystalGolem cryGolem = world.crystalGolems.get(npc);
				L2CharPosition newpos = new L2CharPosition(cryGolem.foodItem.getX(), cryGolem.foodItem.getY(), cryGolem.foodItem.getZ(), 0);
				cryGolem.oldpos = new L2CharPosition(npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, newpos);
				startQuestTimer("reachFood", 2000, npc, null, true);
				cancelQuestTimers("getFood");
			}
		}
		return "";
	}

	private void giveRewards(L2PcInstance player, int instanceId, int bossCry, boolean isBaylor)
	{
		final int num = 1;

		L2Party party = player.getParty();
		if (party != null)
		{
			for (L2PcInstance partyMember : party.getPartyMembers())
				if (partyMember.getInstanceId() == instanceId)
				{
					QuestState st = partyMember.getQuestState(QN);
					if (st == null)
						st = newQuestState(partyMember);
					if (!isBaylor && st.getQuestItemsCount(CONT_CRYSTAL) > 0)
					{
						st.takeItems(CONT_CRYSTAL, 1);
						st.giveItems(bossCry, 1);
					}
					if (Rnd.get(10) < 5)
						st.giveItems(WHITE_SEED, num);
					else
						st.giveItems(BLACK_SEED, num);
				}
		}
		else if (player.getInstanceId() == instanceId)
		{
			QuestState st = player.getQuestState(QN);
			if (st == null)
				st = newQuestState(player);
			if (!isBaylor && st.getQuestItemsCount(CONT_CRYSTAL) > 0)
			{
				st.takeItems(CONT_CRYSTAL, 1);
				st.giveItems(bossCry, 1);
			}
			if (Rnd.get(10) < 5)
				st.giveItems(WHITE_SEED, num);
			else
				st.giveItems(BLACK_SEED, num);
		}

	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		int npcID = npc.getNpcId();
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof CCWorld)
		{
			CCWorld world = (CCWorld) tmpworld;
			if (world.status == 2 && world.npcList1.containsKey(npc))
			{
				world.npcList1.put(npc, true);
				for (boolean isDead : world.npcList1.values())
					if (!isDead)
						return "";
				world.status = 3;
				world.tears = addSpawn(TEARS, 144298, 154420, -11854, 32767, false, 0, false, world.instanceId); // Tears
				CrystalGolem crygolem1 = new CrystalGolem();
				CrystalGolem crygolem2 = new CrystalGolem();
				world.crystalGolems.put(addSpawn(CRYSTAL_GOLEM, 140547, 151670, -11813, 32767, false, 0, false, world.instanceId), crygolem1);
				world.crystalGolems.put(addSpawn(CRYSTAL_GOLEM, 141941, 151684, -11813, 63371, false, 0, false, world.instanceId), crygolem2);
				for (L2Npc crygolem : world.crystalGolems.keySet())
					startQuestTimer("autoFood", 2000, crygolem, null);
			}
			else if (world.status == 4 && npc.getNpcId() == TEARS)
			{
				InstanceManager.getInstance().getInstance(world.instanceId).setDuration(300000);
				addSpawn(32280, 144312, 154420, -11855, 0, false, 0, false, world.instanceId);
				giveRewards(player, npc.getInstanceId(), BOSS_CRYSTAL_3, false);
			}
			else if (world.status == 2 && world.keyKeepers.contains(npc))
			{
				if (npc.getNpcId() == GK1)
				{
					((L2MonsterInstance) npc).dropItem(player, 9698, 1);
					runEmeraldSquare(world);
				}
				else if (npc.getNpcId() == GK2)
				{
					((L2MonsterInstance) npc).dropItem(player, 9699, 1);
					runSteamRooms(world, STEAM1_SPAWNS, 22);
					L2Party party = player.getParty();
					if (party != null)
						for (L2PcInstance partyMember : party.getPartyMembers())
						{
							if (partyMember.getInstanceId() == world.instanceId)
							{
								SkillTable.getInstance().getInfo(5239, 1).getEffects(partyMember, partyMember);
								startQuestTimer("Timer2", 300000, npc, partyMember);
							}
						}
					else
					{
						SkillTable.getInstance().getInfo(5239, 1).getEffects(player, player);
						startQuestTimer("Timer2", 300000, npc, player);
					}
					startQuestTimer("Timer21", 300000, npc, null);
				}
				for (L2Npc gk : world.keyKeepers)
					if (gk != npc)
						gk.decayMe();
			}
			else if (world.status == 3)
			{
				if (checkKillProgress(0, npc, world))
				{
					world.status = 4;
					addSpawn(TOURMALINE, 148202, 144791, -12235, 0, false, 0, false, world.instanceId);
				}
				else
					return "";
			}
			else if (world.status == 4 && npcID == TOURMALINE)
			{
				world.status = 5;
				addSpawn(TEROD, 147777, 146780, -12281, 0, false, 0, false, world.instanceId);
			}
			else if (world.status == 5 && npcID == TEROD)
			{
				world.status = 6;
				addSpawn(TOURMALINE, 143694, 142659, -11882, 0, false, 0, false, world.instanceId);
			}
			else if (world.status == 6 && npcID == TOURMALINE)
			{
				world.status = 7;
				addSpawn(DOLPH, 142054, 143288, -11825, 0, false, 0, false, world.instanceId);
			}
			else if (world.status == 7 && npcID == DOLPH)
			{
				world.status = 8;
				runEmeraldRooms(world, ROOM1_SPAWNS, 1);
			}
			else if (world.status == 8)
			{
				switch (npcID)
				{
					case GUARDIAN:
						world.emelardMiniRbKilled++;
						if (world.emelardMiniRbKilled > 2)
						{
							runDarnel(world);
							world.cleanedRooms++;
						}
						break;
					case MINI_RB2:
						world.emelardMiniRbKilled++;
						if (world.emelardMiniRbKilled > 2)
						{
							runDarnel(world);
							world.cleanedRooms++;
						}
						break;
					case GUARDIAN2:
						world.emelardMiniRbKilled++;
						if (world.emelardMiniRbKilled > 2)
						{
							runDarnel(world);
							world.cleanedRooms++;
						}
						break;
				}
			}
			else if (world.status >= 22 && world.status <= 25)
			{
				if (npc.getNpcId() == 22416)
				{
					for (L2Npc oracle : world.oracles.keySet())
						if (world.oracles.get(oracle) == npc)
							world.oracles.put(oracle, null);
				}
				if (checkKillProgress(0, npc, world))
				{
					world.npcList2.clear();
					int[][] oracleOrder;
					switch (world.status)
					{
						case 22:
							closeDoor(DOOR6, npc.getInstanceId());
							oracleOrder = ORDER_ORACLE1;
							break;
						case 23:
							oracleOrder = ORDER_ORACLE2;
							break;
						case 24:
							oracleOrder = ORDER_ORACLE3;
							break;
						case 25:
							world.status = 26;
							L2Party party = player.getParty();
							if (party != null)
								for (L2PcInstance partyMember : party.getPartyMembers())
									partyMember.stopSkillEffects(5239);
							cancelQuestTimers("Timer5");
							cancelQuestTimers("Timer51");
							openDoor(DOOR3, npc.getInstanceId());
							openDoor(DOOR4, npc.getInstanceId());
							L2Npc kechi = addSpawn(KECHI, 154069, 149525, -12158, 51165, false, 0, false, world.instanceId);
							startQuestTimer("checkKechiAttack", 5000, kechi, null);
							return "";
						default:
							_log.warn("CrystalCavern-SteamCorridor: status " + world.status + " error. OracleOrder not found in " + world.instanceId);
							return "";
					}
					runSteamOracles(world, oracleOrder);
				}
			}
			else if ((world.status == 9 && npc.getNpcId() == DARNEL) || (world.status == 26 && npc.getNpcId() == KECHI))
			{
				InstanceManager.getInstance().getInstance(world.instanceId).setDuration(300000);
				int bossCry;
				if (npc.getNpcId() == KECHI)
				{
					bossCry = BOSS_CRYSTAL_2;
					addSpawn(32280, 154077, 149527, -12159, 0, false, 0, false, world.instanceId);
				}
				else if (npc.getNpcId() == DARNEL)
				{
					bossCry = BOSS_CRYSTAL_1;
					addSpawn(32280, 152761, 145950, -12588, 0, false, 0, false, world.instanceId);
				}
				else
				{
					// something is wrong
					return "";
				}
				giveRewards(player, npc.getInstanceId(), bossCry, false);
			}
			if (npc.getNpcId() == ALARMID)
			{
				world._baylor.removeSkill(5244);
				world._baylor.removeSkill(5245);
				world._alarm = null;
				if (world._baylor.getMaxHp() * 0.3 < world._baylor.getStatus().getCurrentHp())
					startQuestTimer("baylor_alarm", 40000, world._baylor, null);
			}
			else if (npc.getNpcId() == BAYLOR)
			{
				world.status = 31;
				world._baylor = null;
				npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
				Instance baylorInstance = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				baylorInstance.setDuration(300000);
				this.startQuestTimer("spawn_oracle", 1000, npc, null);
				giveRewards(player, npc.getInstanceId(), -1, true);
			}
		}
		return "";
	}

	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(QN);
		if (st == null)
			st = newQuestState(player);
		if (npcId == ORACLE_GUIDE_1)
		{
			teleCoord tele = new teleCoord();
			tele.x = 143348;
			tele.y = 148707;
			tele.z = -11972;
			enterInstance(player, "CrystalCaverns.xml", tele);
			return "";
		}
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof CCWorld)
		{
			CCWorld world = (CCWorld) tmpworld;
			if (npcId == CRYSTAL_GOLEM)
			{
			}
			else if (npc.getNpcId() == ORACLE_GUIDE_4 && world.status == 31)
			{
				teleCoord teleto = new teleCoord();
				teleto.instanceId = npc.getInstanceId();
				teleto.x = 153522;
				teleto.y = 144212;
				teleto.z = -9747;
				teleportplayer(player, teleto);
			}
		}
		return "";
	}

	@Override
	public final String onEnterZone(L2Character character, L2Zone zone)
	{
		if (character instanceof L2PcInstance)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if (tmpworld instanceof CCWorld)
			{
				CCWorld world = (CCWorld) tmpworld;
				if (world.status == 8)
				{
					int room;
					int[][] spawns;
					switch (zone.getId())
					{
						case 2:
							spawns = ROOM2_SPAWNS;
							room = 2;
							break;
						case 3:
							spawns = ROOM3_SPAWNS;
							room = 3;
							break;
						case 4:
							spawns = ROOM4_SPAWNS;
							room = 4;
							break;
						default:
							return super.onEnterZone(character, zone);
					}
					for (L2DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors())
						if (door.getDoorId() == (room + 24220000))
						{
							if (door.isOpen())
								return "";
							else
							{
								QuestState st = ((L2PcInstance) character).getQuestState(QN);
								if (st == null)
									st = newQuestState((L2PcInstance) character);
								if (st.getQuestItemsCount(RACE_KEY) == 0)
									return "";
								if (world.roomsStatus[zone.getId() - 1] == 0)
									runEmeraldRooms(world, spawns, room);
								door.openMe();
								st.takeItems(RACE_KEY, 1);
								world.openedDoors.put(door, (L2PcInstance) character);
							}
							break;
						}
				}
			}
		}
		return super.onEnterZone(character, zone);
	}

	@Override
	public final String onExitZone(L2Character character, L2Zone zone)
	{
		if (character instanceof L2PcInstance)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if (tmpworld instanceof CCWorld)
			{
				CCWorld world = (CCWorld) tmpworld;
				if (world.status == 8)
				{
					int doorId;
					switch (zone.getId())
					{
						case 2:
							doorId = 24220002;
							break;
						case 3:
							doorId = 24220003;
							break;
						case 4:
							doorId = 24220004;
							break;
						default:
							return super.onExitZone(character, zone);
					}
					for (L2DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors())
						if (door.getDoorId() == doorId)
						{
							if (door.isOpen() && world.openedDoors.get(door) == character)
							{
								door.closeMe();
								world.openedDoors.remove(door);
							}
							break;
						}

				}
			}
		}
		return super.onExitZone(character, zone);
	}

	public static void main(String[] args)
	{
		// now call the constructor (starts up the)
		new CrystalCaverns(-1, QN, "instances");
	}
}