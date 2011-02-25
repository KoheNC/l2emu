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
package official_events.SavingSanta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.Announcements;
import net.l2emuproject.gameserver.datatables.EventDroplist;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.model.quest.QuestState;
import net.l2emuproject.gameserver.model.quest.jython.QuestJython;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.network.serverpackets.MagicSkillUse;
import net.l2emuproject.gameserver.network.serverpackets.SocialAction;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.script.DateRange;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.util.Broadcast;
import net.l2emuproject.gameserver.util.Util;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.zone.L2Zone;
import net.l2emuproject.tools.random.Rnd;

/**
 * @author TheOne & janiii & Gigiikun
 */
public class SavingSanta extends QuestJython
{
	private static final String		QN							= "SavingSanta";
	/**
	 * Array of global drops for this event.
	 */
	private static final int[]		EVENT_GLOBAL_DROP			=
																{ 5556, 5557, 5558, 5559, 5562, 5563, 5564, 5565, 5566 };
	/**
	 * Minimum and maximum count of dropped items.
	 */
	private static final int[]		EVENT_GLOBAL_DROP_COUNT		=
																{ 1, 1 };

	/**
	 * Announcement displayed to player when entering game.
	 */
	private static final String[]	ON_ENTER_ANNOUNCE			=
																{ "Christmas Event:\nAll the mobs on server have a chance of dropping Christmas tree parts and Holiday carol Echo Crystals.  See the Santa's Helpers in every town for more informations or to trade your Christmas tree parts!" };

	private static final DateRange	EVENT_DATES					= DateRange.parse(Config.SAVING_SANTA_DATE, new SimpleDateFormat("dd MM yyyy", Locale.US));

	private static final Date		END_DATE					= EVENT_DATES.getEndDate();
	private static final Date		CURRENT_DATE				= new Date();

	private static final int[]		_requiredPieces				=
																{ 5556, 5557, 5558, 5559 };
	private static final int[]		_requiredQty				=
																{ 4, 4, 10, 1 };
	private static final int		SANTA						= 31863;
	private static final int		HOLIDAY_SANTA				= 4;
	private static final int		THOMAS						= 13183;
	private static final long		MIN_TIME_BETWEEN_2_REWARDS	= 43200000;
	private static final int		HOLIDAYSANTASREWARD			= 20101;
	private static final int		HOLIDAYBUFFID				= 23017;
	private static final int[]		RANDOM_A_PLUS_10_WEAPON		=
																{ 81, 151, 164, 213, 236, 270, 289, 2500, 7895, 7902, 5706 };
	// 0: Santas Helper Auto buff, 1: Saving Santa part
	private static final boolean[]	CONFIG						=
																{ false, true };
	private static final int[]		THOMAS_LOC					=
																{ 117935, -126003, -2585, 54625 };
	private static final int[]		SANTA_MAGE_BUFFS			=
																{ 7055, 7054, 7051 };
	private static final int[]		SANTA_FIGHTER_BUFFS			=
																{ 7043, 7057, 7051 };

	private static final int[]		_santaX						=
																{
			147698,
			147443,
			82218,
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
			-117195											};
	private static final int[]		_santaY						=
																{
			-56025,
			26942,
			148605,
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
			46837												};
	private static final int[]		_santaZ						=
																{
			-2775,
			-2205,
			-3470,
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
			367												};

