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
package net.l2emuproject.gameserver;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Set;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.config.PersistentProperties;
import net.l2emuproject.gameserver.dataholders.AugmentationDataHolder;
import net.l2emuproject.gameserver.dataholders.ClassBalanceDataHolder;
import net.l2emuproject.gameserver.dataholders.EnchantHPBonusDataHolder;
import net.l2emuproject.gameserver.dataholders.EnchantItemDataHolder;
import net.l2emuproject.gameserver.dataholders.FishDataHolder;
import net.l2emuproject.gameserver.dataholders.MerchantPriceConfigDataHolder;
import net.l2emuproject.gameserver.dataholders.MinionDataHolder;
import net.l2emuproject.gameserver.dataholders.NpcWalkerRoutesDataHolder;
import net.l2emuproject.gameserver.dataholders.SummonItemsDataHolder;
import net.l2emuproject.gameserver.dataholders.TeleportDataHolder;
import net.l2emuproject.gameserver.dataholders.UIDataHolder;
import net.l2emuproject.gameserver.datatables.ArmorSetsTable;
import net.l2emuproject.gameserver.datatables.BuffTemplateTable;
import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.datatables.CharTemplateTable;
import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.datatables.DoorTable;
import net.l2emuproject.gameserver.datatables.ExtractableItemsData;
import net.l2emuproject.gameserver.datatables.ExtractableSkillsData;
import net.l2emuproject.gameserver.datatables.GMSkillTable;
import net.l2emuproject.gameserver.datatables.GlobalDropTable;
import net.l2emuproject.gameserver.datatables.GmListTable;
import net.l2emuproject.gameserver.datatables.HennaTable;
import net.l2emuproject.gameserver.datatables.HennaTreeTable;
import net.l2emuproject.gameserver.datatables.HeroSkillTable;
import net.l2emuproject.gameserver.datatables.ItemMarketTable;
import net.l2emuproject.gameserver.datatables.ItemTable;
import net.l2emuproject.gameserver.datatables.LevelUpData;
import net.l2emuproject.gameserver.datatables.NobleSkillTable;
import net.l2emuproject.gameserver.datatables.NpcTable;
import net.l2emuproject.gameserver.datatables.PetDataTable;
import net.l2emuproject.gameserver.datatables.PetSkillsTable;
import net.l2emuproject.gameserver.datatables.ResidentialSkillTable;
import net.l2emuproject.gameserver.datatables.ShotTable;
import net.l2emuproject.gameserver.datatables.SkillSpellbookTable;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.datatables.SpawnTable;
import net.l2emuproject.gameserver.datatables.StaticObjects;
import net.l2emuproject.gameserver.datatables.TradeListTable;
import net.l2emuproject.gameserver.events.custom.TvT.TvT;
import net.l2emuproject.gameserver.events.custom.leaderboards.ArenaManager;
import net.l2emuproject.gameserver.events.custom.leaderboards.FishermanManager;
import net.l2emuproject.gameserver.events.global.clanhallsiege.CCHManager;
import net.l2emuproject.gameserver.events.global.clanhallsiege.CCHSiege;
import net.l2emuproject.gameserver.events.global.clanhallsiege.ClanHallManager;
import net.l2emuproject.gameserver.events.global.dimensionalrift.DimensionalRiftManager;
import net.l2emuproject.gameserver.events.global.fortsiege.Fort;
import net.l2emuproject.gameserver.events.global.fortsiege.FortManager;
import net.l2emuproject.gameserver.events.global.fortsiege.FortSiegeManager;
import net.l2emuproject.gameserver.events.global.krateicube.KrateiCube;
import net.l2emuproject.gameserver.events.global.olympiad.Hero;
import net.l2emuproject.gameserver.events.global.olympiad.Olympiad;
import net.l2emuproject.gameserver.events.global.sevensigns.SevenSigns;
import net.l2emuproject.gameserver.events.global.sevensigns.SevenSignsFestival;
import net.l2emuproject.gameserver.events.global.siege.Castle;
import net.l2emuproject.gameserver.events.global.siege.CastleManager;
import net.l2emuproject.gameserver.events.global.siege.SiegeManager;
import net.l2emuproject.gameserver.events.global.territorywar.TerritoryWarManager;
import net.l2emuproject.gameserver.handler.AdminCommandHandler;
import net.l2emuproject.gameserver.handler.BypassHandler;
import net.l2emuproject.gameserver.handler.ChatHandler;
import net.l2emuproject.gameserver.handler.ItemHandler;
import net.l2emuproject.gameserver.handler.SkillHandler;
import net.l2emuproject.gameserver.handler.SkillTargetHandler;
import net.l2emuproject.gameserver.handler.UserCommandHandler;
import net.l2emuproject.gameserver.handler.VoicedCommandHandler;
import net.l2emuproject.gameserver.items.ItemsAutoDestroy;
import net.l2emuproject.gameserver.items.ItemsOnGroundManager;
import net.l2emuproject.gameserver.manager.AirShipManager;
import net.l2emuproject.gameserver.manager.AutoChatManager;
import net.l2emuproject.gameserver.manager.AutoSpawnManager;
import net.l2emuproject.gameserver.manager.BoatManager;
import net.l2emuproject.gameserver.manager.CrownManager;
import net.l2emuproject.gameserver.manager.DayNightSpawnManager;
import net.l2emuproject.gameserver.manager.MercTicketManager;
import net.l2emuproject.gameserver.manager.boss.RaidBossManager;
import net.l2emuproject.gameserver.manager.gracia.SeedOfDestructionManager;
import net.l2emuproject.gameserver.manager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.network.L2GameSelectorThread;
import net.l2emuproject.gameserver.services.SystemService;
import net.l2emuproject.gameserver.services.VersionService;
import net.l2emuproject.gameserver.services.auction.AuctionService;
import net.l2emuproject.gameserver.services.blocklist.BlockListService;
import net.l2emuproject.gameserver.services.community.CommunityService;
import net.l2emuproject.gameserver.services.couple.CoupleService;
import net.l2emuproject.gameserver.services.crafting.RecipeService;
import net.l2emuproject.gameserver.services.cursedweapons.CursedWeaponsService;
import net.l2emuproject.gameserver.services.friendlist.FriendListService;
import net.l2emuproject.gameserver.services.itemauction.ItemAuctionService;
import net.l2emuproject.gameserver.services.mail.MailService;
import net.l2emuproject.gameserver.services.manor.CastleManorService;
import net.l2emuproject.gameserver.services.manor.L2Manor;
import net.l2emuproject.gameserver.services.party.PartyRoomManager;
import net.l2emuproject.gameserver.services.petition.PetitionService;
import net.l2emuproject.gameserver.services.quest.QuestService;
import net.l2emuproject.gameserver.services.recommendation.RecommendationService;
import net.l2emuproject.gameserver.services.transactions.L2Multisell;
import net.l2emuproject.gameserver.services.transformation.TransformationService;
import net.l2emuproject.gameserver.status.GameStatusServer;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.system.announcements.AutoAnnouncements;
import net.l2emuproject.gameserver.system.cache.CrestCache;
import net.l2emuproject.gameserver.system.cache.HtmCache;
import net.l2emuproject.gameserver.system.database.L2DatabaseFactory;
import net.l2emuproject.gameserver.system.idfactory.IdFactory;
import net.l2emuproject.gameserver.system.restriction.ObjectRestrictions;
import net.l2emuproject.gameserver.system.scripting.CompiledScriptCache;
import net.l2emuproject.gameserver.system.scripting.L2ScriptEngineManager;
import net.l2emuproject.gameserver.system.taskmanager.AttackStanceTaskManager;
import net.l2emuproject.gameserver.system.taskmanager.DecayTaskManager;
import net.l2emuproject.gameserver.system.taskmanager.KnownListUpdateTaskManager;
import net.l2emuproject.gameserver.system.taskmanager.LeakTaskManager;
import net.l2emuproject.gameserver.system.taskmanager.MovementController;
import net.l2emuproject.gameserver.system.taskmanager.OnlineStatusTask;
import net.l2emuproject.gameserver.system.taskmanager.PacketBroadcaster;
import net.l2emuproject.gameserver.system.taskmanager.SQLQueue;
import net.l2emuproject.gameserver.system.taskmanager.tasks.TaskManager;
import net.l2emuproject.gameserver.system.threadmanager.DeadlockDetector;
import net.l2emuproject.gameserver.system.threadmanager.ThreadPoolManager;
import net.l2emuproject.gameserver.system.time.GameTimeController;
import net.l2emuproject.gameserver.system.util.DatabaseBackupManager;
import net.l2emuproject.gameserver.system.util.OfflineTradeManager;
import net.l2emuproject.gameserver.system.util.TableOptimizer;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.tools.geoeditorcon.GeoEditorListener;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.geodata.GeoData;
import net.l2emuproject.gameserver.world.geodata.pathfinding.PathFinding;
import net.l2emuproject.gameserver.world.mapregion.MapRegionManager;
import net.l2emuproject.gameserver.world.town.TownManager;
import net.l2emuproject.gameserver.world.zone.ZoneManager;
import net.l2emuproject.lang.L2Thread;
import net.l2emuproject.util.L2FastSet;
import net.l2emuproject.util.concurrent.RunnableStatsManager;

