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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2MerchantInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;

public class RentPet implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "RentPet" };

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2MerchantInstance))
			return false;

		if (!Config.ALLOW_RENTPET)
			return false;

		if (!Config.LIST_PET_RENT_NPC.contains(((L2Npc) target).getTemplate().getNpcId()))
			return false;

		try
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();

			if (st.countTokens() < 1)
			{
				((L2MerchantInstance) target).showRentPetWindow(activeChar);
			}
			else
			{
				int val = Integer.parseInt(st.nextToken());
				((L2MerchantInstance) target).tryRentPet(activeChar, val);
			}

			return true;
		}
		catch (Exception e)
		{
			_log.info("Exception in " + getClass().getSimpleName());
		}

		return false;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
