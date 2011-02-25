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
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import net.l2emuproject.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;

public class Augment implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "Augment" };

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		try
		{
			if (command.startsWith("Augment"))
			{
				int cmdChoice = Integer.parseInt(command.substring(8, 9).trim());
				switch (cmdChoice)
				{
					case 1:
						activeChar.sendPacket(SystemMessageId.SELECT_THE_ITEM_TO_BE_AUGMENTED);
						activeChar.sendPacket(new ExShowVariationMakeWindow());
						return true;
					case 2:
						activeChar.sendPacket(SystemMessageId.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION);
						activeChar.sendPacket(new ExShowVariationCancelWindow());
						return true;
				}
			}
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
