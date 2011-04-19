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

import net.l2emuproject.gameserver.handler.bypasshandlers.*;
import net.l2emuproject.util.HandlerRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author nBd
 */
public class BypassHandler extends HandlerRegistry<String, IBypassHandler>
{
	private static final Log					_log	= LogFactory.getLog(BypassHandler.class);

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
		registerBypassHandler(new ArenaInfo());
		registerBypassHandler(new Augment());
		registerBypassHandler(new Buy());
		registerBypassHandler(new BuyShadowItem());
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
		
		_log.info(getClass().getSimpleName() + " : Loaded " + size() + " handler(s).");
	}

	public void registerBypassHandler(IBypassHandler handler)
	{
		registerAll(handler, handler.getBypassList());
	}

	public IBypassHandler getBypassHandler(String BypassCommand)
	{
		final String command = BypassCommand.trim();
		String bypass = command;
		
		if (command.indexOf(" ") != -1)
			bypass = command.substring(0, command.indexOf(" "));
		
		return get(bypass);
	}
}
