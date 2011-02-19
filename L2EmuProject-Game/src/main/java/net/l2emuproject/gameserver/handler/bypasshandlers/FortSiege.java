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

import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.instancemanager.TerritoryWarManager;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.actor.instance.L2FortSiegeNpcInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.clan.L2Clan;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;

public final class FortSiege implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "fort_register", "fort_unregister" };

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2FortSiegeNpcInstance))
			return false;

		if (activeChar.getClanId() > 0 && (activeChar.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) == L2Clan.CP_CS_MANAGE_SIEGE)
		{
			if (command.toLowerCase().startsWith(COMMANDS[0])) // register
			{
				if (System.currentTimeMillis() < TerritoryWarManager.getInstance().getTWStartTimeInMillis()
						&& TerritoryWarManager.getInstance().getIsRegistrationOver())
				{
					activeChar.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
					return false;
				}
				else if (System.currentTimeMillis() > TerritoryWarManager.getInstance().getTWStartTimeInMillis()
						&& TerritoryWarManager.getInstance().isTWChannelOpen())
				{
					activeChar.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
					return false;
				}
				else if (((L2Npc) target).getFort().getSiege().registerAttacker(activeChar, false))
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.REGISTERED_TO_S1_FORTRESS_BATTLE);
					sm.addString(((L2Npc) target).getFort().getName());
					activeChar.sendPacket(sm);
					((L2Npc) target).showChatWindow(activeChar, 7);
					return true;
				}
			}
			else if (command.toLowerCase().startsWith(COMMANDS[1])) // unregister
			{
				((L2Npc) target).getFort().getSiege().removeSiegeClan(activeChar.getClan());
				((L2Npc) target).showChatWindow(activeChar, 8);
				return true;
			}
			return false;
		}
		else
			((L2Npc) target).showChatWindow(activeChar, 10);

		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
