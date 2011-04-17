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
package net.l2emuproject;

import gnu.trove.TIntArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.l2emuproject.config.L2Properties;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.util.L2FastSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This class contains global server configuration.<br>
 * It has static final fields initialized from configuration files.<br>
 * It's initialized at the very begin of startup, and later JIT will optimize
 * away debug/unused code.
 * 
 * @author mkizub
 */
public class Config extends L2Config
{
	static
	{
		System.setProperty("python.home", ".");

		// Administration
		registerConfig(new GMAccessConfig());
		registerConfig(new CommandPrivilegesConfig());
		registerConfig(new DeveloperConfig());

		// Network
		registerConfig(new NetworkConfig());
		registerConfig(new SecurityConfig());

		// Main
		registerConfig(new GSConfig());
		registerConfig(new AltConfig());
		registerConfig(new CraftingConfig());
		registerConfig(new BossConfig());
		registerConfig(new CastleConfig());
		registerConfig(new ClanHallConfig());
		registerConfig(new ClansConfig());
		registerConfig(new CommunityConfig());
		registerConfig(new CustomConfig());
		registerConfig(new DBBackupConfig());
		registerConfig(new DateTimeConfig());
		registerConfig(new EnchantConfig());
		registerConfig(new EquipmentConfig());
		registerConfig(new FameConfig());
		registerConfig(new FantasyIslandConfig());
		registerConfig(new FourSepulchersConfig());
		registerConfig(new DimensionRiftConfig());
		registerConfig(new GeoConfig());
		registerConfig(new IDConfig());
		registerConfig(new OptionsConfig());
		registerConfig(new DropsConfig());
		registerConfig(new GridConfig());
		registerConfig(new AuctionConfig());
		registerConfig(new PermissionsConfig());
		registerConfig(new OtherConfig());
		registerConfig(new PetitionsConfig());
		registerConfig(new PrivateStoresConfig());
		registerConfig(new InventoryConfig());
		registerConfig(new WarehouseConfig());
		registerConfig(new PartyConfig());
		registerConfig(new RespawnsConfig());
		registerConfig(new RegenerationConfig());
		registerConfig(new PvPConfig());
		registerConfig(new RatesConfig());
		registerConfig(new SkillsConfig());
		registerConfig(new VitalityConfig());
		registerConfig(new ScriptsConfig());

		// Main/Events
		registerConfig(new FortSiegeConfig());
		registerConfig(new LotteryConfig());
		registerConfig(new ManorConfig());
		registerConfig(new OlympiadConfig());
		registerConfig(new SevenSignsConfig());
		registerConfig(new SiegeConfig());
		registerConfig(new TerritoryWarConfig());

		// Chat
		registerConfig(new ChatFilterConfig());
		registerConfig(new ChatFilter());
		registerConfig(new ChatConfig());

		// Mods
		registerConfig(new BankingConfig());
		registerConfig(new ChampionsConfig());
		registerConfig(new ClassBalanceConfig());
		registerConfig(new ClassMasterConfig());
		registerConfig(new CraftManagerConfig());
		registerConfig(new FunEnginesConfig());
		registerConfig(new OfflineTradeConfig());
		registerConfig(new WeddingConfig());
		registerConfig(new ProtectorConfig());
		registerConfig(new LevelChangerConfig());
		registerConfig(new AnnouncerConfig());
		registerConfig(new BufferConfig());

		// Versionning
		registerConfig(new DataPackBuildConfig());
	}

	public static void load() throws Exception
	{
		Util.printSection("Configuration");
		loadHexId();

		L2Config.loadConfigs();
		registerConfig(new AllConfig());
	}

	private static final class AllConfig extends ConfigLoader
	{
		@Override
		protected String getName()
		{
			return "all";
		}

		private boolean	_reloading	= false;

		@Override
		protected void load() throws Exception
		{
			if (_reloading)
				return;

			_reloading = true;
			try
			{
				Config.load();
			}
			finally
			{
				_reloading = false;
			}
		}
	}

	// *******************************************************************************************
	public static final String	DATAPACK_BUILD_FILE	= "./config/versionning/datapack-infos.properties";
	// *******************************************************************************************
	public static String		DATAPACK_VERSION;
	public static String		DATAPACK_REVISION;
	public static String		DATAPACK_BUILD_DATE;

