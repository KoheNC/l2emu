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

import net.l2emuproject.Config;
import net.l2emuproject.L2DatabaseFactory;
import net.l2emuproject.gameserver.datatables.TradeListTable;
import net.l2emuproject.gameserver.instancemanager.GrandBossSpawnManager;
import net.l2emuproject.gameserver.instancemanager.ItemsOnGroundManager;
import net.l2emuproject.gameserver.instancemanager.MercTicketManager;
import net.l2emuproject.gameserver.instancemanager.QuestManager;
import net.l2emuproject.gameserver.instancemanager.RaidBossSpawnManager;
import net.l2emuproject.gameserver.instancemanager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.instancemanager.leaderboards.ArenaManager;
import net.l2emuproject.gameserver.instancemanager.leaderboards.FishermanManager;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.olympiad.Olympiad;
import net.l2emuproject.gameserver.model.restriction.ObjectRestrictions;
import net.l2emuproject.gameserver.model.world.L2World;
import net.l2emuproject.gameserver.network.Disconnection;
import net.l2emuproject.gameserver.network.L2GameSelectorThread;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.cursedweapons.CursedWeaponsService;
import net.l2emuproject.gameserver.services.itemauction.ItemAuctionService;
import net.l2emuproject.gameserver.services.manor.CastleManorService;
import net.l2emuproject.gameserver.taskmanager.SQLQueue;
import net.l2emuproject.gameserver.util.DatabaseBackupManager;
import net.l2emuproject.gameserver.util.OfflineTradeManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides the functions for shutting down and restarting the server It closes all open clientconnections
 * and saves all data.
 * 
 * @version $Revision: 1.2.4.5 $ $Date: 2005/03/27 15:29:09 $
 */
public final class Shutdown extends Thread
{
	public static enum ShutdownMode
	{
		NONE("terminating"),
		SHUTDOWN("shutting down"),
		RESTART("restarting");
		
		private final String _text;
		
		private ShutdownMode(String text)
		{
			_text = text;
		}
		
		private String getText()
		{
			return _text;
		}
	}
	
	private static final Log _log = LogFactory.getLog(Shutdown.class);
	
	private static int _counter = Integer.MAX_VALUE;
	private static ShutdownMode _mode = ShutdownMode.NONE;
	
	private static Shutdown _counterInstance;
	private static Shutdown _hookInstance;
	
	public static Shutdown getInstance()
	{
		if (_hookInstance == null)
			_hookInstance = new Shutdown();
		
		return _hookInstance;
	}
	
	private Shutdown()
	{
	}
	
	@Override
	public void run()
	{
		if (this == _counterInstance)
			countdown();
		else if (this == _hookInstance)
			shutdownHook();
	}
	
