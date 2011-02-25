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
package net.l2emuproject.gameserver.handler.admincommandhandlers;

import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.entity.player.PlayerCustom.CharCustomData;
import net.l2emuproject.gameserver.world.object.L2Object;

/**
 * @author lord_rex
 */
public final class AdminEditCharCustomData implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{
			"admin_set_custom_data_hero",
			"admin_set_custom_data_noble",
			"admin_set_custom_data_donator",
			"admin_delete_custom_data"				};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();

		if (command.equalsIgnoreCase("admin_set_custom_data_hero"))
		{
			if (target instanceof L2PcInstance)
			{
				if (((L2PcInstance) target).isHero())
				{
					activeChar.sendMessage("Your target is already Hero.");
				}
				else
				{
					((L2PcInstance) target).setHero(true);
					((L2PcInstance) target).getPlayerCustom().updateCustomStatus(CharCustomData.HERO);

					((L2PcInstance) target).showHTMLMessage("You have received custom Hero status from Game Master.");
					activeChar.sendMessage("Your gave custom hero status to " + ((L2PcInstance) target).getName() + ".");

					((L2PcInstance) target).broadcastUserInfo();
				}
			}
			else
				activeChar.sendMessage("Your target is not player!");
		}
		else if (command.equalsIgnoreCase("admin_set_custom_data_noble"))
		{
			if (target instanceof L2PcInstance)
			{
				if (((L2PcInstance) target).isNoble())
				{
					activeChar.sendMessage("Your target is already Noble.");
				}
				else
				{
					((L2PcInstance) target).setNoble(true);
					((L2PcInstance) target).getPlayerCustom().updateCustomStatus(CharCustomData.NOBLE);

					((L2PcInstance) target).showHTMLMessage("You have received custom Noble status from Game Master.");
					activeChar.sendMessage("Your gave custom noble status to " + ((L2PcInstance) target).getName() + ".");

					((L2PcInstance) target).broadcastUserInfo();
				}
			}
			else
				activeChar.sendMessage("Your target is not player!");
		}
		else if (command.equalsIgnoreCase("admin_set_custom_data_donator"))
		{
			if (target instanceof L2PcInstance)
			{
				if (((L2PcInstance) target).isDonator())
				{
					activeChar.sendMessage("Your target is already Donator.");
				}
				else
				{
					((L2PcInstance) target).setDonator(true);
					((L2PcInstance) target).getPlayerCustom().updateCustomStatus(CharCustomData.DONATOR);

					((L2PcInstance) target).showHTMLMessage("You have received custom Donator status from Game Master.");
					activeChar.sendMessage("Your gave custom donator status to " + ((L2PcInstance) target).getName() + ".");

					((L2PcInstance) target).getAppearance().updateNameTitleColor();
					((L2PcInstance) target).broadcastUserInfo();
				}
			}
			else
				activeChar.sendMessage("Your target is not player!");
		}
		else if (command.equalsIgnoreCase("admin_delete_custom_data"))
		{
			if (target instanceof L2PcInstance)
			{
				if (((L2PcInstance) target).isHero() || ((L2PcInstance) target).isNoble() || ((L2PcInstance) target).isDonator())
				{
					((L2PcInstance) target).setHero(false);
					((L2PcInstance) target).setNoble(false);
					((L2PcInstance) target).setDonator(false);

					((L2PcInstance) target).getPlayerCustom().updateCustomStatus(CharCustomData.DELETE);
					((L2PcInstance) target).getPlayerCustom().restoreCustomStatus();

					((L2PcInstance) target).showHTMLMessage("Your all custom status was removed by Game Master.");
					activeChar.sendMessage("Your removed custom all status from " + ((L2PcInstance) target).getName() + ".");

					((L2PcInstance) target).getAppearance().updateNameTitleColor();
					((L2PcInstance) target).broadcastUserInfo();
				}
				else
					activeChar.sendMessage("Your target haven't got custom status.");
			}
			else
				activeChar.sendMessage("Your target is not player!");
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
