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
package net.l2emuproject.gameserver.handler;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.ThreadPoolManager;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminAI;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminAdmin;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminAutoAnnouncements;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminBBS;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminBan;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminBanChat;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminBuffs;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminCache;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminCamera;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminChangeAccessLevel;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminContest;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminCreateItem;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminDelete;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminDoorControl;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminEditChar;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminEditCharCustomData;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminEditNpc;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminEffects;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminElement;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminEnchant;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminExpSp;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminFightCalculator;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminFortSiege;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminGMSpeed;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminGeoEditor;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminGeodata;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminGm;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminGmChat;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminHTMLShow;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminHeal;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminHellbound;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminHelpPage;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminHide;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminInstance;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminInvul;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminJail;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminKick;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminKill;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminLevel;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminLogin;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminMammon;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminManor;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminMenu;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminMobGroup;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminMonsterRace;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminShowMovie;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminPForge;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminPetition;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminPledge;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminPolymorph;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminQuest;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminRegion;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminRepairChar;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminRes;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminRide;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminSeedOfDestruction;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminSendHome;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminShop;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminShowQuests;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminShutdown;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminSiege;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminSkill;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminSmartShop;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminSortMultisellItems;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminSpawn;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminSummon;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminSystem;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminTarget;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminTeleport;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminTerritoryWar;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminTest;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminUnblockIp;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminVitality;
import net.l2emuproject.gameserver.handler.admincommandhandlers.AdminZone;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.system.util.logging.GMAudit;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.util.HandlerRegistry;
import net.l2emuproject.util.logging.ListeningLog;
import net.l2emuproject.util.logging.ListeningLog.LogListener;

public final class AdminCommandHandler extends HandlerRegistry<String, IAdminCommandHandler>
{
	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private AdminCommandHandler()
	{
		register(new AdminAI());
		register(new AdminAdmin());
		register(new AdminAnnouncements());
		register(new AdminBBS());
		register(new AdminBan());
		register(new AdminBanChat());
		register(new AdminBuffs());
		register(new AdminCache());
		register(new AdminCamera());
		register(new AdminChangeAccessLevel());
		register(new AdminContest());
		register(new AdminCreateItem());
		register(new AdminCursedWeapons());
		register(new AdminDelete());
		register(new AdminDoorControl());
		register(new AdminEditChar());
		register(new AdminEditNpc());
		register(new AdminEffects());
		register(new AdminElement());
		register(new AdminEnchant());
		register(new AdminExpSp());
		register(new AdminFightCalculator());
		register(new AdminFortSiege());
		register(new AdminGeoEditor());
		register(new AdminGeodata());
		register(new AdminGm());
		register(new AdminGmChat());
		register(new AdminHeal());
		register(new AdminHelpPage());
		register(new AdminInstance());
		register(new AdminInvul());
		register(new AdminJail());
		register(new AdminKick());
		register(new AdminKill());
		register(new AdminLevel());
		register(new AdminLogin());
		register(new AdminMammon());
		register(new AdminManor());
		register(new AdminMenu());
		register(new AdminMobGroup());
		register(new AdminMonsterRace());
		register(new AdminPForge());
		register(new AdminPetition());
		register(new AdminPledge());
		register(new AdminPolymorph());
		register(new AdminQuest());
		register(new AdminRegion());
		register(new AdminRepairChar());
		register(new AdminRes());
		register(new AdminRide());
		register(new AdminSeedOfDestruction());
		register(new AdminSendHome());
		register(new AdminShop());
		register(new AdminShowQuests());
		register(new AdminShutdown());
		register(new AdminSiege());
		register(new AdminSkill());
		register(new AdminSmartShop());
		register(new AdminSortMultisellItems());
		register(new AdminSpawn());
		register(new AdminSummon());
		register(new AdminTarget());
		register(new AdminTeleport());
		register(new AdminTerritoryWar());
		register(new AdminTest());
		register(new AdminUnblockIp());
		register(new AdminVitality());
		register(new AdminZone());
		// L2EMU_ADD
		register(new AdminHellbound());
		register(new AdminGMSpeed());
		register(new AdminHide());
		register(new AdminShowMovie());
		register(new AdminSystem());
		register(new AdminEditCharCustomData());
		register(new AdminAutoAnnouncements());
		register(new AdminHTMLShow());
		// L2EMU_ADD

		// Dynamic testing extensions
		try
		{
			register((IAdminCommandHandler) Class.forName("net.l2emuproject.gameserver.handler.admincommandhandlers.AdminRuntimeTest").newInstance());
		}
		catch (Throwable t)
		{
		}
		
		_log.info("AdminCommandHandler: Loaded " + size() + " handlers.");
		
		for (String cmd : Config.GM_COMMAND_PRIVILEGES.keySet())
			if (get(cmd) == null)
				_log.warn("AdminCommandHandler: Command \"" + cmd + "\" isn't used anymore.");
	}
	
