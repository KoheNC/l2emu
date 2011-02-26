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
package net.l2emuproject.gameserver.handler.bypasshandlers;

import java.util.StringTokenizer;

import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2MerchantInstance;

public class Buy implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "Buy" };

	@Override
	public boolean useBypass(String command, L2Player activeChar, L2Character target)
	{
		if (!(target instanceof L2MerchantInstance))
			return false;

		try
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();

			if (st.countTokens() < 1)
				return false;

			int val = Integer.parseInt(st.nextToken());
			((L2MerchantInstance) target).showBuySellRefundWindow(activeChar, val);

			return true;
		}
		catch (Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName());
		}
		return false;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