	// *******************************************************************************************
	private static final class DataPackBuildConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "versionning/datapack-infos";
		}

		@Override
		protected void loadImpl(L2Properties datapackVersionSettings)
		{
			DATAPACK_VERSION = datapackVersionSettings.getProperty("BuildVersion", "unknown");
			DATAPACK_REVISION = datapackVersionSettings.getProperty("BuildRevision", "unknown");
			DATAPACK_BUILD_DATE = datapackVersionSettings.getProperty("BuildDate", "unknown");
		}

	}

	// *******************************************************************************************
	public static final String	NETWORK_FILE	= "./config/network/network.properties";
	// *******************************************************************************************
	public static int			GAME_SERVER_LOGIN_PORT;
	public static String		GAME_SERVER_LOGIN_HOST;
	public static String		INTERNAL_HOSTNAME;
	public static String		INTERNAL_NETWORKS;
	public static String		EXTERNAL_HOSTNAME;
	public static String		OPTIONAL_NETWORKS;
	public static int			PORT_GAME;													// Game Server ports
	public static String		GAMESERVER_HOSTNAME;										// Hostname of the Game Server
	public static String		DATABASE_DRIVER;											// Driver to access to database
	public static String		DATABASE_URL;												// Path to access to database
	public static String		DATABASE_LOGIN;											// Database login
	public static String		DATABASE_PASSWORD;											// Database password
	public static int			DATABASE_MAX_CONNECTIONS;									// Maximum number of connections to the database
	public static boolean		CONNECTION_FILTERING;

	public static boolean		IS_TELNET_ENABLED;											// Is telnet enabled ?
	public static boolean		ALT_TELNET;												// Use alternative telnet ?
	public static boolean		ALT_TELNET_GM_ANNOUNCER_NAME;								// Show GM's name behind his announcement ? (only works if AltTelnet = true)

	// *******************************************************************************************
	private static final class NetworkConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "network/network";
		}

		@Override
		protected void loadImpl(L2Properties networkSettings)
		{
			GAME_SERVER_LOGIN_HOST = networkSettings.getProperty("LoginHost", "127.0.0.1");
			GAME_SERVER_LOGIN_PORT = Integer.parseInt(networkSettings.getProperty("LoginPort", "9013"));
			PORT_GAME = Integer.parseInt(networkSettings.getProperty("GameserverPort", "7777"));

			GAMESERVER_HOSTNAME = networkSettings.getProperty("GameserverHostname");

			INTERNAL_HOSTNAME = networkSettings.getProperty("InternalHostname", "127.0.0.1");
			INTERNAL_NETWORKS = networkSettings.getProperty("InternalNetworks", "");
			EXTERNAL_HOSTNAME = networkSettings.getProperty("ExternalHostname", "127.0.0.1");
			OPTIONAL_NETWORKS = networkSettings.getProperty("OptionalNetworks", "");

			DATABASE_DRIVER = networkSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = networkSettings.getProperty("URL", "jdbc:mysql://localhost/L2Emu_DB");
			DATABASE_LOGIN = networkSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = networkSettings.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS = Integer.parseInt(networkSettings.getProperty("MaximumDbConnections", "10"));

			CONNECTION_FILTERING = Boolean.parseBoolean(networkSettings.getProperty("ConnectionFiltering", "True"));

			IS_TELNET_ENABLED = Boolean.parseBoolean(networkSettings.getProperty("EnableTelnet", "false"));
			ALT_TELNET = Boolean.parseBoolean(networkSettings.getProperty("AltTelnet", "false"));
			ALT_TELNET_GM_ANNOUNCER_NAME = Boolean.parseBoolean(networkSettings.getProperty("AltTelnetGmAnnouncerName", "false"));
		}
	}

	// *******************************************************************************************
	public static final String	SECURITY_FILE	= "./config/network/security.properties";
	// *******************************************************************************************
	public static boolean		GAMEGUARD_ENFORCE;
	public static boolean		GAMEGUARD_PROHIBITACTION;

	public static int			DEFAULT_PUNISH;											// Default punishment for illegal actions
	public static int			DEFAULT_PUNISH_PARAM;										// Parameter for default punishment

	public static boolean		BAN_CLIENT_EMULATORS;										// simultaneously on server
	public static boolean		SAFE_REBOOT;												// Safe mode will disable some feature during restart/shutdown to prevent exploit
	public static int			SAFE_REBOOT_TIME;
	public static boolean		SAFE_REBOOT_DISABLE_ENCHANT;
	public static boolean		SAFE_REBOOT_DISABLE_TELEPORT;
	public static boolean		SAFE_REBOOT_DISABLE_CREATEITEM;
	public static boolean		SAFE_REBOOT_DISABLE_TRANSACTION;
	public static boolean		SAFE_REBOOT_DISABLE_PC_ITERACTION;
	public static boolean		SAFE_REBOOT_DISABLE_NPC_ITERACTION;

	// *******************************************************************************************
	private static final class SecurityConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "network/security";
		}

		@Override
		protected void loadImpl(L2Properties securitySettings)
		{
			GAMEGUARD_ENFORCE = Boolean.parseBoolean(securitySettings.getProperty("GameGuardEnforce", "False"));
			GAMEGUARD_PROHIBITACTION = Boolean.parseBoolean(securitySettings.getProperty("GameGuardProhibitAction", "False"));

			DEFAULT_PUNISH = Integer.parseInt(securitySettings.getProperty("DefaultPunish", "2"));
			DEFAULT_PUNISH_PARAM = Integer.parseInt(securitySettings.getProperty("DefaultPunishParam", "0"));

			BAN_CLIENT_EMULATORS = Boolean.parseBoolean(securitySettings.getProperty("AutoBanClientEmulators", "True"));

			SAFE_REBOOT = Boolean.parseBoolean(securitySettings.getProperty("SafeReboot", "False"));
			SAFE_REBOOT_TIME = Integer.parseInt(securitySettings.getProperty("SafeRebootTime", "10"));
			SAFE_REBOOT_DISABLE_ENCHANT = Boolean.parseBoolean(securitySettings.getProperty("SafeRebootDisableEnchant", "False"));
			SAFE_REBOOT_DISABLE_TELEPORT = Boolean.parseBoolean(securitySettings.getProperty("SafeRebootDisableTeleport", "False"));
			SAFE_REBOOT_DISABLE_CREATEITEM = Boolean.parseBoolean(securitySettings.getProperty("SafeRebootDisableCreateItem", "False"));
			SAFE_REBOOT_DISABLE_TRANSACTION = Boolean.parseBoolean(securitySettings.getProperty("SafeRebootDisableTransaction", "False"));
			SAFE_REBOOT_DISABLE_PC_ITERACTION = Boolean.parseBoolean(securitySettings.getProperty("SafeRebootDisablePcIteraction", "False"));
			SAFE_REBOOT_DISABLE_NPC_ITERACTION = Boolean.parseBoolean(securitySettings.getProperty("SafeRebootDisableNpcIteraction", "False"));
		}
	}

	// *******************************************************************************************
	public static final String	CONFIGURATION_FILE		= "./config/main/gameserver.properties";
	// *******************************************************************************************	
	public static int			MAXIMUM_ONLINE_USERS;												// Maximum number of players allowed to play

	public static Pattern		CNAME_PATTERN;														// Character name template
	public static Pattern		PET_NAME_PATTERN;													// Pet name template
	public static Pattern		CLAN_ALLY_NAME_PATTERN;											// Clan and ally name template
	public static Pattern		TITLE_PATTERN;														// Clan title template
	public static int			MAX_CHARACTERS_NUMBER_PER_ACCOUNT;									// Maximum number of characters per account
	public static boolean		PACKET_HANDLER_DEBUG;

	public static TIntArrayList	PROTOCOL_LIST;

	public static File			DATAPACK_ROOT;														// Datapack root directory

	public static int			REQUEST_ID;														// ID for request to the server
	public static boolean		RESERVE_HOST_ON_LOGIN	= false;
	public static boolean		ACCEPT_ALTERNATE_ID;												// Accept alternate ID for server ?

	// not to be loaded from file
	public static boolean		DISABLE_ALL_CHAT		= false;

	// *******************************************************************************************
	private static final class GSConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/gameserver";
		}

		@Override
		protected void loadImpl(L2Properties gsSettings)
		{
			REQUEST_ID = Integer.parseInt(gsSettings.getProperty("RequestServerID", "0"));
			ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(gsSettings.getProperty("AcceptAlternateID", "True"));

			try
			{
				CNAME_PATTERN = Pattern.compile(gsSettings.getProperty("CnameTemplate", "[A-Za-z0-9\\-]{2,16}"));
			}
			catch (PatternSyntaxException e)
			{
				_log.warn("Character name pattern is wrong!", e);
				CNAME_PATTERN = Pattern.compile("[A-Za-z0-9\\-]{2,16}");
			}

			try
			{
				PET_NAME_PATTERN = Pattern.compile(gsSettings.getProperty("PetNameTemplate", "[A-Za-z0-9\\-]{2,16}"));
			}
			catch (PatternSyntaxException e)
			{
				_log.warn("Pet name pattern is wrong!", e);
				PET_NAME_PATTERN = Pattern.compile("[A-Za-z0-9\\-]{2,16}");
			}

			try
			{
				CLAN_ALLY_NAME_PATTERN = Pattern.compile(gsSettings.getProperty("ClanAllyNameTemplate", "[A-Za-z0-9 \\-]{3,16}"));
			}
			catch (PatternSyntaxException e)
			{
				_log.warn("Clan and ally name pattern is wrong!", e);
				CLAN_ALLY_NAME_PATTERN = Pattern.compile("[A-Za-z0-9 \\-]{3,16}");
			}

			try
			{
				TITLE_PATTERN = Pattern.compile(gsSettings.getProperty("TitleTemplate", "[A-Za-z0-9 \\\\[\\\\]\\(\\)\\<\\>\\|\\!]{3,16}"));
			}
			catch (PatternSyntaxException e)
			{
				_log.warn("Character title pattern is wrong!", e);
				TITLE_PATTERN = Pattern.compile("[A-Za-z0-9 \\\\[\\\\]\\(\\)\\<\\>\\|\\!]{3,16}");
			}
			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(gsSettings.getProperty("CharMaxNumber", "0"));

			try
			{
				DATAPACK_ROOT = new File(gsSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
			}
			catch (Exception e)
			{
				_log.error(e);
			}

			PACKET_HANDLER_DEBUG = Boolean.parseBoolean(gsSettings.getProperty("PacketHandlerDebug", "false"));

			MAXIMUM_ONLINE_USERS = Integer.parseInt(gsSettings.getProperty("MaximumOnlineUsers", "100"));

			EXTENDED_LOG_LEVEL = Level.parse(gsSettings.getProperty("ExtendedLogLevel", "OFF"));

			final String[] protocols = gsSettings.getProperty("AllowedProtocolRevisions", "267;268").split(";");
			PROTOCOL_LIST = new TIntArrayList(protocols.length);
			for (String protocol : protocols)
			{
				try
				{
					PROTOCOL_LIST.add(Integer.parseInt(protocol.trim()));
				}
				catch (NumberFormatException e)
				{
					_log.info("Wrong config protocol version: " + protocol + ". Skipped.");
				}
			}
		}
	}

	// *******************************************************************************************
	public static final String	GEO_FILE	= "./config/main/geodata.properties";
	// *******************************************************************************************
	public static int			GEODATA;
	public static boolean		GEODATA_CELLFINDING;
	public static int			GEODATA_MAX_PATHFINDING_LENGTH;
	public static boolean		FORCE_GEODATA;
	public static File			GEODATA_ROOT;

	public static enum CorrectSpawnsZ
	{
		TOWN, MONSTER, ALL, NONE
	}

	public static CorrectSpawnsZ	GEO_CORRECT_Z;			// Enable spawns' z-correction
	public static boolean			ACCEPT_GEOEDITOR_CONN;	// Accept connection from geodata editor
	public static boolean			DEGUG_DOOR_GEODATA;

	// *******************************************************************************************
	private static final class GeoConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/geodata";
		}

		@Override
		protected void loadImpl(L2Properties geoSettings)
		{
			GEODATA = Integer.parseInt(geoSettings.getProperty("GeoData", "0"));
			GEODATA_CELLFINDING = Boolean.parseBoolean(geoSettings.getProperty("CellPathFinding", "False"));
			GEODATA_MAX_PATHFINDING_LENGTH = Integer.parseInt(geoSettings.getProperty("MaxPathFindingLength", "3500"));
			FORCE_GEODATA = Boolean.parseBoolean(geoSettings.getProperty("ForceGeoData", "True"));
			String correctZ = geoSettings.getProperty("GeoCorrectZ", "ALL");
			GEO_CORRECT_Z = CorrectSpawnsZ.valueOf(correctZ.toUpperCase());
			ACCEPT_GEOEDITOR_CONN = Boolean.parseBoolean(geoSettings.getProperty("AcceptGeoeditorConn", "False"));
			DEGUG_DOOR_GEODATA = Boolean.parseBoolean(geoSettings.getProperty("DebugDoorGeodata", "False"));

			try
			{
				GEODATA_ROOT = new File(geoSettings.getProperty("GeoDataRoot", ".")).getCanonicalFile();
			}
			catch (Exception e)
			{
				_log.error(e);
			}
		}
	}

	// *******************************************************************************************
	public static final String	CLANS_FILE	= "./config/main/clans.properties";
	// *******************************************************************************************
	public static int			ALT_CLAN_MEMBERS_FOR_WAR;						// Number of members needed to request a clan war
	public static int			ALT_CLAN_JOIN_DAYS;							// Number of days before joining a new clan
	public static int			ALT_CLAN_CREATE_DAYS;							// Number of days before creating a new clan
	public static int			ALT_CLAN_DISSOLVE_DAYS;						// Number of days it takes to dissolve a clan
	public static int			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;				// Number of days before joining a new alliance when clan voluntarily leave an alliance
	public static int			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;				// Number of days before joining a new alliance when clan was dismissed from an
	// alliance
	public static int			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;			// Number of days before accepting a new clan for alliance when clan was dismissed
	// from an alliance
	public static int			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;			// Number of days before creating a new alliance when dissolved an alliance
	public static int			ALT_MAX_NUM_OF_CLANS_IN_ALLY;					// Maximum number of clans in ally

	public static enum ClanLeaderColored
	{
		name, title
	} // Clan leader name color

	public static boolean			CLAN_LEADER_COLOR_ENABLED;
	public static ClanLeaderColored	CLAN_LEADER_COLORED;
	public static int				CLAN_LEADER_COLOR;
	public static int				CLAN_LEADER_COLOR_CLAN_LEVEL;
	public static int				MEMBER_FOR_LEVEL_SIX;			// Number of members to level up a clan to lvl 6
	public static int				MEMBER_FOR_LEVEL_SEVEN;		// Number of members to level up a clan to lvl 7
	public static int				MEMBER_FOR_LEVEL_EIGHT;		// Number of members to level up a clan to lvl 8
	public static int				MEMBER_FOR_LEVEL_NINE;			// Number of members to level up a clan to lvl 9
	public static int				MEMBER_FOR_LEVEL_TEN;			// Number of members to level up a clan to lvl 10
	public static int				MEMBER_FOR_LEVEL_ELEVEN;		// Number of members to level up a clan to lvl 11

	// Clan Fame
	public static int				TAKE_FORT_POINTS;
	public static int				LOOSE_FORT_POINTS;
	public static int				TAKE_CASTLE_POINTS;
	public static int				LOOSE_CASTLE_POINTS;
	public static int				CASTLE_DEFENDED_POINTS;
	public static int				FESTIVAL_WIN_POINTS;
	public static int				HERO_POINTS;
	public static int				ROYAL_GUARD_COST;
	public static int				KNIGHT_UNIT_COST;
	public static int				KNIGHT_REINFORCE_COST;
	public static int				BALLISTA_POINTS;
	public static int				BLOODALLIANCE_POINTS;
	public static int				BLOODOATH_POINTS;
	public static int				KNIGHTSEPAULETTE_POINTS;
	public static int				REPUTATION_SCORE_PER_KILL;
	public static int				JOIN_ACADEMY_MIN_REP_SCORE;
	public static int				JOIN_ACADEMY_MAX_REP_SCORE;
	public static int				RAID_RANKING_1ST;
	public static int				RAID_RANKING_2ND;
	public static int				RAID_RANKING_3RD;
	public static int				RAID_RANKING_4TH;
	public static int				RAID_RANKING_5TH;
	public static int				RAID_RANKING_6TH;
	public static int				RAID_RANKING_7TH;
	public static int				RAID_RANKING_8TH;
	public static int				RAID_RANKING_9TH;
	public static int				RAID_RANKING_10TH;
	public static int				RAID_RANKING_UP_TO_50TH;
	public static int				RAID_RANKING_UP_TO_100TH;
	public static int				CLAN_LEVEL_6_COST;
	public static int				CLAN_LEVEL_7_COST;
	public static int				CLAN_LEVEL_8_COST;
	public static int				CLAN_LEVEL_9_COST;
	public static int				CLAN_LEVEL_10_COST;
	public static int				CLAN_LEVEL_11_COST;

	// *******************************************************************************************
	private static final class ClansConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/clans";
		}

		@Override
		protected void loadImpl(L2Properties clansSettings)
		{
			ALT_CLAN_MEMBERS_FOR_WAR = Integer.parseInt(clansSettings.getProperty("AltClanMembersForWar", "15"));
			ALT_CLAN_JOIN_DAYS = Integer.parseInt(clansSettings.getProperty("DaysBeforeJoinAClan", "5"));
			ALT_CLAN_CREATE_DAYS = Integer.parseInt(clansSettings.getProperty("DaysBeforeCreateAClan", "10"));
			ALT_CLAN_DISSOLVE_DAYS = Integer.parseInt(clansSettings.getProperty("DaysToPassToDissolveAClan", "7"));
			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Integer.parseInt(clansSettings.getProperty("DaysBeforeJoinAllyWhenLeaved", "1"));
			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Integer.parseInt(clansSettings.getProperty("DaysBeforeJoinAllyWhenDismissed", "1"));
			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Integer.parseInt(clansSettings.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "1"));
			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Integer.parseInt(clansSettings.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "1"));
			ALT_MAX_NUM_OF_CLANS_IN_ALLY = Integer.parseInt(clansSettings.getProperty("AltMaxNumOfClansInAlly", "3"));
			CLAN_LEADER_COLOR_ENABLED = Boolean.parseBoolean(clansSettings.getProperty("ClanLeaderNameColorEnabled", "True"));
			CLAN_LEADER_COLORED = ClanLeaderColored.valueOf(clansSettings.getProperty("ClanLeaderColored", "name"));
			CLAN_LEADER_COLOR = Integer.decode("0x" + clansSettings.getProperty("ClanLeaderColor", "00FFFF"));
			CLAN_LEADER_COLOR_CLAN_LEVEL = Integer.parseInt(clansSettings.getProperty("ClanLeaderColorAtClanLevel", "1"));
			MEMBER_FOR_LEVEL_SIX = Integer.parseInt(clansSettings.getProperty("MemberForLevel6", "30"));
			MEMBER_FOR_LEVEL_SEVEN = Integer.parseInt(clansSettings.getProperty("MemberForLevel7", "80"));
			MEMBER_FOR_LEVEL_EIGHT = Integer.parseInt(clansSettings.getProperty("MemberForLevel8", "120"));
			MEMBER_FOR_LEVEL_NINE = Integer.parseInt(clansSettings.getProperty("MemberForLevel9", "120"));
			MEMBER_FOR_LEVEL_TEN = Integer.parseInt(clansSettings.getProperty("MemberForLevel10", "140"));
			MEMBER_FOR_LEVEL_ELEVEN = Integer.parseInt(clansSettings.getProperty("MemberForLevel11", "170"));

			// Clan Fame
			TAKE_FORT_POINTS = Integer.parseInt(clansSettings.getProperty("TakeFortPoints", "200"));
			LOOSE_FORT_POINTS = Integer.parseInt(clansSettings.getProperty("LooseFortPoints", "400"));
			TAKE_CASTLE_POINTS = Integer.parseInt(clansSettings.getProperty("TakeCastlePoints", "1500"));
			LOOSE_CASTLE_POINTS = Integer.parseInt(clansSettings.getProperty("LooseCastlePoints", "3000"));
			CASTLE_DEFENDED_POINTS = Integer.parseInt(clansSettings.getProperty("CastleDefendedPoints", "750"));
			FESTIVAL_WIN_POINTS = Integer.parseInt(clansSettings.getProperty("FestivalOfDarknessWin", "200"));
			HERO_POINTS = Integer.parseInt(clansSettings.getProperty("HeroPoints", "1000"));
			ROYAL_GUARD_COST = Integer.parseInt(clansSettings.getProperty("CreateRoyalGuardCost", "5000"));
			KNIGHT_UNIT_COST = Integer.parseInt(clansSettings.getProperty("CreateKnightUnitCost", "10000"));
			KNIGHT_REINFORCE_COST = Integer.parseInt(clansSettings.getProperty("ReinforceKnightUnitCost", "5000"));
			BALLISTA_POINTS = Integer.parseInt(clansSettings.getProperty("KillBallistaPoints", "30"));
			BLOODALLIANCE_POINTS = Integer.parseInt(clansSettings.getProperty("BloodAlliancePoints", "500"));
			BLOODOATH_POINTS = Integer.parseInt(clansSettings.getProperty("BloodOathPoints", "200"));
			KNIGHTSEPAULETTE_POINTS = Integer.parseInt(clansSettings.getProperty("KnightsEpaulettePoints", "20"));
			REPUTATION_SCORE_PER_KILL = Integer.parseInt(clansSettings.getProperty("ReputationScorePerKill", "1"));
			JOIN_ACADEMY_MIN_REP_SCORE = Integer.parseInt(clansSettings.getProperty("CompleteAcademyMinPoints", "190"));
			JOIN_ACADEMY_MAX_REP_SCORE = Integer.parseInt(clansSettings.getProperty("CompleteAcademyMaxPoints", "650"));
			RAID_RANKING_1ST = Integer.parseInt(clansSettings.getProperty("1stRaidRankingPoints", "1250"));
			RAID_RANKING_2ND = Integer.parseInt(clansSettings.getProperty("2ndRaidRankingPoints", "900"));
			RAID_RANKING_3RD = Integer.parseInt(clansSettings.getProperty("3rdRaidRankingPoints", "700"));
			RAID_RANKING_4TH = Integer.parseInt(clansSettings.getProperty("4thRaidRankingPoints", "600"));
			RAID_RANKING_5TH = Integer.parseInt(clansSettings.getProperty("5thRaidRankingPoints", "450"));
			RAID_RANKING_6TH = Integer.parseInt(clansSettings.getProperty("6thRaidRankingPoints", "350"));
			RAID_RANKING_7TH = Integer.parseInt(clansSettings.getProperty("7thRaidRankingPoints", "300"));
			RAID_RANKING_8TH = Integer.parseInt(clansSettings.getProperty("8thRaidRankingPoints", "200"));
			RAID_RANKING_9TH = Integer.parseInt(clansSettings.getProperty("9thRaidRankingPoints", "150"));
			RAID_RANKING_10TH = Integer.parseInt(clansSettings.getProperty("10thRaidRankingPoints", "100"));
			RAID_RANKING_UP_TO_50TH = Integer.parseInt(clansSettings.getProperty("UpTo50thRaidRankingPoints", "25"));
			RAID_RANKING_UP_TO_100TH = Integer.parseInt(clansSettings.getProperty("UpTo100thRaidRankingPoints", "12"));
			CLAN_LEVEL_6_COST = Integer.parseInt(clansSettings.getProperty("ClanLevel6Cost", "10000"));
			CLAN_LEVEL_7_COST = Integer.parseInt(clansSettings.getProperty("ClanLevel7Cost", "20000"));
			CLAN_LEVEL_8_COST = Integer.parseInt(clansSettings.getProperty("ClanLevel8Cost", "40000"));
			CLAN_LEVEL_9_COST = Integer.parseInt(clansSettings.getProperty("ClanLevel9Cost", "40000"));
			CLAN_LEVEL_10_COST = Integer.parseInt(clansSettings.getProperty("ClanLevel10Cost", "40000"));
			CLAN_LEVEL_11_COST = Integer.parseInt(clansSettings.getProperty("ClanLevel11Cost", "75000"));
		}
	}

	// *******************************************************************************************
	public static final String	CHAMPIONS_FILE	= "./config/mods/champions.properties";
	// *******************************************************************************************
	public static int			CHAMPION_FREQUENCY;									// Frequency of spawn
	public static boolean		CHAMPION_PASSIVE;
	public static String		CHAMPION_TITLE;
	public static int			CHAMPION_HP;											// Hp multiplier
	public static float			CHAMPION_HP_REGEN;										// Hp.reg multiplier
	public static float			CHAMPION_ATK;											// P.Atk & M.Atk multiplier
	public static float			CHAMPION_SPD_ATK;										// Attack speed multiplier
	public static int			CHAMPION_SEALSTONE;
	public static int			CHAMPION_ADENA;										// Adena/Sealstone reward multiplier
	public static int			CHAMPION_REWARDS;										// Drop/Spoil reward multiplier
	public static int			CHAMPION_EXP_SP;										// Exp/Sp reward multiplier
	public static boolean		CHAMPION_BOSS;											// Bosses can be champions
	public static int			CHAMPION_MIN_LEVEL;									// Champion Minimum Level
	public static int			CHAMPION_MAX_LEVEL;									// Champion Maximum Level
	public static boolean		CHAMPION_MINIONS;										// set Minions to champions when leader champion
	public static int			CHAMPION_SPCL_LVL_DIFF;								// level diff with mob level is more this value - don't drop an special reward
	// item.
	public static int			CHAMPION_SPCL_CHANCE;									// Chance in % to drop an special reward item.
	public static int			CHAMPION_SPCL_ITEM;									// Item ID that drops from Champs.
	public static int			CHAMPION_SPCL_QTY;										// Amount of special champ drop items.

	// *******************************************************************************************
	private static final class ChampionsConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/champions";
		}

		@Override
		protected void loadImpl(L2Properties championsSettings)
		{
			CHAMPION_FREQUENCY = Integer.parseInt(championsSettings.getProperty("ChampionFrequency", "0"));
			CHAMPION_PASSIVE = Boolean.parseBoolean(championsSettings.getProperty("ChampionPassive", "false"));
			CHAMPION_TITLE = championsSettings.getProperty("ChampionTitle", "Champion").trim();
			CHAMPION_HP = Integer.parseInt(championsSettings.getProperty("ChampionHp", "7"));
			CHAMPION_HP_REGEN = Float.parseFloat(championsSettings.getProperty("ChampionHpRegen", "1."));
			CHAMPION_REWARDS = Integer.parseInt(championsSettings.getProperty("ChampionRewards", "8"));
			CHAMPION_SEALSTONE = Integer.parseInt(championsSettings.getProperty("ChampionSealStoneRewards", "1"));
			CHAMPION_ADENA = Integer.parseInt(championsSettings.getProperty("ChampionAdenasRewards", "1"));
			CHAMPION_ATK = Float.parseFloat(championsSettings.getProperty("ChampionAtk", "1."));
			CHAMPION_SPD_ATK = Float.parseFloat(championsSettings.getProperty("ChampionSpdAtk", "1."));
			CHAMPION_EXP_SP = Integer.parseInt(championsSettings.getProperty("ChampionExpSp", "8"));
			CHAMPION_BOSS = Boolean.parseBoolean(championsSettings.getProperty("ChampionBoss", "false"));
			CHAMPION_MIN_LEVEL = Integer.parseInt(championsSettings.getProperty("ChampionMinLevel", "20"));
			CHAMPION_MAX_LEVEL = Integer.parseInt(championsSettings.getProperty("ChampionMaxLevel", "60"));
			CHAMPION_MINIONS = Boolean.parseBoolean(championsSettings.getProperty("ChampionMinions", "false"));
			CHAMPION_SPCL_LVL_DIFF = Integer.parseInt(championsSettings.getProperty("ChampionSpecialItemLevelDiff", "0"));
			CHAMPION_SPCL_CHANCE = Integer.parseInt(championsSettings.getProperty("ChampionSpecialItemChance", "0"));
			CHAMPION_SPCL_ITEM = Integer.parseInt(championsSettings.getProperty("ChampionSpecialItemID", "6393"));
			CHAMPION_SPCL_QTY = Integer.parseInt(championsSettings.getProperty("ChampionSpecialItemAmount", "1"));
		}
	}

	// *******************************************************************************************
	public static final String	CLASS_BALANCE_FILE	= "./config/mods/class_balance.properties";
	// *******************************************************************************************
	public static boolean		ENABLE_CLASS_BALANCE_SYSTEM;

	// *******************************************************************************************
	private static final class ClassBalanceConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/class_balance";
		}

		@Override
		protected void loadImpl(L2Properties balanceSettings)
		{
			ENABLE_CLASS_BALANCE_SYSTEM = Boolean.parseBoolean(balanceSettings.getProperty("EnableClassBalanceSystem", "False"));
		}
	}

	// *******************************************************************************************
	public static final String	LOTTERY_FILE	= "./config/main/events/lottery.properties";
	// *******************************************************************************************
	public static long			ALT_LOTTERY_PRIZE;												// Initial Lottery prize
	public static long			ALT_LOTTERY_TICKET_PRICE;										// Lottery Ticket Price
	public static float			ALT_LOTTERY_5_NUMBER_RATE;										// What part of jackpot amount should receive characters who pick 5 wining
	// numbers
	public static float			ALT_LOTTERY_4_NUMBER_RATE;										// What part of jackpot amount should receive characters who pick 4 wining
	// numbers
	public static float			ALT_LOTTERY_3_NUMBER_RATE;										// What part of jackpot amount should receive characters who pick 3 wining
	// numbers
	public static long			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;								// How much adena receive characters who pick two or less of the winning

	// number

	// *******************************************************************************************
	private static final class LotteryConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/events/lottery";
		}

		@Override
		protected void loadImpl(L2Properties lotterySettings)
		{
			ALT_LOTTERY_PRIZE = Long.parseLong(lotterySettings.getProperty("AltLotteryPrize", "50000"));
			ALT_LOTTERY_TICKET_PRICE = Long.parseLong(lotterySettings.getProperty("AltLotteryTicketPrice", "2000"));
			ALT_LOTTERY_5_NUMBER_RATE = Float.parseFloat(lotterySettings.getProperty("AltLottery5NumberRate", "0.6"));
			ALT_LOTTERY_4_NUMBER_RATE = Float.parseFloat(lotterySettings.getProperty("AltLottery4NumberRate", "0.2"));
			ALT_LOTTERY_3_NUMBER_RATE = Float.parseFloat(lotterySettings.getProperty("AltLottery3NumberRate", "0.2"));
			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Long.parseLong(lotterySettings.getProperty("AltLottery2and1NumberPrize", "200"));
		}
	}

	// *******************************************************************************************
	public static final String	WEDDING_FILE	= "./config/mods/wedding.properties";
	// *******************************************************************************************
	public static boolean		ALLOW_WEDDING;
	public static int			WEDDING_PRICE;
	public static boolean		WEDDING_PUNISH_INFIDELITY;
	public static boolean		WEDDING_TELEPORT;
	public static int			WEDDING_TELEPORT_PRICE;
	public static int			WEDDING_TELEPORT_INTERVAL;
	public static boolean		WEDDING_SAMESEX;
	public static boolean		WEDDING_FORMALWEAR;
	public static boolean		WEDDING_GIVE_CUPID_BOW;
	public static boolean		WEDDING_HONEYMOON_PORT;
	public static int			WEDDING_DIVORCE_COSTS;

	// *******************************************************************************************
	private static final class WeddingConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/wedding";
		}

		@Override
		protected void loadImpl(L2Properties weddingSettings)
		{
			ALLOW_WEDDING = Boolean.parseBoolean(weddingSettings.getProperty("AllowWedding", "False"));
			WEDDING_PRICE = Integer.parseInt(weddingSettings.getProperty("WeddingPrice", "500000"));
			WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(weddingSettings.getProperty("WeddingPunishInfidelity", "true"));
			WEDDING_TELEPORT = Boolean.parseBoolean(weddingSettings.getProperty("WeddingTeleport", "true"));
			WEDDING_TELEPORT_PRICE = Integer.parseInt(weddingSettings.getProperty("WeddingTeleportPrice", "500000"));
			WEDDING_TELEPORT_INTERVAL = Integer.parseInt(weddingSettings.getProperty("WeddingTeleportInterval", "120"));
			WEDDING_SAMESEX = Boolean.parseBoolean(weddingSettings.getProperty("WeddingAllowSameSex", "false"));
			WEDDING_FORMALWEAR = Boolean.parseBoolean(weddingSettings.getProperty("WeddingFormalWear", "true"));
			WEDDING_DIVORCE_COSTS = Integer.parseInt(weddingSettings.getProperty("WeddingDivorceCosts", "20"));
			WEDDING_GIVE_CUPID_BOW = Boolean.parseBoolean(weddingSettings.getProperty("WeddingGiveBow", "true"));
			WEDDING_HONEYMOON_PORT = Boolean.parseBoolean(weddingSettings.getProperty("WeddingHoneyMoon", "false"));
		}
	}

	// *******************************************************************************************
	public static final String	PROTECTOR_FILE	= "./config/mods/protector.properties";
	// *******************************************************************************************
	public static boolean		PROTECTOR_PLAYER_PK;
	public static boolean		PROTECTOR_PLAYER_PVP;
	public static int			PROTECTOR_RADIUS_ACTION;
	public static int			PROTECTOR_SKILLID;
	public static int			PROTECTOR_SKILLLEVEL;
	public static int			PROTECTOR_SKILLTIME;
	public static String		PROTECTOR_MESSAGE;

	// *******************************************************************************************
	private static final class ProtectorConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/protector";
		}

		@Override
		protected void loadImpl(L2Properties protectorSettings)
		{
			PROTECTOR_PLAYER_PK = Boolean.parseBoolean(protectorSettings.getProperty("ProtectorPlayerPK", "False"));
			PROTECTOR_PLAYER_PVP = Boolean.parseBoolean(protectorSettings.getProperty("ProtectorPlayerPVP", "False"));
			PROTECTOR_RADIUS_ACTION = Integer.parseInt(protectorSettings.getProperty("ProtectorRadiusAction", "500"));
			PROTECTOR_SKILLID = Integer.parseInt(protectorSettings.getProperty("ProtectorSkillId", "1069"));
			PROTECTOR_SKILLLEVEL = Integer.parseInt(protectorSettings.getProperty("ProtectorSkillLevel", "42"));
			PROTECTOR_SKILLTIME = Integer.parseInt(protectorSettings.getProperty("ProtectorSkillTime", "800"));
			PROTECTOR_MESSAGE = protectorSettings.getProperty("ProtectorMessage", "Protector, not spawnkilling here, go read the rules !!!");
		}
	}

	// *******************************************************************************************
	public static final String	LEVEL_CHANGER_FILE	= "./config/mods/level_changer.properties";
	// *******************************************************************************************
	public static boolean		ALLOW_NPC_CHANGELEVEL;
	public static int			DECREASE_PRICE0;
	public static int			DECREASE_PRICE1;
	public static int			DECREASE_PRICE2;
	public static int			DECREASE_PRICE3;
	public static int			COEFF_DIVISIONPRICE_R4;										/*for RaceID = 4*/

	// *******************************************************************************************
	private static final class LevelChangerConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/level_changer";
		}

		@Override
		protected void loadImpl(L2Properties npcChLevelSettings)
		{
			ALLOW_NPC_CHANGELEVEL = Boolean.parseBoolean(npcChLevelSettings.getProperty("AllowNpcChangeLevel", "False"));
			DECREASE_PRICE0 = Integer.parseInt(npcChLevelSettings.getProperty("DecreasePrice0", "10000"));
			DECREASE_PRICE1 = Integer.parseInt(npcChLevelSettings.getProperty("DecreasePrice1", "50000"));
			DECREASE_PRICE2 = Integer.parseInt(npcChLevelSettings.getProperty("DecreasePrice2", "500000"));
			DECREASE_PRICE3 = Integer.parseInt(npcChLevelSettings.getProperty("DecreasePrice3", "5000000"));
			COEFF_DIVISIONPRICE_R4 = Integer.parseInt(npcChLevelSettings.getProperty("CoeffDivisionPriceR4", "2"));
		}
	}

	// *******************************************************************************************
	public static final String	ANNOUNCER_FILE	= "./config/mods/announcer.properties";
	// *******************************************************************************************
	public static int			PRICE_PER_ANNOUNCE;
	public static int			MAX_ANNOUNCES_PER_DAY;
	public static int			MIN_LVL_TO_ANNOUNCE;
	public static int			MAX_LVL_TO_ANNOUNCE;
	public static boolean		ALLOW_ANNOUNCER;
	public static boolean		ANNOUNCER_DONATOR_ONLY;

	// *******************************************************************************************
	private static final class AnnouncerConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/announcer";
		}

		@Override
		protected void loadImpl(L2Properties npcAnnouncerSettings)
		{
			ALLOW_ANNOUNCER = Boolean.parseBoolean(npcAnnouncerSettings.getProperty("AllowNpcAnnouncer", "False"));
			ANNOUNCER_DONATOR_ONLY = Boolean.parseBoolean(npcAnnouncerSettings.getProperty("NpcAnnouncerDonatorOnly", "False"));
			PRICE_PER_ANNOUNCE = Integer.parseInt(npcAnnouncerSettings.getProperty("PricePerAnnounce", "10000"));
			MAX_ANNOUNCES_PER_DAY = Integer.parseInt(npcAnnouncerSettings.getProperty("AnnouncesPerDay", "20"));
			MIN_LVL_TO_ANNOUNCE = Integer.parseInt(npcAnnouncerSettings.getProperty("MinLevelToAnnounce", "0"));
			MAX_LVL_TO_ANNOUNCE = Integer.parseInt(npcAnnouncerSettings.getProperty("MaxLevelToAnnounce", "85"));
		}
	}

	// *******************************************************************************************
	public static final String	BUFFER_FILE	= "./config/mods/buffer.properties";
	// *******************************************************************************************
	/** Single Magic Prices */
	public static int			PRICE_PROPHET;
	public static int			PRICE_DANCE;
	public static int			PRICE_SONG;
	public static int			PRICE_ORC;
	public static int			PRICE_CUBIC;
	public static int			PRICE_SUMMON;
	public static int			PRICE_HERO;
	public static int			PRICE_NOBLE;
	public static int			PRICE_OTHER;

	/** Allow / Disallow to Sell Single Magic **/
	public static boolean		SELL_HERO_BUFFS;
	public static boolean		SELL_NOBLE_BUFFS;
	public static boolean		SELL_SUMMON_BUFFS;
	public static boolean		SELL_PROPHET_BUFFS;
	public static boolean		SELL_SONGS;
	public static boolean		SELL_DANCES;
	public static boolean		SELL_CUBICS_BUFFS;
	public static boolean		SELL_ORC_BUFFS;
	public static boolean		SELL_OTHER_BUFFS;

	/** PC Level Variables **/
	public static int			MAX_PC_LVL_FOR_BUFFS;
	public static int			MIN_PC_LVL_FOR_BUFFS;

	/** Other General Settings **/
	public static boolean		ANIMATION;
	public static boolean		DISABLE_BUFFER_ON_FUN_EVENTS;
	public static boolean		ALLOW_NORMAL_BUFFER;
	public static boolean		ALLOW_DONATOR_BUFFER;
	public static boolean		ALLOW_KARMA_PLAYER;

	// *******************************************************************************************
	private static final class BufferConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/buffer";
		}

		@Override
		protected void loadImpl(L2Properties bufferSettings)
		{
			// Single Buffs Prices
			PRICE_SUMMON = Integer.parseInt(bufferSettings.getProperty("PricePerSummonBuff", "10000"));
			PRICE_HERO = Integer.parseInt(bufferSettings.getProperty("PricePerHeroBuff", "10000"));
			PRICE_NOBLE = Integer.parseInt(bufferSettings.getProperty("PricePerNobleBuff", "10000"));
			PRICE_PROPHET = Integer.parseInt(bufferSettings.getProperty("ProphetBuffPrice", "10000"));
			PRICE_SONG = Integer.parseInt(bufferSettings.getProperty("SongPrice", "10000"));
			PRICE_DANCE = Integer.parseInt(bufferSettings.getProperty("DancePrice", "10000"));
			PRICE_ORC = Integer.parseInt(bufferSettings.getProperty("OrcBuffPrice", "10000"));
			PRICE_CUBIC = Integer.parseInt(bufferSettings.getProperty("PricePerCubic", "10000"));
			PRICE_CUBIC = Integer.parseInt(bufferSettings.getProperty("PriceOther", "10000"));

			// Single Magic Allows /Disallows
			SELL_OTHER_BUFFS = Boolean.parseBoolean(bufferSettings.getProperty("SellOtherBuffs", "false"));
			SELL_HERO_BUFFS = Boolean.parseBoolean(bufferSettings.getProperty("SellHeroBuffs", "false"));
			SELL_NOBLE_BUFFS = Boolean.parseBoolean(bufferSettings.getProperty("SellNobleBuffs", "false"));
			SELL_SUMMON_BUFFS = Boolean.parseBoolean(bufferSettings.getProperty("SellSummonBuffs", "false"));
			SELL_PROPHET_BUFFS = Boolean.parseBoolean(bufferSettings.getProperty("SellProphetBuffs", "false"));
			SELL_SONGS = Boolean.parseBoolean(bufferSettings.getProperty("SellSongs", "false"));
			SELL_DANCES = Boolean.parseBoolean(bufferSettings.getProperty("SellDances", "false"));
			SELL_CUBICS_BUFFS = Boolean.parseBoolean(bufferSettings.getProperty("SellCubics", "false"));
			SELL_ORC_BUFFS = Boolean.parseBoolean(bufferSettings.getProperty("SellOrcBuffs", "false"));

			MIN_PC_LVL_FOR_BUFFS = Integer.parseInt(bufferSettings.getProperty("MinPcLevelForBuffs", "1"));
			MAX_PC_LVL_FOR_BUFFS = Integer.parseInt(bufferSettings.getProperty("MaxPcLevelForBuffs", "85"));

			// General
			ALLOW_NORMAL_BUFFER = Boolean.parseBoolean(bufferSettings.getProperty("AllowNormalBuffer", "false"));
			ALLOW_DONATOR_BUFFER = Boolean.parseBoolean(bufferSettings.getProperty("AllowDonatorBuffer", "false"));
			ALLOW_KARMA_PLAYER = Boolean.parseBoolean(bufferSettings.getProperty("AllowKarmaPlayer", "false"));
			ANIMATION = Boolean.parseBoolean(bufferSettings.getProperty("Animation", "true"));
			DISABLE_BUFFER_ON_FUN_EVENTS = Boolean.parseBoolean(bufferSettings.getProperty("DisableBufferOnFunEvents", "false"));
		}
	}

	// *******************************************************************************************
	public static final String	RATES_FILE				= "./config/main/rates.properties";
	// *******************************************************************************************
	public static float			RATE_XP;
	public static float			RATE_SP;
	public static float			RATE_PARTY_XP;
	public static float			RATE_PARTY_SP;
	public static float			RATE_QUESTS_REWARD_EXPSP;
	public static float			RATE_QUESTS_REWARD_ADENA;
	public static float			RATE_QUESTS_REWARD_ITEMS;
	public static float			RATE_DROP_SEALSTONE;
	public static float			RATE_DROP_ADENA;
	public static float			RATE_DROP_ADENA_RAID;
	public static float			RATE_DROP_ADENA_GRAND_BOSS;
	public static float			RATE_CONSUMABLE_COST;
	public static float			RATE_CRAFT_COST;
	public static int			RATE_MASTERWORK;
	public static int			RATE_CRITICAL_CRAFT_CHANCE;
	public static int			RATE_CRITICAL_CRAFT_MULTIPLIER;
	public static float			RATE_DROP_ITEMS;
	public static float			RATE_DROP_ITEMS_RAID;
	public static float			RATE_DROP_ITEMS_GRAND_BOSS;
	public static float			RATE_DROP_ITEMS_JEWEL;
	public static float			RATE_DROP_SPOIL;
	public static float			RATE_DROP_SPOIL_RAID;
	public static float			RATE_DROP_SPOIL_GRAND_BOSS;
	public static int			RATE_DROP_MANOR;
	public static float			RATE_DROP_QUEST;
	public static int			RATE_EXTR_FISH;
	public static float			RATE_KARMA_EXP_LOST;
	public static float			RATE_SIEGE_GUARDS_PRICE;
	public static float			RATE_RUN_SPEED;
	public static float			ALT_GAME_EXPONENT_XP;										// Alternative Xp/Sp rewards, if not 0, then calculated as
	// 2^((mob.level-player.level) / coef
	public static float			ALT_GAME_EXPONENT_SP;
	public static int			PLAYER_DROP_LIMIT;
	public static int			PLAYER_RATE_DROP;
	public static int			PLAYER_RATE_DROP_ITEM;
	public static int			PLAYER_RATE_DROP_EQUIP;
	public static int			PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float			PET_XP_RATE;
	public static float			PET_FOOD_RATE;
	public static float			SINEATER_XP_RATE;
	public static float			RATE_DROP_COMMON_HERBS;
	public static float			RATE_DROP_MP_HP_HERBS;
	public static float			RATE_DROP_GREATER_HERBS;
	public static float			RATE_DROP_SUPERIOR_HERBS;
	public static float			RATE_DROP_SPECIAL_HERBS;
	public static int			KARMA_DROP_LIMIT;
	public static int			KARMA_RATE_DROP;
	public static int			KARMA_RATE_DROP_ITEM;
	public static int			KARMA_RATE_DROP_EQUIP;
	public static int			KARMA_RATE_DROP_EQUIP_WEAPON;
	public static double[]		PLAYER_XP_PERCENT_LOST;

	public static boolean		COMPENSATE_QUEST_ITEM_REWARDS;

	public static boolean		ALLOW_DYNAMIC_RATES;

	public static float			NO_GRADE_XP_BONUS;
	public static float			D_GRADE_XP_BONUS;
	public static float			C_GRADE_XP_BONUS;
	public static float			B_GRADE_XP_BONUS;
	public static float			A_GRADE_XP_BONUS;
	public static float			S_GRADE_XP_BONUS;
	public static float			S80_GRADE_XP_BONUS;

	public static float			NO_GRADE_SP_BONUS;
	public static float			D_GRADE_SP_BONUS;
	public static float			C_GRADE_SP_BONUS;
	public static float			B_GRADE_SP_BONUS;
	public static float			A_GRADE_SP_BONUS;
	public static float			S_GRADE_SP_BONUS;
	public static float			S80_GRADE_SP_BONUS;

	public static String		SPECIAL_ITEMS;
	public static List<Integer>	LIST_OF_SPECIAL_ITEMS	= new FastList<Integer>();
	public static float			RATE_DROP_SPECIAL_ITEMS;

	public static float			RATE_DROP_COMMON_ITEMS;

	public static float			RATE_TRUST_POINT;

	// *******************************************************************************************
	private static final class RatesConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/rates";
		}

		@Override
		protected void loadImpl(L2Properties ratesSettings)
		{
			RATE_XP = Float.parseFloat(ratesSettings.getProperty("RateXp", "1."));
			RATE_SP = Float.parseFloat(ratesSettings.getProperty("RateSp", "1."));
			RATE_PARTY_XP = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1."));
			RATE_PARTY_SP = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1."));

			RATE_QUESTS_REWARD_EXPSP = Float.parseFloat(ratesSettings.getProperty("RateQuestsRewardExpSp", "1."));
			RATE_QUESTS_REWARD_ADENA = Float.parseFloat(ratesSettings.getProperty("RateQuestsRewardAdena", "1."));
			RATE_QUESTS_REWARD_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateQuestsRewardItems", "1."));

			RATE_DROP_SEALSTONE = Float.parseFloat(ratesSettings.getProperty("RateDropSealStone", "1."));
			RATE_DROP_ADENA = Float.parseFloat(ratesSettings.getProperty("RateDropAdena", "1."));
			RATE_DROP_ADENA_RAID = Float.parseFloat(ratesSettings.getProperty("RateDropAdenaRaid", "1."));
			RATE_DROP_ADENA_GRAND_BOSS = Float.parseFloat(ratesSettings.getProperty("RateDropAdenaGrandBoss", "1."));
			RATE_CONSUMABLE_COST = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1."));
			RATE_CRAFT_COST = Float.parseFloat(ratesSettings.getProperty("RateCraftCost", "1."));
			RATE_MASTERWORK = Integer.parseInt(ratesSettings.getProperty("RateMasterwork", "5"));
			RATE_CRITICAL_CRAFT_CHANCE = Integer.parseInt(ratesSettings.getProperty("RateCriticalCraftChance", "5"));
			RATE_CRITICAL_CRAFT_MULTIPLIER = Integer.parseInt(ratesSettings.getProperty("RateCriticalCraftMutliplier", "2"));
			RATE_DROP_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1."));
			RATE_DROP_ITEMS_RAID = Float.parseFloat(ratesSettings.getProperty("RateDropItemsRaid", "1."));
			RATE_DROP_ITEMS_GRAND_BOSS = Float.parseFloat(ratesSettings.getProperty("RateDropItemsGrandBoss", "1."));
			RATE_DROP_ITEMS_JEWEL = Float.parseFloat(ratesSettings.getProperty("RateDropItemsJewel", "1."));
			RATE_DROP_SPOIL = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1."));
			RATE_DROP_SPOIL_RAID = Float.parseFloat(ratesSettings.getProperty("RateDropSpoilRaid", "1."));
			RATE_DROP_SPOIL_GRAND_BOSS = Float.parseFloat(ratesSettings.getProperty("RateDropSpoilGrandBoss", "1."));
			RATE_DROP_MANOR = Integer.parseInt(ratesSettings.getProperty("RateDropManor", "1"));
			RATE_DROP_QUEST = Float.parseFloat(ratesSettings.getProperty("RateDropQuest", "1."));
			RATE_EXTR_FISH = Integer.parseInt(ratesSettings.getProperty("RateExtractFish", "1"));
			RATE_RUN_SPEED = Float.parseFloat(ratesSettings.getProperty("RateRunSpeed", "1."));
			RATE_KARMA_EXP_LOST = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1."));
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1."));

			ALT_GAME_EXPONENT_XP = Float.parseFloat(ratesSettings.getProperty("AltGameExponentXp", "1."));
			ALT_GAME_EXPONENT_SP = Float.parseFloat(ratesSettings.getProperty("AltGameExponentSp", "1."));

			RATE_DROP_COMMON_HERBS = Float.parseFloat(ratesSettings.getProperty("RateCommonHerbs", "15."));
			RATE_DROP_MP_HP_HERBS = Float.parseFloat(ratesSettings.getProperty("RateHpMpHerbs", "10."));
			RATE_DROP_GREATER_HERBS = Float.parseFloat(ratesSettings.getProperty("RateGreaterHerbs", "4."));
			RATE_DROP_SUPERIOR_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSuperiorHerbs", "0.8")) * 10;
			RATE_DROP_SPECIAL_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSpecialHerbs", "0.2")) * 10;

			PLAYER_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "3"));
			PLAYER_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "5"));
			PLAYER_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "70"));
			PLAYER_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "25"));
			PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "5"));

			PET_XP_RATE = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1."));
			PET_FOOD_RATE = Float.parseFloat(ratesSettings.getProperty("PetFoodRate", "1"));
			SINEATER_XP_RATE = Float.parseFloat(ratesSettings.getProperty("SinEaterXpRate", "1."));

			KARMA_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
			KARMA_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
			KARMA_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));

			// Initializing table
			PLAYER_XP_PERCENT_LOST = new double[Byte.MAX_VALUE + 1];

			// Default value
			for (int i = 0; i <= Byte.MAX_VALUE; i++)
				PLAYER_XP_PERCENT_LOST[i] = 1.;

			// Now loading into table parsed values
			try
			{
				String[] values = ratesSettings.getProperty("PlayerXPPercentLost", "0,39-7.0;40,75-4.0;76,76-2.5;77,77-2.0;78,78-1.5").split(";");

				for (String s : values)
				{
					int min;
					int max;
					double val;

					String[] vals = s.split("-");
					String[] mM = vals[0].split(",");

					min = Integer.parseInt(mM[0]);
					max = Integer.parseInt(mM[1]);
					val = Double.parseDouble(vals[1]);

					for (int i = min; i <= max; i++)
						PLAYER_XP_PERCENT_LOST[i] = val;
				}
			}
			catch (Exception e)
			{
				_log.warn("Error while loading Player XP percent lost");
				e.printStackTrace();
			}

			COMPENSATE_QUEST_ITEM_REWARDS = Boolean.parseBoolean(ratesSettings.getProperty("AdenaInsteadOfMoreItems", "False"));

			ALLOW_DYNAMIC_RATES = Boolean.parseBoolean(ratesSettings.getProperty("AllowDynamicRates", "true"));

			NO_GRADE_XP_BONUS = Float.parseFloat(ratesSettings.getProperty("NoGradeXpBonus", "1."));
			D_GRADE_XP_BONUS = Float.parseFloat(ratesSettings.getProperty("DGradeXpBonus", "1."));
			C_GRADE_XP_BONUS = Float.parseFloat(ratesSettings.getProperty("CGradeXpBonus", "1."));
			B_GRADE_XP_BONUS = Float.parseFloat(ratesSettings.getProperty("BGradeXpBonus", "1."));
			A_GRADE_XP_BONUS = Float.parseFloat(ratesSettings.getProperty("AGradeXpBonus", "1."));
			S_GRADE_XP_BONUS = Float.parseFloat(ratesSettings.getProperty("SGradeXpBonus", "1."));
			S80_GRADE_XP_BONUS = Float.parseFloat(ratesSettings.getProperty("S80GradeXpBonus", "1."));

			NO_GRADE_SP_BONUS = Float.parseFloat(ratesSettings.getProperty("NoGradeSpBonus", "1."));
			D_GRADE_SP_BONUS = Float.parseFloat(ratesSettings.getProperty("DGradeSpBonus", "1."));
			C_GRADE_SP_BONUS = Float.parseFloat(ratesSettings.getProperty("CGradeSpBonus", "1."));
			B_GRADE_SP_BONUS = Float.parseFloat(ratesSettings.getProperty("BGradeSpBonus", "1."));
			A_GRADE_SP_BONUS = Float.parseFloat(ratesSettings.getProperty("AGradeSpBonus", "1."));
			S_GRADE_SP_BONUS = Float.parseFloat(ratesSettings.getProperty("SGradeSpBonus", "1."));
			S80_GRADE_SP_BONUS = Float.parseFloat(ratesSettings.getProperty("S80GradeSpBonus", "1."));

			RATE_DROP_SPECIAL_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateDropSpecialItems", "1."));
			SPECIAL_ITEMS = ratesSettings.getProperty("SpecialItems", "0");
			LIST_OF_SPECIAL_ITEMS = new FastList<Integer>();
			for (String id : SPECIAL_ITEMS.split(","))
				LIST_OF_SPECIAL_ITEMS.add(Integer.parseInt(id));

			RATE_DROP_COMMON_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateDropCommonItems", "1."));

			RATE_TRUST_POINT = Float.parseFloat(ratesSettings.getProperty("RateTrustPoint", "3."));
		}
	}

	// *******************************************************************************************
	public static final String	ENCHANT_FILE	= "./config/main/enchant.properties";
	// *******************************************************************************************
	public static int			ENCHANT_CHANCE_ELEMENT;
	public static boolean		ALLOW_CRYSTAL_SCROLL;
	public static boolean		ENCHANT_HERO_WEAPONS;									// Enchant hero weapons?
	public static boolean		ENCHANT_DWARF_SYSTEM;									// Dwarf enchant System?
	public static int			ENCHANT_MAX_WEAPON;									// Maximum level of enchantment
	public static int			ENCHANT_MAX_ARMOR;
	public static int			ENCHANT_MAX_JEWELRY;
	public static int			ENCHANT_SAFE_MAX;										// maximum level of safe enchantment
	public static int			ENCHANT_SAFE_MAX_FULL;
	public static int			ENCHANT_DWARF_1_ENCHANTLEVEL;							// Dwarf enchant System Dwarf 1 Enchantlevel?
	public static int			ENCHANT_DWARF_2_ENCHANTLEVEL;							// Dwarf enchant System Dwarf 2 Enchantlevel?
	public static int			ENCHANT_DWARF_3_ENCHANTLEVEL;							// Dwarf enchant System Dwarf 3 Enchantlevel?
	public static int			ENCHANT_DWARF_1_CHANCE;								// Dwarf enchant System Dwarf 1 chance?
	public static int			ENCHANT_DWARF_2_CHANCE;								// Dwarf enchant System Dwarf 2 chance?
	public static int			ENCHANT_DWARF_3_CHANCE;								// Dwarf enchant System Dwarf 3 chance?

	public static boolean		AUGMENT_EXCLUDE_NOTDONE;
	public static int			AUGMENTATION_NG_SKILL_CHANCE;							// Chance to get a skill while using a NoGrade Life Stone
	public static int			AUGMENTATION_NG_GLOW_CHANCE;							// Chance to get a Glow effect while using a NoGrade Life Stone(only if you get a skill)
	public static int			AUGMENTATION_MID_SKILL_CHANCE;							// Chance to get a skill while using a MidGrade Life Stone
	public static int			AUGMENTATION_MID_GLOW_CHANCE;							// Chance to get a Glow effect while using a MidGrade Life Stone(only if you get a skill)
	public static int			AUGMENTATION_HIGH_SKILL_CHANCE;						// Chance to get a skill while using a HighGrade Life Stone
	public static int			AUGMENTATION_HIGH_GLOW_CHANCE;							// Chance to get a Glow effect while using a HighGrade Life Stone
	public static int			AUGMENTATION_TOP_SKILL_CHANCE;							// Chance to get a skill while using a TopGrade Life Stone
	public static int			AUGMENTATION_TOP_GLOW_CHANCE;							// Chance to get a Glow effect while using a TopGrade Life Stone
	public static int			AUGMENTATION_BASESTAT_CHANCE;							// Chance to get a BaseStatModifier in the augmentation process
	public static int			AUGMENTATION_ACC_SKILL_CHANCE;
	public static int[]			AUGMENTATION_BLACKLIST;

	public static String		ENCHANT_BLACK_LIST;

	// *******************************************************************************************
	private static final class EnchantConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/enchant";
		}

		@Override
		protected void loadImpl(L2Properties enchantSettings)
		{
			/* chance to enchant an item crystal scroll */
			ALLOW_CRYSTAL_SCROLL = Boolean.parseBoolean(enchantSettings.getProperty("AllowCrystalScroll", "False"));
			/* chance to enchant an item with elemental attribute */
			ENCHANT_CHANCE_ELEMENT = Integer.parseInt(enchantSettings.getProperty("EnchantChanceElement", "50"));
			/* enchat hero weapons? */
			ENCHANT_HERO_WEAPONS = Boolean.parseBoolean(enchantSettings.getProperty("EnchantHeroWeapons", "False"));
			/* enchant dwarf system */
			ENCHANT_DWARF_SYSTEM = Boolean.parseBoolean(enchantSettings.getProperty("EnchantDwarfSystem", "False"));
			/* limit on enchant */
			ENCHANT_MAX_WEAPON = Integer.parseInt(enchantSettings.getProperty("EnchantMaxWeapon", "255"));
			ENCHANT_MAX_ARMOR = Integer.parseInt(enchantSettings.getProperty("EnchantMaxArmor", "255"));
			ENCHANT_MAX_JEWELRY = Integer.parseInt(enchantSettings.getProperty("EnchantMaxJewelry", "255"));
			/* limit of safe enchant */
			ENCHANT_SAFE_MAX = Integer.parseInt(enchantSettings.getProperty("EnchantSafeMax", "3"));
			ENCHANT_SAFE_MAX_FULL = Integer.parseInt(enchantSettings.getProperty("EnchantSafeMaxFull", "4"));
			ENCHANT_DWARF_1_ENCHANTLEVEL = Integer.parseInt(enchantSettings.getProperty("EnchantDwarf1Enchantlevel", "8"));
			ENCHANT_DWARF_2_ENCHANTLEVEL = Integer.parseInt(enchantSettings.getProperty("EnchantDwarf2Enchantlevel", "10"));
			ENCHANT_DWARF_3_ENCHANTLEVEL = Integer.parseInt(enchantSettings.getProperty("EnchantDwarf3Enchantlevel", "12"));
			ENCHANT_DWARF_1_CHANCE = Integer.parseInt(enchantSettings.getProperty("EnchantDwarf1Chance", "15"));
			ENCHANT_DWARF_2_CHANCE = Integer.parseInt(enchantSettings.getProperty("EnchantDwarf2Chance", "15"));
			ENCHANT_DWARF_3_CHANCE = Integer.parseInt(enchantSettings.getProperty("EnchantDwarf3Chance", "15"));

			AUGMENT_EXCLUDE_NOTDONE = Boolean.parseBoolean(enchantSettings.getProperty("AugmentExcludeNotdone", "False"));
			AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(enchantSettings.getProperty("AugmentationNGSkillChance", "15"));
			AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(enchantSettings.getProperty("AugmentationNGGlowChance", "0"));
			AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(enchantSettings.getProperty("AugmentationMidSkillChance", "30"));
			AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(enchantSettings.getProperty("AugmentationMidGlowChance", "40"));
			AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(enchantSettings.getProperty("AugmentationHighSkillChance", "45"));
			AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(enchantSettings.getProperty("AugmentationHighGlowChance", "70"));
			AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(enchantSettings.getProperty("AugmentationTopSkillChance", "60"));
			AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(enchantSettings.getProperty("AugmentationTopGlowChance", "100"));
			AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(enchantSettings.getProperty("AugmentationBaseStatChance", "1"));
			AUGMENTATION_ACC_SKILL_CHANCE = Integer.parseInt(enchantSettings.getProperty("AugmentationAccSkillChance", "0"));
			StringTokenizer st = new StringTokenizer(
					enchantSettings
							.getProperty(
									"AugmentationBlackList",
									"6656,6657,6658,6659,6660,6661,6662,8191,10170,10314,13034,13035,13036,13042,13043,13044,13052,13053,13054,13740,13741,13742,13743,13744,13745,13746,13747,13748,14592,14593,14594,14595,14596,14597,14598,14599,14600,14664,14665,14666,14667,14668,14669,14670,14671,14672,14801,14802,14803,14804,14805,14806,14807,14808,14809,15282,15283,15284,15285,15286,15287,15288,15289,15290,15291,15292,15293,15294,15295,15296,15297,15298,15299"),
					",");
			AUGMENTATION_BLACKLIST = new int[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++)
				AUGMENTATION_BLACKLIST[i] = Integer.parseInt(st.nextToken());

			ENCHANT_BLACK_LIST = enchantSettings.getProperty("EnchantBlackList", "7816-7831;13034-13036;13042-13044;13052-13054;13293;13294;13296");
		}
	}

	// *******************************************************************************************
	public static final String	PVP_FILE	= "./config/main/pvp_settings.properties";
	// *******************************************************************************************
	public static int			KARMA_MIN_KARMA;
	public static int			KARMA_MAX_KARMA;
	public static float			KARMA_RATE;
	public static int			KARMA_XP_DIVIDER;
	public static int			KARMA_LOST_BASE;
	public static boolean		KARMA_DROP_GM;
	public static boolean		KARMA_AWARD_PK_KILL;
	public static int			KARMA_PK_LIMIT;
	public static int[]			KARMA_LIST_NONDROPPABLE_PET_ITEMS;
	public static int[]			KARMA_LIST_NONDROPPABLE_ITEMS;
	public static int			PVP_TIME;
	public static boolean		ALT_PLAYER_CAN_DROP_ADENA;								// Player can drop adena ?
	public static boolean		ALT_ANNOUNCE_PK;										// Announce Pks ?
	public static boolean		ALT_ANNOUNCE_PK_NORMAL_MESSAGE;
	public static int			PLAYER_RATE_DROP_ADENA;
	public static int			PVP_NORMAL_TIME;										// Duration (in ms) while a player stay in PVP mode
	// after hitting an innocent
	public static int			PVP_PVP_TIME;											// Duration (in ms) while a player stay in PVP mode
	// after hitting a purple player
	public static boolean		CURSED_WEAPON_NPC_INTERACT;

	public static boolean		ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;		// Karma Punishment
	public static boolean		ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean		ALT_GAME_KARMA_PLAYER_CAN_USE_GK;						// Allow player with karma to use GK ?
	public static boolean		ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean		ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean		ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;

	// *******************************************************************************************
	private static final class PvPConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/pvp_settings";
		}

		@Override
		protected void loadImpl(L2Properties pvpSettings)
		{
			/* KARMA SYSTEM */
			KARMA_MIN_KARMA = Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
			KARMA_MAX_KARMA = Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
			KARMA_RATE = Float.parseFloat(pvpSettings.getProperty("KarmaRate", "1."));
			KARMA_XP_DIVIDER = Integer.parseInt(pvpSettings.getProperty("XPDivider", "260"));
			KARMA_LOST_BASE = Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "0"));

			KARMA_DROP_GM = Boolean.parseBoolean(pvpSettings.getProperty("CanGMDropEquipment", "false"));
			KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AwardPKKillPVPPoint", "true"));

			KARMA_PK_LIMIT = Integer.parseInt(pvpSettings.getProperty("MinimumPKRequiredToDrop", "5"));

			String KARMA_NONDROPPABLE_PET_ITEMS = pvpSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650,9882");
			String KARMA_NONDROPPABLE_ITEMS = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369");

			String[] ids = KARMA_NONDROPPABLE_PET_ITEMS.trim().split(",");
			KARMA_LIST_NONDROPPABLE_PET_ITEMS = new int[ids.length];
			for (int i = 0; i < ids.length; i++)
			{
				KARMA_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(ids[i].trim());
			}

			ids = KARMA_NONDROPPABLE_ITEMS.trim().split(",");
			KARMA_LIST_NONDROPPABLE_ITEMS = new int[ids.length];
			for (int i = 0; i < ids.length; i++)
			{
				KARMA_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(ids[i].trim());
			}

			// sorting so binarySearch can be used later
			Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS);
			Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS);

			ALT_PLAYER_CAN_DROP_ADENA = Boolean.parseBoolean(pvpSettings.getProperty("PlayerCanDropAdena", "false"));
			PLAYER_RATE_DROP_ADENA = Integer.parseInt(pvpSettings.getProperty("PlayerRateDropAdena", "1"));
			ALT_ANNOUNCE_PK = Boolean.parseBoolean(pvpSettings.getProperty("AnnouncePk", "false"));
			ALT_ANNOUNCE_PK_NORMAL_MESSAGE = Boolean.parseBoolean(pvpSettings.getProperty("AnnouncePkNormalMessage", "false"));
			PVP_NORMAL_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsNormalTime", "120000"));
			PVP_PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsPvPTime", "60000"));
			PVP_TIME = PVP_NORMAL_TIME;
			CURSED_WEAPON_NPC_INTERACT = Boolean.parseBoolean(pvpSettings.getProperty("CursedWeaponNpcInteract", "false"));

			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanShop", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanUseGK", "false"));
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanTeleport", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanTrade", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));
		}
	}

	// *******************************************************************************************
	public static final String	ID_FILE	= "./config/main/id_factory.properties";

	// *******************************************************************************************
	public static enum IdFactoryType
	{
		Compaction, BitSet, Stack, Increment, Rebuild
	}

	public static IdFactoryType	IDFACTORY_TYPE;	// ID Factory type
	public static boolean		BAD_ID_CHECKING;	// Check for bad ID ?

	// *******************************************************************************************
	private static final class IDConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/id_factory";
		}

		@Override
		protected void loadImpl(L2Properties idSettings)
		{
			IDFACTORY_TYPE = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "BitSet"));
			BAD_ID_CHECKING = Boolean.parseBoolean(idSettings.getProperty("BadIdChecking", "True"));
		}
	}

	// *******************************************************************************************
	public static final String			OTHER_FILE			= "./config/main/other_settings.properties";
	// *******************************************************************************************
	public static boolean				JAIL_IS_PVP;														// Jail config
	public static boolean				JAIL_DISABLE_CHAT;													// Jail config
	public static int					GREAT_WOLF_MOUNT_LEVEL;
	public static boolean				ALLOW_WYVERN_UPGRADER;
	public static boolean				STORE_EFFECTS;														// Store skills cooltime on char exit/relogin
	public static boolean				STORE_EFFECTS_ON_SUBCLASS_CHANGE;
	public static int					SEND_NOTDONE_SKILLS;
	public static boolean				ANNOUNCE_MAMMON_SPAWN;

	public static long					STARTING_ADENA;													// Amount of adenas when starting a new character
	public static byte					STARTING_LEVEL;
	public static int					STARTING_SP;
	public static int					UNSTUCK_INTERVAL;
	public static int					TELEPORT_WATCHDOG_TIMEOUT;
	public static int					PLAYER_SPAWN_PROTECTION;											// Player Protection control
	public static int					PLAYER_FAKEDEATH_UP_PROTECTION;

	/**
	 * Allow lesser effects to be canceled if stronger effects are used when
	 * effects of the same stack group are used.<br>
	 * New effects that are added will be canceled if they are of lesser
	 * priority to the old one.
	 */
	public static boolean				EFFECT_CANCELING;
	public static String				NONDROPPABLE_ITEMS;
	public static String				PET_RENT_NPC;
	public static final Set<Integer>	LIST_PET_RENT_NPC	= new L2FastSet<Integer>();
	public static boolean				LEVEL_ADD_LOAD;
	public static int					DEATH_PENALTY_CHANCE;												// Death Penalty chance

	// *******************************************************************************************
	// *******************************************************************************************
	private static final class OtherConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/other_settings";
		}

		@Override
		protected void loadImpl(L2Properties otherSettings)
		{
			EFFECT_CANCELING = Boolean.parseBoolean(otherSettings.getProperty("CancelLesserEffect", "True"));

			GREAT_WOLF_MOUNT_LEVEL = Integer.parseInt(otherSettings.getProperty("GreatWolfMountLevel", "70"));

			ALLOW_WYVERN_UPGRADER = Boolean.parseBoolean(otherSettings.getProperty("AllowWyvernUpgrader", "False"));

			/* Config weight limit */
			LEVEL_ADD_LOAD = Boolean.parseBoolean(otherSettings.getProperty("IncreaseWeightLimitByLevel", "false"));

			STARTING_ADENA = Long.parseLong(otherSettings.getProperty("StartingAdena", "100"));
			STARTING_LEVEL = Byte.parseByte(otherSettings.getProperty("StartingLevel", "1"));
			STARTING_SP = Integer.parseInt(otherSettings.getProperty("StartingSP", "0"));
			UNSTUCK_INTERVAL = Integer.parseInt(otherSettings.getProperty("UnstuckInterval", "300"));
			TELEPORT_WATCHDOG_TIMEOUT = Integer.parseInt(otherSettings.getProperty("TeleportWatchdogTimeout", "0"));

			/* Player protection after teleport or login */
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerSpawnProtection", "0"));

			/* Player protection after recovering from fake death (works against mobs only) */
			PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerFakeDeathUpProtection", "0"));

			STORE_EFFECTS = Boolean.parseBoolean(otherSettings.getProperty("StoreSkillCooltime", "true"));
			STORE_EFFECTS_ON_SUBCLASS_CHANGE = Boolean.parseBoolean(otherSettings.getProperty("SubclassStoreSkillCooltime", "false"));

			SEND_NOTDONE_SKILLS = Integer.parseInt(otherSettings.getProperty("SendNOTDONESkills", "2"));

			PET_RENT_NPC = otherSettings.getProperty("ListPetRentNpc", "30827");
			LIST_PET_RENT_NPC.clear();
			for (String id : PET_RENT_NPC.split(","))
			{
				LIST_PET_RENT_NPC.add(Integer.parseInt(id));
			}

			ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(otherSettings.getProperty("AnnounceMammonSpawn", "True"));

			JAIL_IS_PVP = Boolean.parseBoolean(otherSettings.getProperty("JailIsPvp", "True"));
			JAIL_DISABLE_CHAT = Boolean.parseBoolean(otherSettings.getProperty("JailDisableChat", "True"));
			DEATH_PENALTY_CHANCE = Integer.parseInt(otherSettings.getProperty("DeathPenaltyChance", "20"));
		}
	}

	// *******************************************************************************************
	public static final String	PRIVATE_STORE_FILE	= "./config/main/private_stores.properties";
	// *******************************************************************************************
	// Slot limits for private store
	public static int			MAX_PVTSTORESELL_SLOTS_DWARF;
	public static int			MAX_PVTSTORESELL_SLOTS_OTHER;
	public static int			MAX_PVTSTOREBUY_SLOTS_DWARF;
	public static int			MAX_PVTSTOREBUY_SLOTS_OTHER;

	// *******************************************************************************************
	private static final class PrivateStoresConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/private_stores";
		}

		@Override
		protected void loadImpl(L2Properties privatestoresSettings)
		{
			/* Maximum number of available slots for pvt stores */
			MAX_PVTSTORESELL_SLOTS_DWARF = Integer.parseInt(privatestoresSettings.getProperty("MaxPvtStoreSellSlotsDwarf", "4"));
			MAX_PVTSTORESELL_SLOTS_OTHER = Integer.parseInt(privatestoresSettings.getProperty("MaxPvtStoreSellSlotsOther", "3"));
			MAX_PVTSTOREBUY_SLOTS_DWARF = Integer.parseInt(privatestoresSettings.getProperty("MaxPvtStoreBuySlotsDwarf", "5"));
			MAX_PVTSTOREBUY_SLOTS_OTHER = Integer.parseInt(privatestoresSettings.getProperty("MaxPvtStoreBuySlotsOther", "4"));
		}
	}

	// *******************************************************************************************
	public static final String	PETITIONS_FILE	= "./config/main/petitions.properties";
	// *******************************************************************************************
	public static boolean		PETITIONING_ALLOWED;
	public static int			MAX_PETITIONS_PER_PLAYER;
	public static int			MAX_PETITIONS_PENDING;

	// *******************************************************************************************
	private static final class PetitionsConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/petitions";
		}

		@Override
		protected void loadImpl(L2Properties petitionsSettings)
		{
			PETITIONING_ALLOWED = Boolean.parseBoolean(petitionsSettings.getProperty("PetitioningAllowed", "True"));
			MAX_PETITIONS_PER_PLAYER = Integer.parseInt(petitionsSettings.getProperty("MaxPetitionsPerPlayer", "5"));
			MAX_PETITIONS_PENDING = Integer.parseInt(petitionsSettings.getProperty("MaxPetitionsPending", "25"));
		}
	}

	// *******************************************************************************************
	public static final String	INVENTORY_FILE	= "./config/main/inventory.properties";
	// *******************************************************************************************
	public static boolean		ALT_AUTO_LOOT;											// Accept auto-loot ?
	public static boolean		ALT_AUTO_LOOT_RAID;
	public static boolean		ALT_AUTO_LOOT_ADENA;
	public static boolean		ALT_AUTO_LOOT_HERBS;
	public static int			INVENTORY_MAXIMUM_NO_DWARF;							// Inventory slots limits
	public static int			INVENTORY_MAXIMUM_DWARF;								// Inventory slots limits
	public static int			INVENTORY_MAXIMUM_GM;									// Inventory slots limits
	public static int			INVENTORY_MAXIMUM_QUEST_ITEMS;

	public static boolean		FORCE_INVENTORY_UPDATE;
	public static int			MAX_ITEM_IN_PACKET;

	// *******************************************************************************************
	private static final class InventoryConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/inventory";
		}

		@Override
		protected void loadImpl(L2Properties inventorySettings)
		{
			ALT_AUTO_LOOT = Boolean.parseBoolean(inventorySettings.getProperty("AutoLoot", "true"));
			ALT_AUTO_LOOT_RAID = Boolean.parseBoolean(inventorySettings.getProperty("AutoLootRaid", "true"));
			ALT_AUTO_LOOT_ADENA = Boolean.parseBoolean(inventorySettings.getProperty("AutoLootAdena", "true"));
			ALT_AUTO_LOOT_HERBS = Boolean.parseBoolean(inventorySettings.getProperty("AutoLootHerbs", "true"));

			/* Inventory slots limits */
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(inventorySettings.getProperty("MaximumSlotsForNoDwarf", "80"));
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(inventorySettings.getProperty("MaximumSlotsForDwarf", "100"));
			INVENTORY_MAXIMUM_GM = Integer.parseInt(inventorySettings.getProperty("MaximumSlotsForGMPlayer", "250"));

			INVENTORY_MAXIMUM_QUEST_ITEMS = Integer.parseInt(inventorySettings.getProperty("MaximumSlotsForQuestItems", "100"));

			/* Inventory slots limits */
			MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));

			FORCE_INVENTORY_UPDATE = Boolean.parseBoolean(inventorySettings.getProperty("ForceInventoryUpdate", "False"));
		}
	}

	// *******************************************************************************************
	public static final String	WAREHOUSE_FILE	= "./config/main/warehouse.properties";
	// *******************************************************************************************
	public static boolean		ALLOW_WAREHOUSE;
	public static boolean		ENABLE_WAREHOUSESORTING_CLAN;							// Warehouse Sorting Clan
	public static boolean		ENABLE_WAREHOUSESORTING_PRIVATE;						// Warehouse Sorting Privat
	public static int			WAREHOUSE_CACHE_TIME;									// How long store WH datas
	public static int			WAREHOUSE_SLOTS_NO_DWARF;								// Warehouse slots limits
	public static int			WAREHOUSE_SLOTS_DWARF;									// Warehouse slots limits
	public static int			WAREHOUSE_SLOTS_CLAN;									// Warehouse slots limits

	// *******************************************************************************************
	private static final class WarehouseConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/warehouse";
		}

		@Override
		protected void loadImpl(L2Properties warehouseSettings)
		{
			ALLOW_WAREHOUSE = Boolean.parseBoolean(warehouseSettings.getProperty("AllowWarehouse", "True"));
			ENABLE_WAREHOUSESORTING_CLAN = Boolean.parseBoolean(warehouseSettings.getProperty("EnableWarehouseSortingClan", "False"));
			ENABLE_WAREHOUSESORTING_PRIVATE = Boolean.parseBoolean(warehouseSettings.getProperty("EnableWarehouseSortingPrivate", "False"));
			WAREHOUSE_CACHE_TIME = Integer.parseInt(warehouseSettings.getProperty("WarehouseCacheTime", "15"));
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(warehouseSettings.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(warehouseSettings.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(warehouseSettings.getProperty("MaximumWarehouseSlotsForClan", "150"));
		}
	}

	// *******************************************************************************************
	public static final String	REGENERATION_FILE	= "./config/main/regeneration.properties";
	// *******************************************************************************************
	public static double		NPC_HP_REGEN_MULTIPLIER;										// NPC regen multipliers
	public static double		NPC_MP_REGEN_MULTIPLIER;										// NPC regen multipliers
	public static double		PLAYER_HP_REGEN_MULTIPLIER;									// Player regen multipliers
	public static double		PLAYER_MP_REGEN_MULTIPLIER;									// Player regen multipliers
	public static double		PLAYER_CP_REGEN_MULTIPLIER;									// Player regen multipliers
	public static double		RAID_HP_REGEN_MULTIPLIER;										// Multiplier for Raid boss HP regeneration
	public static double		RAID_MP_REGEN_MULTIPLIER;										// Mulitplier for Raid boss MP regeneration

	// *******************************************************************************************
	private static final class RegenerationConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/regeneration";
		}

		@Override
		protected void loadImpl(L2Properties regenerationSettings)
		{
			/* if different from 100 (ie 100%) heal rate is modified acordingly */
			NPC_HP_REGEN_MULTIPLIER = Double.parseDouble(regenerationSettings.getProperty("NPCHpRegenMultiplier", "100")) / 100;
			NPC_MP_REGEN_MULTIPLIER = Double.parseDouble(regenerationSettings.getProperty("NPCMpRegenMultiplier", "100")) / 100;

			PLAYER_HP_REGEN_MULTIPLIER = Double.parseDouble(regenerationSettings.getProperty("PlayerHpRegenMultiplier", "100")) / 100;
			PLAYER_MP_REGEN_MULTIPLIER = Double.parseDouble(regenerationSettings.getProperty("PlayerMpRegenMultiplier", "100")) / 100;
			PLAYER_CP_REGEN_MULTIPLIER = Double.parseDouble(regenerationSettings.getProperty("PlayerCpRegenMultiplier", "100")) / 100;

			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(regenerationSettings.getProperty("RaidHpRegenMultiplier", "100")) / 100;
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(regenerationSettings.getProperty("RaidMpRegenMultiplier", "100")) / 100;
		}
	}

	// *******************************************************************************************
	public static final String	RESPAWNS_FILE	= "./config/main/respawns.properties";
	// *******************************************************************************************
	public static double		RESPAWN_RESTORE_CP;									// Percent CP is restore on respawn
	public static double		RESPAWN_RESTORE_HP;									// Percent HP is restore on respawn
	public static double		RESPAWN_RESTORE_MP;									// Percent MP is restore on respawn
	public static boolean		RESPAWN_RANDOM_ENABLED;								// Allow randomizing of the respawn point in towns.
	public static int			RESPAWN_RANDOM_MAX_OFFSET;								// The maximum offset from the base respawn point to allow.
	public static double		RAID_MINION_RESPAWN_TIMER;								// Raid Boss Minin Spawn Timer
	public static float			RAID_MIN_RESPAWN_MULTIPLIER;							// Mulitplier for Raid boss minimum time respawn
	public static float			RAID_MAX_RESPAWN_MULTIPLIER;							// Mulitplier for Raid boss maximum time respawn

	// *******************************************************************************************
	private static final class RespawnsConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/respawns";
		}

		@Override
		protected void loadImpl(L2Properties respawnsSettings)
		{
			/* Amount of HP, MP, and CP is restored */
			RESPAWN_RESTORE_CP = Double.parseDouble(respawnsSettings.getProperty("RespawnRestoreCP", "0")) / 100;
			RESPAWN_RESTORE_HP = Double.parseDouble(respawnsSettings.getProperty("RespawnRestoreHP", "70")) / 100;
			RESPAWN_RESTORE_MP = Double.parseDouble(respawnsSettings.getProperty("RespawnRestoreMP", "70")) / 100;

			RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(respawnsSettings.getProperty("RespawnRandomInTown", "False"));
			RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(respawnsSettings.getProperty("RespawnRandomMaxOffset", "50"));

			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(respawnsSettings.getProperty("RaidMinionRespawnTime", "300000"));

			RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(respawnsSettings.getProperty("RaidMinRespawnMultiplier", "1.0"));
			RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(respawnsSettings.getProperty("RaidMaxRespawnMultiplier", "1.0"));
		}
	}

	// *******************************************************************************************
	public static final String	PARTY_FILE	= "./config/main/party.properties";
	// *******************************************************************************************
	public static String		PARTY_XP_CUTOFF_METHOD;						// Define Party XP cutoff point method - Possible values: level and
	// percentage
	public static int			PARTY_XP_CUTOFF_LEVEL;							// Define the cutoff point value for the "level" method
	public static double		PARTY_XP_CUTOFF_PERCENT;						// Define the cutoff point value for the "percentage" method
	public static int			MAX_PARTY_LEVEL_DIFFERENCE;					// Maximum level difference between party members in levels

	public static int			ALT_PARTY_RANGE;
	public static int			ALT_PARTY_RANGE2;

	// *******************************************************************************************
	private static final class PartyConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/party";
		}

		@Override
		protected void loadImpl(L2Properties partySettings)
		{
			/* Defines some Party XP related values */
			PARTY_XP_CUTOFF_METHOD = partySettings.getProperty("PartyXpCutoffMethod", "percentage");
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(partySettings.getProperty("PartyXpCutoffPercent", "3."));
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(partySettings.getProperty("PartyXpCutoffLevel", "30"));
			MAX_PARTY_LEVEL_DIFFERENCE = Integer.parseInt(partySettings.getProperty("PartyMaxLevelDifference", "20"));

			ALT_PARTY_RANGE = Integer.parseInt(partySettings.getProperty("AltPartyRange", "1600"));
			ALT_PARTY_RANGE2 = Integer.parseInt(partySettings.getProperty("AltPartyRange2", "1400"));
		}
	}

	// *******************************************************************************************
	public static final String			OPTIONS_FILE			= "./config/main/options.properties";
	// *******************************************************************************************
	public static boolean				TEST_KNOWNLIST			= false;								// Internal properties for developers tests only
	public static boolean				SERVER_PVP;													// Is PvP combat enabled? [NO CORE SUPPORT]
	public static int					SERVER_AGE_LIM;												// Server age limitation
	public static boolean				SERVER_GMONLY;													// Set the server as GM only at startup?
	public static boolean				SERVER_BIT_1;													// UNK
	public static boolean				SERVER_BIT_3;													// UNK/HideName
	public static boolean				SERVER_LIST_BRACKET;											// Display [] in front of server name

	public static int					THREAD_POOL_SIZE;

	public static boolean				AUTODELETE_INVALID_QUEST_DATA;									// Auto-delete invalid quest data ?

	public static Pattern[]				FILTER_LIST				= new Pattern[0];
	public static int					AUTODESTROY_ITEM_AFTER;										// Time after which item will auto-destroy
	public static int					HERB_AUTO_DESTROY_TIME;										// Auto destroy herb time
	public static final Set<Integer>	LIST_PROTECTED_ITEMS	= new L2FastSet<Integer>();			// List of items that will not be destroyed
	public static int					CHAR_STORE_INTERVAL;											// Interval that the gameserver will update and store character information
	public static boolean				UPDATE_ITEMS_ON_CHAR_STORE;									// Update items owned by this char when storing the char on DB
	public static boolean				LAZY_ITEMS_UPDATE;												// Update items only when strictly necessary

	/**
	 * This is setting of experimental Client <--> Server Player coordinates
	 * synchronization<br>
	 * <b><u>Values :</u></b> <li>0 - no synchronization at all</li> <li>1 -
	 * parcial synchronization Client --> Server only * using this option it is
	 * difficult for players to bypass obstacles</li> <li>2 - parcial
	 * synchronization Server --> Client only</li> <li>3 - full synchronization
	 * Client <--> Server</li> <li>-1 - Old system: will synchronize Z only</li>
	 */
	public static int					COORD_SYNCHRONIZE;
	public static boolean				RESTORE_PLAYER_INSTANCE;
	public static boolean				ALLOW_SUMMON_TO_INSTANCE;
	public static int					DELETE_DAYS;
	public static int					MAX_DRIFT_RANGE;												// Maximum range mobs can randomly go from spawn point

	public static int					SOCIAL_TIME;													// Flood protector delay between socials
	public static boolean				LOG_CHAT;														// Logging Chat Window
	public static boolean				LOG_ITEMS;
	public static boolean				GM_AUDIT;
	public static int					ZONE_TOWN;														// Zone Setting
	public static int					MIN_NPC_ANIMATION;												// random animation interval
	public static int					MAX_NPC_ANIMATION;
	public static int					MIN_MONSTER_ANIMATION;
	public static int					MAX_MONSTER_ANIMATION;
	public static boolean				SHOW_MONSTER_LVL;												// Show L2Monster level and aggro ?
	public static boolean				CHECK_SKILLS_ON_ENTER;											// Skill Tree check on EnterWorld
	public static final Set<Integer>	ALLOWED_SKILLS_LIST		= new L2FastSet<Integer>();
	public static boolean				ONLY_GM_ITEMS_FREE;											// Only GM buy items for free

	public static boolean				SERVER_LIST_CLOCK;												// Displays a clock next to the server name ?
	public static boolean				SERVER_LIST_TESTSERVER;										// Display test server in the list of servers ?

	public static boolean				ENTERWORLD_QUEUING;
	public static int					ENTERWORLD_TICK;
	public static int					ENTERWORLD_PPT;

	public static int					MERCENARY_SAVING_DELAY;

	public static int					RETARGET_BLOCKING_PERIOD;

	// *******************************************************************************************

	public static boolean				OPTIMIZE_DATABASE;

	public static String				HTML_CACHE_FILE;

	public static boolean				REBUILD_HTML_CACHE_ON_BOOT;

	public static String				HTML_ENCODING;

	public static boolean				BAN_DUPLICATE_ITEM_OWNER;

	public static boolean				STORE_UI_SETTINGS;

	public static boolean				ALLOW_KEYBOARD_MOVEMENT;

	// *******************************************************************************************
	private static final class OptionsConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/options";
		}

		@Override
		protected void loadImpl(L2Properties optionsSettings)
		{
			SERVER_LIST_TESTSERVER = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "false"));
			SERVER_LIST_BRACKET = Boolean.parseBoolean(optionsSettings.getProperty("ServerListBrackets", "false"));
			SERVER_LIST_CLOCK = Boolean.parseBoolean(optionsSettings.getProperty("ServerListClock", "false"));

			SERVER_LIST_BRACKET = Boolean.parseBoolean(optionsSettings.getProperty("ServerListBrackets", "false"));
			SERVER_BIT_1 = Boolean.parseBoolean(optionsSettings.getProperty("ServerB1UNK", "false"));
			SERVER_BIT_3 = !Boolean.parseBoolean(optionsSettings.getProperty("ServerShowName", "true"));
			SERVER_GMONLY = Boolean.parseBoolean(optionsSettings.getProperty("ServerGMOnly", "false"));
			SERVER_PVP = Boolean.parseBoolean(optionsSettings.getProperty("ServerPvPEnabled", "true"));
			SERVER_AGE_LIM = Integer.parseInt(optionsSettings.getProperty("ServerAgeLimitation", "15"));

			CHAR_STORE_INTERVAL = Integer.parseInt(optionsSettings.getProperty("CharacterDataStoreInterval", "15"));
			UPDATE_ITEMS_ON_CHAR_STORE = Boolean.parseBoolean(optionsSettings.getProperty("UpdateItemsOnCharStore", "false"));
			LAZY_ITEMS_UPDATE = Boolean.parseBoolean(optionsSettings.getProperty("LazyItemsUpdate", "false"));

			COORD_SYNCHRONIZE = Integer.parseInt(optionsSettings.getProperty("CoordSynchronize", "-1"));

			RESTORE_PLAYER_INSTANCE = Boolean.parseBoolean(optionsSettings.getProperty("RestorePlayerInstance", "False"));
			ALLOW_SUMMON_TO_INSTANCE = Boolean.parseBoolean(optionsSettings.getProperty("AllowSummonToInstance", "True"));

			SOCIAL_TIME = Integer.parseInt(optionsSettings.getProperty("SocialTime", "26"));

			LOG_CHAT = Boolean.parseBoolean(optionsSettings.getProperty("LogChat", "false"));
			LOG_ITEMS = Boolean.parseBoolean(optionsSettings.getProperty("LogItems", "false"));

			GM_AUDIT = Boolean.parseBoolean(optionsSettings.getProperty("GMAudit", "False"));

			ZONE_TOWN = Integer.parseInt(optionsSettings.getProperty("ZoneTown", "0"));

			MAX_DRIFT_RANGE = Integer.parseInt(optionsSettings.getProperty("MaxDriftRange", "300"));

			MIN_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MinNPCAnimation", "10"));
			MAX_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MaxNPCAnimation", "20"));
			MIN_MONSTER_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MinMonsterAnimation", "5"));
			MAX_MONSTER_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MaxMonsterAnimation", "20"));

			SHOW_MONSTER_LVL = Boolean.parseBoolean(optionsSettings.getProperty("ShowMonsterLevel", "False"));

			AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(optionsSettings.getProperty("AutoDeleteInvalidQuestData", "False"));

			final int baseThreadPoolSize = Integer.parseInt(optionsSettings.getProperty("BaseThreadPoolSize", "2"));
			final int extraThreadPerCore = Integer.parseInt(optionsSettings.getProperty("ExtraThreadPerCore", "4"));

			THREAD_POOL_SIZE = baseThreadPoolSize + Runtime.getRuntime().availableProcessors() * extraThreadPerCore;

			DELETE_DAYS = Integer.parseInt(optionsSettings.getProperty("DeleteCharAfterDays", "7"));

			ONLY_GM_ITEMS_FREE = Boolean.parseBoolean(optionsSettings.getProperty("OnlyGMItemsFree", "True"));

			// ---------------------------------------------------
			// Configuration values not found in config files
			// ---------------------------------------------------

			CHECK_SKILLS_ON_ENTER = Boolean.parseBoolean(optionsSettings.getProperty("CheckSkillsOnEnter", "false"));

			String ALLOWED_SKILLS = optionsSettings.getProperty("AllowedSkills", ""); // List of Skills that are allowed for all Classes if CHECK_SKILLS_ON_ENTER = true
			ALLOWED_SKILLS_LIST.clear();
			for (String id : StringUtils.split(ALLOWED_SKILLS, ","))
			{
				ALLOWED_SKILLS_LIST.add(Integer.parseInt(id.trim()));
			}

			ENTERWORLD_QUEUING = Boolean.parseBoolean(optionsSettings.getProperty("DiscreteStarterPackets", "false"));
			ENTERWORLD_TICK = Integer.parseInt(optionsSettings.getProperty("StarterPacketTick", "100"));
			ENTERWORLD_PPT = Integer.parseInt(optionsSettings.getProperty("StarterPacketsPerTick", "8"));

			MERCENARY_SAVING_DELAY = Integer.parseInt(optionsSettings.getProperty("MercenaryPosUpdateDelay", "90000"));

			RETARGET_BLOCKING_PERIOD = Integer.parseInt(optionsSettings.getProperty("CannotRetargetFor", "400"));

			OPTIMIZE_DATABASE = Boolean.parseBoolean(optionsSettings.getProperty("OptimizeDatabaseTables", "True"));

			HTML_CACHE_FILE = optionsSettings.getProperty("HtmlCacheFile", "./html.cache");

			REBUILD_HTML_CACHE_ON_BOOT = Boolean.parseBoolean(optionsSettings.getProperty("RebuildHtmlCacheOnBoot", "False"));

			HTML_ENCODING = optionsSettings.getProperty("HtmlEncoding", "UTF-8");

			BAN_DUPLICATE_ITEM_OWNER = Boolean.parseBoolean(optionsSettings.getProperty("BanDuplicateItemOwner", "False"));

			STORE_UI_SETTINGS = Boolean.parseBoolean(optionsSettings.getProperty("StoreCharUiSettings", "True"));

			ALLOW_KEYBOARD_MOVEMENT = Boolean.parseBoolean(optionsSettings.getProperty("AllowKeyboardMovement", "True"));
		}
	}

	// *******************************************************************************************
	public static final String	DROPS_FILE	= "./config/main/drops.properties";
	// *******************************************************************************************
	public static boolean		DESTROY_DROPPED_PLAYER_ITEM;					// Auto destroy nonequipable items dropped by players
	public static boolean		DESTROY_PLAYER_INVENTORY_DROP;					// Auto destroy items dropped by players from inventory
	public static boolean		DESTROY_EQUIPABLE_PLAYER_ITEM;					// Auto destroy equipable items dropped by players
	public static boolean		SAVE_DROPPED_ITEM;								// Save items on ground for restoration on server restart
	public static boolean		EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;			// Empty table ItemsOnGround after load all items
	public static int			SAVE_DROPPED_ITEM_INTERVAL;					// Time interval to save into db items on ground
	public static boolean		CLEAR_DROPPED_ITEM_TABLE;						// Clear all items stored in ItemsOnGround table
	public static boolean		MULTIPLE_ITEM_DROP;							// Accept multi-items drop ?
	public static boolean		DEEPBLUE_DROP_RULES;							// Deep Blue Mobs' Drop Rules Enabled
	public static boolean		DEEPBLUE_DROP_RULES_RAID;						// Deep Blue Mobs' Drop Rules Enabled

	// *******************************************************************************************
	private static final class DropsConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/drops";
		}

		@Override
		protected void loadImpl(L2Properties dropsSettings)
		{
			AUTODESTROY_ITEM_AFTER = Integer.parseInt(dropsSettings.getProperty("AutoDestroyDroppedItemAfter", "600")) * 1000;
			HERB_AUTO_DESTROY_TIME = Integer.parseInt(dropsSettings.getProperty("AutoDestroyHerbTime", "60")) * 1000;
			String PROTECTED_ITEMS = dropsSettings.getProperty("ListOfProtectedItems", "0");
			LIST_PROTECTED_ITEMS.clear();
			for (String id : PROTECTED_ITEMS.trim().split(","))
			{
				LIST_PROTECTED_ITEMS.add(Integer.parseInt(id.trim()));
			}
			DESTROY_DROPPED_PLAYER_ITEM = Boolean.parseBoolean(dropsSettings.getProperty("DestroyPlayerDroppedItem", "false"));
			DESTROY_PLAYER_INVENTORY_DROP = Boolean.parseBoolean(dropsSettings.getProperty("DestroyPlayerInventoryDrop", "false"));
			DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.parseBoolean(dropsSettings.getProperty("DestroyEquipableItem", "false"));
			SAVE_DROPPED_ITEM = Boolean.parseBoolean(dropsSettings.getProperty("SaveDroppedItem", "false"));
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.parseBoolean(dropsSettings.getProperty("EmptyDroppedItemTableAfterLoad", "false"));
			SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(dropsSettings.getProperty("SaveDroppedItemInterval", "0")) * 60000;
			CLEAR_DROPPED_ITEM_TABLE = Boolean.parseBoolean(dropsSettings.getProperty("ClearDroppedItemTable", "false"));

			MULTIPLE_ITEM_DROP = Boolean.parseBoolean(dropsSettings.getProperty("MultipleItemDrop", "True"));

			DEEPBLUE_DROP_RULES = Boolean.parseBoolean(dropsSettings.getProperty("UseDeepBlueDropRules", "True"));
			DEEPBLUE_DROP_RULES_RAID = Boolean.parseBoolean(dropsSettings.getProperty("UseDeepBlueDropRulesRaid", "True"));
		}
	}

	// *******************************************************************************************
	public static final String	GRID_FILE	= "./config/main/grid.properties";
	// *******************************************************************************************
	public static boolean		GRIDS_ALWAYS_ON;								// Grid Options
	public static int			GRID_NEIGHBOR_TURNON_TIME;						// Grid Options
	public static int			GRID_NEIGHBOR_TURNOFF_TIME;					// Grid Options

	// *******************************************************************************************
	private static final class GridConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/grid";
		}

		@Override
		protected void loadImpl(L2Properties gridSettings)
		{
			GRIDS_ALWAYS_ON = Boolean.parseBoolean(gridSettings.getProperty("GridsAlwaysOn", "False"));
			GRID_NEIGHBOR_TURNON_TIME = Integer.parseInt(gridSettings.getProperty("GridNeighborTurnOnTime", "1"));
			GRID_NEIGHBOR_TURNOFF_TIME = Integer.parseInt(gridSettings.getProperty("GridNeighborTurnOffTime", "90"));
		}
	}

	// *******************************************************************************************
	public static final String			AUCTION_FILE				= "./config/main/auction.properties";
	// *******************************************************************************************
	public static int					AUCTION_SPECIAL_CURRENCY;
	public static String				AUCTION_SPECIAL_CURRENCY_ICON;
	public static String				AUCTION_EXCLUDED_ITEMS;
	public static final Set<Integer>	AUCTION_EXCLUDED_ITEMS_LIST	= new L2FastSet<Integer>();
	public static String				AUCTION_INCLUDED_ITEMS;
	public static final Set<Integer>	AUCTION_INCLUDED_ITEMS_LIST	= new L2FastSet<Integer>();

	public static boolean				ALT_ITEM_AUCTION_ENABLED;
	public static int					ALT_ITEM_AUCTION_EXPIRED_AFTER;
	public static long					ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;

	// *******************************************************************************************
	private static final class AuctionConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/auction";
		}

		@Override
		protected void loadImpl(L2Properties auctionSettings)
		{
			AUCTION_SPECIAL_CURRENCY = Integer.parseInt(auctionSettings.getProperty("AuctionSpecialCurrency", "8575"));
			AUCTION_SPECIAL_CURRENCY_ICON = auctionSettings.getProperty("AuctionSpecialCurrencyIcon", "etc_box_of_adventure_3_i00");
			AUCTION_EXCLUDED_ITEMS = auctionSettings.getProperty("AuctionExcludedItems", "57,5575");
			AUCTION_EXCLUDED_ITEMS_LIST.clear();
			for (String id : AUCTION_EXCLUDED_ITEMS.trim().split(","))
			{
				AUCTION_EXCLUDED_ITEMS_LIST.add(Integer.parseInt(id.trim()));
			}

			AUCTION_INCLUDED_ITEMS = auctionSettings.getProperty("AuctionIncludedItems", "8575");
			AUCTION_INCLUDED_ITEMS_LIST.clear();
			for (String id : AUCTION_INCLUDED_ITEMS.trim().split(","))
			{
				AUCTION_INCLUDED_ITEMS_LIST.add(Integer.parseInt(id.trim()));
			}

			ALT_ITEM_AUCTION_ENABLED = Boolean.valueOf(auctionSettings.getProperty("AltItemAuctionEnabled", "True"));
			ALT_ITEM_AUCTION_EXPIRED_AFTER = Integer.valueOf(auctionSettings.getProperty("AltItemAuctionExpiredAfter", "14"));
			ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID = 1000 * (long) Integer.valueOf(auctionSettings.getProperty("AltItemAuctionTimeExtendsOnBid", "0"));
		}
	}

	// *******************************************************************************************
	public static final String	DEVELOPER_FILE	= "./config/administration/developer.properties";
	// *******************************************************************************************
	public static boolean		ASSERT;															// Enable/disable assertions
	public static boolean		DEVELOPER;															// Enable/disable DEVELOPER TREATMENT
	public static boolean		ALT_DEV_NO_SPAWNS;													// Alt Settings for devs
	public static boolean		ALT_DEV_NO_HTMLS;													// Alt Settings for devs
	public static boolean		ENABLE_JYTHON_SHELL;												// JythonShell
	public static boolean		ALT_DEV_VERIFY_NPC_SKILLS;											// Alt Settings for devs
	public static int			DEADLOCKCHECK_INTERVAL;

	// *******************************************************************************************
	private static final class DeveloperConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "administration/developer";
		}

		@Override
		protected void loadImpl(L2Properties developerSettings)
		{
			ASSERT = Boolean.parseBoolean(developerSettings.getProperty("Assert", "false"));
			DEVELOPER = Boolean.parseBoolean(developerSettings.getProperty("Developer", "false"));

			ALT_DEV_NO_SPAWNS = Boolean.parseBoolean(developerSettings.getProperty("AltDevNoSpawns", "False"));
			ALT_DEV_NO_HTMLS = Boolean.parseBoolean(developerSettings.getProperty("AltDevNoHtmls", "False"));
			ENABLE_JYTHON_SHELL = Boolean.parseBoolean(developerSettings.getProperty("EnableJythonShell", "False"));
			ALT_DEV_VERIFY_NPC_SKILLS = Boolean.parseBoolean(developerSettings.getProperty("AltDevVerifyNpcSkills", "False"));
			DEADLOCKCHECK_INTERVAL = Integer.parseInt(developerSettings.getProperty("DeadLockCheck", "10000"));
		}
	}

	// *******************************************************************************************
	public static final String	PERMISSIONS_FILE	= "./config/main/permissions.properties";
	// *******************************************************************************************

	public static boolean		ALLOW_FISHING;
	public static boolean		ALLOW_GUARDS;													// Allow guards against aggressive monsters

	public static boolean		ALLOW_DISCARDITEM;
	public static boolean		ALLOW_WEAR;
	public static int			WEAR_DELAY;
	public static int			WEAR_PRICE;
	public static boolean		ALLOW_LOTTERY;
	public static boolean		ALLOW_RACE;
	public static boolean		ALLOW_WATER;
	public static boolean		ALLOW_RENTPET;
	public static boolean		ALLOW_BOAT;
	public static boolean		ALLOW_CURSED_WEAPONS;											// Allow cursed weapons ?
	public static boolean		ALLOW_NPC_WALKERS;												// WALKER NPC
	public static boolean		ALLOW_PET_WALKERS;

	public static boolean		ALLOW_REFUND;
	public static boolean		ALLOW_MAIL;
	public static boolean		ALLOW_ATTACHMENTS;

	// *******************************************************************************************
	private static final class PermissionsConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/permissions";
		}

		@Override
		protected void loadImpl(L2Properties permissionsSettings)
		{
			ALLOW_WEAR = Boolean.parseBoolean(permissionsSettings.getProperty("AllowWear", "False"));
			WEAR_DELAY = Integer.parseInt(permissionsSettings.getProperty("WearDelay", "5"));
			WEAR_PRICE = Integer.parseInt(permissionsSettings.getProperty("WearPrice", "10"));
			ALLOW_LOTTERY = Boolean.parseBoolean(permissionsSettings.getProperty("AllowLottery", "False"));
			ALLOW_RACE = Boolean.parseBoolean(permissionsSettings.getProperty("AllowRace", "False"));
			ALLOW_WATER = Boolean.parseBoolean(permissionsSettings.getProperty("AllowWater", "True"));
			ALLOW_RENTPET = Boolean.parseBoolean(permissionsSettings.getProperty("AllowRentPet", "False"));
			ALLOW_DISCARDITEM = Boolean.parseBoolean(permissionsSettings.getProperty("AllowDiscardItem", "True"));
			ALLOW_FISHING = Boolean.parseBoolean(permissionsSettings.getProperty("AllowFishing", "True"));
			ALLOW_BOAT = Boolean.parseBoolean(permissionsSettings.getProperty("AllowBoat", "False"));
			ALLOW_NPC_WALKERS = Boolean.parseBoolean(permissionsSettings.getProperty("AllowNpcWalkers", "True"));
			ALLOW_PET_WALKERS = Boolean.parseBoolean(permissionsSettings.getProperty("AllowPetWalkers", "False"));
			ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(permissionsSettings.getProperty("AllowCursedWeapons", "False"));
			ALLOW_GUARDS = Boolean.parseBoolean(permissionsSettings.getProperty("AllowGuards", "False"));

			ALLOW_REFUND = Boolean.parseBoolean(permissionsSettings.getProperty("AllowRefund", "False"));
			ALLOW_MAIL = Boolean.parseBoolean(permissionsSettings.getProperty("AllowMail", "False"));
			ALLOW_ATTACHMENTS = Boolean.parseBoolean(permissionsSettings.getProperty("AllowAttachments", "False"));
		}
	}

	// *******************************************************************************************
	public static final String	CHAT_CONFIG	= "./config/chat/chat.properties";
	// *******************************************************************************************
	public static ChatMode		DEFAULT_GLOBAL_CHAT;							// Global chat state
	public static int			GLOBAL_CHAT_TIME;
	public static ChatMode		DEFAULT_TRADE_CHAT;							// Trade chat state
	public static int			TRADE_CHAT_TIME;
	public static boolean		REGION_CHAT_ALSO_BLOCKED;

	public static enum ChatMode
	{
		GLOBAL, REGION, GM, OFF
	}

	// *******************************************************************************************
	private static final class ChatConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "chat/chat";
		}

		@Override
		protected void loadImpl(L2Properties chatSettings)
		{
			DEFAULT_GLOBAL_CHAT = ChatMode.valueOf(chatSettings.getProperty("GlobalChat", "REGION").toUpperCase());
			GLOBAL_CHAT_TIME = Integer.parseInt(chatSettings.getProperty("GlobalChatTime", "1"));
			DEFAULT_TRADE_CHAT = ChatMode.valueOf(chatSettings.getProperty("TradeChat", "REGION").toUpperCase());
			TRADE_CHAT_TIME = Integer.parseInt(chatSettings.getProperty("TradeChatTime", "1"));
			REGION_CHAT_ALSO_BLOCKED = Boolean.parseBoolean(chatSettings.getProperty("RegionChatAlsoBlocked", "false"));
		}
	}

	// *******************************************************************************************
	public static final String	OFFLINE_TRADE_FILE	= "./config/mods/offline_trade.properties";
	// *******************************************************************************************
	// *******************************************************************************************
	// offline trade
	public static boolean		ALLOW_OFFLINE_TRADE;
	public static boolean		ALLOW_OFFLINE_TRADE_CRAFT;
	public static boolean		ALLOW_OFFLINE_TRADE_COLOR_NAME;
	public static int			OFFLINE_TRADE_COLOR_NAME;
	public static boolean		ALLOW_OFFLINE_TRADE_PROTECTION;
	public static long			OFFLINE_TRADE_PRICE;
	public static int			OFFLINE_TRADE_PRICE_ITEM;
	public static boolean		ENABLE_OFFLINE_TRADERS_RESTORE;

	// *******************************************************************************************
	private static final class OfflineTradeConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/offline_trade";
		}

		@Override
		protected void loadImpl(L2Properties otSettings)
		{
			// *******************************************************************************************
			// offline trade
			ALLOW_OFFLINE_TRADE = Boolean.parseBoolean(otSettings.getProperty("AllowOfflineTrade", "false"));
			ALLOW_OFFLINE_TRADE_CRAFT = Boolean.parseBoolean(otSettings.getProperty("AllowOfflineTradeCraft", "true"));
			ALLOW_OFFLINE_TRADE_COLOR_NAME = Boolean.parseBoolean(otSettings.getProperty("AllowOfflineTradeColorName", "true"));
			OFFLINE_TRADE_COLOR_NAME = Integer.decode("0x" + otSettings.getProperty("OfflineTradeColorName", "999999"));
			ALLOW_OFFLINE_TRADE_PROTECTION = Boolean.parseBoolean(otSettings.getProperty("AllowOfflineTradeProtection", "true"));
			ENABLE_OFFLINE_TRADERS_RESTORE = Boolean.parseBoolean(otSettings.getProperty("EnableOfflineTradersRestore", "false"));
			OFFLINE_TRADE_PRICE = Long.parseLong(otSettings.getProperty("OfflineTradePrice", "0"));
			OFFLINE_TRADE_PRICE_ITEM = Integer.parseInt(otSettings.getProperty("OfflineTradePriceItem", "57"));
		}
	}

	// *******************************************************************************************
	public static final String	COMMUNITY_FILE	= "./config/main/community_board.properties";
	// *******************************************************************************************											// Show player(s) in jail in CB ?
	public static boolean		ALLOW_COMMUNITY_BOARD;
	public static int			MIN_PLAYER_LVL_FOR_FORUM;
	public static int			MIN_CLAN_LVL_FOR_FORUM;

	// *******************************************************************************************
	private static final class CommunityConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/community_board";
		}

		@Override
		protected void loadImpl(L2Properties cbSettings)
		{
			ALLOW_COMMUNITY_BOARD = Boolean.parseBoolean(cbSettings.getProperty("AllowCommunityBoard", "True"));
			MIN_PLAYER_LVL_FOR_FORUM = Integer.parseInt(cbSettings.getProperty("MinimumPlayerLevelForForum", "1"));
			MIN_CLAN_LVL_FOR_FORUM = Integer.parseInt(cbSettings.getProperty("MinimumClanLevelForForum", "1"));
		}
	}

	// *******************************************************************************************
	public static final String	DB_BACKUP_FILE	= "./config/main/database_backup.properties";
	// *******************************************************************************************
	public static boolean		DATABASE_BACKUP_MAKE_BACKUP_ON_STARTUP;
	public static boolean		DATABASE_BACKUP_MAKE_BACKUP_ON_SHUTDOWN;
	public static String		DATABASE_BACKUP_DATABASE_NAME;
	public static String		DATABASE_BACKUP_SAVE_PATH;
	public static boolean		DATABASE_BACKUP_COMPRESSION;
	public static String		DATABASE_BACKUP_MYSQLDUMP_PATH;

	// *******************************************************************************************
	private static final class DBBackupConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/database_backup";
		}

		@Override
		protected void loadImpl(L2Properties dbbackupSettings)
		{
			// *******************************************************************************************
			// Database Backup Settings
			DATABASE_BACKUP_MAKE_BACKUP_ON_STARTUP = Boolean.parseBoolean(dbbackupSettings.getProperty("DatabaseBackupMakeBackupOnStartup", "False"));
			DATABASE_BACKUP_MAKE_BACKUP_ON_SHUTDOWN = Boolean.parseBoolean(dbbackupSettings.getProperty("DatabaseBackupMakeBackupOnShutdown", "False"));
			DATABASE_BACKUP_DATABASE_NAME = dbbackupSettings.getProperty("DatabaseBackupDatabaseName", "L2Emu_DB");
			DATABASE_BACKUP_SAVE_PATH = dbbackupSettings.getProperty("DatabaseBackupSavePath", "/backup/database/");
			DATABASE_BACKUP_COMPRESSION = Boolean.parseBoolean(dbbackupSettings.getProperty("DatabaseBackupCompression", "True"));
			DATABASE_BACKUP_MYSQLDUMP_PATH = dbbackupSettings.getProperty("DatabaseBackupMysqldumpPath", ".");
		}
	}

	// *******************************************************************************************
	public static final String	ALT_SETTINGS_FILE					= "./config/main/altgame.properties";
	// *******************************************************************************************
	public static int			ALT_DEFAULT_RESTARTTOWN;													// Set alternative default restarttown
	public static double		ALT_WEIGHT_LIMIT;															// Alternative game weight limit multiplier - default 1
	public static int			ALT_MINIMUM_FALL_HEIGHT;													// Minimum Height(Z) that a character needs to fall, in
	// order for it to be considered a fall.
	public static boolean		ALT_DISABLE_RAIDBOSS_PETRIFICATION;										// Disable Raidboss Petrification
	public static boolean		ALT_GAME_TIREDNESS;														// Alternative game - use tiredness, instead of CP
	public static boolean		ALT_GAME_SHIELD_BLOCKS;													// Alternative shield defence
	public static int			ALT_PERFECT_SHLD_BLOCK;													// Alternative Perfect shield defence rate
	public static boolean		ALT_MOB_AGGRO_IN_PEACEZONE;												// -
	public static boolean		ALT_ATTACKABLE_NPCS;
	// target
	public static boolean		ALT_GAME_DELEVEL;															// Alternative gameing - loss of XP on death
	public static boolean		ALT_GAME_MAGICFAILURES;													// Alternative gameing - magic dmg failures
	public static boolean		ALT_GAME_FREE_TELEPORT;													// Alternative gameing - magic dmg failures
	public static float			ALT_GAME_FREE_TELEPORT_LEVEL;												// Alternative gaming - allow free teleporting around the world.
	public static boolean		ALT_RECOMMEND;																// Disallow recommend character twice or more a day ?
	public static boolean		ALT_GAME_SUBCLASS_WITHOUT_QUESTS;											// Alternative gaming - allow sub-class addition without
	public static boolean		ALT_GAME_SUBCLASS_EVERYWHERE;
	// quest completion.
	public static int			ALT_MAX_SUBCLASS;															// Allow to change max number of subclasses
	public static byte			ALT_MAX_SUBCLASS_LEVEL;
	public static boolean		ALT_GAME_VIEWNPC;															// View npc stats/drop by shift-cliking it for nongm-players
	public static boolean		ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE	= true;								// Alternative gaming - all new characters always are newbies.
	public static boolean		ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;										// Alternative gaming - clan members with see privilege can
	// also withdraw from clan warehouse.

	public static boolean		ALT_STRICT_HERO_SYSTEM;													// Strict Hero Mode
	public static boolean		ALT_STRICT_SEVENSIGNS;														// Strict Seven Signs
	public static int			ALT_DIFF_CUTOFF;															// No exp cutoff
	public static boolean		ALT_SPAWN_WYVERN_MANAGER;
	public static int			ALT_MANAGER_CRYSTAL_COUNT;
	public static boolean		ALT_SPAWN_SIEGE_GUARD;														// Config for spawn siege guards
	public static int			ALT_TIME_IN_A_DAY_OF_OPEN_A_DOOR;
	public static int			ALT_TIME_OF_OPENING_A_DOOR;
	public static int			ALT_TIMELIMITOFINVADE;														//Time limit of invade to lair of bosses after server restarted
	public static int			ALT_CHANCE_BREAK;															// Chance For Soul Crystal to Break
	public static int			ALT_CHANCE_LEVEL;															// Chance For Soul Crystal to Level
	public static int			ALT_PLAYER_PROTECTION_LEVEL;												// Player Protection Level
	public static boolean		ALT_AUTO_LEARN_SKILLS;														// Config for Auto Learn Skills
	public static boolean		ALT_AUTO_LEARN_DIVINE_INSPIRATION;											// Alternative auto skill learning for divine inspiration (+4 max buff count)
	public static boolean		ALT_GRADE_PENALTY;															// Disable Grade penalty
	//public static boolean				ALT_FAIL_FAKEDEATH;														// Config for Fake Death Fail Feature
	public static boolean		ALT_FLYING_WYVERN_IN_SIEGE;												// Config for Wyvern enable flying in siege **/
	public static float			ALT_GAME_SUMMON_PENALTY_RATE;												// Alternative game summon penalty

	public static boolean		ALT_ITEM_SKILLS_NOT_INFLUENCED;
	public static int			ALT_AUTOCHAT_DELAY;
	public static boolean		ALT_SPECIAL_PETS_FOR_ALL;
	public static int			ALT_INVENTORY_MAXIMUM_PET;

	public static boolean		ALT_SHOW_FULL_HENNA_LIST;
	public static boolean		ALT_SHOW_RESTART_TOWN;
	public static boolean		ALT_AUTO_FISHING_SHOT;
	public static boolean		ALT_ENABLE_DIMENSIONAL_MERCHANTS;
	public static boolean		ALT_ENABLE_NEWBIE_COUPONS;
	public static boolean		ALT_MONSTER_HAVE_ENCHANTED_WEAPONS;

	public static boolean		ALT_KEEP_ITEM_BUFFS;

	public static boolean		ALLOW_NAIA_MULTY_PARTY_INVASION;
	public static int			ALT_NAIA_ROOM_DURATION;

	public static boolean		ALT_ENABLE_EVENT_ITEM_DROP_FOR_BOSSES;

	public static boolean		ALT_DISABLE_TUTORIAL;

	// *******************************************************************************************
	private static final class AltConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/altgame";
		}

		@Override
		protected void loadImpl(L2Properties altSettings)
		{
			ALT_DEFAULT_RESTARTTOWN = Integer.parseInt(altSettings.getProperty("AltDefaultRestartTown", "0"));
			ALT_GAME_TIREDNESS = Boolean.parseBoolean(altSettings.getProperty("AltGameTiredness", "false"));
			ALT_WEIGHT_LIMIT = Double.parseDouble(altSettings.getProperty("AltWeightLimit", "1."));
			ALT_MINIMUM_FALL_HEIGHT = Integer.parseInt(altSettings.getProperty("AltMinimumFallHeight", "400"));
			ALT_ITEM_SKILLS_NOT_INFLUENCED = Boolean.parseBoolean(altSettings.getProperty("AltItemSkillsNotInfluenced", "false"));
			ALT_GAME_DELEVEL = Boolean.parseBoolean(altSettings.getProperty("Delevel", "true"));
			ALT_MOB_AGGRO_IN_PEACEZONE = Boolean.parseBoolean(altSettings.getProperty("AltMobAgroInPeaceZone", "true"));
			ALT_ATTACKABLE_NPCS = Boolean.parseBoolean(altSettings.getProperty("AltAttackableNpcs", "True"));

			ALT_SPAWN_WYVERN_MANAGER = Boolean.parseBoolean(altSettings.getProperty("SpawnWyvernManager", "True"));
			ALT_MANAGER_CRYSTAL_COUNT = Integer.parseInt(altSettings.getProperty("ManagerCrystalCount", "25"));

			ALT_CHANCE_BREAK = Integer.parseInt(altSettings.getProperty("ChanceToBreak", "10"));
			ALT_CHANCE_LEVEL = Integer.parseInt(altSettings.getProperty("ChanceToLevel", "32"));
			//ALT_FAIL_FAKEDEATH = Boolean.parseBoolean(altSettings.getProperty("FailFakeDeath", "true"));
			ALT_FLYING_WYVERN_IN_SIEGE = Boolean.parseBoolean(altSettings.getProperty("AltFlyingWyvernInSiege", "false"));

			ALT_PLAYER_PROTECTION_LEVEL = Integer.parseInt(altSettings.getProperty("AltPlayerProtectionLevel", "0"));
			ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(altSettings.getProperty("AltFreeTeleporting", "False"));
			ALT_GAME_FREE_TELEPORT_LEVEL = Float.parseFloat(altSettings.getProperty("AltFreeTeleportingLevel", "40"));
			ALT_RECOMMEND = Boolean.parseBoolean(altSettings.getProperty("AltRecommend", "False"));
			ALT_GAME_VIEWNPC = Boolean.parseBoolean(altSettings.getProperty("AltGameViewNpc", "False"));
			//ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.parseBoolean(altSettings.getProperty("AltNewCharAlwaysIsNewbie", "False"));
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(altSettings.getProperty("AltMembersCanWithdrawFromClanWH", "False"));

			ALT_GRADE_PENALTY = Boolean.parseBoolean(altSettings.getProperty("GradePenalty", "true"));

			ALT_STRICT_HERO_SYSTEM = Boolean.parseBoolean(altSettings.getProperty("StrictHeroSystem", "True"));
			ALT_STRICT_SEVENSIGNS = Boolean.parseBoolean(altSettings.getProperty("StrictSevenSigns", "True"));

			ALT_SPAWN_SIEGE_GUARD = Boolean.parseBoolean(altSettings.getProperty("SpawnSiegeGuard", "true"));
			ALT_DISABLE_RAIDBOSS_PETRIFICATION = Boolean.parseBoolean(altSettings.getProperty("DisableRaidBossPetrification", "false"));

			ALT_TIME_IN_A_DAY_OF_OPEN_A_DOOR = Integer.parseInt(altSettings.getProperty("TimeInADayOfOpenADoor", "0"));
			ALT_TIME_OF_OPENING_A_DOOR = Integer.parseInt(altSettings.getProperty("TimeOfOpeningADoor", "5"));

			ALT_TIMELIMITOFINVADE = Integer.parseInt(altSettings.getProperty("TimeLimitOfInvade", "1800000"));

			ALT_GAME_SUMMON_PENALTY_RATE = Float.parseFloat(altSettings.getProperty("AltSummonPenaltyRate", "1"));

			ALT_AUTOCHAT_DELAY = Integer.parseInt(altSettings.getProperty("AutoChatDelay", "30000"));
			ALT_SPECIAL_PETS_FOR_ALL = Boolean.parseBoolean(altSettings.getProperty("EverybodyCanUseSpecPets", "false"));
			ALT_INVENTORY_MAXIMUM_PET = Integer.parseInt(altSettings.getProperty("MaximumSlotsForPet", "12"));

			ALT_SHOW_FULL_HENNA_LIST = Boolean.parseBoolean(altSettings.getProperty("AltShowFullHennaList", "false"));
			ALT_SHOW_RESTART_TOWN = Boolean.parseBoolean(altSettings.getProperty("AltShowRestartTown", "false"));
			ALT_AUTO_FISHING_SHOT = Boolean.parseBoolean(altSettings.getProperty("AltAllowAutoFishShot", "false"));
			ALT_ENABLE_DIMENSIONAL_MERCHANTS = Boolean.parseBoolean(altSettings.getProperty("AltEnableDimensionalMerchants", "false"));
			ALT_ENABLE_NEWBIE_COUPONS = Boolean.parseBoolean(altSettings.getProperty("AltEnableNewbieCoupons", "true"));
			ALT_MONSTER_HAVE_ENCHANTED_WEAPONS = Boolean.parseBoolean(altSettings.getProperty("AltEnableEnchantedWeaponOnMonsters", "false"));

			ALT_KEEP_ITEM_BUFFS = Boolean.parseBoolean(altSettings.getProperty("AltKeepItemBuffs", "false"));

			ALLOW_NAIA_MULTY_PARTY_INVASION = Boolean.parseBoolean(altSettings.getProperty("AllowNaiaMultiPartyInvasion", "false"));
			ALT_NAIA_ROOM_DURATION = Integer.parseInt(altSettings.getProperty("AltNaiaRoomDuration", "5"));

			ALT_ENABLE_EVENT_ITEM_DROP_FOR_BOSSES = Boolean.parseBoolean(altSettings.getProperty("AltEnableEventItemDropForBosses", "False"));

			ALT_DISABLE_TUTORIAL = Boolean.parseBoolean(altSettings.getProperty("DisableTutorial", "false"));
		}
	}

	// *******************************************************************************************
	public static final String	CRAFTING_FILE	= "./config/main/crafting.properties";
	// *******************************************************************************************
	public static boolean		ALT_IS_CRAFTING_ENABLED;								// Crafting Enabled?

	public static int			ALT_DWARF_RECIPE_LIMIT;								// Recipebook limits
	public static int			ALT_COMMON_RECIPE_LIMIT;

	public static boolean		ALT_GAME_CREATION;										// Alternative game crafting
	public static double		ALT_GAME_CREATION_SPEED;								// Alternative game crafting speed mutiplier - default 0 (fastest but still not instant)
	public static double		ALT_GAME_CREATION_XP_RATE;								// Alternative game crafting XP rate multiplier - default 1
	public static double		ALT_GAME_CREATION_RARE_XPSP_RATE;
	public static double		ALT_GAME_CREATION_SP_RATE;								// Alternative game crafting SP rate multiplier - default 1
	public static boolean		ALT_BLACKSMITH_USE_RECIPES;							// Alternative setting to blacksmith use of recipes to craft - default true

	public static boolean		ALT_MASTERWORK_CONFIG;
	public static boolean		ALLOW_MASTERWORK;
	public static boolean		ALLOW_CRITICAL_CRAFT;

	// *******************************************************************************************
	private static final class CraftingConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/crafting";
		}

		@Override
		protected void loadImpl(L2Properties craftingSettings)
		{
			ALT_IS_CRAFTING_ENABLED = Boolean.parseBoolean(craftingSettings.getProperty("CraftingEnabled", "true"));

			ALT_DWARF_RECIPE_LIMIT = Integer.parseInt(craftingSettings.getProperty("DwarfRecipeLimit", "50"));
			ALT_COMMON_RECIPE_LIMIT = Integer.parseInt(craftingSettings.getProperty("CommonRecipeLimit", "50"));

			ALT_GAME_CREATION = Boolean.parseBoolean(craftingSettings.getProperty("AltGameCreation", "false"));
			ALT_GAME_CREATION_SPEED = Double.parseDouble(craftingSettings.getProperty("AltGameCreationSpeed", "1"));
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(craftingSettings.getProperty("AltGameCreationXpRate", "1"));
			ALT_GAME_CREATION_RARE_XPSP_RATE = Double.parseDouble(craftingSettings.getProperty("AltGameCreationRareXpSpRate", "2"));
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(craftingSettings.getProperty("AltGameCreationSpRate", "1"));
			ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(craftingSettings.getProperty("AltBlacksmithUseRecipes", "true"));

			ALT_MASTERWORK_CONFIG = Boolean.parseBoolean(craftingSettings.getProperty("AltMasterworkConfig", "False"));
			ALLOW_MASTERWORK = Boolean.parseBoolean(craftingSettings.getProperty("AllowMasterwork", "False"));
			ALLOW_CRITICAL_CRAFT = Boolean.parseBoolean(craftingSettings.getProperty("AllowCriticalCraft", "False"));
		}
	}

	// *******************************************************************************************
	public static final String	SKILLS_FILE	= "./config/main/skills.properties";
	// *******************************************************************************************
	public static boolean		ALT_SP_BOOK_NEEDED;								// Spell Book needed to learn skill
	public static boolean		ALT_LIFE_CRYSTAL_NEEDED;							// Clan Item needed to learn clan skills
	public static boolean		ALT_ES_SP_BOOK_NEEDED;								// Spell Book needet to enchant skill
	public static boolean		DIVINE_SP_BOOK_NEEDED;

	public static float			ALT_PROPHET_BUFF_TIME;
	public static float			ALT_DANCE_TIME;
	public static float			ALT_SONG_TIME;
	public static float			ALT_CUBIC_TIME;
	public static float			ALT_HERO_BUFF_TIME;
	public static float			ALT_NOBLE_BUFF_TIME;
	public static float			ALT_SUMMON_BUFF_TIME;
	public static float			ALT_ORC_BUFF_TIME;
	public static float			ALT_OTHER_BUFF_TIME;
	public static float			ALT_VITALITY_BUFF_TIME;

	public static boolean		ALT_DANCE_MP_CONSUME;
	public static int			ALT_MAX_PATK_SPEED;								// Config for limit physical attack speed
	public static int			ALT_MAX_MATK_SPEED;								// Config for limit magical attack speed

	public static float			ALT_MAGES_PHYSICAL_DAMAGE_MULTI;					// Config for damage multiplies
	public static float			ALT_MAGES_MAGICAL_DAMAGE_MULTI;					// Config for damage multiplies
	public static float			ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;				// Config for damage multiplies
	public static float			ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;					// Config for damage multiplies
	public static float			ALT_PETS_PHYSICAL_DAMAGE_MULTI;					// Config for damage multiplies
	public static float			ALT_PETS_MAGICAL_DAMAGE_MULTI;						// Config for damage multiplies
	public static float			ALT_NPC_PHYSICAL_DAMAGE_MULTI;						// Config for damage multiplies
	public static float			ALT_NPC_MAGICAL_DAMAGE_MULTI;						// Config for damage multiplies

	public static float			ALT_MAGES_PHYSICAL_DEFENSE_MULTI;					// Config for defense multiplies
	public static float			ALT_MAGES_MAGICAL_DEFENSE_MULTI;					// Config for defense multiplies
	public static float			ALT_FIGHTERS_PHYSICAL_DEFENSE_MULTI;				// Config for defense multiplies
	public static float			ALT_FIGHTERS_MAGICAL_DEFENSE_MULTI;				// Config for defense multiplies
	public static float			ALT_PETS_PHYSICAL_DEFENSE_MULTI;					// Config for defense multiplies
	public static float			ALT_PETS_MAGICAL_DEFENSE_MULTI;					// Config for defense multiplies
	public static float			ALT_NPC_PHYSICAL_DEFENSE_MULTI;					// Config for defense multiplies
	public static float			ALT_NPC_MAGICAL_DEFENSE_MULTI;						// Config for defense multiplies

	public static double		RAID_PDAMAGE_MULTIPLIER;							// Multiplier for Raid boss power damage multiplier
	public static double		RAID_MDAMAGE_MULTIPLIER;							// Multiplier for Raid boss magic damage multiplier

	public static double		RAID_PDEFENCE_MULTIPLIER;							// Multiplier for Raid boss power defense multiplier
	public static double		RAID_MDEFENCE_MULTIPLIER;							// Multiplier for Raid boss magic defense multiplier

	public static double		GB_PDAMAGE_MULTIPLIER;								// Multiplier for Grand boss power damage multiplier
	public static double		GB_MDAMAGE_MULTIPLIER;								// Multiplier for Grand boss magic damage multiplier

	public static double		GB_PDEFENCE_MULTIPLIER;							// Multiplier for Grand boss power defense multiplier
	public static double		GB_MDEFENCE_MULTIPLIER;							// Multiplier for Grand boss magic defense multiplier

	public static int			ALT_URN_TEMP_FAIL;									// Config for URN temp fail
	public static int			ALT_BUFFER_HATE;									// Buffer Hate
	public static int			ALT_PCRITICAL_CAP;									// PCritical Cap
	public static int			ALT_MCRITICAL_CAP;									// MCritical Cap
	public static int			ALT_MAX_EVASION;
	public static int			ALT_MAX_RUN_SPEED;									// Runspeed limit
	public static float			ALT_MCRIT_RATE;
	public static float			ALT_MCRIT_PVP_RATE;

	public static double		ALT_POLEARM_DAMAGE_MULTI;
	public static double		ALT_POLEARM_VAMPIRIC_MULTI;

	public static boolean		ALT_GAME_SKILL_LEARN;								// Alternative game skill learning
	public static boolean		ALT_GAME_CANCEL_BOW;								// Cancel attack bow by hit
	public static boolean		ALT_GAME_CANCEL_CAST;								// Cancel cast by hit
	public static int			ALT_BUFFS_MAX_AMOUNT;								// Alternative number of cumulated buff
	public static int			ALT_DANCES_SONGS_MAX_AMOUNT;						// Alternative number of cumulated dances/songs
	public static boolean		DANCE_CANCEL_BUFF;
	public static float			ALT_INSTANT_KILL_EFFECT_2;							// Rate of Instant kill effect 2(CP no change ,HP =1,no kill
	public static boolean		ALT_DAGGER_FORMULA;								// Alternative success rate formulas for skills such
	// dagger/critical skills and blows
	public static float			ALT_ATTACK_DELAY;									// Alternative config for next hit delay
	public static int			ALT_DAGGER_RATE;									// Alternative success rate for dagger blow,MAX value 100
	// (100% rate)
	public static int			ALT_DAGGER_FAIL_RATE;								// Alternative fail rate for dagger blow,MAX value 100 (100%
	// rate)
	public static int			ALT_DAGGER_RATE_BEHIND;							// Alternative increasement to success rate for dagger/critical
	// skills if activeChar is Behind the target
	public static int			ALT_DAGGER_RATE_FRONT;								// Alternative increasement to success rate for

	// dagger/critical skills if activeChar is in Front of

	// *******************************************************************************************
	private static final class SkillsConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/skills";
		}

		@Override
		protected void loadImpl(L2Properties skillsSettings)
		{
			ALT_PROPHET_BUFF_TIME = Float.parseFloat(skillsSettings.getProperty("AltProphetBuffTime", "1."));

			ALT_DANCE_TIME = Float.parseFloat(skillsSettings.getProperty("AltDanceTime", "1."));
			ALT_SONG_TIME = Float.parseFloat(skillsSettings.getProperty("AltSongTime", "1."));
			ALT_CUBIC_TIME = Float.parseFloat(skillsSettings.getProperty("AltCubicTime", "1."));
			ALT_HERO_BUFF_TIME = Float.parseFloat(skillsSettings.getProperty("AltHeroBuffTime", "1."));
			ALT_NOBLE_BUFF_TIME = Float.parseFloat(skillsSettings.getProperty("AltNobleBuffTime", "1."));
			ALT_SUMMON_BUFF_TIME = Float.parseFloat(skillsSettings.getProperty("AltSummonBuffTime", "1."));
			ALT_ORC_BUFF_TIME = Float.parseFloat(skillsSettings.getProperty("AltOrcBuffTime", "1."));
			ALT_OTHER_BUFF_TIME = Float.parseFloat(skillsSettings.getProperty("AltOtherBuffTime", "1."));
			ALT_VITALITY_BUFF_TIME = Float.parseFloat(skillsSettings.getProperty("AltVitalityBuffTime", "1."));

			ALT_DANCE_MP_CONSUME = Boolean.parseBoolean(skillsSettings.getProperty("AltDanceMpConsume", "false"));
			ALT_AUTO_LEARN_SKILLS = Boolean.parseBoolean(skillsSettings.getProperty("AutoLearnSkills", "false"));
			ALT_AUTO_LEARN_DIVINE_INSPIRATION = Boolean.parseBoolean(skillsSettings.getProperty("AutoLearnDivineInspiration", "false"));
			ALT_MAX_PATK_SPEED = Integer.parseInt(skillsSettings.getProperty("MaxPAtkSpeed", "1500"));
			ALT_MAX_MATK_SPEED = Integer.parseInt(skillsSettings.getProperty("MaxMAtkSpeed", "1999"));

			ALT_MAGES_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltPDamageMages", "1.00"));
			ALT_MAGES_MAGICAL_DAMAGE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltMDamageMages", "1.00"));
			ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltPDamageFighters", "1.00"));
			ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltMDamageFighters", "1.00"));
			ALT_PETS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltPDamagePets", "1.00"));
			ALT_PETS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltMDamagePets", "1.00"));
			ALT_NPC_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltPDamageNpc", "1.00"));
			ALT_NPC_MAGICAL_DAMAGE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltMDamageNpc", "1.00"));

			ALT_MAGES_PHYSICAL_DEFENSE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltPDefenseMages", "1.00"));
			ALT_MAGES_MAGICAL_DEFENSE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltMDefenseMages", "1.00"));
			ALT_FIGHTERS_PHYSICAL_DEFENSE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltPDefenseFighters", "1.00"));
			ALT_FIGHTERS_MAGICAL_DEFENSE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltMDefenseFighters", "1.00"));
			ALT_PETS_PHYSICAL_DEFENSE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltPDefensePets", "1.00"));
			ALT_PETS_MAGICAL_DEFENSE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltMDefensePets", "1.00"));
			ALT_NPC_PHYSICAL_DEFENSE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltPDefenseNpc", "1.00"));
			ALT_NPC_MAGICAL_DEFENSE_MULTI = Float.parseFloat(skillsSettings.getProperty("AltMDefenseNpc", "1.00"));

			RAID_PDAMAGE_MULTIPLIER = Float.parseFloat(skillsSettings.getProperty("RaidPDamageMultiplier", "1.00"));
			RAID_MDAMAGE_MULTIPLIER = Float.parseFloat(skillsSettings.getProperty("RaidMDamageMultiplier", "1.00"));

			RAID_PDEFENCE_MULTIPLIER = Float.parseFloat(skillsSettings.getProperty("RaidPDefenceMultiplier", "1.00"));
			RAID_MDEFENCE_MULTIPLIER = Float.parseFloat(skillsSettings.getProperty("RaidMDefenceMultiplier", "1.00"));

			GB_PDAMAGE_MULTIPLIER = Float.parseFloat(skillsSettings.getProperty("GrandBossPDamageMultiplier", "1.00"));
			GB_MDAMAGE_MULTIPLIER = Float.parseFloat(skillsSettings.getProperty("GrandBossMDamageMultiplier", "1.00"));

			GB_PDEFENCE_MULTIPLIER = Float.parseFloat(skillsSettings.getProperty("GrandBossPDefenceMultiplier", "1.00"));
			GB_MDEFENCE_MULTIPLIER = Float.parseFloat(skillsSettings.getProperty("GrandBossMDefenceMultiplier", "1.00"));

			ALT_BUFFER_HATE = Integer.parseInt(skillsSettings.getProperty("BufferHate", "4"));
			ALT_URN_TEMP_FAIL = Integer.parseInt(skillsSettings.getProperty("UrnTempFail", "10"));
			ALT_PCRITICAL_CAP = Integer.parseInt(skillsSettings.getProperty("AltPCriticalCap", "500"));
			ALT_MCRITICAL_CAP = Integer.parseInt(skillsSettings.getProperty("AltMCriticalCap", "200"));
			ALT_MAX_EVASION = Integer.parseInt(skillsSettings.getProperty("MaxEvasion", "200"));
			ALT_MAX_RUN_SPEED = Integer.parseInt(skillsSettings.getProperty("MaxRunSpeed", "250"));
			ALT_MCRIT_RATE = Float.parseFloat(skillsSettings.getProperty("AltMCritRate", "3.0"));
			ALT_MCRIT_PVP_RATE = Float.parseFloat(skillsSettings.getProperty("AltMCritPvpRate", "2.5"));

			ALT_POLEARM_DAMAGE_MULTI = Double.parseDouble(skillsSettings.getProperty("AltPolearmDamageMulti", "1.0"));
			ALT_POLEARM_VAMPIRIC_MULTI = Double.parseDouble(skillsSettings.getProperty("AltPolearmVampiricMulti", "0.5"));

			ALT_SP_BOOK_NEEDED = Boolean.parseBoolean(skillsSettings.getProperty("SpBookNeeded", "false"));
			ALT_LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(skillsSettings.getProperty("LifeCrystalNeeded", "true"));
			ALT_ES_SP_BOOK_NEEDED = Boolean.parseBoolean(skillsSettings.getProperty("EnchantSkillSpBookNeeded", "true"));
			DIVINE_SP_BOOK_NEEDED = Boolean.parseBoolean(skillsSettings.getProperty("DivineInspirationSpBookNeeded", "true"));

			ALT_INSTANT_KILL_EFFECT_2 = Float.parseFloat(skillsSettings.getProperty("InstantKillEffect2", "2"));
			ALT_DAGGER_FORMULA = Boolean.parseBoolean(skillsSettings.getProperty("AltGameDaggerFormula", "false"));
			ALT_DAGGER_RATE = Integer.parseInt(skillsSettings.getProperty("AltCancelRate", "85"));
			ALT_DAGGER_FAIL_RATE = Integer.parseInt(skillsSettings.getProperty("AltFailRate", "15"));
			ALT_DAGGER_RATE_BEHIND = Integer.parseInt(skillsSettings.getProperty("AltSuccessRateBehind", "20"));
			ALT_DAGGER_RATE_FRONT = Integer.parseInt(skillsSettings.getProperty("AltSuccessRateFront", "5"));
			ALT_ATTACK_DELAY = Float.parseFloat(skillsSettings.getProperty("AltAttackDelay", "1.00"));

			ALT_BUFFS_MAX_AMOUNT = Integer.parseInt(skillsSettings.getProperty("MaxBuffAmount", "20"));
			ALT_DANCES_SONGS_MAX_AMOUNT = Integer.parseInt(skillsSettings.getProperty("MaxDanceSongAmount", "12"));
			DANCE_CANCEL_BUFF = Boolean.parseBoolean(skillsSettings.getProperty("DanceCancelBuff", "false"));
			ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(skillsSettings.getProperty("AltGameSkillLearn", "false"));

			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(skillsSettings.getProperty("AltSubClassWithoutQuests", "False"));
			ALT_GAME_SUBCLASS_EVERYWHERE = Boolean.parseBoolean(skillsSettings.getProperty("AltSubclassEverywhere", "False"));
			ALT_MAX_SUBCLASS = Integer.parseInt(skillsSettings.getProperty("MaxSubclass", "3"));
			ALT_MAX_SUBCLASS_LEVEL = Byte.parseByte(skillsSettings.getProperty("MaxSubclassLevel", "80"));

			ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(skillsSettings.getProperty("MagicFailures", "false"));

			String cancel = skillsSettings.getProperty("AltGameCancelByHit", "Cast").trim();
			ALT_GAME_CANCEL_BOW = (cancel.equalsIgnoreCase("bow") || cancel.equalsIgnoreCase("all"));
			ALT_GAME_CANCEL_CAST = (cancel.equalsIgnoreCase("cast") || cancel.equalsIgnoreCase("all"));
			ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(skillsSettings.getProperty("AltShieldBlocks", "false"));
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(skillsSettings.getProperty("AltPerfectShieldBlockRate", "10"));
		}
	}

	// *******************************************************************************************
	public static final String			OLYMPIAD_FILE					= "./config/main/events/olympiad.properties";
	// *******************************************************************************************
	public static int					ALT_OLY_START_TIME;															// Olympiad Competition Starting time
	public static int					ALT_OLY_MIN;																	// Olympiad Minutes
	public static int					ALT_OLY_CPERIOD;																// Olympaid Competition Period
	public static int					ALT_OLY_BATTLE;																// Olympiad Battle Period
	public static int					ALT_OLY_WPERIOD;																// Olympiad Weekly Period
	public static int					ALT_OLY_VPERIOD;																// Olympiad Validation Period
	public static boolean				ALT_OLY_SAME_IP;																// Olympiad allow Matches from same Ip
	public static int					ALT_OLY_CLASSED;
	public static int					ALT_OLY_BATTLE_REWARD_ITEM;
	public static int					ALT_OLY_CLASSED_RITEM_C;
	public static int					ALT_OLY_NONCLASSED_RITEM_C;
	public static int					ALT_OLY_REG_DISPLAY;
	public static int					ALT_OLY_GP_PER_POINT;
	public static int					ALT_OLY_HERO_POINTS;
	public static int					ALT_OLY_RANK1_POINTS;
	public static int					ALT_OLY_RANK2_POINTS;
	public static int					ALT_OLY_RANK3_POINTS;
	public static int					ALT_OLY_RANK4_POINTS;
	public static int					ALT_OLY_RANK5_POINTS;
	public static int					ALT_OLY_MAX_POINTS;
	public static boolean				ALT_OLY_LOG_FIGHTS;
	public static boolean				ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static boolean				ALT_OLY_ANNOUNCE_GAMES;
	public static int					ALT_OLY_ENCHANT_LIMIT;
	public static final Set<Integer>	ALT_LIST_OLY_RESTRICTED_ITEMS	= new L2FastSet<Integer>();
	public static int					ALT_OLY_NONCLASSED;
	public static boolean				ALT_OLY_REMOVE_CUBICS;

	// *******************************************************************************************
	private static final class OlympiadConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/events/olympiad";
		}

		@Override
		protected void loadImpl(L2Properties olympiadSettings)
		{
			ALT_OLY_START_TIME = Integer.parseInt(olympiadSettings.getProperty("AltOlyStartTime", "18"));
			ALT_OLY_MIN = Integer.parseInt(olympiadSettings.getProperty("AltOlyMin", "00"));
			ALT_OLY_CPERIOD = Integer.parseInt(olympiadSettings.getProperty("AltOlyCPeriod", "21600000"));
			ALT_OLY_BATTLE = Integer.parseInt(olympiadSettings.getProperty("AltOlyBattle", "360000"));
			ALT_OLY_WPERIOD = Integer.parseInt(olympiadSettings.getProperty("AltOlyWperiod", "604800000"));
			ALT_OLY_VPERIOD = Integer.parseInt(olympiadSettings.getProperty("AltOlyVperiod", "86400000"));
			ALT_OLY_SAME_IP = Boolean.parseBoolean(olympiadSettings.getProperty("AltOlySameIp", "true"));
			ALT_OLY_CLASSED = Integer.parseInt(olympiadSettings.getProperty("AltOlyClassedParticipants", "5"));
			ALT_OLY_NONCLASSED = Integer.parseInt(olympiadSettings.getProperty("AltOlyNonClassedParticipants", "9"));
			ALT_OLY_REMOVE_CUBICS = Boolean.parseBoolean(olympiadSettings.getProperty("AltOlyRemoveCubics", "false"));
			ALT_OLY_BATTLE_REWARD_ITEM = Integer.parseInt(olympiadSettings.getProperty("AltOlyBattleRewItem", "13722"));
			ALT_OLY_CLASSED_RITEM_C = Integer.parseInt(olympiadSettings.getProperty("AltOlyClassedRewItemCount", "50"));
			ALT_OLY_NONCLASSED_RITEM_C = Integer.parseInt(olympiadSettings.getProperty("AltOlyNonClassedRewItemCount", "30"));
			ALT_OLY_REG_DISPLAY = Integer.parseInt(olympiadSettings.getProperty("AltOlyRegistrationDisplayNumber", "100"));
			ALT_OLY_GP_PER_POINT = Integer.parseInt(olympiadSettings.getProperty("AltOlyGPPerPoint", "1000"));
			ALT_OLY_HERO_POINTS = Integer.parseInt(olympiadSettings.getProperty("AltOlyHeroPoints", "180"));
			ALT_OLY_RANK1_POINTS = Integer.parseInt(olympiadSettings.getProperty("AltOlyRank1Points", "120"));
			ALT_OLY_RANK2_POINTS = Integer.parseInt(olympiadSettings.getProperty("AltOlyRank2Points", "80"));
			ALT_OLY_RANK3_POINTS = Integer.parseInt(olympiadSettings.getProperty("AltOlyRank3Points", "55"));
			ALT_OLY_RANK4_POINTS = Integer.parseInt(olympiadSettings.getProperty("AltOlyRank4Points", "35"));
			ALT_OLY_RANK5_POINTS = Integer.parseInt(olympiadSettings.getProperty("AltOlyRank5Points", "20"));
			ALT_OLY_MAX_POINTS = Integer.parseInt(olympiadSettings.getProperty("AltOlyMaxPoints", "10"));
			ALT_OLY_LOG_FIGHTS = Boolean.parseBoolean(olympiadSettings.getProperty("AlyOlyLogFights", "false"));
			ALT_OLY_SHOW_MONTHLY_WINNERS = Boolean.parseBoolean(olympiadSettings.getProperty("AltOlyShowMonthlyWinners", "true"));
			ALT_OLY_ANNOUNCE_GAMES = Boolean.parseBoolean(olympiadSettings.getProperty("AltOlyAnnounceGames", "true"));
			ALT_LIST_OLY_RESTRICTED_ITEMS.clear();
			for (String id : olympiadSettings.getProperty("AltOlyRestrictedItems", "0").split(","))
			{
				ALT_LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
			}
			ALT_OLY_ENCHANT_LIMIT = Integer.parseInt(olympiadSettings.getProperty("AltOlyEnchantLimit", "-1"));
		}
	}

	// *******************************************************************************************
	public static final String	MANOR_FILE	= "./config/main/events/manor.properties";
	// *******************************************************************************************
	public static boolean		ALLOW_MANOR;											// Allow Manor system
	public static int			ALT_MANOR_REFRESH_TIME;								// Manor Refresh Starting time
	public static int			ALT_MANOR_REFRESH_MIN;									// Manor Refresh Min
	public static int			ALT_MANOR_APPROVE_TIME;								// Manor Next Period Approve Starting time
	public static int			ALT_MANOR_APPROVE_MIN;									// Manor Next Period Approve Min
	public static int			ALT_MANOR_MAINTENANCE_PERIOD;							// Manor Maintenance Time
	public static boolean		ALT_MANOR_SAVE_ALL_ACTIONS;							// Manor Save All Actions
	public static int			ALT_MANOR_SAVE_PERIOD_RATE;							// Manor Save Period Rate

	// *******************************************************************************************
	private static final class ManorConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/events/manor";
		}

		@Override
		protected void loadImpl(L2Properties manorSettings)
		{
			ALLOW_MANOR = Boolean.parseBoolean(manorSettings.getProperty("AllowManor", "False"));
			ALT_MANOR_REFRESH_TIME = Integer.parseInt(manorSettings.getProperty("AltManorRefreshTime", "20"));
			ALT_MANOR_REFRESH_MIN = Integer.parseInt(manorSettings.getProperty("AltManorRefreshMin", "00"));
			ALT_MANOR_APPROVE_TIME = Integer.parseInt(manorSettings.getProperty("AltManorApproveTime", "6"));
			ALT_MANOR_APPROVE_MIN = Integer.parseInt(manorSettings.getProperty("AltManorApproveMin", "00"));
			ALT_MANOR_MAINTENANCE_PERIOD = Integer.parseInt(manorSettings.getProperty("AltManorMaintenancePeriod", "360000"));
			ALT_MANOR_SAVE_ALL_ACTIONS = Boolean.parseBoolean(manorSettings.getProperty("AltManorSaveAllActions", "false"));
			ALT_MANOR_SAVE_PERIOD_RATE = Integer.parseInt(manorSettings.getProperty("AltManorSavePeriodRate", "2"));
		}
	}

	// *******************************************************************************************
	public static final String	FAME_FILE	= "./config/main/fame.properties";
	// *******************************************************************************************
	public static boolean		ALT_FAME_FOR_DEAD_PLAYERS;

	public static int			MAX_PERSONAL_FAME_POINTS;
	public static int			FORTRESS_ZONE_FAME_TASK_FREQUENCY;
	public static int			FORTRESS_ZONE_FAME_AQUIRE_POINTS;
	public static int			CASTLE_ZONE_FAME_TASK_FREQUENCY;
	public static int			CASTLE_ZONE_FAME_AQUIRE_POINTS;

	public static boolean		ALT_TAX_CHANGE_DELAYED;
	public static boolean		ALT_CCH_REPUTATION;

	// *******************************************************************************************
	private static final class FameConfig extends ConfigPropertiesLoader
	{
		protected String getName()
		{
			return "main/fame";
		}

		@Override
		protected void loadImpl(L2Properties fameSettings)
		{
			ALT_FAME_FOR_DEAD_PLAYERS = Boolean.parseBoolean(fameSettings.getProperty("FameForDeadPlayers", "true"));
			MAX_PERSONAL_FAME_POINTS = Integer.parseInt(fameSettings.getProperty("MaxPersonalFamePoints", "65535"));
			FORTRESS_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(fameSettings.getProperty("FortressZoneFameTaskFrequency", "300"));
			FORTRESS_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(fameSettings.getProperty("FortressZoneFameAquirePoints", "31"));
			CASTLE_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(fameSettings.getProperty("CastleZoneFameTaskFrequency", "300"));
			CASTLE_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(fameSettings.getProperty("CastleZoneFameAquirePoints", "125"));

			ALT_TAX_CHANGE_DELAYED = Boolean.parseBoolean(fameSettings.getProperty("CastleTaxChangeDelayed", "true"));
			ALT_CCH_REPUTATION = Boolean.parseBoolean(fameSettings.getProperty("AltClanHallSiegeReputation", "false"));
		}
	}

	// *******************************************************************************************
	public static final String	FOUR_SEPULCHERS_FILE	= "./config/main/four_sepulchers.properties";

	// *******************************************************************************************

	// *******************************************************************************************
	private static final class FourSepulchersConfig extends ConfigPropertiesLoader
	{
		protected String getName()
		{
			return "main/four_sepulchers";
		}

		@Override
		protected void loadImpl(L2Properties foursepulchersSettings)
		{
		}
	}

	// *******************************************************************************************
	public static final String	DIMENSION_RIFT_FILE	= "./config/main/dimension_rift.properties";
	// *******************************************************************************************
	// Dimensional Rift
	public static int			ALT_RIFT_MIN_PARTY_SIZE;											// Minimum siz e of a party that may enter dimensional rift
	public static int			ALT_RIFT_SPAWN_DELAY;												// Time in ms the party has to wait until the mobs spawn
	// when entering a room
	public static int			ALT_RIFT_MAX_JUMPS;												// Amount of random rift jumps before party is ported back
	public static int			ALT_RIFT_AUTO_JUMPS_TIME_MIN;										// Random time between two jumps in dimensional rift - in
	// seconds
	public static int			ALT_RIFT_AUTO_JUMPS_TIME_MAX;
	public static int			ALT_RIFT_ENTER_COST_RECRUIT;										// Dimensional Fragment cost for entering rift
	public static int			ALT_RIFT_ENTER_COST_SOLDIER;
	public static int			ALT_RIFT_ENTER_COST_OFFICER;
	public static int			ALT_RIFT_ENTER_COST_CAPTAIN;
	public static int			ALT_RIFT_ENTER_COST_COMMANDER;
	public static int			ALT_RIFT_ENTER_COST_HERO;
	public static float			ALT_RIFT_BOSS_ROOM_TIME_MUTIPLY;									// Time multiplier for boss room

	// *******************************************************************************************
	private static final class DimensionRiftConfig extends ConfigPropertiesLoader
	{
		protected String getName()
		{
			return "main/dimension_rift";
		}

		@Override
		protected void loadImpl(L2Properties dimensionriftSettings)
		{
			// Dimensional Rift Config
			ALT_RIFT_MIN_PARTY_SIZE = Integer.parseInt(dimensionriftSettings.getProperty("RiftMinPartySize", "5"));
			ALT_RIFT_MAX_JUMPS = Integer.parseInt(dimensionriftSettings.getProperty("MaxRiftJumps", "4"));
			ALT_RIFT_SPAWN_DELAY = Integer.parseInt(dimensionriftSettings.getProperty("RiftSpawnDelay", "10000"));
			ALT_RIFT_AUTO_JUMPS_TIME_MIN = Integer.parseInt(dimensionriftSettings.getProperty("AutoJumpsDelayMin", "480"));
			ALT_RIFT_AUTO_JUMPS_TIME_MAX = Integer.parseInt(dimensionriftSettings.getProperty("AutoJumpsDelayMax", "600"));
			ALT_RIFT_ENTER_COST_RECRUIT = Integer.parseInt(dimensionriftSettings.getProperty("RecruitCost", "18"));
			ALT_RIFT_ENTER_COST_SOLDIER = Integer.parseInt(dimensionriftSettings.getProperty("SoldierCost", "21"));
			ALT_RIFT_ENTER_COST_OFFICER = Integer.parseInt(dimensionriftSettings.getProperty("OfficerCost", "24"));
			ALT_RIFT_ENTER_COST_CAPTAIN = Integer.parseInt(dimensionriftSettings.getProperty("CaptainCost", "27"));
			ALT_RIFT_ENTER_COST_COMMANDER = Integer.parseInt(dimensionriftSettings.getProperty("CommanderCost", "30"));
			ALT_RIFT_ENTER_COST_HERO = Integer.parseInt(dimensionriftSettings.getProperty("HeroCost", "33"));
			ALT_RIFT_BOSS_ROOM_TIME_MUTIPLY = Float.parseFloat(dimensionriftSettings.getProperty("BossRoomTimeMultiply", "1.5"));
		}
	}

	// *******************************************************************************************
	public static final String	EQUIPMENT_FILE	= "./config/main/equipment.properties";
	// *******************************************************************************************
	public static boolean		ALT_CASTLE_SHIELD;										// Alternative gaming - Castle Shield can be equiped by all clan members if they own a castle. - default True
	public static boolean		ALT_CLANHALL_SHIELD;									// Alternative gaming - Clan Hall Shield can be equiped by all clan members if they own a clan hall. - default True
	public static boolean		ALT_APELLA_ARMORS;										// Alternative gaming - Apella armors can be equiped only by clan members if their class is Baron or higher - default True
	public static boolean		ALT_OATH_ARMORS;										// Alternative gaming - Clan Oath Armors can be equiped only by clan members - default True
	public static boolean		ALT_CASTLE_CROWN;										// Alternative gaming - Castle Crown can be equiped only by castle lord - default True
	public static boolean		ALT_CASTLE_CIRCLETS;									// Alternative gaming - Castle Circlets can be equiped only by clan members if they own a castle - default True
	public static boolean		ALT_REMOVE_CASTLE_CIRCLETS;							// Remove Castle circlets after clan lose his castle? - default
	public static boolean		ALT_ONLY_CLANLEADER_CAN_SIT_ON_THRONE;

	// *******************************************************************************************
	private static final class EquipmentConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/equipment";
		}

		@Override
		protected void loadImpl(L2Properties altSettings)
		{
			ALT_CASTLE_SHIELD = Boolean.parseBoolean(altSettings.getProperty("CastleShieldRestriction", "True"));
			ALT_CLANHALL_SHIELD = Boolean.parseBoolean(altSettings.getProperty("ClanHallShieldRestriction", "True"));
			ALT_APELLA_ARMORS = Boolean.parseBoolean(altSettings.getProperty("ApellaArmorsRestriction", "True"));
			ALT_OATH_ARMORS = Boolean.parseBoolean(altSettings.getProperty("OathArmorsRestriction", "True"));
			ALT_CASTLE_CROWN = Boolean.parseBoolean(altSettings.getProperty("CastleLordsCrownRestriction", "True"));
			ALT_CASTLE_CIRCLETS = Boolean.parseBoolean(altSettings.getProperty("CastleCircletsRestriction", "True"));

			ALT_REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(altSettings.getProperty("RemoveCastleCirclets", "true"));
			ALT_ONLY_CLANLEADER_CAN_SIT_ON_THRONE = Boolean.parseBoolean(altSettings.getProperty("AltOnlyClanleaderCanSitOnThrone", "false"));
		}
	}

	// *******************************************************************************************
	public static final String			CLASS_MASTER_FILE	= "./config/mods/class_master.properties";
	// *******************************************************************************************
	public static boolean				ALT_SPAWN_CLASS_MASTER;
	public static boolean				ALT_CLASS_MASTER_STRIDER_UPDATE;
	public static String				ALT_CLASS_MASTER_SETTINGS_LINE;
	public static ClassMasterSettings	ALT_CLASS_MASTER_SETTINGS;
	public static int					ALLOWED_MAX_CHANGE_LEVEL;
	public static boolean				ALT_L2J_CLASS_MASTER;
	public static boolean				ALT_CLASS_MASTER_ENTIRE_TREE;
	public static boolean				ALT_CLASS_MASTER_TUTORIAL;

	// *******************************************************************************************
	private static final class ClassMasterConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/class_master";
		}

		@Override
		protected void loadImpl(L2Properties cmSettings)
		{
			ALT_SPAWN_CLASS_MASTER = Boolean.parseBoolean(cmSettings.getProperty("SpawnClassMaster", "False"));
			ALT_CLASS_MASTER_STRIDER_UPDATE = Boolean.parseBoolean(cmSettings.getProperty("ClassMasterUpdateStrider", "False"));
			if (!cmSettings.getProperty("ConfigClassMaster").trim().equalsIgnoreCase("False"))
				ALT_CLASS_MASTER_SETTINGS_LINE = cmSettings.getProperty("ConfigClassMaster");

			ALT_CLASS_MASTER_SETTINGS = new ClassMasterSettings(ALT_CLASS_MASTER_SETTINGS_LINE);

			ALLOWED_MAX_CHANGE_LEVEL = Integer.parseInt(cmSettings.getProperty("AllowedMaxChangeLevel", "3"));

			ALT_L2J_CLASS_MASTER = Boolean.parseBoolean(cmSettings.getProperty("L2JClassMaster", "False"));
			ALT_CLASS_MASTER_ENTIRE_TREE = Boolean.parseBoolean(cmSettings.getProperty("ClassMasterEntireTree", "False"));
			ALT_CLASS_MASTER_TUTORIAL = Boolean.parseBoolean(cmSettings.getProperty("AltClassMaster", "False"));
		}
	}

	// *******************************************************************************************
	public static final String	CRAFT_MANAGER_FILE	= "./config/mods/craft_manager.properties";
	// *******************************************************************************************
	public static double		ALT_CRAFT_PRICE;												// reference price multiplier
	public static int			ALT_CRAFT_DEFAULT_PRICE;										// default price, in case reference is 0
	public static boolean		ALT_CRAFT_ALLOW_CRAFT;											// allow to craft dwarven recipes
	public static boolean		ALT_CRAFT_ALLOW_CRYSTALLIZE;									// allow to break items
	public static boolean		ALT_CRAFT_ALLOW_COMMON;										// allow to craft common craft recipes

	// *******************************************************************************************
	private static final class CraftManagerConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/craft_manager";
		}

		@Override
		protected void loadImpl(L2Properties craftSettings)
		{
			ALT_CRAFT_ALLOW_CRAFT = Boolean.parseBoolean(craftSettings.getProperty("CraftManagerDwarvenCraft", "True"));
			ALT_CRAFT_ALLOW_COMMON = Boolean.parseBoolean(craftSettings.getProperty("CraftManagerCommonCraft", "False"));
			ALT_CRAFT_ALLOW_CRYSTALLIZE = Boolean.parseBoolean(craftSettings.getProperty("CraftManagerCrystallize", "True"));
			ALT_CRAFT_PRICE = Float.parseFloat(craftSettings.getProperty("CraftManagerPriceMultiplier", "0.1"));
			ALT_CRAFT_DEFAULT_PRICE = Integer.parseInt(craftSettings.getProperty("CraftManagerDefaultPrice", "50000"));
		}
	}

	// *******************************************************************************************
	public static final String	GM_ACCESS_FILE	= "./config/administration/gm_access.properties";
	// *******************************************************************************************
	public static int			GM_ACCESSLEVEL;
	public static int			GM_MIN;															// General GM Minimal AccessLevel
	public static int			GM_ALTG_MIN_LEVEL;													// Minimum privileges level for a GM to do Alt+G
	public static int			GM_CREATE_ITEM;													// General GM AccessLevel can /create_item and /gmshop
	public static int			GM_FREE_SHOP;														// General GM AccessLevel can shop for free
	public static int			GM_CHAR_VIEW_INFO;													// General GM AccessLevel with character view rights ALT+G
	public static int			GM_CHAR_INVENTORY;													// General GM AccessLevel with character view inventory rights ALT+G
	public static int			GM_CHAR_CLAN_VIEW;													// General GM AccessLevel with character view clan info rights ALT+G
	public static int			GM_CHAR_VIEW_QUEST;												// General GM AccessLevel with character view quest rights ALT+G
	public static int			GM_CHAR_VIEW_SKILL;												// General GM AccessLevel with character view skill rights ALT+G
	public static int			GM_CHAR_VIEW_WAREHOUSE;											// General GM AccessLevel with character warehouse view rights ALT+G
	public static int			GM_ESCAPE;															// General GM AccessLevel to unstuck without 5min delay
	public static int			GM_FIXED;															// General GM AccessLevel to resurect fixed after death									// General GM AccessLevel with Resurrection rights
	public static int			GM_PEACEATTACK;													// General GM AccessLevel to attack in the peace zone
	public static boolean		GM_DISABLE_TRANSACTION;											// Disable transaction on AccessLevel
	public static int			GM_TRANSACTION_MIN;
	public static int			GM_TRANSACTION_MAX;
	public static int			GM_CAN_GIVE_DAMAGE;												// Minimum level to allow a GM giving damage
	public static int			GM_DONT_TAKE_EXPSP;												// Minimum level to don't give Exp/Sp in party
	public static int			GM_DONT_TAKE_AGGRO;												// Minimum level to don't take aggro
	public static boolean		GM_NAME_COLOR_ENABLED;												// GM name color
	public static boolean		GM_TITLE_COLOR_ENABLED;
	public static int			GM_NAME_COLOR;
	public static int			GM_TITLE_COLOR;
	public static int			ADMIN_NAME_COLOR;
	public static int			ADMIN_TITLE_COLOR;
	public static boolean		SHOW_GM_LOGIN;														// GM Announce at login
	public static boolean		GM_HIDE;
	public static boolean		GM_STARTUP_INVISIBLE;
	public static boolean		GM_STARTUP_SILENCE;
	public static boolean		GM_STARTUP_AUTO_LIST;
	public static String		GM_ADMIN_MENU_STYLE;
	public static int			STANDARD_RESPAWN_DELAY;											// Standard Respawn Delay
	public static boolean		GM_HERO_AURA;														// Place an aura around the GM ?
	public static boolean		GM_STARTUP_INVULNERABLE;											// Set the GM invulnerable at startup ?
	public static boolean		GM_ANNOUNCER_NAME;
	public static boolean		GM_ITEM_RESTRICTION;
	public static boolean		GM_SKILL_RESTRICTION;
	public static boolean		GM_TRADE_RESTRICTED_ITEMS;
	public static boolean		GM_RESTART_FIGHTING;
	public static boolean		GM_ALLOW_CHAT_INVISIBLE;
	public static boolean		GM_NAME_HAS_BRACELETS;
	public static boolean		GM_GIVE_SPECIAL_SKILLS;
	public static boolean		GM_EVERYBODY_HAS_ADMIN_RIGHTS;

	// *******************************************************************************************
	private static final class GMAccessConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "administration/gm_access";
		}

		@Override
		protected void loadImpl(L2Properties gmSettings)
		{
			GM_ACCESSLEVEL = Integer.parseInt(gmSettings.getProperty("GMAccessLevel", "100"));
			GM_MIN = Integer.parseInt(gmSettings.getProperty("GMMinLevel", "100"));
			GM_ALTG_MIN_LEVEL = Integer.parseInt(gmSettings.getProperty("GMCanAltG", "100"));
			GM_CREATE_ITEM = Integer.parseInt(gmSettings.getProperty("GMCanShop", "100"));
			GM_FREE_SHOP = Integer.parseInt(gmSettings.getProperty("GMCanBuyFree", "100"));
			GM_CHAR_VIEW_INFO = Integer.parseInt(gmSettings.getProperty("GMViewCharacterInfo", "100"));
			GM_CHAR_INVENTORY = Integer.parseInt(gmSettings.getProperty("GMViewItemList", "100"));
			GM_CHAR_CLAN_VIEW = Integer.parseInt(gmSettings.getProperty("GMViewClanInfo", "100"));
			GM_CHAR_VIEW_QUEST = Integer.parseInt(gmSettings.getProperty("GMViewQuestList", "100"));
			GM_CHAR_VIEW_SKILL = Integer.parseInt(gmSettings.getProperty("GMViewSkillInfo", "100"));
			GM_CHAR_VIEW_WAREHOUSE = Integer.parseInt(gmSettings.getProperty("GMViewWarehouseWithdrawList", "100"));
			GM_ESCAPE = Integer.parseInt(gmSettings.getProperty("GMFastUnstuck", "100"));
			GM_FIXED = Integer.parseInt(gmSettings.getProperty("GMResurectFixed", "100"));
			GM_PEACEATTACK = Integer.parseInt(gmSettings.getProperty("GMPeaceAttack", "100"));
			GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(gmSettings.getProperty("GMStartupAutoList", "True"));
			GM_ADMIN_MENU_STYLE = gmSettings.getProperty("GMAdminMenuStyle", "modern");
			GM_HERO_AURA = Boolean.parseBoolean(gmSettings.getProperty("GMHeroAura", "True"));
			GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(gmSettings.getProperty("GMStartupInvulnerable", "True"));
			STANDARD_RESPAWN_DELAY = Integer.parseInt(gmSettings.getProperty("StandardRespawnDelay", "0"));
			GM_ANNOUNCER_NAME = Boolean.parseBoolean(gmSettings.getProperty("GMShowAnnouncerName", "False"));

			String gmTrans = gmSettings.getProperty("GMDisableTransaction", "False");

			if (!gmTrans.trim().equalsIgnoreCase("False"))
			{
				String[] params = gmTrans.trim().split(",");
				GM_DISABLE_TRANSACTION = true;
				GM_TRANSACTION_MIN = Integer.parseInt(params[0].trim());
				GM_TRANSACTION_MAX = Integer.parseInt(params[1].trim());
			}
			else
			{
				GM_DISABLE_TRANSACTION = false;
			}
			GM_CAN_GIVE_DAMAGE = Integer.parseInt(gmSettings.getProperty("GMCanGiveDamage", "90"));
			GM_DONT_TAKE_AGGRO = Integer.parseInt(gmSettings.getProperty("GMDontTakeAggro", "90"));
			GM_DONT_TAKE_EXPSP = Integer.parseInt(gmSettings.getProperty("GMDontGiveExpSp", "90"));

			GM_NAME_COLOR_ENABLED = Boolean.parseBoolean(gmSettings.getProperty("GMNameColorEnabled", "True"));
			GM_TITLE_COLOR_ENABLED = Boolean.parseBoolean(gmSettings.getProperty("GMTitleColorEnabled", "True"));
			GM_NAME_COLOR = Integer.decode("0x" + gmSettings.getProperty("GMNameColor", "00FF00"));
			GM_TITLE_COLOR = Integer.decode("0x" + gmSettings.getProperty("GMTitleColor", "00FF00"));
			ADMIN_NAME_COLOR = Integer.decode("0x" + gmSettings.getProperty("AdminNameColor", "00CCFF"));
			ADMIN_TITLE_COLOR = Integer.decode("0x" + gmSettings.getProperty("AdminTitleColor", "00CCFF"));
			SHOW_GM_LOGIN = Boolean.parseBoolean(gmSettings.getProperty("ShowGMLogin", "False"));
			GM_HIDE = Boolean.parseBoolean(gmSettings.getProperty("GMHide", "True"));
			GM_STARTUP_INVISIBLE = Boolean.parseBoolean(gmSettings.getProperty("GMStartupInvisible", "True"));
			GM_STARTUP_SILENCE = Boolean.parseBoolean(gmSettings.getProperty("GMStartupSilence", "True"));

			GM_ITEM_RESTRICTION = Boolean.parseBoolean(gmSettings.getProperty("GMItemRestriction", "True"));
			GM_SKILL_RESTRICTION = Boolean.parseBoolean(gmSettings.getProperty("GMSkillRestriction", "True"));
			GM_TRADE_RESTRICTED_ITEMS = Boolean.parseBoolean(gmSettings.getProperty("GMTradeRestrictedItems", "False"));
			GM_RESTART_FIGHTING = Boolean.parseBoolean(gmSettings.getProperty("GMRestartFighting", "True"));
			GM_ALLOW_CHAT_INVISIBLE = Boolean.parseBoolean(gmSettings.getProperty("GMChatInvisible", "False"));

			GM_NAME_HAS_BRACELETS = Boolean.parseBoolean(gmSettings.getProperty("GmBracelets", "True"));

			GM_GIVE_SPECIAL_SKILLS = Boolean.parseBoolean(gmSettings.getProperty("GMGiveSpecialSkills", "False"));

			GM_EVERYBODY_HAS_ADMIN_RIGHTS = Boolean.parseBoolean(gmSettings.getProperty("EverybodyHasAdminRights", "False"));
		}
	}

	// *******************************************************************************************
	public static final String	DATETIME_FILE	= "./config/main/datetime.properties";
	// *******************************************************************************************
	public static boolean		DATETIME_SAVECAL;
	public static int			DATETIME_SUNRISE;
	public static int			DATETIME_SUNSET;
	public static int			DATETIME_MULTI;
	public static int			DATETIME_MOVE_DELAY;

	// *******************************************************************************************
	private static final class DateTimeConfig extends ConfigPropertiesLoader
	{
		protected String getName()
		{
			return "main/datetime";
		}

		@Override
		protected void loadImpl(L2Properties datetimeSettings)
		{

			DATETIME_SAVECAL = Boolean.parseBoolean(datetimeSettings.getProperty("SaveDate", "false"));
			DATETIME_SUNSET = Integer.parseInt(datetimeSettings.getProperty("SunSet", "24"));
			DATETIME_SUNRISE = Integer.parseInt(datetimeSettings.getProperty("SunRise", "6"));
			DATETIME_MULTI = Integer.parseInt(datetimeSettings.getProperty("TimeMulti", "6"));
			DATETIME_MOVE_DELAY = Integer.parseInt(datetimeSettings.getProperty("MoveDelay", "2"));
		}
	}

	// *******************************************************************************************
	public static final String			SIEGE_CONFIGURATION_FILE	= "./config/main/events/siege.properties";
	// *******************************************************************************************

	public static int					SIEGE_MAX_ATTACKER;
	public static int					SIEGE_MAX_DEFENDER;
	public static int					SIEGE_RESPAWN_DELAY_ATTACKER;
	public static int					SIEGE_RESPAWN_DELAY_DEFENDER;

	public static int					SIEGE_CT_LOSS_PENALTY;
	public static int					SIEGE_FLAG_MAX_COUNT;
	public static int					SIEGE_CLAN_MIN_LEVEL;
	public static int					SIEGE_LENGTH_MINUTES;

	public static boolean				SIEGE_ONLY_REGISTERED;
	public static boolean				SIEGE_GATE_CONTROL;
	public static boolean				USE_MISSING_CCH_MESSAGES;

	public static int					GLUDIO_MAX_MERCENARIES;
	public static int					DION_MAX_MERCENARIES;
	public static int					GIRAN_MAX_MERCENARIES;
	public static int					OREN_MAX_MERCENARIES;
	public static int					ADEN_MAX_MERCENARIES;
	public static int					INNADRIL_MAX_MERCENARIES;
	public static int					GODDARD_MAX_MERCENARIES;
	public static int					RUNE_MAX_MERCENARIES;
	public static int					SCHUTTGART_MAX_MERCENARIES;

	public static final Set<String>		CL_SET_SIEGE_TIME_LIST		= new L2FastSet<String>();
	public static final Set<Integer>	SIEGE_HOUR_LIST_MORNING		= new L2FastSet<Integer>();
	public static final Set<Integer>	SIEGE_HOUR_LIST_AFTERNOON	= new L2FastSet<Integer>();

	private static final class SiegeConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/events/siege";
		}

		@Override
		protected void loadImpl(L2Properties siegeSettings)
		{
			SIEGE_MAX_ATTACKER = Integer.parseInt(siegeSettings.getProperty("AttackerMaxClans", "500"));
			SIEGE_MAX_DEFENDER = Integer.parseInt(siegeSettings.getProperty("DefenderMaxClans", "500"));
			SIEGE_RESPAWN_DELAY_ATTACKER = Integer.parseInt(siegeSettings.getProperty("AttackerRespawn", "0"));
			SIEGE_RESPAWN_DELAY_DEFENDER = Integer.parseInt(siegeSettings.getProperty("DefenderRespawn", "30000"));

			SIEGE_CT_LOSS_PENALTY = Integer.parseInt(siegeSettings.getProperty("CTLossPenalty", "60000"));
			SIEGE_FLAG_MAX_COUNT = Integer.parseInt(siegeSettings.getProperty("MaxFlags", "1"));
			SIEGE_CLAN_MIN_LEVEL = Integer.parseInt(siegeSettings.getProperty("SiegeClanMinLevel", "5"));
			SIEGE_LENGTH_MINUTES = Integer.parseInt(siegeSettings.getProperty("SiegeLength", "120"));

			SIEGE_ONLY_REGISTERED = Boolean.parseBoolean(siegeSettings.getProperty("OnlyRegistered", "true"));
			SIEGE_GATE_CONTROL = Boolean.parseBoolean(siegeSettings.getProperty("AllowGateControl", "false"));
			USE_MISSING_CCH_MESSAGES = Boolean.parseBoolean(siegeSettings.getProperty("ClanHallSiegeSysMsgs", "false"));

			GLUDIO_MAX_MERCENARIES = Integer.parseInt(siegeSettings.getProperty("GludioMaxMercenaries", "100"));
			DION_MAX_MERCENARIES = Integer.parseInt(siegeSettings.getProperty("DionMaxMercenaries", "150"));
			GIRAN_MAX_MERCENARIES = Integer.parseInt(siegeSettings.getProperty("GiranMaxMercenaries", "200"));
			OREN_MAX_MERCENARIES = Integer.parseInt(siegeSettings.getProperty("OrenMaxMercenaries", "300"));
			ADEN_MAX_MERCENARIES = Integer.parseInt(siegeSettings.getProperty("AdenMaxMercenaries", "400"));
			INNADRIL_MAX_MERCENARIES = Integer.parseInt(siegeSettings.getProperty("InnadrilMaxMercenaries", "400"));
			GODDARD_MAX_MERCENARIES = Integer.parseInt(siegeSettings.getProperty("GoddardMaxMercenaries", "400"));
			RUNE_MAX_MERCENARIES = Integer.parseInt(siegeSettings.getProperty("RuneMaxMercenaries", "400"));
			SCHUTTGART_MAX_MERCENARIES = Integer.parseInt(siegeSettings.getProperty("SchuttgartMaxMercenaries", "400"));

			CL_SET_SIEGE_TIME_LIST.clear();
			SIEGE_HOUR_LIST_MORNING.clear();
			SIEGE_HOUR_LIST_AFTERNOON.clear();
			String[] sstl = siegeSettings.getProperty("CLSetSiegeTimeList", "").split(",");
			if (sstl.length != 0)
			{
				boolean isHour = false;
				for (String st : sstl)
				{
					if (st.equalsIgnoreCase("day") || st.equalsIgnoreCase("hour") || st.equalsIgnoreCase("minute"))
					{
						if (st.equalsIgnoreCase("hour"))
							isHour = true;
						CL_SET_SIEGE_TIME_LIST.add(st.toLowerCase());
					}
					else
					{
						System.out.println("[CLSetSiegeTimeList]: invalid config property -> CLSetSiegeTimeList \"" + st + "\"");
					}
				}
				if (isHour)
				{
					String[] shl = siegeSettings.getProperty("SiegeHourList", "").split(",");
					for (String st : shl)
					{
						if (!st.equalsIgnoreCase(""))
						{
							int val = Integer.valueOf(st);
							if (val > 23 || val < 0)
								System.out.println("[SiegeHourList]: invalid config property -> SiegeHourList \"" + st + "\"");
							else if (val < 12)
								SIEGE_HOUR_LIST_MORNING.add(val);
							else
							{
								val -= 12;
								SIEGE_HOUR_LIST_AFTERNOON.add(val);
							}
						}
					}
					if (Config.SIEGE_HOUR_LIST_AFTERNOON.isEmpty() && Config.SIEGE_HOUR_LIST_AFTERNOON.isEmpty())
					{
						System.out.println("[SiegeHourList]: invalid config property -> SiegeHourList is empty");
						CL_SET_SIEGE_TIME_LIST.remove("hour");
					}
				}
			}
		}
	}

	// *******************************************************************************************
	public static final String	FORTSIEGE_CONFIGURATION_FILE	= "./config/main/events/fortsiege.properties";
	// *******************************************************************************************

	public static int			FORTSIEGE_MAX_ATTACKER;
	public static int			FORTSIEGE_FLAG_MAX_COUNT;
	public static int			FORTSIEGE_CLAN_MIN_LEVEL;
	public static int			FORTSIEGE_LENGTH_MINUTES;
	public static int			FORTSIEGE_COUNTDOWN_LENGTH;
	public static int			FORTSIEGE_MERCHANT_DELAY;

	public static long			FS_TELE_FEE_RATIO;
	public static int			FS_TELE1_FEE;
	public static int			FS_TELE2_FEE;
	public static long			FS_MPREG_FEE_RATIO;
	public static int			FS_MPREG1_FEE;
	public static int			FS_MPREG2_FEE;
	public static long			FS_HPREG_FEE_RATIO;
	public static int			FS_HPREG1_FEE;
	public static int			FS_HPREG2_FEE;
	public static long			FS_EXPREG_FEE_RATIO;
	public static int			FS_EXPREG1_FEE;
	public static int			FS_EXPREG2_FEE;
	public static long			FS_SUPPORT_FEE_RATIO;
	public static int			FS_SUPPORT1_FEE;
	public static int			FS_SUPPORT2_FEE;
	public static int			FS_BLOOD_OATH_COUNT;
	public static int			FS_BLOOD_OATH_FRQ;

	private static final class FortSiegeConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/events/fortsiege";
		}

		@Override
		protected void loadImpl(L2Properties fortSiegeSettings)
		{
			FORTSIEGE_MAX_ATTACKER = Integer.parseInt(fortSiegeSettings.getProperty("AttackerMaxClans", "500"));
			FORTSIEGE_FLAG_MAX_COUNT = Integer.parseInt(fortSiegeSettings.getProperty("MaxFlags", "1"));
			FORTSIEGE_CLAN_MIN_LEVEL = Integer.parseInt(fortSiegeSettings.getProperty("SiegeClanMinLevel", "4"));
			FORTSIEGE_LENGTH_MINUTES = Integer.parseInt(fortSiegeSettings.getProperty("SiegeLength", "60"));
			FORTSIEGE_COUNTDOWN_LENGTH = Integer.decode(fortSiegeSettings.getProperty("CountDownLength", "10"));
			FORTSIEGE_MERCHANT_DELAY = Integer.decode(fortSiegeSettings.getProperty("SuspiciousMerchantRespawnDelay", "180"));

			FS_TELE_FEE_RATIO = Long.parseLong(fortSiegeSettings.getProperty("FortressTeleportFunctionFeeRatio", "604800000"));
			FS_TELE1_FEE = Integer.parseInt(fortSiegeSettings.getProperty("FortressTeleportFunctionFeeLvl1", "1000"));
			FS_TELE2_FEE = Integer.parseInt(fortSiegeSettings.getProperty("FortressTeleportFunctionFeeLvl2", "10000"));
			FS_SUPPORT_FEE_RATIO = Long.parseLong(fortSiegeSettings.getProperty("FortressSupportFunctionFeeRatio", "86400000"));
			FS_SUPPORT1_FEE = Integer.parseInt(fortSiegeSettings.getProperty("FortressSupportFeeLvl1", "7000"));
			FS_SUPPORT2_FEE = Integer.parseInt(fortSiegeSettings.getProperty("FortressSupportFeeLvl2", "17000"));
			FS_MPREG_FEE_RATIO = Long.parseLong(fortSiegeSettings.getProperty("FortressMpRegenerationFunctionFeeRatio", "86400000"));
			FS_MPREG1_FEE = Integer.parseInt(fortSiegeSettings.getProperty("FortressMpRegenerationFeeLvl1", "6500"));
			FS_MPREG2_FEE = Integer.parseInt(fortSiegeSettings.getProperty("FortressMpRegenerationFeeLvl2", "9300"));
			FS_HPREG_FEE_RATIO = Long.parseLong(fortSiegeSettings.getProperty("FortressHpRegenerationFunctionFeeRatio", "86400000"));
			FS_HPREG1_FEE = Integer.parseInt(fortSiegeSettings.getProperty("FortressHpRegenerationFeeLvl1", "2000"));
			FS_HPREG2_FEE = Integer.parseInt(fortSiegeSettings.getProperty("FortressHpRegenerationFeeLvl2", "3500"));
			FS_EXPREG_FEE_RATIO = Long.parseLong(fortSiegeSettings.getProperty("FortressExpRegenerationFunctionFeeRatio", "86400000"));
			FS_EXPREG1_FEE = Integer.parseInt(fortSiegeSettings.getProperty("FortressExpRegenerationFeeLvl1", "9000"));
			FS_EXPREG2_FEE = Integer.parseInt(fortSiegeSettings.getProperty("FortressExpRegenerationFeeLvl2", "10000"));

			FS_BLOOD_OATH_COUNT = Integer.parseInt(fortSiegeSettings.getProperty("FortressBloodOathCount", "1"));
			FS_BLOOD_OATH_FRQ = Integer.parseInt(fortSiegeSettings.getProperty("FortressBloodOathFrequency", "360"));
		}
	}

	// *******************************************************************************************
	public static final String	KRATEI_CUBE_FILE	= "./config/main/fantasy_island.properties";
	// *******************************************************************************************
	public static int			KRATEI_CUBE_MIN_PARTICIPANTS;
	public static int			KRATEI_CUBE_MAX_PARTICIPANTS;
	public static long			KRATEI_CUBE_REGISTRATION_START_TIME;
	public static long			KRATEI_CUBE_REGISTRATION_LENGHT_TIME;
	public static long			KRATEI_CUBE_PREPARATION_TIME;
	public static long			KRATEI_CUBE_MATCH_TIME;
	public static long			KRATEI_CUBE_INSTANCE_EMPTY_DESTROY_TIME;

	public static boolean		ENABLE_BLOCK_CHECKER_EVENT;
	public static int			MIN_BLOCK_CHECKER_TEAM_MEMBERS;
	public static boolean		HBCE_FAIR_PLAY;

	// *******************************************************************************************
	private static final class FantasyIslandConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/fantasy_island";
		}

		@Override
		protected void loadImpl(L2Properties fantasyIslandSettings)
		{
			KRATEI_CUBE_MIN_PARTICIPANTS = Integer.parseInt(fantasyIslandSettings.getProperty("KrateiCubeMinParticipants", "1"));
			KRATEI_CUBE_MAX_PARTICIPANTS = Integer.parseInt(fantasyIslandSettings.getProperty("KrateiCubeMaxParticipants", "20"));
			KRATEI_CUBE_REGISTRATION_START_TIME = Long.parseLong(fantasyIslandSettings.getProperty("KrateiCubeRegistrationStartTime", "1800000"));
			KRATEI_CUBE_REGISTRATION_LENGHT_TIME = Long.parseLong(fantasyIslandSettings.getProperty("KrateiCubeRegistrationLenghtTime", "180000"));
			KRATEI_CUBE_PREPARATION_TIME = Long.parseLong(fantasyIslandSettings.getProperty("KrateiCubePreparationTime", "180000"));
			KRATEI_CUBE_MATCH_TIME = Long.parseLong(fantasyIslandSettings.getProperty("KrateiCubeMatchTime", "1200000"));
			KRATEI_CUBE_INSTANCE_EMPTY_DESTROY_TIME = Long.parseLong(fantasyIslandSettings.getProperty("KrateiCubeInstanceEmptyDestroyTime", "1200000"));

			ENABLE_BLOCK_CHECKER_EVENT = Boolean.valueOf(fantasyIslandSettings.getProperty("EnableBlockCheckerEvent", "false"));
			MIN_BLOCK_CHECKER_TEAM_MEMBERS = Integer.valueOf(fantasyIslandSettings.getProperty("BlockCheckerMinTeamMembers", "2"));
			HBCE_FAIR_PLAY = Boolean.parseBoolean(fantasyIslandSettings.getProperty("HBCEFairPlay", "false"));
		}
	}

	// *******************************************************************************************
	public static final String	TERRITORY_WAR_FILE	= "./config/main/events/territory_war.properties";
	// *******************************************************************************************
	public static int			DEFENDERMAXCLANS;														// Max number of clans
	public static int			DEFENDERMAXPLAYERS;													// Max number of individual player
	public static int			CLANMINLEVEL;
	public static int			PLAYERMINLEVEL;
	public static int			MINTWBADGEFORNOBLESS;
	public static int			MINTWBADGEFORSTRIDERS;
	public static int			MINTWBADGEFORBIGSTRIDER;
	public static Long			WARLENGTH;
	public static boolean		PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean		SPAWN_WARDS_WHEN_TW_IS_NOT_IN_PROGRESS;
	public static boolean		RETURN_WARDS_WHEN_TW_STARTS;

	// *******************************************************************************************
	private static final class TerritoryWarConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/events/territory_war";
		}

		@Override
		protected void loadImpl(L2Properties territoryWarSettings)
		{
			// Siege setting
			DEFENDERMAXCLANS = Integer.decode(territoryWarSettings.getProperty("DefenderMaxClans", "500"));
			DEFENDERMAXPLAYERS = Integer.decode(territoryWarSettings.getProperty("DefenderMaxPlayers", "500"));
			CLANMINLEVEL = Integer.decode(territoryWarSettings.getProperty("ClanMinLevel", "0"));
			PLAYERMINLEVEL = Integer.decode(territoryWarSettings.getProperty("PlayerMinLevel", "40"));
			WARLENGTH = Long.decode(territoryWarSettings.getProperty("WarLength", "120")) * 60000;
			PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(territoryWarSettings
					.getProperty("PlayerWithWardCanBeKilledInPeaceZone", "False"));
			SPAWN_WARDS_WHEN_TW_IS_NOT_IN_PROGRESS = Boolean.parseBoolean(territoryWarSettings.getProperty("SpawnWardsWhenTWIsNotInProgress", "False"));
			RETURN_WARDS_WHEN_TW_STARTS = Boolean.parseBoolean(territoryWarSettings.getProperty("ReturnWardsWhenTWStarts", "False"));
			MINTWBADGEFORNOBLESS = Integer.decode(territoryWarSettings.getProperty("MinTerritoryBadgeForNobless", "100"));
			MINTWBADGEFORSTRIDERS = Integer.decode(territoryWarSettings.getProperty("MinTerritoryBadgeForStriders", "50"));
			MINTWBADGEFORBIGSTRIDER = Integer.decode(territoryWarSettings.getProperty("MinTerritoryBadgeForBigStrider", "80"));
		}
	}

	// *******************************************************************************************
	public static final String	HEXID_FILE	= "./config/network/hexid.txt";
	// *******************************************************************************************
	public static byte[]		HEX_ID;									// Hexadecimal ID of the game server
	/** Server ID used with the HexID */
	public static int			SERVER_ID;

	// *******************************************************************************************
	private static void loadHexId()
	{
		try
		{
			L2Properties Settings = new L2Properties(HEXID_FILE);

			SERVER_ID = Integer.parseInt(Settings.getProperty("ServerID"));
			HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
		}
		catch (Exception e)
		{
			_log.warn("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
		}
	}

	// *******************************************************************************************
	public static final String					COMMAND_PRIVILEGES_FILE	= "./config/administration/command-privileges.properties";
	// *******************************************************************************************
	public static final Map<String, Integer>	GM_COMMAND_PRIVILEGES	= new FastMap<String, Integer>();

	// *******************************************************************************************
	private static final class CommandPrivilegesConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "administration/command-privileges";
		}

		@Override
		protected void loadImpl(L2Properties commandPrivileges)
		{
			for (Map.Entry<Object, Object> _command : commandPrivileges.entrySet())
			{
				String command = String.valueOf(_command.getKey());
				String commandLevel = String.valueOf(_command.getValue()).trim();

				int accessLevel = GM_ACCESSLEVEL;

				try
				{
					accessLevel = Integer.parseInt(commandLevel);
				}
				catch (Exception e)
				{
					_log.warn("Failed to parse command \"" + command + "\"!", e);
				}

				GM_COMMAND_PRIVILEGES.put(command, accessLevel);
			}
		}
	}

	// *******************************************************************************************
	public static final String	SEVENSIGNS_FILE	= "./config/main/events/sevensigns.properties";
	// *******************************************************************************************
	public static boolean		ALT_GAME_CASTLE_DAWN;											// Alternative gaming - players must be in a castle-owning clan or ally to sign up for Dawn.
	public static boolean		ALT_GAME_CASTLE_DUSK;											// Alternative gaming - players being in a castle-owning clan or ally cannot sign up for Dusk.
	public static int			ALT_FESTIVAL_MIN_PLAYER;										// Minimum number of player to participate in SevenSigns Festival
	public static long			ALT_MAXIMUM_PLAYER_CONTRIB;									// Maximum of player contrib during Festival
	public static long			ALT_FESTIVAL_MANAGER_START;									// Festival Manager start time.
	public static long			ALT_FESTIVAL_LENGTH;											// Festival Length
	public static long			ALT_FESTIVAL_CYCLE_LENGTH;										// Festival Cycle Length
	public static long			ALT_FESTIVAL_FIRST_SPAWN;										// Festival First Spawn
	public static long			ALT_FESTIVAL_FIRST_SWARM;										// Festival First Swarm
	public static long			ALT_FESTIVAL_SECOND_SPAWN;										// Festival Second Spawn
	public static long			ALT_FESTIVAL_SECOND_SWARM;										// Festival Second Swarm
	public static long			ALT_FESTIVAL_CHEST_SPAWN;										// Festival Chest Spawn
	public static int			ALT_FESTIVAL_ARCHER_AGGRO;										// Aggro value of Archer in SevenSigns Festival
	public static int			ALT_FESTIVAL_CHEST_AGGRO;										// Aggro value of Chest in SevenSigns Festival
	public static int			ALT_FESTIVAL_MONSTER_AGGRO;									// Aggro value of Monster in SevenSigns Festival
	public static long			ALT_DAWN_JOIN_COST;											// Amount of adena to pay to join Dawn Cabal

	public static double		ALT_SIEGE_DAWN_GATES_PDEF_MULT;
	public static double		ALT_SIEGE_DUSK_GATES_PDEF_MULT;
	public static double		ALT_SIEGE_DAWN_GATES_MDEF_MULT;
	public static double		ALT_SIEGE_DUSK_GATES_MDEF_MULT;

	// *******************************************************************************************
	private static final class SevenSignsConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/events/sevensigns";
		}

		@Override
		protected void loadImpl(L2Properties SevenSettings)
		{
			ALT_GAME_CASTLE_DAWN = Boolean.parseBoolean(SevenSettings.getProperty("AltCastleForDawn", "True"));
			ALT_GAME_CASTLE_DUSK = Boolean.parseBoolean(SevenSettings.getProperty("AltCastleForDusk", "True"));
			ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(SevenSettings.getProperty("AltFestivalMinPlayer", "5"));
			ALT_MAXIMUM_PLAYER_CONTRIB = Long.parseLong(SevenSettings.getProperty("AltMaxPlayerContrib", "1000000"));
			ALT_FESTIVAL_MANAGER_START = Long.parseLong(SevenSettings.getProperty("AltFestivalManagerStart", "120000"));
			ALT_FESTIVAL_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalLength", "1080000"));
			ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalCycleLength", "2280000"));
			ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSpawn", "120000"));
			ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSwarm", "300000"));
			ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSpawn", "540000"));
			ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSwarm", "720000"));
			ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalChestSpawn", "900000"));
			ALT_FESTIVAL_ARCHER_AGGRO = Integer.parseInt(SevenSettings.getProperty("AltFestivalArcherAggro", "200"));
			ALT_FESTIVAL_CHEST_AGGRO = Integer.parseInt(SevenSettings.getProperty("AltFestivalChestAggro", "0"));
			ALT_FESTIVAL_MONSTER_AGGRO = Integer.parseInt(SevenSettings.getProperty("AltFestivalMonsterAggro", "200"));
			ALT_DAWN_JOIN_COST = Long.parseLong(SevenSettings.getProperty("AltJoinDawnCost", "50000"));

			ALT_SIEGE_DAWN_GATES_PDEF_MULT = Double.parseDouble(SevenSettings.getProperty("AltDawnGatesPdefMult", "1.1"));
			ALT_SIEGE_DUSK_GATES_PDEF_MULT = Double.parseDouble(SevenSettings.getProperty("AltDuskGatesPdefMult", "0.8"));
			ALT_SIEGE_DAWN_GATES_MDEF_MULT = Double.parseDouble(SevenSettings.getProperty("AltDawnGatesMdefMult", "1.1"));
			ALT_SIEGE_DUSK_GATES_MDEF_MULT = Double.parseDouble(SevenSettings.getProperty("AltDuskGatesMdefMult", "0.8"));
		}
	}

	// *******************************************************************************************
	public static final String	CLANHALL_FILE	= "./config/main/clanhall.properties";
	// *******************************************************************************************
	/** Clan Hall function related configs */
	public static long			CH_TELE_FEE_RATIO;
	public static int			CH_TELE1_FEE;
	public static int			CH_TELE2_FEE;
	public static long			CH_ITEM_FEE_RATIO;
	public static int			CH_ITEM1_FEE;
	public static int			CH_ITEM2_FEE;
	public static int			CH_ITEM3_FEE;
	public static long			CH_MPREG_FEE_RATIO;
	public static int			CH_MPREG1_FEE;
	public static int			CH_MPREG2_FEE;
	public static int			CH_MPREG3_FEE;
	public static int			CH_MPREG4_FEE;
	public static int			CH_MPREG5_FEE;
	public static long			CH_HPREG_FEE_RATIO;
	public static int			CH_HPREG1_FEE;
	public static int			CH_HPREG2_FEE;
	public static int			CH_HPREG3_FEE;
	public static int			CH_HPREG4_FEE;
	public static int			CH_HPREG5_FEE;
	public static int			CH_HPREG6_FEE;
	public static int			CH_HPREG7_FEE;
	public static int			CH_HPREG8_FEE;
	public static int			CH_HPREG9_FEE;
	public static int			CH_HPREG10_FEE;
	public static int			CH_HPREG11_FEE;
	public static int			CH_HPREG12_FEE;
	public static int			CH_HPREG13_FEE;
	public static long			CH_EXPREG_FEE_RATIO;
	public static int			CH_EXPREG1_FEE;
	public static int			CH_EXPREG2_FEE;
	public static int			CH_EXPREG3_FEE;
	public static int			CH_EXPREG4_FEE;
	public static int			CH_EXPREG5_FEE;
	public static int			CH_EXPREG6_FEE;
	public static int			CH_EXPREG7_FEE;
	public static long			CH_SUPPORT_FEE_RATIO;
	public static int			CH_SUPPORT1_FEE;
	public static int			CH_SUPPORT2_FEE;
	public static int			CH_SUPPORT3_FEE;
	public static int			CH_SUPPORT4_FEE;
	public static int			CH_SUPPORT5_FEE;
	public static int			CH_SUPPORT6_FEE;
	public static int			CH_SUPPORT7_FEE;
	public static int			CH_SUPPORT8_FEE;
	public static long			CH_CURTAIN_FEE_RATIO;
	public static int			CH_CURTAIN1_FEE;
	public static int			CH_CURTAIN2_FEE;
	public static long			CH_FRONT_FEE_RATIO;
	public static int			CH_FRONT1_FEE;
	public static int			CH_FRONT2_FEE;

	// *******************************************************************************************
	private static final class ClanHallConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/clanhall";
		}

		@Override
		protected void loadImpl(L2Properties clanhallSettings)
		{
			CH_TELE_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeRatio", "604800000"));
			CH_TELE1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl1", "7000"));
			CH_TELE2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl2", "14000"));
			CH_SUPPORT_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallSupportFunctionFeeRatio", "86400000"));
			CH_SUPPORT1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl1", "2500"));
			CH_SUPPORT2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl2", "5000"));
			CH_SUPPORT3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl3", "7000"));
			CH_SUPPORT4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl4", "11000"));
			CH_SUPPORT5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl5", "21000"));
			CH_SUPPORT6_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl6", "36000"));
			CH_SUPPORT7_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl7", "37000"));
			CH_SUPPORT8_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl8", "52000"));
			CH_MPREG_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallMpRegenerationFunctionFeeRatio", "86400000"));
			CH_MPREG1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl1", "2000"));
			CH_MPREG2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl2", "3750"));
			CH_MPREG3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl3", "6500"));
			CH_MPREG4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl4", "13750"));
			CH_MPREG5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl5", "20000"));
			CH_HPREG_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallHpRegenerationFunctionFeeRatio", "86400000"));
			CH_HPREG1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl1", "700"));
			CH_HPREG2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl2", "800"));
			CH_HPREG3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl3", "1000"));
			CH_HPREG4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl4", "1166"));
			CH_HPREG5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl5", "1500"));
			CH_HPREG6_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl6", "1750"));
			CH_HPREG7_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl7", "2000"));
			CH_HPREG8_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl8", "2250"));
			CH_HPREG9_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl9", "2500"));
			CH_HPREG10_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl10", "3250"));
			CH_HPREG11_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl11", "3270"));
			CH_HPREG12_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl12", "4250"));
			CH_HPREG13_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl13", "5166"));
			CH_EXPREG_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallExpRegenerationFunctionFeeRatio", "86400000"));
			CH_EXPREG1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl1", "3000"));
			CH_EXPREG2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl2", "6000"));
			CH_EXPREG3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl3", "9000"));
			CH_EXPREG4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl4", "15000"));
			CH_EXPREG5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl5", "21000"));
			CH_EXPREG6_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl6", "23330"));
			CH_EXPREG7_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl7", "30000"));
			CH_ITEM_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeRatio", "86400000"));
			CH_ITEM1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl1", "30000"));
			CH_ITEM2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl2", "70000"));
			CH_ITEM3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl3", "140000"));
			CH_CURTAIN_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeRatio", "86400000"));
			CH_CURTAIN1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl1", "2000"));
			CH_CURTAIN2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl2", "2500"));
			CH_FRONT_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeRatio", "259200000"));
			CH_FRONT1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "1300"));
			CH_FRONT2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "4000"));
		}
	}

	// *******************************************************************************************
	public static final String	CASTLE_FILE	= "./config/main/castle.properties";
	// *******************************************************************************************
	/** Clan Hall function related configs */
	public static long			CS_TELE_FEE_RATIO;
	public static int			CS_TELE1_FEE;
	public static int			CS_TELE2_FEE;
	public static long			CS_MPREG_FEE_RATIO;
	public static int			CS_MPREG1_FEE;
	public static int			CS_MPREG2_FEE;
	public static int			CS_MPREG3_FEE;
	public static int			CS_MPREG4_FEE;
	public static long			CS_HPREG_FEE_RATIO;
	public static int			CS_HPREG1_FEE;
	public static int			CS_HPREG2_FEE;
	public static int			CS_HPREG3_FEE;
	public static int			CS_HPREG4_FEE;
	public static int			CS_HPREG5_FEE;
	public static long			CS_EXPREG_FEE_RATIO;
	public static int			CS_EXPREG1_FEE;
	public static int			CS_EXPREG2_FEE;
	public static int			CS_EXPREG3_FEE;
	public static int			CS_EXPREG4_FEE;
	public static long			CS_SUPPORT_FEE_RATIO;
	public static int			CS_SUPPORT1_FEE;
	public static int			CS_SUPPORT2_FEE;
	public static int			CS_SUPPORT3_FEE;
	public static int			CS_SUPPORT4_FEE;
	public static int			CS_TRAP1_FEE;
	public static int			CS_TRAP2_FEE;
	public static int			CS_TRAP3_FEE;
	public static int			CS_TRAP4_FEE;
	public static int			CS_REINFORCE_OUTER1_FEE;
	public static int			CS_REINFORCE_OUTER2_FEE;
	public static int			CS_REINFORCE_OUTER3_FEE;
	public static int			CS_REINFORCE_INNER1_FEE;
	public static int			CS_REINFORCE_INNER2_FEE;
	public static int			CS_REINFORCE_INNER3_FEE;
	public static int			CS_REINFORCE_WALL1_FEE;
	public static int			CS_REINFORCE_WALL2_FEE;
	public static int			CS_REINFORCE_WALL3_FEE;
	public static long			CS_SECURITY_FEE_RATIO;
	public static int			CS_SECURITY1_FEE;
	public static int			CS_SECURITY2_FEE;
	public static int			CS_SECURITY3_FEE;

	// *******************************************************************************************
	private static final class CastleConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/castle";
		}

		@Override
		protected void loadImpl(L2Properties castleSettings)
		{
			CS_TELE_FEE_RATIO = Long.parseLong(castleSettings.getProperty("CastleTeleportFunctionFeeRatio", "604800000"));
			CS_TELE1_FEE = Integer.parseInt(castleSettings.getProperty("CastleTeleportFunctionFeeLvl1", "7000"));
			CS_TELE2_FEE = Integer.parseInt(castleSettings.getProperty("CastleTeleportFunctionFeeLvl2", "14000"));
			CS_SUPPORT_FEE_RATIO = Long.parseLong(castleSettings.getProperty("CastleSupportFunctionFeeRatio", "86400000"));
			CS_SUPPORT1_FEE = Integer.parseInt(castleSettings.getProperty("CastleSupportFeeLvl1", "7000"));
			CS_SUPPORT2_FEE = Integer.parseInt(castleSettings.getProperty("CastleSupportFeeLvl2", "21000"));
			CS_SUPPORT3_FEE = Integer.parseInt(castleSettings.getProperty("CastleSupportFeeLvl3", "37000"));
			CS_SUPPORT4_FEE = Integer.parseInt(castleSettings.getProperty("CastleSupportFeeLvl4", "52000"));
			CS_MPREG_FEE_RATIO = Long.parseLong(castleSettings.getProperty("CastleMpRegenerationFunctionFeeRatio", "86400000"));
			CS_MPREG1_FEE = Integer.parseInt(castleSettings.getProperty("CastleMpRegenerationFeeLvl1", "2000"));
			CS_MPREG2_FEE = Integer.parseInt(castleSettings.getProperty("CastleMpRegenerationFeeLvl2", "6500"));
			CS_MPREG3_FEE = Integer.parseInt(castleSettings.getProperty("CastleMpRegenerationFeeLvl3", "13750"));
			CS_MPREG4_FEE = Integer.parseInt(castleSettings.getProperty("CastleMpRegenerationFeeLvl4", "20000"));
			CS_HPREG_FEE_RATIO = Long.parseLong(castleSettings.getProperty("CastleHpRegenerationFunctionFeeRatio", "86400000"));
			CS_HPREG1_FEE = Integer.parseInt(castleSettings.getProperty("CastleHpRegenerationFeeLvl1", "1000"));
			CS_HPREG2_FEE = Integer.parseInt(castleSettings.getProperty("CastleHpRegenerationFeeLvl2", "1500"));
			CS_HPREG3_FEE = Integer.parseInt(castleSettings.getProperty("CastleHpRegenerationFeeLvl3", "2250"));
			CS_HPREG4_FEE = Integer.parseInt(castleSettings.getProperty("CastleHpRegenerationFeeLvl4", "3270"));
			CS_HPREG5_FEE = Integer.parseInt(castleSettings.getProperty("CastleHpRegenerationFeeLvl5", "5166"));
			CS_EXPREG_FEE_RATIO = Long.parseLong(castleSettings.getProperty("CastleExpRegenerationFunctionFeeRatio", "86400000"));
			CS_EXPREG1_FEE = Integer.parseInt(castleSettings.getProperty("CastleExpRegenerationFeeLvl1", "9000"));
			CS_EXPREG2_FEE = Integer.parseInt(castleSettings.getProperty("CastleExpRegenerationFeeLvl2", "15000"));
			CS_EXPREG3_FEE = Integer.parseInt(castleSettings.getProperty("CastleExpRegenerationFeeLvl3", "21000"));
			CS_EXPREG4_FEE = Integer.parseInt(castleSettings.getProperty("CastleExpRegenerationFeeLvl4", "30000"));
			CS_TRAP1_FEE = Integer.parseInt(castleSettings.getProperty("CastleTrapFeeLvl1", "3000000"));
			CS_TRAP2_FEE = Integer.parseInt(castleSettings.getProperty("CastleTrapFeeLvl2", "6000000"));
			CS_TRAP3_FEE = Integer.parseInt(castleSettings.getProperty("CastleTrapFeeLvl3", "9000000"));
			CS_TRAP4_FEE = Integer.parseInt(castleSettings.getProperty("CastleTrapFeeLvl4", "12000000"));
			CS_REINFORCE_OUTER1_FEE = Integer.parseInt(castleSettings.getProperty("CastleReinforceOuterFeeLvl1", "750000"));
			CS_REINFORCE_OUTER2_FEE = Integer.parseInt(castleSettings.getProperty("CastleReinforceOuterFeeLvl2", "4000000"));
			CS_REINFORCE_OUTER3_FEE = Integer.parseInt(castleSettings.getProperty("CastleReinforceOuterFeeLvl3", "5000000"));
			CS_REINFORCE_INNER1_FEE = Integer.parseInt(castleSettings.getProperty("CastleReinforceInnerFeeLvl1", "1600000"));
			CS_REINFORCE_INNER2_FEE = Integer.parseInt(castleSettings.getProperty("CastleReinforceInnerFeeLvl2", "1800000"));
			CS_REINFORCE_INNER3_FEE = Integer.parseInt(castleSettings.getProperty("CastleReinforceInnerFeeLvl3", "3000000"));
			CS_REINFORCE_WALL1_FEE = Integer.parseInt(castleSettings.getProperty("CastleReinforceWallsFeeLvl1", "1600000"));
			CS_REINFORCE_WALL2_FEE = Integer.parseInt(castleSettings.getProperty("CastleReinforceWallsFeeLvl2", "1800000"));
			CS_REINFORCE_WALL3_FEE = Integer.parseInt(castleSettings.getProperty("CastleReinforceWallsFeeLvl3", "3000000"));
			CS_SECURITY_FEE_RATIO = Long.parseLong(castleSettings.getProperty("CastleSecurityFunctionFeeRatio", "86400000"));
			CS_SECURITY1_FEE = Integer.parseInt(castleSettings.getProperty("CastleSecurityFeeLvl1", "1000000"));
			CS_SECURITY2_FEE = Integer.parseInt(castleSettings.getProperty("CastleSecurityFeeLvl2", "1000000"));
			CS_SECURITY3_FEE = Integer.parseInt(castleSettings.getProperty("CastleSecurityFeeLvl3", "1000000"));
		}
	}

	// *******************************************************************************************

	// *******************************************************************************************
	public static final String	FUN_ENGINES_FILE		= "./config/mods/fun_engines.properties";
	// *******************************************************************************************
	public static boolean		ARENA_ENABLED;
	public static int			ARENA_INTERVAL;
	public static int			ARENA_REWARD_ID;
	public static int			ARENA_REWARD_COUNT;
	public static boolean		FISHERMAN_ENABLED;
	public static int			FISHERMAN_INTERVAL;
	public static int			FISHERMAN_REWARD_ID;
	public static int			FISHERMAN_REWARD_COUNT;

	public static boolean		TVT_ENABLED;

	public static long			TVT_DELAY_INITIAL_REGISTRATION;
	public static long			TVT_DELAY_BETWEEN_EVENTS;
	public static long			TVT_PERIOD_LENGHT_REGISTRATION;
	public static long			TVT_PERIOD_LENGHT_PREPARATION;
	public static long			TVT_PERIOD_LENGHT_EVENT;
	public static long			TVT_PERIOD_LENGHT_REWARDS;

	public static int			TVT_REGISTRATION_ANNOUNCEMENT_COUNT;

	public static int			MINIMUM_LEVEL_FOR_TVT;
	public static int			MAXIMUM_LEVEL_FOR_TVT;

	public static String		TVT_INSTANCE_FILE;
	public static int			TVT_START_LEAVE_TELEPORT_DELAY;

	public static int			TVT_PARTICIPANTS_MAX;
	public static int			TVT_PARTICIPANTS_MIN;

	public static long			TVT_REVIVE_DELAY;

	public static String		TVT_FIRST_TEAM_NAME;
	public static String		TVT_SECOND_TEAM_NAME;

	public static int			TVT_FIRST_TEAM_COLOR;
	public static int			TVT_SECOND_TEAM_COLOR;

	public static int[]			TVT_FIRST_TEAM_COORDS	= new int[3];
	public static int[]			TVT_SECOND_TEAM_COORDS	= new int[3];

	public static int			REQUIRED_KILLS_FOR_REWARD;
	public static int[]			TVT_REWARD_IDS;
	public static long[]		TVT_REWARD_COUNT;

	// *******************************************************************************************
	// *******************************************************************************************
	private static final class FunEnginesConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/fun_engines";
		}

		@Override
		protected void loadImpl(L2Properties funEnginesSettings)
		{
			ARENA_ENABLED = Boolean.parseBoolean(funEnginesSettings.getProperty("ArenaEnabled", "false"));
			ARENA_INTERVAL = Integer.parseInt(funEnginesSettings.getProperty("ArenaInterval", "60"));
			ARENA_REWARD_ID = Integer.parseInt(funEnginesSettings.getProperty("ArenaRewardId", "57"));
			ARENA_REWARD_COUNT = Integer.parseInt(funEnginesSettings.getProperty("ArenaRewardCount", "100"));

			FISHERMAN_ENABLED = Boolean.parseBoolean(funEnginesSettings.getProperty("FishermanEnabled", "false"));
			FISHERMAN_INTERVAL = Integer.parseInt(funEnginesSettings.getProperty("FishermanInterval", "60"));
			FISHERMAN_REWARD_ID = Integer.parseInt(funEnginesSettings.getProperty("FishermanRewardId", "57"));
			FISHERMAN_REWARD_COUNT = Integer.parseInt(funEnginesSettings.getProperty("FishermanRewardCount", "100"));

			TVT_ENABLED = Boolean.parseBoolean(funEnginesSettings.getProperty("TvTEnabled", "false"));

			TVT_DELAY_INITIAL_REGISTRATION = Long.parseLong(funEnginesSettings.getProperty("TvTDelayInitial", "900000"));
			TVT_DELAY_BETWEEN_EVENTS = Long.parseLong(funEnginesSettings.getProperty("TvTDelayBetweenEvents", "900000"));
			TVT_PERIOD_LENGHT_REGISTRATION = Long.parseLong(funEnginesSettings.getProperty("TvTLengthRegistration", "300000"));
			TVT_PERIOD_LENGHT_PREPARATION = Long.parseLong(funEnginesSettings.getProperty("TvTLengthPreparation", "50000"));
			TVT_PERIOD_LENGHT_EVENT = Long.parseLong(funEnginesSettings.getProperty("TvTLengthCombat", "240000"));
			TVT_PERIOD_LENGHT_REWARDS = Long.parseLong(funEnginesSettings.getProperty("TvTLengthRewards", "15000"));
			TVT_REGISTRATION_ANNOUNCEMENT_COUNT = Integer.parseInt(funEnginesSettings.getProperty("TvTAnnounceRegistration", "3"));

			MINIMUM_LEVEL_FOR_TVT = Integer.parseInt(funEnginesSettings.getProperty("MinimumLevelForTvT", "1"));
			MAXIMUM_LEVEL_FOR_TVT = Integer.parseInt(funEnginesSettings.getProperty("MaximumLevelForTvT", "85"));

			TVT_INSTANCE_FILE = funEnginesSettings.getProperty("TvTInstanceFile", "coliseum.xml");
			TVT_START_LEAVE_TELEPORT_DELAY = Integer.parseInt(funEnginesSettings.getProperty("TvTStartLeaveTeleportDelay", "20000"));

			TVT_PARTICIPANTS_MAX = Integer.parseInt(funEnginesSettings.getProperty("TvTMaxParticipants", "16"));
			TVT_PARTICIPANTS_MIN = Integer.parseInt(funEnginesSettings.getProperty("TvTMinParticipants", "4"));

			TVT_REVIVE_DELAY = Integer.parseInt(funEnginesSettings.getProperty("TvTReviveDelay", "5000"));

			TVT_FIRST_TEAM_NAME = funEnginesSettings.getProperty("TvTFirstTeamName", "greens");
			TVT_SECOND_TEAM_NAME = funEnginesSettings.getProperty("TvTSecondTeamName", "yellows");

			TVT_FIRST_TEAM_COLOR = Integer.decode("0x" + funEnginesSettings.getProperty("TvTFirstTeamColor", "00FF00"));
			TVT_SECOND_TEAM_COLOR = Integer.decode("0x" + funEnginesSettings.getProperty("TvTSecondTeamColor", "00CCFF"));

			String[] firstTeamCoords = funEnginesSettings.getProperty("TvTFirstTeamCoords", "0,0,0").split(",");
			TVT_FIRST_TEAM_COORDS[0] = Integer.parseInt(firstTeamCoords[0]);
			TVT_FIRST_TEAM_COORDS[1] = Integer.parseInt(firstTeamCoords[1]);
			TVT_FIRST_TEAM_COORDS[2] = Integer.parseInt(firstTeamCoords[2]);

			String[] secondTeamCoords = funEnginesSettings.getProperty("TvTSecondTeamCoords", "0,0,0").split(",");
			TVT_SECOND_TEAM_COORDS[0] = Integer.parseInt(secondTeamCoords[0]);
			TVT_SECOND_TEAM_COORDS[1] = Integer.parseInt(secondTeamCoords[1]);
			TVT_SECOND_TEAM_COORDS[2] = Integer.parseInt(secondTeamCoords[2]);

			REQUIRED_KILLS_FOR_REWARD = Integer.parseInt(funEnginesSettings.getProperty("RequiredKillsForReward", "3"));
			StringTokenizer coords;
			StringTokenizer locations = new StringTokenizer(funEnginesSettings.getProperty("TvTRewards", ""), ";");
			TVT_REWARD_IDS = new int[locations.countTokens()];
			TVT_REWARD_COUNT = new long[locations.countTokens()];
			for (int i = 0; i < TVT_REWARD_IDS.length; i++)
			{
				coords = new StringTokenizer(locations.nextToken(), ",");
				if (coords.countTokens() == 2)
				{
					TVT_REWARD_IDS[i] = Integer.parseInt(coords.nextToken());
					TVT_REWARD_COUNT[i] = Integer.parseInt(coords.nextToken());
				}
				else
					throw new IllegalArgumentException("Cannot parse TvTRewards!");
			}
		}
	}

	// *******************************************************************************************
	public static final String	BOSS_FILE	= "./config/main/boss.properties";

	// *******************************************************************************************

	// *******************************************************************************************
	private static final class BossConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/boss";
		}

		@Override
		protected void loadImpl(L2Properties bossSettings)
		{

		}
	}

	// *******************************************************************************************
	public static final String	CHAT_FILTER_CONFIG	= "./config/chat/chat_filter.properties";
	// *******************************************************************************************
	public static boolean		USE_CHAT_FILTER;
	public static String		CHAT_FILTER_CHARS;
	public static String		CHAT_FILTER_PUNISHMENT;
	public static int			CHAT_FILTER_PUNISHMENT_PARAM1;

	// *******************************************************************************************
	private static final class ChatFilterConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "chat/chat_filter";
		}

		@Override
		protected void loadImpl(L2Properties filterSettings)
		{
			USE_CHAT_FILTER = Boolean.parseBoolean(filterSettings.getProperty("UseChatFilter", "False"));
			CHAT_FILTER_CHARS = filterSettings.getProperty("ChatFilterChars", "***");
			CHAT_FILTER_PUNISHMENT = filterSettings.getProperty("ChatFilterPunishment", "warn");
			CHAT_FILTER_PUNISHMENT_PARAM1 = Integer.parseInt(filterSettings.getProperty("ChatFilterPunishmentParam1", "1"));
		}
	}

	// *******************************************************************************************
	public static final String	CHAT_FILTER_FILE	= "./config/chat/chatfilter.txt";

	// *******************************************************************************************
	private static final class ChatFilter extends ConfigFileLoader
	{
		@Override
		protected String getFileName()
		{
			return CHAT_FILTER_FILE;
		}

		@Override
		protected String getName()
		{
			return "chat/chatfilter";
		}

		@Override
		protected void loadReader(BufferedReader reader) throws Exception
		{
			try
			{
				FILTER_LIST = new Pattern[0];

				if (USE_CHAT_FILTER)
				{
					for (String line; (line = reader.readLine()) != null;)
					{
						line = line.trim();

						if (line.length() == 0 || line.startsWith("#"))
							continue;

						final Pattern pattern = Pattern.compile(line);

						FILTER_LIST = (Pattern[]) ArrayUtils.add(FILTER_LIST, pattern);
					}

					_log.info("Say Filter: Loaded " + FILTER_LIST.length + " words");
				}
			}
			finally
			{
				IOUtils.closeQuietly(reader);
			}
		}
	}

	// *******************************************************************************************
	public static final String	VITALITY_FILE	= "./config/main/vitality.properties";
	// *******************************************************************************************
	public static boolean		ENABLE_VITALITY;
	public static boolean		RECOVER_VITALITY_ON_RECONNECT;
	public static boolean		ENABLE_VITALITY_CHAMPION;
	public static boolean		ENABLE_DROP_VITALITY_HERBS;
	public static float			DECREASE_VITALITY;
	public static float			RATE_VITALITY_LEVEL_1;
	public static float			RATE_VITALITY_LEVEL_2;
	public static float			RATE_VITALITY_LEVEL_3;
	public static float			RATE_VITALITY_LEVEL_4;
	public static float			RATE_DROP_VITALITY_HERBS;
	public static float			RATE_RECOVERY_VITALITY_PEACE_ZONE;
	public static float			RATE_VITALITY_LOST;
	public static float			RATE_VITALITY_GAIN;
	public static float			RATE_RECOVERY_ON_RECONNECT;

	// *******************************************************************************************
	private static final class VitalityConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/vitality";
		}

		@Override
		protected void loadImpl(L2Properties vitalitySettings)
		{
			ENABLE_VITALITY = Boolean.parseBoolean(vitalitySettings.getProperty("EnableVitality", "True"));
			RECOVER_VITALITY_ON_RECONNECT = Boolean.parseBoolean(vitalitySettings.getProperty("RecoverVitalityOnReconnect", "True"));
			ENABLE_VITALITY_CHAMPION = Boolean.parseBoolean(vitalitySettings.getProperty("EnableVitalityOnChampion", "False"));
			ENABLE_DROP_VITALITY_HERBS = Boolean.parseBoolean(vitalitySettings.getProperty("EnableVitalityHerbs", "True"));
			DECREASE_VITALITY = Float.parseFloat(vitalitySettings.getProperty("VitalityDecreaseLevel", "10"));
			RATE_VITALITY_LEVEL_1 = Float.parseFloat(vitalitySettings.getProperty("RateVitalityLevel1", "1.5"));
			RATE_VITALITY_LEVEL_2 = Float.parseFloat(vitalitySettings.getProperty("RateVitalityLevel2", "2."));
			RATE_VITALITY_LEVEL_3 = Float.parseFloat(vitalitySettings.getProperty("RateVitalityLevel3", "2.5"));
			RATE_VITALITY_LEVEL_4 = Float.parseFloat(vitalitySettings.getProperty("RateVitalityLevel4", "3."));
			RATE_DROP_VITALITY_HERBS = Float.parseFloat(vitalitySettings.getProperty("RateVitalityHerbs", "2."));
			RATE_RECOVERY_VITALITY_PEACE_ZONE = Float.parseFloat(vitalitySettings.getProperty("RateRecoveryPeaceZone", "1."));
			RATE_VITALITY_LOST = Float.parseFloat(vitalitySettings.getProperty("RateVitalityLost", "1."));
			RATE_VITALITY_GAIN = Float.parseFloat(vitalitySettings.getProperty("RateVitalityGain", "1."));
			RATE_RECOVERY_ON_RECONNECT = Float.parseFloat(vitalitySettings.getProperty("RateRecoveryOnReconnect", "4."));
		}
	}

	// *******************************************************************************************
	public static final String	BANKING_FILE	= "./config/mods/banking.properties";
	// *******************************************************************************************
	public static boolean		BANKING_SYSTEM_ENABLED;
	public static int			BANKING_SYSTEM_GOLDBARS;
	public static int			BANKING_SYSTEM_ADENA;

	// *******************************************************************************************
	private static final class BankingConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "mods/banking";
		}

		@Override
		protected void loadImpl(L2Properties bankingSettings)
		{
			BANKING_SYSTEM_ENABLED = Boolean.parseBoolean(bankingSettings.getProperty("BankingEnabled", "false"));
			BANKING_SYSTEM_GOLDBARS = Integer.parseInt(bankingSettings.getProperty("BankingGoldbarCount", "1"));
			BANKING_SYSTEM_ADENA = Integer.parseInt(bankingSettings.getProperty("BankingAdenaCount", "500000000"));
		}
	}

	// *******************************************************************************************
	public static final String	CUSTOM_FILE				= "./config/main/custom.properties";
	// *******************************************************************************************
	public static String		SERVER_NAME;
	public static boolean		SHOW_LICENSE;													// Show License at login
	public static boolean		SHOW_HTML_WELCOME;												// Show html window at login
	public static boolean		SHOW_HTML_NEWBIE;
	public static boolean		SHOW_HTML_GM;
	public static int			LEVEL_HTML_NEWBIE;												// Show newbie html when player's level is < to define level
	public static boolean		CHAR_VIP_SKIP_SKILLS_CHECK;									// VIP Characters configuration
	public static boolean		CHAR_VIP_COLOR_ENABLED;										// VIP Characters configuration
	public static int			CHAR_VIP_COLOR;												// VIP Characters configuration
	public static boolean		ONLINE_PLAYERS_AT_STARTUP;										// Show Online Players announce
	public static int			ONLINE_PLAYERS_ANNOUNCE_INTERVAL;

	public static boolean		ANNOUNCE_CASTLE_LORDS_LOGIN;

	public static boolean		ANNOUNCE_7S_AT_START_UP;

	public static boolean		ALLOW_QUAKE_SYSTEM;

	public static boolean		ALT_MANA_POTIONS;

	public static int			MANA_POTION_LVL;

	public static boolean		ALLOW_CUSTOM_STARTER_ITEMS;
	public static List<int[]>	CUSTOM_STARTER_ITEMS	= new FastList<int[]>();

	public static boolean		ALLOW_PVP_REWARD;
	public static int			PVP_REWARD_ITEM_ID;
	public static int			PVP_REWARD_ITEM_AMOUNT;

	public static boolean		ALLOW_NEW_CHARACTER_TITLE;
	public static String		NEW_CHARACTER_TITLE;

	public static int			DONATOR_NAME_COLOR;
	public static int			DONATOR_TITLE_COLOR;

	public static boolean		ALLOW_LEVEL_DIFFERENCE;
	public static int			LEVEL_DIFFERENCE;

	public static boolean		ALT_ANTIFEED_ENABLE;
	public static boolean		ALT_ANTIFEED_DUALBOX;
	public static boolean		ALT_ANTIFEED_DISCONNECTED_AS_DUALBOX;
	public static int			ALT_ANTIFEED_INTERVAL;

	public static boolean		PVP_COLOR_SYSTEM_ENABLED;
	public static int			PVP_AMOUNT1;
	public static int			PVP_AMOUNT2;
	public static int			PVP_AMOUNT3;
	public static int			PVP_AMOUNT4;
	public static int			PVP_AMOUNT5;
	public static int			NAME_COLOR_FOR_PVP_AMOUNT1;
	public static int			NAME_COLOR_FOR_PVP_AMOUNT2;
	public static int			NAME_COLOR_FOR_PVP_AMOUNT3;
	public static int			NAME_COLOR_FOR_PVP_AMOUNT4;
	public static int			NAME_COLOR_FOR_PVP_AMOUNT5;
	public static boolean		PK_COLOR_SYSTEM_ENABLED;
	public static int			PK_AMOUNT1;
	public static int			PK_AMOUNT2;
	public static int			PK_AMOUNT3;
	public static int			PK_AMOUNT4;
	public static int			PK_AMOUNT5;
	public static int			TITLE_COLOR_FOR_PK_AMOUNT1;
	public static int			TITLE_COLOR_FOR_PK_AMOUNT2;
	public static int			TITLE_COLOR_FOR_PK_AMOUNT3;
	public static int			TITLE_COLOR_FOR_PK_AMOUNT4;
	public static int			TITLE_COLOR_FOR_PK_AMOUNT5;

	// *******************************************************************************************
	private static final class CustomConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/custom";
		}

		@Override
		protected void loadImpl(L2Properties customSettings)
		{
			SERVER_NAME = customSettings.getProperty("ServerName", "L2EmuProject");

			SHOW_LICENSE = Boolean.parseBoolean(customSettings.getProperty("ShowLicense", "false"));
			SHOW_HTML_WELCOME = Boolean.parseBoolean(customSettings.getProperty("ShowHTMLWelcome", "false"));
			SHOW_HTML_NEWBIE = Boolean.parseBoolean(customSettings.getProperty("ShowHTMLNewbie", "False"));
			SHOW_HTML_GM = Boolean.parseBoolean(customSettings.getProperty("ShowHTMLGm", "False"));
			LEVEL_HTML_NEWBIE = Integer.parseInt(customSettings.getProperty("LevelShowHTMLNewbie", "10"));

			ANNOUNCE_CASTLE_LORDS_LOGIN = Boolean.parseBoolean(customSettings.getProperty("AnnounceCastleLordsLogin", "False"));
			ANNOUNCE_7S_AT_START_UP = Boolean.parseBoolean(customSettings.getProperty("Announce7SAtStartUp", "True"));

			CHAR_VIP_SKIP_SKILLS_CHECK = Boolean.parseBoolean(customSettings.getProperty("CharViPSkipSkillsCheck", "false"));
			CHAR_VIP_COLOR_ENABLED = Boolean.parseBoolean(customSettings.getProperty("CharViPAllowColor", "false"));
			CHAR_VIP_COLOR = Integer.decode("0x" + customSettings.getProperty("CharViPNameColor", "00CCFF"));

			ONLINE_PLAYERS_AT_STARTUP = Boolean.parseBoolean(customSettings.getProperty("ShowOnlinePlayersAtStartup", "True"));
			ONLINE_PLAYERS_ANNOUNCE_INTERVAL = Integer.parseInt(customSettings.getProperty("OnlinePlayersAnnounceInterval", "900000"));

			ALLOW_QUAKE_SYSTEM = Boolean.parseBoolean(customSettings.getProperty("AllowQuakeSystem", "false"));

			ALLOW_CUSTOM_STARTER_ITEMS = Boolean.parseBoolean(customSettings.getProperty("AllowCustomStarterItems", "false"));

			if (ALLOW_CUSTOM_STARTER_ITEMS)
			{
				String[] propertySplit = customSettings.getProperty("CustomStarterItems", "0,0").split(";");
				for (String starteritems : propertySplit)
				{
					String[] starteritemsSplit = starteritems.split(",");
					if (starteritemsSplit.length != 2)
					{
						ALLOW_CUSTOM_STARTER_ITEMS = false;
						_log.warn("StarterItems[Config.load()]: invalid config property -> starter items \"" + starteritems + "\"");
					}
					else
					{
						try
						{
							CUSTOM_STARTER_ITEMS.add(new int[]
							{ Integer.valueOf(starteritemsSplit[0]), Integer.valueOf(starteritemsSplit[1]) });
						}
						catch (NumberFormatException nfe)
						{
							if (!starteritems.equals(""))
							{
								ALLOW_CUSTOM_STARTER_ITEMS = false;
								_log.warn("StarterItems[Config.load()]: invalid config property -> starter items \"" + starteritems + "\"");
							}
						}
					}
				}
			}

			ALLOW_PVP_REWARD = Boolean.parseBoolean(customSettings.getProperty("AllowPvpReward", "False"));
			PVP_REWARD_ITEM_ID = Integer.parseInt(customSettings.getProperty("PvpRewardItemId", "57"));
			PVP_REWARD_ITEM_AMOUNT = Integer.parseInt(customSettings.getProperty("PvpRewardAmount", "100"));

			ALT_MANA_POTIONS = Boolean.parseBoolean(customSettings.getProperty("AllowManaPotions", "False"));

			MANA_POTION_LVL = Integer.parseInt(customSettings.getProperty("ManaPotionLvl", "1"));

			ALLOW_NEW_CHARACTER_TITLE = Boolean.parseBoolean(customSettings.getProperty("AllowNewCharacterTitle", "False"));
			NEW_CHARACTER_TITLE = customSettings.getProperty("NewCharacterTitle", "Newbie");

			DONATOR_NAME_COLOR = Integer.decode("0x" + customSettings.getProperty("DonatorNameColor", "FFCC00"));
			DONATOR_TITLE_COLOR = Integer.decode("0x" + customSettings.getProperty("DonatorTitleColor", "FFCC00"));

			ALLOW_LEVEL_DIFFERENCE = Boolean.parseBoolean(customSettings.getProperty("AllowLevelDifference", "False"));
			LEVEL_DIFFERENCE = Integer.parseInt(customSettings.getProperty("LevelDifference", "10"));

			ALT_ANTIFEED_ENABLE = Boolean.parseBoolean(customSettings.getProperty("AntiFeedEnable", "False"));
			ALT_ANTIFEED_DUALBOX = Boolean.parseBoolean(customSettings.getProperty("AntiFeedDualbox", "True"));
			ALT_ANTIFEED_DISCONNECTED_AS_DUALBOX = Boolean.parseBoolean(customSettings.getProperty("AntiFeedDisconnectedAsDualbox", "True"));
			ALT_ANTIFEED_INTERVAL = 1000 * Integer.parseInt(customSettings.getProperty("AntiFeedInterval", "120"));

			// PVP Name Color System configs - Start
			PVP_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(customSettings.getProperty("EnablePvPColorSystem", "false"));
			PVP_AMOUNT1 = Integer.parseInt(customSettings.getProperty("PvpAmount1", "500"));
			PVP_AMOUNT2 = Integer.parseInt(customSettings.getProperty("PvpAmount2", "1000"));
			PVP_AMOUNT3 = Integer.parseInt(customSettings.getProperty("PvpAmount3", "1500"));
			PVP_AMOUNT4 = Integer.parseInt(customSettings.getProperty("PvpAmount4", "2500"));
			PVP_AMOUNT5 = Integer.parseInt(customSettings.getProperty("PvpAmount5", "5000"));
			NAME_COLOR_FOR_PVP_AMOUNT1 = Integer.decode("0x" + customSettings.getProperty("ColorForAmount1", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT2 = Integer.decode("0x" + customSettings.getProperty("ColorForAmount2", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT3 = Integer.decode("0x" + customSettings.getProperty("ColorForAmount3", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT4 = Integer.decode("0x" + customSettings.getProperty("ColorForAmount4", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT5 = Integer.decode("0x" + customSettings.getProperty("ColorForAmount5", "00FF00"));
			// PvP Name Color System configs - End

			// PK Title Color System configs - Start
			PK_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(customSettings.getProperty("EnablePkColorSystem", "false"));
			PK_AMOUNT1 = Integer.parseInt(customSettings.getProperty("PkAmount1", "500"));
			PK_AMOUNT2 = Integer.parseInt(customSettings.getProperty("PkAmount2", "1000"));
			PK_AMOUNT3 = Integer.parseInt(customSettings.getProperty("PkAmount3", "1500"));
			PK_AMOUNT4 = Integer.parseInt(customSettings.getProperty("PkAmount4", "2500"));
			PK_AMOUNT5 = Integer.parseInt(customSettings.getProperty("PkAmount5", "5000"));
			TITLE_COLOR_FOR_PK_AMOUNT1 = Integer.decode("0x" + customSettings.getProperty("TitleForAmount1", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT2 = Integer.decode("0x" + customSettings.getProperty("TitleForAmount2", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT3 = Integer.decode("0x" + customSettings.getProperty("TitleForAmount3", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT4 = Integer.decode("0x" + customSettings.getProperty("TitleForAmount4", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT5 = Integer.decode("0x" + customSettings.getProperty("TitleForAmount5", "00FF00"));
			//PK Title Color System configs - End
		}
	}

	// *******************************************************************************************
	public static final String	SCRIPTS_FILE			= "./config/main/scripts.properties";
	// *******************************************************************************************
	// April Fools
	public static boolean		ALLOW_APRIL_FOOLS;
	public static int			APRIL_FOOLS_DATE;
	public static int			APRIL_FOOLS_DROP_CHANCE	= 7;

	// Bunny
	public static boolean		ALLOW_BUNNY;
	public static String		BUNNY_DATE;
	public static int			BUNNY_DROP_CHANCE;
	public static int			BUNNY_DROP_FLY_CHANCE;

	// Fifth Anniversary
	public static boolean		ALLOW_FIFTH_ANNIVERSARY;
	public static String		FIFTH_ANNIVERSARY_DATE;

	// Gift of Vitality
	public static boolean		ALLOW_GIFT_OF_VITALITY;

	// Heavy Medal
	public static boolean		ALLOW_HEAVY_MEDAL;
	public static int			HEAVY_MEDAL_WIN_CHANCE;

	// Holly Cow
	public static boolean		ALLOW_HOLLY_COW;

	// New Era
	public static boolean		ALLOW_NEW_ERA;
	public static String		NEW_ERA_DATE;
	public static int			NEW_ERA_DROP_CHANCE;

	// Rabbits To Riches
	public static boolean		ALLOW_RABBITS_TO_RICHES;
	public static String		RABBITS_TO_RICHES_DATE;
	public static int			RABBITS_TO_RICHES_DROP_CHANCE;
	public static int			RABBITS_TO_RICHES_MIN_LVL;

	// Saving Santa
	public static boolean		ALLOW_SAVING_SANTA;
	public static String		SAVING_SANTA_DATE;
	public static int			SAVING_SANTA_DROP_CHANCE;

	// Squash Event
	public static boolean		ALLOW_SQUASH_EVENT;

	// The Valentine
	public static boolean		ALLOW_THE_VALENTINE;
	public static String		THE_VALENTINE_DATE;
	public static int			THE_VALENTINE_DROP_CHANCE;

	// Zaken's Curse
	public static boolean		ALLOW_ZAKENS_CURSE;
	public static String		ZAKENS_CURSE_DATE;
	public static int			ZAKENS_CURSE_DROP_CHANCE;
	public static int			ZAKENS_CURSE_DROP_FLY_CHANCE;
	public static byte			ZAKENS_CURSE_EVENT_TYPE;

	// Freya Celebration
	public static boolean		ALLOW_FREYA_CELEBRATION;

	// Christmas Gift
	public static boolean		ALLOW_CHRISTMAS_GIFT;

	// Christmas Event
	public static boolean		ALLOW_CHRISTMAS_EVENT;
	public static int			CHRISTMAS_EVENT_DROP_CHANCE;
	public static String		CHRISTMAS_EVENT_DATE;

	// Santa Claus
	public static boolean		ALLOW_SANTA_CLAUS_EVENT;

	// *******************************************************************************************
	private static final class ScriptsConfig extends ConfigPropertiesLoader
	{
		@Override
		protected String getName()
		{
			return "main/scripts";
		}

		@Override
		protected void loadImpl(L2Properties scriptsSettings)
		{
			// April Fools
			ALLOW_APRIL_FOOLS = Boolean.parseBoolean(scriptsSettings.getProperty("AllowAprilFools", "False"));
			APRIL_FOOLS_DATE = Integer.parseInt(scriptsSettings.getProperty("AprilFoolsDate", "20110401"));
			APRIL_FOOLS_DROP_CHANCE = Integer.parseInt(scriptsSettings.getProperty("AprilFoolsDropChance", "7"));

			// Bunny
			ALLOW_BUNNY = Boolean.parseBoolean(scriptsSettings.getProperty("AllowBunny", "False"));
			BUNNY_DATE = scriptsSettings.getProperty("BunnyDate", "26 03 2011-04 6 2011");
			BUNNY_DROP_CHANCE = Integer.parseInt(scriptsSettings.getProperty("BunnyDropChance", "25"));
			BUNNY_DROP_FLY_CHANCE = Integer.parseInt(scriptsSettings.getProperty("BunnyDropFlyChance", "50"));

			// Fifth Anniversary
			ALLOW_FIFTH_ANNIVERSARY = Boolean.parseBoolean(scriptsSettings.getProperty("AllowFifthAnniversary", "False"));
			FIFTH_ANNIVERSARY_DATE = scriptsSettings.getProperty("FifthAnniversaryDate", "28 03 2011-05 05 2011");

			// Gift of Vitality
			ALLOW_GIFT_OF_VITALITY = Boolean.parseBoolean(scriptsSettings.getProperty("AllowGiftOfVitality", "False"));

			// Heavy Medal
			ALLOW_HEAVY_MEDAL = Boolean.parseBoolean(scriptsSettings.getProperty("AllowHeavyMedal", "False"));
			HEAVY_MEDAL_WIN_CHANCE = Integer.parseInt(scriptsSettings.getProperty("HeavyMedalWinChance", "50"));

			// Holly Cow
			ALLOW_HOLLY_COW = Boolean.parseBoolean(scriptsSettings.getProperty("AllowHollyCow", "False"));

			// New Era
			ALLOW_NEW_ERA = Boolean.parseBoolean(scriptsSettings.getProperty("AllowNewEra", "False"));
			NEW_ERA_DATE = scriptsSettings.getProperty("NewEraDate", "28 03 2011-05 05 2011");
			NEW_ERA_DROP_CHANCE = Integer.parseInt(scriptsSettings.getProperty("NewEraDropChance", "25"));

			// Rabbits To Riches
			ALLOW_RABBITS_TO_RICHES = Boolean.parseBoolean(scriptsSettings.getProperty("AllowRabbitsToRiches", "False"));
			RABBITS_TO_RICHES_DATE = scriptsSettings.getProperty("RabbitsToRichesDate", "1 09 2011-10 09 2011");
			RABBITS_TO_RICHES_DROP_CHANCE = Integer.parseInt(scriptsSettings.getProperty("RabbitsToRichesDropChance", "10"));
			RABBITS_TO_RICHES_MIN_LVL = Integer.parseInt(scriptsSettings.getProperty("RabbitsToRichesMinLvl", "40"));

			// Saving Santa
			ALLOW_SAVING_SANTA = Boolean.parseBoolean(scriptsSettings.getProperty("AllowSavingSanta", "False"));
			SAVING_SANTA_DATE = scriptsSettings.getProperty("SavingSantaDate", "15 12 2011-10 01 2012");
			SAVING_SANTA_DROP_CHANCE = Integer.parseInt(scriptsSettings.getProperty("SavingSantaDropChance", "3"));

			// Squash Event
			ALLOW_SQUASH_EVENT = Boolean.parseBoolean(scriptsSettings.getProperty("AllowSquashEvent", "False"));

			// The Valentine
			ALLOW_THE_VALENTINE = Boolean.parseBoolean(scriptsSettings.getProperty("AllowTheValentine", "False"));
			THE_VALENTINE_DATE = scriptsSettings.getProperty("TheValentineDate", "10 02 2011-17 02 2011");
			THE_VALENTINE_DROP_CHANCE = Integer.parseInt(scriptsSettings.getProperty("TheValentineDropChance", "5"));

			// Zaken's Curse
			ALLOW_ZAKENS_CURSE = Boolean.parseBoolean(scriptsSettings.getProperty("AllowZakensCurse", "False"));
			ZAKENS_CURSE_DATE = scriptsSettings.getProperty("ZakensCurseDate", "23 08 2011-31 07 2011");
			ZAKENS_CURSE_DROP_CHANCE = Integer.parseInt(scriptsSettings.getProperty("ZakensCurseDropChance", "1"));
			ZAKENS_CURSE_DROP_FLY_CHANCE = Integer.parseInt(scriptsSettings.getProperty("ZakensCurseDropFlyChance", "2"));
			ZAKENS_CURSE_EVENT_TYPE = Byte.parseByte(scriptsSettings.getProperty("ZakensCurseEventType", "1"));

			// Freya Celebration
			ALLOW_FREYA_CELEBRATION = Boolean.parseBoolean(scriptsSettings.getProperty("AllowFreyaCelebration", "False"));

			// Christmas Gift
			ALLOW_CHRISTMAS_GIFT = Boolean.parseBoolean(scriptsSettings.getProperty("AllowChristmasGift", "False"));

			// Christmas Event
			ALLOW_CHRISTMAS_EVENT = Boolean.parseBoolean(scriptsSettings.getProperty("AllowChristmasEvent", "False"));
			CHRISTMAS_EVENT_DATE = scriptsSettings.getProperty("ChristmasEventDate", "23 12 2011-06 01 2012");
			CHRISTMAS_EVENT_DROP_CHANCE = Integer.parseInt(scriptsSettings.getProperty("ChristmasEventDropChance", "3"));

			// Santa Claus
			ALLOW_SANTA_CLAUS_EVENT = Boolean.parseBoolean(scriptsSettings.getProperty("AllowSantaClausEvent", "False"));
		}
	}

	public static class ClassMasterSettings
	{
		private final FastMap<Integer, FastMap<Integer, Integer>>	_claimItems;
		private final FastMap<Integer, FastMap<Integer, Integer>>	_rewardItems;
		private final FastMap<Integer, Boolean>						_allowedClassChange;

		public ClassMasterSettings(String _configLine)
		{
			_claimItems = new FastMap<Integer, FastMap<Integer, Integer>>();
			_rewardItems = new FastMap<Integer, FastMap<Integer, Integer>>();
			_allowedClassChange = new FastMap<Integer, Boolean>();
			if (_configLine != null)
				parseConfigLine(_configLine.trim());
		}

		private void parseConfigLine(String _configLine)
		{
			StringTokenizer st = new StringTokenizer(_configLine, ";");

			while (st.hasMoreTokens())
			{
				// get allowed class change
				int job = Integer.parseInt(st.nextToken());

				_allowedClassChange.put(job, true);

				FastMap<Integer, Integer> _items = new FastMap<Integer, Integer>();
				// parse items needed for class change
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");

					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						int _itemId = Integer.parseInt(st3.nextToken());
						int _quantity = Integer.parseInt(st3.nextToken());
						_items.put(_itemId, _quantity);
					}
				}

				_claimItems.put(job, _items);

				_items = new FastMap<Integer, Integer>();
				// parse gifts after class change
				if (st.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");

					while (st2.hasMoreTokens())
					{
						StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						int _itemId = Integer.parseInt(st3.nextToken());
						int _quantity = Integer.parseInt(st3.nextToken());
						_items.put(_itemId, _quantity);
					}
				}

				_rewardItems.put(job, _items);
			}
		}

		public boolean isAllowed(int job)
		{
			if (_allowedClassChange == null)
				return false;
			if (_allowedClassChange.containsKey(job))
				return _allowedClassChange.get(job);

			return false;
		}

		public FastMap<Integer, Integer> getRewardItems(int job)
		{
			if (_rewardItems.containsKey(job))
				return _rewardItems.get(job);

			return null;
		}

		public FastMap<Integer, Integer> getRequireItems(int job)
		{
			if (_claimItems.containsKey(job))
				return _claimItems.get(job);

			return null;
		}

	}

	/**
	 * Set a new value to a game parameter from the admin console.
	 *
	 * @param pName (String) : name of the parameter to change
	 * @param pValue (String) : new value of the parameter
	 * @return boolean : true if modification has been made
	 * @link useAdminCommand
	 * TODO: Should be rewritten or remove?
	 */
	public static boolean setParameterValue(String pName, String pValue)
	{
		// Server settings
		if (pName.equalsIgnoreCase("RateXp"))
			RATE_XP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateSp"))
			RATE_SP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RatePartyXp"))
			RATE_PARTY_XP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RatePartySp"))
			RATE_PARTY_SP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateQuestsRewardExpSp"))
			RATE_QUESTS_REWARD_EXPSP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateQuestsRewardAdena"))
			RATE_QUESTS_REWARD_ADENA = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateQuestsRewardItems"))
			RATE_QUESTS_REWARD_ITEMS = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateDropAdena"))
			RATE_DROP_ADENA = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateConsumableCost"))
			RATE_CONSUMABLE_COST = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateDropItems"))
			RATE_DROP_ITEMS = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateDropSpoil"))
			RATE_DROP_SPOIL = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateDropManor"))
			RATE_DROP_MANOR = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("RateDropQuest"))
			RATE_DROP_QUEST = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateKarmaExpLost"))
			RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("RateSiegeGuardsPrice"))
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);

		else if (pName.equalsIgnoreCase("PlayerDropLimit"))
			PLAYER_DROP_LIMIT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PlayerRateDrop"))
			PLAYER_RATE_DROP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PlayerRateDropItem"))
			PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PlayerRateDropEquip"))
			PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PlayerRateDropEquipWeapon"))
			PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("KarmaDropLimit"))
			KARMA_DROP_LIMIT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("KarmaRateDrop"))
			KARMA_RATE_DROP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("KarmaRateDropItem"))
			KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("KarmaRateDropEquip"))
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("KarmaRateDropEquipWeapon"))
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("AutoDestroyDroppedItemAfter"))
			AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("SaveDroppedItem"))
			SAVE_DROPPED_ITEM = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CoordSynchronize"))
			COORD_SYNCHRONIZE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("DeleteCharAfterDays"))
			DELETE_DAYS = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("ChanceToBreak"))
			ALT_CHANCE_BREAK = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChanceToLevel"))
			ALT_CHANCE_LEVEL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AllowDiscardItem"))
			ALLOW_DISCARDITEM = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ChampionFrequency"))
			CHAMPION_FREQUENCY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionHp"))
			CHAMPION_HP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionHpRegen"))
			CHAMPION_HP_REGEN = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("ChampionAtk"))
			CHAMPION_ATK = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("ChampionSpdAtk"))
			CHAMPION_SPD_ATK = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("ChampionRewards"))
			CHAMPION_REWARDS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionAdenasRewards"))
			CHAMPION_ADENA = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionExpSp"))
			CHAMPION_EXP_SP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionBoss"))
			CHAMPION_BOSS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ChampionMinLevel"))
			CHAMPION_MIN_LEVEL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionMaxLevel"))
			CHAMPION_MAX_LEVEL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionMinions"))
			CHAMPION_MINIONS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ChampionSpecialItemLevelDiff"))
			CHAMPION_SPCL_LVL_DIFF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionSpecialItemChance"))
			CHAMPION_SPCL_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionSpecialItemID"))
			CHAMPION_SPCL_ITEM = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("ChampionSpecialItemAmount"))
			CHAMPION_SPCL_QTY = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("AllowWarehouse"))
			ALLOW_WAREHOUSE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowWear"))
			ALLOW_WEAR = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("WearDelay"))
			WEAR_DELAY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("WearPrice"))
			WEAR_PRICE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AllowWater"))
			ALLOW_WATER = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowRentPet"))
			ALLOW_RENTPET = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowBoat"))
			ALLOW_BOAT = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowCursedWeapons"))
			ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowManor"))
			ALLOW_MANOR = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowPetWalkers"))
			ALLOW_PET_WALKERS = Boolean.parseBoolean(pValue);

		else if (pName.equalsIgnoreCase("ShowMonsterLevel"))
			SHOW_MONSTER_LVL = Boolean.parseBoolean(pValue);

		else if (pName.equalsIgnoreCase("ForceInventoryUpdate"))
			FORCE_INVENTORY_UPDATE = Boolean.parseBoolean(pValue);

		else if (pName.equalsIgnoreCase("AutoDeleteInvalidQuestData"))
			AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(pValue);

		else if (pName.equalsIgnoreCase("MaximumOnlineUsers"))
			MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("ZoneTown"))
			ZONE_TOWN = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("ShowGMLogin"))
			SHOW_GM_LOGIN = Boolean.parseBoolean(pValue);

		// Other settings
		else if (pName.equalsIgnoreCase("UseDeepBlueDropRules"))
			DEEPBLUE_DROP_RULES = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("UseDeepBlueDropRulesRaid"))
			DEEPBLUE_DROP_RULES_RAID = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CancelLesserEffect"))
			EFFECT_CANCELING = Boolean.parseBoolean(pValue);

		else if (pName.equalsIgnoreCase("MaximumSlotsForNoDwarf"))
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaximumSlotsForDwarf"))
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaximumSlotsForGMPlayer"))
			INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForNoDwarf"))
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForDwarf"))
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForClan"))
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("EnchantChanceElement"))
			ENCHANT_CHANCE_ELEMENT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantMaxWeapon"))
			ENCHANT_MAX_WEAPON = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantMaxArmor"))
			ENCHANT_MAX_ARMOR = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantSafeMax"))
			ENCHANT_SAFE_MAX = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantSafeMaxFull"))
			ENCHANT_SAFE_MAX_FULL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantDwarf1Enchantlevel"))
			ENCHANT_DWARF_1_ENCHANTLEVEL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantDwarf2Enchantlevel"))
			ENCHANT_DWARF_2_ENCHANTLEVEL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantDwarf3Enchantlevel"))
			ENCHANT_DWARF_3_ENCHANTLEVEL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantDwarf1Chance"))
			ENCHANT_DWARF_1_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantDwarf2Chance"))
			ENCHANT_DWARF_2_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("EnchantDwarf3Chance"))
			ENCHANT_DWARF_3_CHANCE = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("AugmentationNGSkillChance"))
			AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationNGGlowChance"))
			AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationMidSkillChance"))
			AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationMidGlowChance"))
			AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationHighSkillChance"))
			AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationHighGlowChance"))
			AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationTopSkillChance"))
			AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationTopGlowChance"))
			AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AugmentationBaseStatChance"))
			AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("NPCHpRegenMultiplier"))
			NPC_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("NPCMpRegenMultiplier"))
			NPC_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("PlayerHpRegenMultiplier"))
			PLAYER_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("PlayerMpRegenMultiplier"))
			PLAYER_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("PlayerCpRegenMultiplier"))
			PLAYER_CP_REGEN_MULTIPLIER = Double.parseDouble(pValue);

		else if (pName.equalsIgnoreCase("RaidHpRegenMultiplier"))
			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("RaidMpRegenMultiplier"))
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("RaidPDefenceMultiplier"))
			RAID_PDEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
		else if (pName.equalsIgnoreCase("RaidMDefenceMultiplier"))
			RAID_MDEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
		else if (pName.equalsIgnoreCase("RaidMinionRespawnTime"))
			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("StartingAdena"))
			STARTING_ADENA = Long.parseLong(pValue);
		else if (pName.equalsIgnoreCase("StartingLevel"))
			STARTING_LEVEL = Byte.parseByte(pValue);
		else if (pName.equalsIgnoreCase("StartingSP"))
			STARTING_SP = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("UnstuckInterval"))
			UNSTUCK_INTERVAL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("TeleportWatchdogTimeout"))
			TELEPORT_WATCHDOG_TIMEOUT = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("PlayerSpawnProtection"))
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("PlayerFakeDeathUpProtection"))
			PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("PartyXpCutoffMethod"))
			PARTY_XP_CUTOFF_METHOD = pValue;
		else if (pName.equalsIgnoreCase("PartyXpCutoffPercent"))
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("PartyXpCutoffLevel"))
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("RespawnRestoreCP"))
			RESPAWN_RESTORE_CP = Double.parseDouble(pValue) / 100;
		else if (pName.equalsIgnoreCase("RespawnRestoreHP"))
			RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100;
		else if (pName.equalsIgnoreCase("RespawnRestoreMP"))
			RESPAWN_RESTORE_MP = Double.parseDouble(pValue) / 100;

		else if (pName.equalsIgnoreCase("MaxPvtStoreSellSlotsDwarf"))
			MAX_PVTSTORESELL_SLOTS_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxPvtStoreSellSlotsOther"))
			MAX_PVTSTORESELL_SLOTS_OTHER = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxPvtStoreBuySlotsDwarf"))
			MAX_PVTSTOREBUY_SLOTS_DWARF = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxPvtStoreBuySlotsOther"))
			MAX_PVTSTOREBUY_SLOTS_OTHER = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("StoreSkillCooltime"))
			STORE_EFFECTS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AnnounceMammonSpawn"))
			ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(pValue);

		// Alternative settings
		else if (pName.equalsIgnoreCase("AltGameTiredness"))
			ALT_GAME_TIREDNESS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltGameCreation"))
			ALT_GAME_CREATION = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltGameCreationSpeed"))
			ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("AltGameCreationXpRate"))
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("AltGameCreationRareXpSpRate"))
			ALT_GAME_CREATION_RARE_XPSP_RATE = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("AltGameCreationSpRate"))
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("AltWeightLimit"))
			ALT_WEIGHT_LIMIT = Double.parseDouble(pValue);
		else if (pName.equalsIgnoreCase("AltBlacksmithUseRecipes"))
			ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltGameSkillLearn"))
			ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltMinimumFallHeight"))
			ALT_MINIMUM_FALL_HEIGHT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AltNbCumulatedBuff"))
			ALT_BUFFS_MAX_AMOUNT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AltSuccessRate"))
			ALT_DAGGER_RATE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("InstantKillEffect2"))
			ALT_INSTANT_KILL_EFFECT_2 = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("AltAttackDelay"))
			ALT_ATTACK_DELAY = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("AltFailRate"))
			ALT_DAGGER_FAIL_RATE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AltBehindRate"))
			ALT_DAGGER_RATE_BEHIND = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AltFrontRate"))
			ALT_DAGGER_RATE_FRONT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AltDanceTime"))
			ALT_DANCE_TIME = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxPAtkSpeed"))
			ALT_MAX_PATK_SPEED = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxMAtkSpeed"))
			ALT_MAX_MATK_SPEED = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("GradePenalty"))
			ALT_GRADE_PENALTY = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("RemoveCastleCirclets"))
			ALT_REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltGameCancelByHit"))
		{
			ALT_GAME_CANCEL_BOW = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
		}

		else if (pName.equalsIgnoreCase("AltShieldBlocks"))
			ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltPerfectShieldBlockRate"))
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("Delevel"))
			ALT_GAME_DELEVEL = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("MagicFailures"))
			ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltMobAgroInPeaceZone"))
			ALT_MOB_AGGRO_IN_PEACEZONE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltAttackableNpcs"))
			ALT_ATTACKABLE_NPCS = Boolean.parseBoolean(pValue);

		else if (pName.equalsIgnoreCase("AltGameExponentXp"))
			ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
		else if (pName.equalsIgnoreCase("AltGameExponentSp"))
			ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);

		else if (pName.equalsIgnoreCase("AltPartyRange"))
			ALT_PARTY_RANGE = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AltPartyRange2"))
			ALT_PARTY_RANGE2 = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("CraftingEnabled"))
			ALT_IS_CRAFTING_ENABLED = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ManagerCrystalCount"))
			ALT_MANAGER_CRYSTAL_COUNT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("SpBookNeeded"))
			ALT_SP_BOOK_NEEDED = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("LifeCrystalNeeded"))
			ALT_LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("EnchantSkillSpBookNeeded"))
			ALT_ES_SP_BOOK_NEEDED = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AutoLoot"))
			ALT_AUTO_LOOT = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AutoLootRaid"))
			ALT_AUTO_LOOT_RAID = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AutoLootAdena"))
			ALT_AUTO_LOOT_ADENA = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AutoLootHerbs"))
			ALT_AUTO_LOOT_HERBS = Boolean.parseBoolean(pValue);

		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanBeKilledInPeaceZone"))
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanShop"))
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseGK"))
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTeleport"))
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTrade"))
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseWareHouse"))
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltCastleForDawn"))
			ALT_GAME_CASTLE_DAWN = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltCastleForDusk"))
			ALT_GAME_CASTLE_DUSK = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltFreeTeleporting"))
			ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltSubClassWithoutQuests"))
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltSubclassEverywhere"))
			ALT_GAME_SUBCLASS_EVERYWHERE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("MaxSubclass"))
			ALT_MAX_SUBCLASS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxSubclassLevel"))
			ALT_MAX_SUBCLASS_LEVEL = Byte.parseByte(pValue);
		//else if (pName.equalsIgnoreCase("AltNewCharAlwaysIsNewbie"))
		//ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("DwarfRecipeLimit"))
			ALT_DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("CommonRecipeLimit"))
			ALT_COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("CastleShieldRestriction"))
			ALT_CASTLE_SHIELD = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ClanHallShieldRestriction"))
			ALT_CLANHALL_SHIELD = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("ApellaArmorsRestriction"))
			ALT_APELLA_ARMORS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("OathArmorsRestriction"))
			ALT_OATH_ARMORS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CastleLordsCrownRestriction"))
			ALT_CASTLE_CROWN = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("CastleCircletsRestriction"))
			ALT_CASTLE_CIRCLETS = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AllowManaPotions"))
			ALT_MANA_POTIONS = Boolean.parseBoolean(pValue);

		// PvP settings
		else if (pName.equalsIgnoreCase("MinKarma"))
			KARMA_MIN_KARMA = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxKarma"))
			KARMA_MAX_KARMA = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("XPDivider"))
			KARMA_XP_DIVIDER = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("BaseKarmaLost"))
			KARMA_LOST_BASE = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("CanGMDropEquipment"))
			KARMA_DROP_GM = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AwardPKKillPVPPoint"))
			KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("MinimumPKRequiredToDrop"))
			KARMA_PK_LIMIT = Integer.parseInt(pValue);

		else if (pName.equalsIgnoreCase("PvPTime"))
			PVP_TIME = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("GlobalChat"))
			DEFAULT_GLOBAL_CHAT = ChatMode.valueOf(pValue.toUpperCase());
		else if (pName.equalsIgnoreCase("TradeChat"))
			DEFAULT_TRADE_CHAT = ChatMode.valueOf(pValue.toUpperCase());
		else if (pName.equalsIgnoreCase("GMAdminMenuStyle"))
			GM_ADMIN_MENU_STYLE = pValue;

		//else if (pName.equalsIgnoreCase("FailFakeDeath"))
		//	ALT_FAIL_FAKEDEATH = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltFlyingWyvernInSiege"))
			ALT_FLYING_WYVERN_IN_SIEGE = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("TimeInADayOfOpenADoor"))
			ALT_TIME_IN_A_DAY_OF_OPEN_A_DOOR = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("TimeOfOpeningADoor"))
			ALT_TIME_OF_OPENING_A_DOOR = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("TimeLimitOfInvade"))
			ALT_TIMELIMITOFINVADE = Integer.parseInt(pValue);

		// Siege settings
		else if (pName.equalsIgnoreCase("AttackerMaxClans"))
			SIEGE_MAX_ATTACKER = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("DefenderMaxClans"))
			SIEGE_MAX_DEFENDER = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("AttackerRespawn"))
			SIEGE_RESPAWN_DELAY_ATTACKER = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("DefenderRespawn"))
			SIEGE_RESPAWN_DELAY_DEFENDER = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("CTLossPenalty"))
			SIEGE_CT_LOSS_PENALTY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("MaxFlags"))
			SIEGE_FLAG_MAX_COUNT = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("SiegeClanMinLevel"))
			SIEGE_CLAN_MIN_LEVEL = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("SiegeLength"))
			SIEGE_LENGTH_MINUTES = Integer.parseInt(pValue);

		// Telnet settings
		else if (pName.equalsIgnoreCase("AltTelnet"))
			ALT_TELNET = Boolean.parseBoolean(pValue);
		else if (pName.equalsIgnoreCase("AltTelnetGmAnnouncerName"))
			ALT_TELNET_GM_ANNOUNCER_NAME = Boolean.parseBoolean(pValue);

		// GM options
		else if (pName.equalsIgnoreCase("GMShowAnnouncerName"))
			GM_ANNOUNCER_NAME = Boolean.parseBoolean(pValue);

		// Options
		else if (pName.equalsIgnoreCase("ShowHTMLGm"))
			SHOW_HTML_GM = Boolean.parseBoolean(pValue);

		else if (pName.equalsIgnoreCase("MaxPersonalFamePoints"))
			MAX_PERSONAL_FAME_POINTS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("FortressZoneFameTaskFrequency"))
			FORTRESS_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("FortressZoneFameAquirePoints"))
			FORTRESS_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("CastleZoneFameTaskFrequency"))
			CASTLE_ZONE_FAME_TASK_FREQUENCY = Integer.parseInt(pValue);
		else if (pName.equalsIgnoreCase("CastleZoneFameAquirePoints"))
			CASTLE_ZONE_FAME_AQUIRE_POINTS = Integer.parseInt(pValue);

		else
			return false;
		return true;
	}

	// it has no instancies
	protected Config()
	{
	}

	/**
	 * Save hexadecimal ID of the server in the properties file.
	 *
	 * @param string (String) : hexadecimal ID of the server to store
	 * @see HEXID_FILE
	 * @see saveHexid(String string, String fileName)
	 * @link LoginServerThread
	 */
	public static void saveHexid(int serverId, String string)
	{
		Config.saveHexid(serverId, string, HEXID_FILE);
	}

	/**
	 * Save hexadecimal ID of the server in the properties file.
	 *
	 * @param hexId (String) : hexadecimal ID of the server to store
	 * @param fileName (String) : name of the properties file
	 */
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			L2Properties hexSetting = new L2Properties();
			File file = new File(fileName);
			// Create a new empty file only if it doesn't exist
			file.createNewFile();
			OutputStream os = new FileOutputStream(file);
			hexSetting.setProperty("ServerID", String.valueOf(serverId));
			hexSetting.setProperty("HexID", hexId);
			hexSetting.store(os, "the hexID to auth into login");
			os.close();
		}
		catch (Exception e)
		{
			_log.warn("Failed to save hex id to " + fileName + " File.");
		}
	}
}
