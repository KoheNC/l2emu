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
package net.l2emuproject.gameserver.system.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.network.Disconnection;
import net.l2emuproject.gameserver.network.L2GameClient;
import net.l2emuproject.gameserver.system.time.GameTimeController;

/**
 * Flood protector
 * 
 * @author durgus, fordfrog
 */
public final class FloodProtector
{
	public static enum Protected
	{
		USEITEM(100), 
		ROLLDICE(4200), 
		FIREWORK(4200), 
		GLOBAL_CHAT(Config.GLOBAL_CHAT_TIME * GameTimeController.MILLIS_IN_TICK), 
		TRADE_CHAT(Config.TRADE_CHAT_TIME * GameTimeController.MILLIS_IN_TICK), 
		ITEMPETSUMMON(1600), HEROVOICE(10000), 
		SOCIAL(Config.SOCIAL_TIME * GameTimeController.MILLIS_IN_TICK), 
		SUBCLASS(500), 
		DROPITEM(500), 
		MULTISELL(500), 
		TRANSACTION(500), 
		SEND_MAIL(60000),
		
		// L2EMU_ADD
		LINK(3000);
		// L2EMU_ADD

		protected final int	_reuseDelay;

		private Protected(int reuseDelay)
		{
			_reuseDelay = reuseDelay;
		}
	}

	private static final Log	_log				= LogFactory.getLog(FloodProtector.class);

	private volatile int		_nextGameTick		= GameTimeController.getGameTicks();
	private volatile boolean	_punishmentInProgress;
	private AtomicInteger		_count				= new AtomicInteger(0);

	private boolean				_logged;

	private final L2GameClient	_client;

	private static final int	PUNISHMENT_LIMIT	= 0;

	public FloodProtector(final L2GameClient client)
	{
		super();
		_client = client;
	}

	public final boolean tryPerformAction(Protected command)
	{
		final int curTick = GameTimeController.getGameTicks();

		if (curTick < _nextGameTick || _punishmentInProgress)
		{
			if (!_logged)
			{
				_log.warn(_client.getActiveChar().getName() + " called command " + command + " ~" + (command.ordinal() - (_nextGameTick - curTick))
						* GameTimeController.MILLIS_IN_TICK + " ms after previous command, so kicked.");

				_logged = true;
			}

			_count.incrementAndGet();

			if (!_punishmentInProgress && _count.get() >= PUNISHMENT_LIMIT)
			{
				_punishmentInProgress = true;
				new Disconnection(_client.getActiveChar()).defaultSequence(false);
				_punishmentInProgress = false;
			}

			return false;
		}

		if (_count.get() > 0)
		{
			_log.warn(_client.getActiveChar().getName() + " issued " + String.valueOf(_count) + " extra requests within ~" + command.ordinal()
					* GameTimeController.MILLIS_IN_TICK + " ms");
		}

		_nextGameTick = curTick + command.ordinal();
		_logged = false;
		_count.set(0);

		return true;
	}
}
