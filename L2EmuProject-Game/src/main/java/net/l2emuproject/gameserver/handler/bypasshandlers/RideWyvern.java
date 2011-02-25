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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.SevenSigns;
import net.l2emuproject.gameserver.datatables.PetDataTable;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2WyvernManagerInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

public class RideWyvern implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "RideWyvern" };

	@Override
	public boolean useBypass(String command, L2Player activeChar, L2Character target)
	{
		if (!(target instanceof L2WyvernManagerInstance))
			return false;

		if (!((L2WyvernManagerInstance) target).isOwnerClan(activeChar))
			return false;

		if ((SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DUSK) && SevenSigns.getInstance().isSealValidationPeriod())
		{
			activeChar.sendPacket(SystemMessageId.SEAL_OF_STRIFE_FORBIDS_SUMMONING);
			return false;
		}

		int petItemId = 0;
		L2ItemInstance petItem = null;

		if (activeChar.getPet() == null)
		{
			if (activeChar.isMounted())
			{
				petItem = activeChar.getInventory().getItemByObjectId(activeChar.getMountObjectID());
				if (petItem != null)
					petItemId = petItem.getItemId();
			}
		}
		else
			petItemId = activeChar.getPet().getControlItemId();

		if (petItemId == 0 || !activeChar.isMounted() || !PetDataTable.isStrider(PetDataTable.getPetIdByItemId(petItemId)))
		{
			activeChar.sendPacket(SystemMessageId.YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER);
			if (!((L2WyvernManagerInstance) target).isCastleManager())
				sendNotPossibleMessage(activeChar);

			return false;
		}
		else if (activeChar.isMounted() && PetDataTable.isStrider(PetDataTable.getPetIdByItemId(petItemId)) && petItem != null
				&& petItem.getEnchantLevel() < 55)
		{
			activeChar.sendMessage("Your Strider has not reached the required level.");

			if (!((L2WyvernManagerInstance) target).isCastleManager())
				sendNotPossibleMessage(activeChar);

			return false;
		}

		// Wyvern requires Config.MANAGER_CRYSTAL_COUNT crystal for ride...
		if (activeChar.getInventory().getItemByItemId(1460) != null
				&& activeChar.getInventory().getItemByItemId(1460).getCount() >= Config.ALT_MANAGER_CRYSTAL_COUNT)
		{
			if (!activeChar.disarmWeapons(true))
				return false;

			if (activeChar.isMounted())
				activeChar.dismount();

			if (activeChar.getPet() != null)
				activeChar.getPet().unSummon(activeChar);

			if (activeChar.mount(12621, 0, true))
			{
				activeChar.getInventory().destroyItemByItemId("Wyvern", 1460, Config.ALT_MANAGER_CRYSTAL_COUNT, activeChar, activeChar.getTarget());
				activeChar.addSkill(SkillTable.getInstance().getInfo(4289, 1));
				activeChar.sendMessage("The Wyvern has been summoned successfully!");
			}
			return true;
		}
		else
		{
			if (!((L2WyvernManagerInstance) target).isCastleManager())
				sendNotPossibleMessage(activeChar);

			activeChar.sendMessage("You need " + Config.ALT_MANAGER_CRYSTAL_COUNT + " Crystals: B Grade.");
		}

		return false;
	}

	private void sendNotPossibleMessage(L2Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/wyvernmanager/fortress-wyvernmanager-notpossible.htm");
		html.replace("%count%", String.valueOf(Config.ALT_MANAGER_CRYSTAL_COUNT));
		player.sendPacket(html);
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