	private void register(IAdminCommandHandler handler)
	{
		registerAll(handler, handler.getAdminCommandList());
		
		for (String element : handler.getAdminCommandList())
			if (!Config.GM_COMMAND_PRIVILEGES.containsKey(element))
				_log.warn("Command \"" + element + "\" have no access level definition. Can't be used.");
	}
	
	public void useAdminCommand(final L2Player activeChar, String message0)
	{
		final String message = message0.trim();
		
		String command = message;
		String params = "";
		
		if (message.indexOf(" ") != -1)
		{
			command = message.substring(0, message.indexOf(" "));
			params = message.substring(message.indexOf(" ") + 1);
		}
		
		command = command.trim().toLowerCase();
		params = params.trim();
		
		if (!activeChar.isGM() && !command.equals("admin_gm"))
		{
			Util.handleIllegalPlayerAction(activeChar, "AdminCommandHandler: A non-gm request.", Config.DEFAULT_PUNISH);
			return;
		}
		
		final IAdminCommandHandler handler = get(command);
		
		if (handler == null)
		{
			activeChar.sendMessage("No handler registered.");
			_log.warn("No handler registered for bypass '" + message + "'");
			return;
		}
		
		if (!Config.GM_COMMAND_PRIVILEGES.containsKey(command))
		{
			activeChar.sendMessage("It has no access level definition. It can't be used.");
			_log.warn(message + "' has no access level definition. It can't be used.");
			return;
		}
		
		if (activeChar.getAccessLevel() < Config.GM_COMMAND_PRIVILEGES.get(command))
		{
			activeChar.sendMessage("You don't have sufficient privileges.");
			_log.warn(activeChar + " does not have sufficient privileges for '" + message + "'.");
			return;
		}
		
		GMAudit.auditGMAction(activeChar, "admincommand", command, params);
		
		final Future<?> task = ThreadPoolManager.getInstance().submitLongRunning(new Runnable() {
			@Override
			public void run()
			{
				_activeGm.set(activeChar);
				
				final long begin = System.currentTimeMillis();
				try
				{
					handler.useAdminCommand(message, activeChar);
				}
				catch (RuntimeException e)
				{
					activeChar.sendMessage("Exception during execution of  '" + message + "': " + e.toString());
					
					throw e;
				}
				finally
				{
					_activeGm.set(null);
					
					final long runtime = System.currentTimeMillis() - begin;
					
					if (runtime < ThreadPoolManager.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING)
						return;
					
					activeChar.sendMessage("The execution of '" + message + "' took " + Util.formatNumber(runtime) + " msec.");
				}
			}
		});
		
		try
		{
			task.get(1000, TimeUnit.MILLISECONDS);
			return;
		}
		catch (Exception e)
		{
			activeChar.sendMessage("The execution of '" + message + "' takes more time than 1000 msec, so execution done asynchronusly.");
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AdminCommandHandler _instance = new AdminCommandHandler();
	}
	
	private static final ThreadLocal<L2Player> _activeGm = new ThreadLocal<L2Player>();
	
	static
	{
		ListeningLog.addListener(new LogListener() {
			@Override
			public void write(String s)
			{
				final L2Player gm = _activeGm.get();
				
				if (gm != null)
					gm.sendMessage(s);
			}
		});
	}
}