	private static final int[]		_treeSpawnX					=
																{
			83254,
			83278,
			83241,
			83281,
			84304,
			84311,
			82948,
			80905,
			80908,
			82957,
			147849,
			147580,
			147581,
			147847,
			149085,
			146340,
			147826,
			147584,
			146235,
			147840,
			147055,
			148694,
			147733,
			147197,
			147266,
			147646,
			147456,
			148078,
			147348,
			117056,
			116473,
			115785,
			115939,
			116833,
			116666,
			-13130,
			-13165,
			-13126,
			15733,
			16208												};
	private static final int[]		_treeSpawnY					=
																{
			148340,
			147900,
			148898,
			149343,
			149133,
			148101,
			147658,
			147659,
			149556,
			149554,
			-55119,
			-55117,
			-57244,
			-57261,
			-55826,
			-55829,
			-54095,
			-54070,
			25921,
			25568,
			25568,
			25929,
			27366,
			27364,
			29065,
			29065,
			27664,
			-55960,
			-55939,
			75627,
			75352,
			76111,
			76544,
			77400,
			76210,
			122533,
			122425,
			122806,
			142767,
			142710												};
	private static final int[]		_treeSpawnZ					=
																{
			-3405,
			-3405,
			-3405,
			-3405,
			-3402,
			-3402,
			-3469,
			-3469,
			-3469,
			-3469,
			-2734,
			-2734,
			-2781,
			-2781,
			-2781,
			-2781,
			-2735,
			-2735,
			-2013,
			-2013,
			-2013,
			-2013,
			-2205,
			-2205,
			-2269,
			-2269,
			-2204,
			-2781,
			-2781,
			-2726,
			-2712,
			-2715,
			-2719,
			-2697,
			-2730,
			-3117,
			-2989,
			-3117,
			-2706,
			-2706												};

	private static boolean			_christmasEvent				= false;
	private static boolean			_isSantaFree				= false;
	private static boolean			_isWaitingForPlayerSkill	= false;
	private static List<L2Npc>		_santaHelpers				= new ArrayList<L2Npc>();
	private static List<L2Npc>		_specialTrees				= new ArrayList<L2Npc>();
	private Map<String, Long>		_rewardedPlayers			= new FastMap<String, Long>();

	public SavingSanta(int questId, String name, String descr)
	{
		super(questId, name, descr);

		EventDroplist.getInstance().addGlobalDrop(EVENT_GLOBAL_DROP, EVENT_GLOBAL_DROP_COUNT, (Config.SAVING_SANTA_DROP_CHANCE * 10000), EVENT_DATES);
		Announcements.getInstance().addEventAnnouncement(EVENT_DATES, ON_ENTER_ANNOUNCE);

		addStartNpc(SANTA);
		addFirstTalkId(SANTA);
		addTalkId(SANTA);
		addFirstTalkId(THOMAS);
		addFirstTalkId(HOLIDAY_SANTA);
		addSkillSeeId(THOMAS);
		addSpellFinishedId(THOMAS);

		startQuestTimer("ChristmasCheck", 1800000, null, null);

		if (EVENT_DATES.isWithinRange(CURRENT_DATE))
		{
			_christmasEvent = true;
		}

		if (_christmasEvent)
		{
			_log.info("Christmas Event - ON");

			for (int i = 0; i < _treeSpawnX.length; i++)
			{
				addSpawn(13006, _treeSpawnX[i], _treeSpawnY[i], _treeSpawnZ[i], 0, false, 0);
			}

			for (int i = 0; i < _santaX.length; i++)
			{
				L2Npc mob = addSpawn(SANTA, _santaX[i], _santaY[i], _santaZ[i], 0, false, 0);
				_santaHelpers.add(mob);
			}
			if (CONFIG[0])
				startQuestTimer("SantaBlessings", 5000, null, null);
			if (CONFIG[1])
				startQuestTimer("ThomasQuest", 120000, null, null);
		}
		else
		{
			_log.info("Christmas Event - OFF");

			Calendar endWeek = Calendar.getInstance();
			endWeek.setTime(END_DATE);
			endWeek.add(Calendar.DATE, 7);

			if (END_DATE.before(CURRENT_DATE) && endWeek.getTime().after(CURRENT_DATE))
			{
				for (int i = 0; i < _santaX.length; i++)
				{
					addSpawn(SANTA, _santaX[i], _santaY[i], _santaZ[i], 0, false, 0);
				}
			}
		}
	}