	private void countdown()
	{
		while (_counter > 0 && this == _counterInstance)
		{
			if (_counter <= 30 || _counter <= 600 && _counter % 60 == 0 || _counter <= 3600 && _counter % 300 == 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS);
				Announcements.getInstance().announceToAll(sm.addNumber(_counter));
			}
			
			try
			{
				if (_counter <= 60)
					LoginServerThread.getInstance().setServerStatusDown();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			_counter--;
			
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		// shutdown aborted
		if (this != _counterInstance)
			return;
		
		// last point where logging is operational :(
		_log.warn("Shutdown countdown is over: " + _mode.getText() + " NOW!");
		
		if (_mode == ShutdownMode.RESTART)
			System.exit(2);
		else
			System.exit(0);
	}
	
	private void shutdownHook()
	{
		try
		{
			Announcements.getInstance().announceToAll("Server is " + _mode.getText() + " NOW!");
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}

		if (Config.ENABLE_OFFLINE_TRADERS_RESTORE)
			OfflineTradeManager.getInstance().store();
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			try
			{
				new Disconnection(player).defaultSequence(true);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
		
		// Seven Signs data is now saved along with Festival data.
		if (!SevenSigns.getInstance().isSealValidationPeriod())
			SevenSignsFestival.getInstance().saveFestivalData(false);
		
		// Save restrictions that are apllied to L2Objects
		ObjectRestrictions.getInstance().shutdown();
		// Save Seven Signs data before closing. :)
		SevenSigns.getInstance().saveSevenSignsData(null, true);
		System.out.println("SevenSigns: Data saved.");
		RaidBossSpawnManager.getInstance().cleanUp();
		System.out.println("RaidBossSpawnManager: All raidboss info saved.");
		GrandBossSpawnManager.getInstance().cleanUp();
		System.out.println("GrandBossSpawnManager: All grandboss info saved.");
		System.out.println("Saving TradeController data, please wait...");
		TradeListTable.getInstance().dataCountStore();
		System.out.println("TradeController: All count Item Saved");
		ItemAuctionService.getInstance().shutdown();
		System.out.println("ItemAuctionService: Items are saved.");
		Olympiad.getInstance().saveOlympiadStatus();
		System.out.println("Olympiad System: Data saved!!");
		
		// Save all manor data
		CastleManorService.getInstance().save();
		
		// Save all global (non-player specific) Quest data that needs to persist after reboot
		QuestManager.getInstance().save();
		
		// Save Arena Data if enabled
		if (Config.ARENA_ENABLED)
		{
			ArenaManager.getInstance().stopSaveTask();
			ArenaManager.getInstance().saveData();
		}
		
		// Save Fishing Data if enabled
		if (Config.FISHERMAN_ENABLED)
		{
			FishermanManager.getInstance().stopSaveTask();
			FishermanManager.getInstance().saveData();
		}
		
		// Save Cursed Weapons data before closing.
		CursedWeaponsService.getInstance().saveData();
		System.out.println("CursedWeaponsService: Data saved.");
		// Save items on ground before closing
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().saveInDb();
			ItemsOnGroundManager.getInstance().cleanUp();
			System.out.println("ItemsOnGroundManager: All items on ground saved.");
		}
		if (MercTicketManager.getInstance().stopSaveTask())
			MercTicketManager.getInstance().saveAll();
		
		HellboundManager.getInstance().cleanUp();
		System.out.println("HellboundManager: Data saved.");
		
		PersistentProperties.store();
		System.out.println("PersistentProperties: All datas are stored.");
		
		SQLQueue.getInstance().run();
		System.out.println("Data saved. All players disconnected, " + _mode.getText() + ".");
		
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			GameTimeController.stopTimer();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		
		try
		{
			LoginServerThread.getInstance().interrupt();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		
		try
		{
			L2GameSelectorThread.getInstance().shutdown();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		
		try
		{
			ThreadPoolManager.getInstance().shutdown();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		
		try
		{
			L2DatabaseFactory.getInstance().shutdown();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		
		if (Config.DATABASE_BACKUP_MAKE_BACKUP_ON_SHUTDOWN)
			DatabaseBackupManager.makeBackup();
		
		if (_mode == ShutdownMode.RESTART)
			Runtime.getRuntime().halt(2);
		else
			Runtime.getRuntime().halt(0);
	}
	
	public static void start(String initiator, int seconds, ShutdownMode mode)
	{
		_log.warn(initiator + " issued shutdown command: " + mode.getText() + " in " + seconds + " seconds!");
		
		String msg = "Server is " + mode.getText() + " in " + seconds + " seconds!";
		
		Announcements.getInstance().announceToAll("Attention players!");
		Announcements.getInstance().announceToAll(msg);
		
		_counter = seconds;
		_mode = mode;
		
		_counterInstance = new Shutdown();
		_counterInstance.start();
	}
	
	public static void abort(String initiator)
	{
		_log.warn(initiator + " issued shutdown abort: " + _mode.getText() + " has been stopped!");
		
		String msg = "Server aborts " + _mode.getText() + " and continues normal operation!";
		
		Announcements.getInstance().announceToAll(msg);

		_counter = Integer.MAX_VALUE;
		_mode = ShutdownMode.NONE;
		
		_counterInstance = null;
	}
	
	public static void halt(String initiator)
	{
		try
		{
			System.out.println(initiator + " issued HALT command: " + _mode.getText() + " has been stopped!");
		}
		finally
		{
			Runtime.getRuntime().halt(2);
		}
	}
	
	public static boolean isActionDisabled(DisableType type)
	{
		return type.isDisabled() && Config.SAFE_REBOOT && _counterInstance != null
			&& _counter <= Config.SAFE_REBOOT_TIME;
	}
	
	public static enum DisableType
	{
		ENCHANT,
		TELEPORT,
		CREATEITEM,
		TRANSACTION,
		PC_ITERACTION,
		NPC_ITERACTION;
		
		private boolean isDisabled()
		{
			switch (this)
			{
				case ENCHANT:
					return Config.SAFE_REBOOT_DISABLE_ENCHANT;
				case TELEPORT:
					return Config.SAFE_REBOOT_DISABLE_TELEPORT;
				case CREATEITEM:
					return Config.SAFE_REBOOT_DISABLE_CREATEITEM;
				case TRANSACTION:
					return Config.SAFE_REBOOT_DISABLE_TRANSACTION;
				case PC_ITERACTION:
					return Config.SAFE_REBOOT_DISABLE_PC_ITERACTION;
				case NPC_ITERACTION:
					return Config.SAFE_REBOOT_DISABLE_NPC_ITERACTION;
				default:
					throw new InternalError();
			}
		}
	}

	public static boolean isInProgress()
	{
		return _counterInstance != null;
	}
}
