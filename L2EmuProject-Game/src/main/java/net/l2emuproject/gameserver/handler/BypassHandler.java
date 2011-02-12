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

import gnu.trove.TIntObjectHashMap;
import net.l2emuproject.gameserver.handler.bypasshandlers.ArenaInfo;
import net.l2emuproject.gameserver.handler.bypasshandlers.Augment;
import net.l2emuproject.gameserver.handler.bypasshandlers.Buy;
import net.l2emuproject.gameserver.handler.bypasshandlers.BuyTransform;
import net.l2emuproject.gameserver.handler.bypasshandlers.CPRecovery;
import net.l2emuproject.gameserver.handler.bypasshandlers.ChatLink;
import net.l2emuproject.gameserver.handler.bypasshandlers.ClanWarehouse;
import net.l2emuproject.gameserver.handler.bypasshandlers.ClassMaster;
import net.l2emuproject.gameserver.handler.bypasshandlers.DrawHenna;
import net.l2emuproject.gameserver.handler.bypasshandlers.Exchange;
import net.l2emuproject.gameserver.handler.bypasshandlers.FishSkillList;
import net.l2emuproject.gameserver.handler.bypasshandlers.FishermanInfo;
import net.l2emuproject.gameserver.handler.bypasshandlers.FortSiege;
import net.l2emuproject.gameserver.handler.bypasshandlers.GiveBlessing;
import net.l2emuproject.gameserver.handler.bypasshandlers.ItemAuctionLink;
import net.l2emuproject.gameserver.handler.bypasshandlers.Loto;
import net.l2emuproject.gameserver.handler.bypasshandlers.MakeBuffs;
import net.l2emuproject.gameserver.handler.bypasshandlers.ManorMenuSelect;
import net.l2emuproject.gameserver.handler.bypasshandlers.Multisell;
import net.l2emuproject.gameserver.handler.bypasshandlers.NobleTeleport;
import net.l2emuproject.gameserver.handler.bypasshandlers.NpcFindById;
import net.l2emuproject.gameserver.handler.bypasshandlers.OpenGate;
import net.l2emuproject.gameserver.handler.bypasshandlers.PrivateWarehouse;
import net.l2emuproject.gameserver.handler.bypasshandlers.QuestLink;
import net.l2emuproject.gameserver.handler.bypasshandlers.QuestList;
import net.l2emuproject.gameserver.handler.bypasshandlers.ReleaseAttribute;
import net.l2emuproject.gameserver.handler.bypasshandlers.RemoveDeathPenalty;
import net.l2emuproject.gameserver.handler.bypasshandlers.RemoveHennaList;
import net.l2emuproject.gameserver.handler.bypasshandlers.RentPet;
import net.l2emuproject.gameserver.handler.bypasshandlers.RideWyvern;
import net.l2emuproject.gameserver.handler.bypasshandlers.Rift;
import net.l2emuproject.gameserver.handler.bypasshandlers.SupportMagic;
import net.l2emuproject.gameserver.handler.bypasshandlers.TerritoryStatus;
import net.l2emuproject.gameserver.handler.bypasshandlers.TerritoryWar;
import net.l2emuproject.gameserver.handler.bypasshandlers.WakeBaium;
import net.l2emuproject.gameserver.handler.bypasshandlers.Wear;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author nBd
 */
public class BypassHandler
{
	private static final Log					_log	= LogFactory.getLog(BypassHandler.class);

	private TIntObjectHashMap<IBypassHandler>	_datatable;

	@SuppressWarnings("synthetic-access")
	private static final class SingletonHolder
	{
		private static final BypassHandler	INSTANCE	= new BypassHandler();
	}

	public static BypassHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private BypassHandler()
	{
		_datatable = new TIntObjectHashMap<IBypassHandler>();

		registerBypassHandler(new ArenaInfo());
		registerBypassHandler(new Augment());
		registerBypassHandler(new Buy());
		registerBypassHandler(new BuyTransform());
		registerBypassHandler(new ChatLink());
		registerBypassHandler(new ClanWarehouse());
		registerBypassHandler(new ClassMaster());
		registerBypassHandler(new CPRecovery());
		registerBypassHandler(new DrawHenna());
		registerBypassHandler(new Exchange());
		registerBypassHandler(new FishermanInfo());
		registerBypassHandler(new FishSkillList());
		registerBypassHandler(new FortSiege());
		registerBypassHandler(new GiveBlessing());
		registerBypassHandler(new ItemAuctionLink());
		registerBypassHandler(new Loto());
		registerBypassHandler(new MakeBuffs());
		registerBypassHandler(new ManorMenuSelect());
		registerBypassHandler(new Multisell());
		registerBypassHandler(new NobleTeleport());
		registerBypassHandler(new NpcFindById());
		registerBypassHandler(new OpenGate());
		registerBypassHandler(new PrivateWarehouse());
		registerBypassHandler(new QuestLink());
		registerBypassHandler(new QuestList());
		registerBypassHandler(new ReleaseAttribute());
		registerBypassHandler(new RemoveDeathPenalty());
		registerBypassHandler(new RemoveHennaList());
		registerBypassHandler(new RentPet());
		registerBypassHandler(new RideWyvern());
		registerBypassHandler(new Rift());
		registerBypassHandler(new SupportMagic());
		registerBypassHandler(new TerritoryStatus());
		registerBypassHandler(new TerritoryWar());
		registerBypassHandler(new WakeBaium());
		registerBypassHandler(new Wear());
		
		_log.info("BypassHandler: Loaded " + size() + " handlers.");
	}

	public void registerBypassHandler(IBypassHandler handler)
	{
		for (String element : handler.getBypassList())
		{
			if (_log.isDebugEnabled())
				_log.debug("Adding handler for command " + element);

			_datatable.put(element.toLowerCase().hashCode(), handler);
		}
	}

	public IBypassHandler getBypassHandler(String BypassCommand)
	{
		String command = BypassCommand;

		if (BypassCommand.indexOf(" ") != -1)
		{
			command = BypassCommand.substring(0, BypassCommand.indexOf(" "));
		}

		if (_log.isDebugEnabled())
			_log.debug("Getting handler for command: " + command + " -> " + (_datatable.get(command.hashCode()) != null));

		return _datatable.get(command.toLowerCase().hashCode());
	}

	public int size()
	{
		return _datatable.size();
	}
}