	@Override
	public String onSkillSee(L2Npc npc, L2Player caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (_isWaitingForPlayerSkill && skill.getId() > 21013 && skill.getId() < 21017)
		{
			caster.broadcastPacket(new MagicSkillUse(caster, caster, 23019, skill.getId() - 21013, 3000, 1));
			SkillTable.getInstance().getInfo(23019, skill.getId() - 21013).getEffects(caster, caster);
		}
		return "";
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2Player player, L2Skill skill)
	{
		if (skill.getId() == 6100)
		{
			_isWaitingForPlayerSkill = false;
			for (L2Player pl : npc.getKnownList().getKnownPlayersInRadius(600))
			{
				if (pl.getFirstEffect(23019) == null)
					continue;
				int result = pl.getFirstEffect(23019).getSkill().getLevel() - skill.getLevel();
				if (result == 1 || result == -2)
				{
					int level = (pl.getFirstEffect(23022) != null ? (pl.getFirstEffect(23022).getSkill().getLevel() + 1) : 1);
					pl.broadcastPacket(new MagicSkillUse(pl, pl, 23022, level, 3000, 1));
					SkillTable.getInstance().getInfo(23022, level).getEffects(pl, pl);
					if (level == 3)
					{
						SkillTable.getInstance().getInfo(23018, 1).getEffects(pl, pl);
					}
					else if (level == 4)
					{
						Announcements.getInstance().announceToAll("Thomas D. Turkey has been disappeared.");
						Broadcast
								.toAllOnlinePlayers(SystemMessage.sendString("Message from Santa Claus: Many blessings to " + pl.getName() + ", who saved me"));
						startQuestTimer("SantaSpawn", 120000, null, null);
						npc.deleteMe();
						_isSantaFree = true;
						break;
					}
				}
				else if (result != 0)
				{
					pl.broadcastPacket(new MagicSkillUse(pl, pl, 23023, 1, 3000, 1));
					pl.stopSkillEffects(23022);
				}
			}
		}
		return "";
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2Player player)
	{
		String htmltext = "";

		if (npc == null)
		{
			if (event.equalsIgnoreCase("ChristmasCheck"))
			{
				startQuestTimer("ChristmasCheck", 1800000, null, null);
				boolean Event1 = false;

				if (EVENT_DATES.isWithinRange(CURRENT_DATE))
				{
					Event1 = true;
				}

				if (!_christmasEvent && Event1)
				{
					_christmasEvent = true;
					_log.info("Christmas Event - ON");
					if (CONFIG[0])
						startQuestTimer("SantaBlessings", 5000, null, null);
					if (CONFIG[1])
						startQuestTimer("ThomasQuest", 120000, null, null);

					for (int i = 0; i < _treeSpawnX.length; i++)
					{
						addSpawn(13006, _treeSpawnX[i], _treeSpawnY[i], _treeSpawnZ[i], 0, false, 0);
					}

					for (int i = 0; i < _santaX.length; i++)
					{
						L2Npc mob = addSpawn(SANTA, _santaX[i], _santaY[i], _santaZ[i], 0, false, 0);
						_santaHelpers.add(mob);
					}
				}
				else if (_christmasEvent && !Event1)
				{
					_christmasEvent = false;
					_log.info("Christmas Event - OFF");
					cancelQuestTimer("SantaBlessings", null, null);
					cancelQuestTimer("ThomasQuest", null, null);
					for (L2Npc santaHelper : _santaHelpers)
					{
						santaHelper.deleteMe();
					}
				}
			}
			else if (event.equalsIgnoreCase("ThomasQuest"))
			{
				startQuestTimer("ThomasQuest", 14400000, null, null);
				L2Npc thomas = addSpawn(THOMAS, THOMAS_LOC[0], THOMAS_LOC[1], THOMAS_LOC[2], THOMAS_LOC[3], false, 1800000);
				Announcements.getInstance().announceToAll("Thomas D. Turkey has been disappeared.");
				startQuestTimer("ThomasCast1", 15000, thomas, null);
				_isSantaFree = false;
			}
			else if (event.equalsIgnoreCase("SantaSpawn"))
			{
				if (_isSantaFree)
				{
					startQuestTimer("SantaSpawn", 120000, null, null);
					for (L2Player pl : L2World.getInstance().getAllPlayers())
					{
						if (pl.isOnline() > 0 && pl.getLevel() >= 20 && pl.isInCombat() && !pl.isInsideZone(L2Zone.FLAG_PEACE) && !pl.isFlyingMounted())
						{
							if (_rewardedPlayers.containsKey(pl.getAccountName()))
							{
								long elapsedTimeSinceLastRewarded = System.currentTimeMillis() - _rewardedPlayers.get(pl.getAccountName());
								if (elapsedTimeSinceLastRewarded < MIN_TIME_BETWEEN_2_REWARDS)
									continue;
							}
							else
							{
								String data = loadGlobalQuestVar(pl.getAccountName());
								if (!data.isEmpty() && (System.currentTimeMillis() - Long.parseLong(data)) < MIN_TIME_BETWEEN_2_REWARDS)
								{
									_rewardedPlayers.put(pl.getAccountName(), Long.parseLong(data));
									continue;
								}
							}
							int locx = (int) (pl.getX() + Math.pow(-1, Rnd.get(1, 2)) * 50);
							int locy = (int) (pl.getY() + Math.pow(-1, Rnd.get(1, 2)) * 50);
							int heading = Util.calculateHeadingFrom(locx, locy, pl.getX(), pl.getY());
							L2Npc santa = addSpawn(HOLIDAY_SANTA, locx, locy, pl.getZ(), heading, false, 30000);
							_rewardedPlayers.put(pl.getAccountName(), System.currentTimeMillis());
							saveGlobalQuestVar(pl.getAccountName(), String.valueOf(System.currentTimeMillis()));
							startQuestTimer("SantaRewarding0", 500, santa, pl);
						}
					}
				}
			}
		}
		else if (event.equalsIgnoreCase("ThomasCast1"))
		{
			if (!npc.isDecayed())
			{
				_isWaitingForPlayerSkill = true;
				startQuestTimer("ThomasCast2", 4000, npc, null);
				npc.doCast(SkillTable.getInstance().getInfo(6116, 1));
			}
			else
			{
				if (!_isSantaFree)
					Announcements.getInstance().announceToAll("Thomas D. Turkey has been disappeared.");
				_isWaitingForPlayerSkill = false;
			}
		}
		else if (event.equalsIgnoreCase("ThomasCast2"))
		{
			if (!npc.isDecayed())
			{
				startQuestTimer("ThomasCast1", 13000, npc, null);
				npc.doCast(SkillTable.getInstance().getInfo(6100, Rnd.get(1, 3)));
			}
			else
			{
				if (!_isSantaFree)
					Announcements.getInstance().announceToAll("Thomas D. Turkey has been disappeared.");
				_isWaitingForPlayerSkill = false;
			}
		}
		else if (event.equalsIgnoreCase("SantaRewarding0"))
		{
			startQuestTimer("SantaRewarding1", 9500, npc, player);
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
		}
		else if (event.equalsIgnoreCase("SantaRewarding1"))
		{
			startQuestTimer("SantaRewarding2", 5000, npc, player);
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
			player.sendMessage("Happy holidays! Thanks to the citizens of Aden for freeing me from the clutches of that miserable turkey.");
		}
		else if (event.equalsIgnoreCase("SantaRewarding2"))
		{
			startQuestTimer("SantaRewarding3", 5000, npc, player);
			player.sendMessage("I have a gift for " + player.getName() + ".");
		}
		else if (event.equalsIgnoreCase("SantaRewarding3"))
		{
			QuestState st = player.getQuestState(QN);
			if (st == null)
				st = newQuestState(player);
			st.giveItems(HOLIDAYSANTASREWARD, 1);
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 2));
			player.sendMessage("Take a look in your inventory. I hope you like your present.");
		}

		else if (event.equalsIgnoreCase("SantaBlessings"))
		{
			if (_christmasEvent)
			{
				startQuestTimer("SantaBlessings", 15000, null, null);
				for (L2Npc santaHelper : _santaHelpers)
				{
					Collection<L2Player> playerList = santaHelper.getKnownList().getKnownPlayers().values();
					for (L2Player playerx : playerList)
					{
						if (playerx.getClassId().isMage())
						{
							for (int buffId : SANTA_MAGE_BUFFS)
							{
								if (playerx.getFirstEffect(buffId) == null)
								{
									playerx.broadcastPacket(new MagicSkillUse(santaHelper, playerx, buffId, 1, 2000, 1));
									SkillTable.getInstance().getInfo(buffId, 1).getEffects(playerx, playerx);
								}
							}
						}
						else
						{
							for (int buffId : SANTA_FIGHTER_BUFFS)
							{
								if (playerx.getFirstEffect(buffId) == null)
								{
									playerx.broadcastPacket(new MagicSkillUse(santaHelper, playerx, buffId, 1, 2000, 1));
									SkillTable.getInstance().getInfo(buffId, 1).getEffects(playerx, playerx);
								}
							}
						}
					}
				}
			}
		}

		else if (player != null)
		{
			QuestState st = player.getQuestState(QN);
			if (st == null)
				st = newQuestState(player);
			if (event.equalsIgnoreCase("Tree"))
			{
				int itemsOk = 0;
				htmltext = "<html><title>L2DC Christmas Event</title><body><br><br><table width=260><tr><td></td><td width=40></td><td width=40></td></tr><tr><td><font color=LEVEL>Christmas Tree</font></td><td width=40><img src=\"Icon.etc_x_mas_tree_i00\" width=32 height=32></td><td width=40></td></tr></table><br><br><table width=260>";

				for (int i = 0; i < _requiredPieces.length; i++)
				{
					long pieceCount = st.getQuestItemsCount(_requiredPieces[i]);
					if (pieceCount >= _requiredQty[i])
					{
						itemsOk = itemsOk + 1;
						htmltext = htmltext + "<tr><td>" + ItemTable.getInstance().getTemplate(_requiredPieces[i]).getName() + "</td><td width=40>"
								+ pieceCount + "</td><td width=40><font color=0FF000>OK</font></td></tr>";
					}

					else
					{
						htmltext = htmltext + "<tr><td>" + ItemTable.getInstance().getTemplate(_requiredPieces[i]).getName() + "</td><td width=40>"
								+ pieceCount + "</td><td width=40><font color=8ae2ffb>NO</font></td></tr>";
					}
				}

				if (itemsOk == 4)
				{
					htmltext = htmltext + "<tr><td><br></td><td width=40></td><td width=40></td></tr></table><table width=260>";
					htmltext = htmltext
							+ "<tr><td><center><button value=\"Get the tree\" action=\"bypass -h QuestEvent SavingSanta buyTree\" width=110 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td></tr></table></body></html>";
				}

				else if (itemsOk < 4)
				{
					htmltext = htmltext + "</table><br><br>You do not have enough items.</center></body></html>";
				}

				return htmltext;
			}

			else if (event.equalsIgnoreCase("buyTree"))
			{
				st.sendPacket(SND_MIDDLE);

				for (int i = 0; i < _requiredPieces.length; i++)
				{
					if (st.getQuestItemsCount(_requiredPieces[i]) < _requiredQty[i])
						return "";
				}

				for (int i = 0; i < _requiredPieces.length; i++)
				{
					st.takeItems(_requiredPieces[i], _requiredQty[i]);
				}
				st.giveItems(5560, 1);
			}

			else if (event.equalsIgnoreCase("SpecialTree") && !CONFIG[1])
			{
				htmltext = "<html><title>L2DC Christmas Event</title><body><br><br><table width=260><tr><td></td><td width=40></td><td width=40></td></tr><tr><td><font color=LEVEL>Special Christmas Tree</font></td><td width=40><img src=\"Icon.etc_x_mas_tree_i00\" width=32 height=32></td><td width=40></td></tr></table><br><br><table width=260>";
				long pieceCount = st.getQuestItemsCount(5560);
				int itemsOk = 0;

				if (pieceCount >= 10)
				{
					itemsOk = 1;
					htmltext = htmltext + "<tr><td>Christmas Tree</td><td width=40>" + pieceCount + "</td><td width=40><font color=0FF000>OK</font></td></tr>";
				}

				else
				{
					htmltext = htmltext + "<tr><td>Christmas Tree</td><td width=40>" + pieceCount + "</td><td width=40><font color=8ae2ffb>NO</font></td></tr>";
				}

				if (itemsOk == 1)
				{
					htmltext = htmltext + "<tr><td><br></td><td width=40></td><td width=40></td></tr></table><table width=260>";
					htmltext = htmltext
							+ "<tr><td><center><button value=\"Get the tree\" action=\"bypass -h QuestEvent SavingSanta buySpecialTree\" width=110 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td></tr></table></body></html>";
				}

				else if (itemsOk == 0)
				{
					htmltext = htmltext + "</table><br><br>You do not have enough items.</center></body></html>";
				}

				return htmltext;
			}

			else if (event.equalsIgnoreCase("buySpecialTree") && !CONFIG[1])
			{
				st.sendPacket(SND_MIDDLE);
				if (st.getQuestItemsCount(5560) < 10)
					return "";
				st.takeItems(5560, 10);
				st.giveItems(5561, 1);
			}

			else if (event.equalsIgnoreCase("SantaHat"))
			{
				htmltext = "<html><title>L2DC Christmas Event</title><body><br><br><table width=260><tr><td></td><td width=40></td><td width=40></td></tr><tr><td><font color=LEVEL>Santa's Hat</font></td><td width=40><img src=\"Icon.Accessory_santas_cap_i00\" width=32 height=32></td><td width=40></td></tr></table><br><br><table width=260>";
				long pieceCount = st.getQuestItemsCount(5560);
				int itemsOk = 0;

				if (pieceCount >= 10)
				{
					itemsOk = 1;
					htmltext = htmltext + "<tr><td>Christmas Tree</td><td width=40>" + pieceCount + "</td><td width=40><font color=0FF000>OK</font></td></tr>";
				}

				else
				{
					htmltext = htmltext + "<tr><td>Christmas Tree</td><td width=40>" + pieceCount + "</td><td width=40><font color=8ae2ffb>NO</font></td></tr>";
				}

				if (itemsOk == 1)
				{
					htmltext = htmltext + "<tr><td><br></td><td width=40></td><td width=40></td></tr></table><table width=260>";
					htmltext = htmltext
							+ "<tr><td><center><button value=\"Get the hat\" action=\"bypass -h QuestEvent SavingSanta buyHat\" width=110 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td></tr></table></body></html>";
				}

				else if (itemsOk == 0)
				{
					htmltext = htmltext + "</table><br><br>You do not have enough items.</center></body></html>";
				}

				return htmltext;
			}

			else if (event.equalsIgnoreCase("buyHat"))
			{
				st.sendPacket(SND_MIDDLE);
				if (st.getQuestItemsCount(5560) < 10)
					return "";
				st.takeItems(5560, 10);
				st.giveItems(7836, 1);
			}
			else if (event.equalsIgnoreCase("SavingSantaHat") && CONFIG[1])
			{
				htmltext = "<html><title>L2DC Christmas Event</title><body><br><br><table width=260><tr><td></td><td width=40></td><td width=40></td></tr><tr><td><font color=LEVEL>Saving Santa's Hat</font></td><td width=40><img src=\"Icon.Accessory_santas_cap_i00\" width=32 height=32></td><td width=40></td></tr></table><br><br><table width=260>";
				long pieceCount = st.getQuestItemsCount(57);
				int itemsOk = 0;

				if (pieceCount >= 50000)
				{
					itemsOk = 1;
					htmltext = htmltext + "<tr><td>Adena</td><td width=40>" + pieceCount + "</td><td width=40><font color=0FF000>OK</font></td></tr>";
				}

				else
				{
					htmltext = htmltext + "<tr><td>Adena</td><td width=40>" + pieceCount + "</td><td width=40><font color=8ae2ffb>NO</font></td></tr>";
				}

				if (itemsOk == 1)
				{
					htmltext = htmltext + "<tr><td><br></td><td width=40></td><td width=40></td></tr></table><table width=260>";
					htmltext = htmltext
							+ "<tr><td><center><button value=\"Get the hat\" action=\"bypass -h QuestEvent SavingSanta buySavingHat\" width=110 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td></tr></table></body></html>";
				}

				else if (itemsOk == 0)
				{
					htmltext = htmltext + "</table><br><br>You do not have enough Adena.</center></body></html>";
				}

				return htmltext;
			}

			else if (event.equalsIgnoreCase("buySavingHat") && CONFIG[1])
			{
				st.sendPacket(SND_MIDDLE);
				if (st.getQuestItemsCount(57) < 50000)
					return "";
				st.takeAdena(50000);
				st.giveItems(20100, 1);
			}
			else if (event.equalsIgnoreCase("HolidayFestival") && CONFIG[1])
			{
				if (_isSantaFree)
				{
					npc.broadcastPacket(new MagicSkillUse(npc, player, HOLIDAYBUFFID, 1, 2000, 1));
					SkillTable.getInstance().getInfo(HOLIDAYBUFFID, 1).getEffects(player, player);
				}
				else
				{
					return "savingsanta-nobuff.htm";
				}
			}
			else if (event.equalsIgnoreCase("getWeapon") && CONFIG[1])
			{
				if (st.getQuestItemsCount(20107) == 0 && st.getQuestItemsCount(20108) == 0)
					return "savingsanta-noweapon.htm";
				return "savingsanta-weapon.htm";
			}
			else if (event.startsWith("weapon_") && CONFIG[1])
			{
				if (st.getQuestItemsCount(20108) != 0)
				{
					st.takeItems(20108, 1);
					st.giveItems(RANDOM_A_PLUS_10_WEAPON[Rnd.get(RANDOM_A_PLUS_10_WEAPON.length)], 1, 10);
					return "";
				}
				else if (st.getQuestItemsCount(20107) == 0 || player.getLevel() < 20)
					return "";
				int grade = player.getSkillLevel(239) - 1;
				if (grade < -1)
					return "";
				int itemId = Integer.parseInt(event.replace("weapon_", ""));
				if (itemId < 1 || itemId > 14)
					return "";
				else if (grade > 4)
					grade = 4;
				itemId += (20108 + grade * 14);
				st.takeItems(20107, 1);
				st.giveItems(itemId, 1, Rnd.get(4, 16));
			}
		}
		return "";
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2Player player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);
		if (npc.getNpcId() == THOMAS || npc.getNpcId() == HOLIDAY_SANTA)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return null;
		}

		if (npc.getNpcId() == SANTA)
		{
			if (CONFIG[1])
				htmltext = "savingsanta.htm";
			else
				htmltext = "santa.htm";
		}
		return htmltext;
	}

	@Override
	public boolean unload()
	{
		for (L2Npc eventnpc : _santaHelpers)
		{
			eventnpc.deleteMe();
		}
		for (L2Npc eventnpc : _specialTrees)
		{
			eventnpc.deleteMe();
		}
		return super.unload();
	}

	public static void main(String[] args)
	{
		if (Config.ALLOW_SAVING_SANTA)
		{
			new SavingSanta(-1, QN, "official_events");
			_log.info("Official Events: Saving Santa is loaded.");
		}
		else
			_log.info("Official Events: Saving Santa is disabled.");
	}
}