public class L2GameServer extends Config
{
	private static final Calendar _serverStarted = Calendar.getInstance();
	
	public static void main(String[] args) throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		
		// Configs
		// --------------------------
		Config.load();
		
		// Database Settings
		// --------------------------
		System.setProperty("net.l2emuproject.db.driverclass", Config.DATABASE_DRIVER);
		System.setProperty("net.l2emuproject.db.urldb", Config.DATABASE_URL);
		System.setProperty("net.l2emuproject.db.user", Config.DATABASE_LOGIN);
		System.setProperty("net.l2emuproject.db.password", Config.DATABASE_PASSWORD);
		System.setProperty("net.l2emuproject.db.maximum.db.connection", Integer.toString(Config.DATABASE_MAX_CONNECTIONS));
		
		// Database
		// --------------------------
		Util.printSection("Database");
		L2DatabaseFactory.getInstance();
		Class.forName(PersistentProperties.class.getName());
		
		// Prints General System Info
		// --------------------------
		Util.printSection("System Info");
		SystemService.printGeneralSystemInfo();

		Util.printSection("World");
		L2World.getInstance();
		MapRegionManager.getInstance();
		Announcements.getInstance();
		AutoAnnouncements.getInstance();
		if (!IdFactory.getInstance().isInitialized())
		{
			_log.fatal("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		_log.info("IdFactory : Free ObjectID's remaining: " + IdFactory.getInstance().size());
		if (Config.OPTIMIZE_DATABASE)
			TableOptimizer.optimize();
		if (Config.DATABASE_BACKUP_MAKE_BACKUP_ON_STARTUP)
			DatabaseBackupManager.makeBackup();
		Class.forName(RunnableStatsManager.class.getName());
		ThreadPoolManager.getInstance();
		DeadlockDetector.getInstance();
		SQLQueue.getInstance();
		
		Util.printSection("GeoData");
		GeoData.getInstance();
		if (Config.GEODATA == 2)
		{
			Util.printSection("PathFinding");
			PathFinding.getInstance();
		}
		
		Util.printSection("World");
		StaticObjects.getInstance();
		GameTimeController.getInstance();
		TeleportDataHolder.getInstance();
		BoatManager.getInstance();
		InstanceManager.getInstance();
		MerchantPriceConfigDataHolder.getInstance().loadInstances();
		
		Util.printSection("TaskManagers");
		AttackStanceTaskManager.getInstance();
		DecayTaskManager.getInstance();
		KnownListUpdateTaskManager.getInstance();
		LeakTaskManager.getInstance();
		MovementController.getInstance();
		PacketBroadcaster.getInstance();
		
		Util.printSection("Skills");
		SkillTreeTable.getInstance();
		SkillTable.getInstance();
		PetSkillsTable.getInstance();
		Class.forName(NobleSkillTable.class.getName());
		Class.forName(GMSkillTable.class.getName());
		Class.forName(HeroSkillTable.class.getName());
		ResidentialSkillTable.getInstance();
		
		Util.printSection("Items");
		Class.forName(ShotTable.class.getName());
		ItemTable.getInstance();
		ArmorSetsTable.getInstance();
		AugmentationDataHolder.getInstance();
		SkillSpellbookTable.getInstance();
		SummonItemsDataHolder.getInstance();
		ExtractableItemsData.getInstance();
		ExtractableSkillsData.getInstance();
		EnchantHPBonusDataHolder.getInstance();
		EnchantItemDataHolder.getInstance();
		L2Multisell.getInstance();
		if (Config.ALLOW_FISHING)
			FishDataHolder.getInstance();
		ItemsOnGroundManager.getInstance();
		if (Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0)
			ItemsAutoDestroy.getInstance();
		
		Util.printSection("Characters");
		CharNameTable.getInstance();
		CharTemplateTable.getInstance();
		LevelUpData.getInstance();
		HennaTable.getInstance();
		HennaTreeTable.getInstance();
		if (Config.ALLOW_WEDDING)
			CoupleService.getInstance();
		CursedWeaponsService.getInstance();
		
		CommunityService.getInstance();
		ClanTable.getInstance();
		CrestCache.getInstance();
		Hero.getInstance();
		BlockListService.getInstance();
		RecommendationService.getInstance();
		FriendListService.getInstance();
		
		// L2EMU_ADD
		UIDataHolder.getInstance();
		// L2EMU_ADD
		
		Util.printSection("NPCs");
		NpcTable.getInstance();
		MinionDataHolder.getInstance();
		// L2EMU_ADD
		GlobalDropTable.getInstance();
		// L2EMU_ADD
		HtmCache.getInstance();
		BuffTemplateTable.getInstance();
		if (Config.ALLOW_NPC_WALKERS)
			NpcWalkerRoutesDataHolder.getInstance();
		PetDataTable.getInstance().loadPetsData();
		
		Util.printSection("SevenSigns");
		SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		
		Util.printSection("Entities and zones");
		Class.forName(CrownManager.class.getName());
		TownManager.getInstance();
		ClanHallManager.getInstance();
		DoorTable.getInstance();
		CastleManager.getInstance().loadInstances();
		SiegeManager.getInstance().getSieges();
		FortManager.getInstance().loadInstances();
		CCHManager.getInstance();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		ZoneManager.getInstance();
		MercTicketManager.getInstance();
		DoorTable.getInstance().registerToClanHalls();
		DoorTable.getInstance().setCommanderDoors();
		AirShipManager.getInstance();
		// make sure that all the scheduled siege dates are in the Seal Validation period
		for (Castle castle : CastleManager.getInstance().getCastles().values())
			castle.getSiege().correctSiegeDateTime();
		for (CCHSiege siege : CCHManager.getInstance().getSieges())
			siege.correctSiegeDateTime();
		PartyRoomManager.getInstance();
		
		Util.printSection("Spawns");
		SpawnTable.getInstance();
		for (Fort fort : FortManager.getInstance().getForts())
			fort.getSpawnManager().initNpcs();
		DayNightSpawnManager.getInstance().notifyChangeMode();
		AutoChatManager.getInstance();
		AutoSpawnManager.getInstance();
		
		Util.printSection("Bosses");
		RaidBossManager.getInstance();
		
		Util.printSection("Hellbound");
		HellboundManager.getInstance();
		
		Util.printSection("Gracia");
		SeedOfDestructionManager.getInstance();
		
		Util.printSection("KrateiCube");
		KrateiCube.getInstance().init();
		
		Util.printSection("Quests");
		QuestService.getInstance();
		TransformationService.getInstance();
		
		Util.printSection("Events/ScriptEngine");
		try
		{
			File scripts = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "data/scripts.cfg");
			L2ScriptEngineManager.getInstance().executeScriptList(scripts);
		}
		catch (IOException ioe)
		{
			_log.fatal("Failed loading scripts.cfg, no script going to be loaded");
		}
		try
		{
			CompiledScriptCache compiledScriptCache = L2ScriptEngineManager.getInstance().getCompiledScriptCache();
			if (compiledScriptCache == null)
				_log.info("Compiled Scripts Cache is disabled.");
			else
			{
				compiledScriptCache.purge();
				if (compiledScriptCache.isModified())
				{
					compiledScriptCache.save();
					_log.info("Compiled Scripts Cache was saved.");
				}
				else
					_log.info("Compiled Scripts Cache is up-to-date.");
			}
			
		}
		catch (IOException e)
		{
			_log.fatal("Failed to store Compiled Scripts Cache.", e);
		}
		
		QuestService.getInstance().report();
		TransformationService.getInstance().report();
		
		Util.printSection("Economy");
		TradeListTable.getInstance();
		CastleManorService.getInstance();
		L2Manor.getInstance();
		AuctionService.getInstance();
		// L2EMU_ADD
		ItemAuctionService.getInstance();
		ItemMarketTable.getInstance();
		// L2EMU_ADD
		RecipeService.getInstance();
		
		Util.printSection("Olympiad");
		Olympiad.getInstance();
		
		Util.printSection("Dungeons");
		DimensionalRiftManager.getInstance();
		
		Util.printSection("Handlers");
		ItemHandler.getInstance();
		SkillTargetHandler.getInstance();
		SkillHandler.getInstance();
		AdminCommandHandler.getInstance();
		BypassHandler.getInstance();
		UserCommandHandler.getInstance();
		VoicedCommandHandler.getInstance();
		ChatHandler.getInstance();

		Util.printSection("Misc");
		ObjectRestrictions.getInstance();
		TaskManager.getInstance();
		Class.forName(GmListTable.class.getName());
		PetitionService.getInstance();
		if (Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL > 0)
			OnlineStatusTask.getInstance();
		
		MailService.getInstance();
		ClassBalanceDataHolder.getInstance();
		
		Util.printSection("Events");
		TvT.getInstance().init();
		if (Config.ARENA_ENABLED)
			ArenaManager.getInstance().engineInit();
		if (Config.FISHERMAN_ENABLED)
			FishermanManager.getInstance().engineInit();
		
		MerchantPriceConfigDataHolder.getInstance().updateReferences();
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		Util.printSection("ServerThreads");
		LoginServerThread.getInstance().start();
		
		L2GameSelectorThread.getInstance().openServerSocket(Config.GAMESERVER_HOSTNAME, Config.PORT_GAME);
		L2GameSelectorThread.getInstance().start();
		
		if (Config.ACCEPT_GEOEDITOR_CONN)
			GeoEditorListener.getInstance();
		
		if (Config.ENABLE_OFFLINE_TRADERS_RESTORE)
		{
			Util.printSection("Offline Trade");
			OfflineTradeManager.getInstance().restore();
		}
		
		// Print general infos related to DP
		// --------------------------------
		Util.printSection("L2EmuProject: DataPack");
		for (String line : VersionService.getDataPackVersionInfo())
			_log.info(line);

		// Print general infos related to GS
		// --------------------------------
		Util.printSection("L2EmuProject: GameServer");
		for (String line : VersionService.getFullVersionInfo())
			_log.info(line);
		
		Util.printSection("Memory");
		System.gc();
		System.runFinalization();
		for (String line : L2Thread.getMemoryUsageStatistics())
			_log.info(line);
		
		Util.printSection("Telnet");
		if (Config.IS_TELNET_ENABLED)
			GameStatusServer.initInstance();
		else
			_log.info("Telnet Server is currently disabled.");
		
		onStartup();
		
		Util.printSection("GameServerLog");
		_log.info("Maximum online users: " + Config.MAXIMUM_ONLINE_USERS);
		_log.info("Total Boot Time: " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");
	}
	
	private static Set<StartupHook> _startupHooks = new L2FastSet<StartupHook>();
	
	public synchronized static void addStartupHook(StartupHook hook)
	{
		if (_startupHooks != null)
			_startupHooks.add(hook);
		else
			hook.onStartup();
	}
	
	private synchronized static void onStartup()
	{
		final Set<StartupHook> startupHooks = _startupHooks;
		
		_startupHooks = null;
		
		for (StartupHook hook : startupHooks)
			hook.onStartup();
	}
	
	public interface StartupHook
	{
		public void onStartup();
	}
	
	public static Calendar getStartedTime()
	{
		return _serverStarted;
	}
}
