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
import net.l2emuproject.gameserver.handler.IBypassHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.NpcHtmlMessage;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.lang.L2TextBuilder;

public class RemoveDeathPenalty implements IBypassHandler
{
	private static final String[]	COMMANDS	=
												{ "remove_dp" };

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;
		
		try
		{
			int cmdChoice = Integer.parseInt(command.substring(10, 11).trim());
			int[] pen_clear_price =
			{ 3600, 8640, 25200, 50400, 86400, 144000, 144000, 144000 };
			int price = pen_clear_price[activeChar.getExpertiseIndex()] * (int) Config.RATE_DROP_ADENA;
			switch (cmdChoice)
			{
				case 1:
					String filename = "data/html/default/30981-1.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
					html.setFile(filename);
					html.replace("%objectId%", String.valueOf(target.getObjectId()));
					html.replace("%dp_price%", String.valueOf(price));
					activeChar.sendPacket(html);
					break;
				case 2:
					NpcHtmlMessage Reply = new NpcHtmlMessage(target.getObjectId());
					L2TextBuilder replyMSG = L2TextBuilder.newInstance("<html><body>Black Judge:<br>");

					if (activeChar.getDeathPenaltyBuffLevel() > 0)
					{
						if (activeChar.getAdena() >= price)
						{
							if (!activeChar.reduceAdena("DeathPenality", price, target, true))
								return false;
							activeChar.setDeathPenaltyBuffLevel(activeChar.getDeathPenaltyBuffLevel() - 1);
							activeChar.sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
							activeChar.sendEtcStatusUpdate();
							return true;
						}

						replyMSG.append("The wound you have received from death's touch is too deep to be healed for the money you have to give me. Find more money if you wish death's mark to be fully removed from you.");
					}
					else
					{
						replyMSG.append("You have no more death wounds that require healing.<br>");
						replyMSG.append("Go forth and fight, both for this world and your own glory.");
					}

					replyMSG.append("</body></html>");
					Reply.setHtml(replyMSG.moveToString());
					activeChar.sendPacket(Reply);
					break;
			}
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
