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
import net.l2emuproject.gameserver.model.world.L2Object;
import net.l2emuproject.gameserver.model.world.L2World;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.services.petition.PetitionService;

/**
 * This class handles commands for GMs to respond to petitions.
 * 
 * @author Tempy
 * 
 */
public class AdminPetition implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{
			"admin_view_petitions",
			"admin_view_petition",
			"admin_accept_petition",
			"admin_reject_petition",
			"admin_reset_petitions",
			"admin_force_peti",
			"admin_add_peti_chat"
													};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		L2Object targetChar = activeChar.getTarget();

		int petitionId = -1;

		try
		{
			petitionId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e)
		{
		}

		if (command.equals("admin_view_petitions"))
			PetitionService.getInstance().sendPendingPetitionList(activeChar);
		else if (command.startsWith("admin_view_petition"))
			PetitionService.getInstance().viewPetition(activeChar, petitionId);
		else if (command.startsWith("admin_accept_petition"))
		{
			if (PetitionService.getInstance().isPlayerInConsultation(activeChar))
			{
				activeChar.sendPacket(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME);
				return true;
			}

			if (PetitionService.getInstance().isPetitionInProcess(petitionId))
			{
				activeChar.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
				return true;
			}

			if (!PetitionService.getInstance().acceptPetition(activeChar, petitionId))
				activeChar.sendPacket(SystemMessageId.NOT_UNDER_PETITION_CONSULTATION);
		}
		else if (command.startsWith("admin_reject_petition"))
		{
			if (!PetitionService.getInstance().rejectPetition(activeChar, petitionId))
				activeChar.sendPacket(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER);
		}
		else if (command.equals("admin_reset_petitions"))
		{
			if (PetitionService.getInstance().isPetitionInProcess())
			{
				activeChar.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS);
				return false;
			}
			PetitionService.getInstance().clearPendingPetitions();
		}
		else if (command.startsWith("admin_force_peti"))
		{
			try
			{
				if (targetChar == null || !(targetChar instanceof L2PcInstance))
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT); // incorrect target!
					return false;
				}
				L2PcInstance targetPlayer = (L2PcInstance) targetChar;

				String val = command.substring(15);

				petitionId = PetitionService.getInstance().submitPetition(targetPlayer, val, 9);
				PetitionService.getInstance().acceptPetition(activeChar, petitionId);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //force_peti text");
				return false;
			}
		}
		else if (command.startsWith("admin_add_peti_chat"))
		{
			L2PcInstance player = L2World.getInstance().getPlayer(command.substring(20));
			if (player == null)
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
				return false;
			}
			petitionId = PetitionService.getInstance().submitPetition(player, "", 9);
			PetitionService.getInstance().acceptPetition(activeChar, petitionId);
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
