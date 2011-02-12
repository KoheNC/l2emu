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

import java.util.StringTokenizer;

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.model.L2Skill;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemChatChannelId;

/**
 * @author lord_rex
 */
public class AdminGMSpeed implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{ "admin_gmspeed" };
	private static final int		SKILL_ID		= 9903;

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if (command.startsWith("admin_gmspeed"))
		{
			try
			{
				final int level;

				if (st.hasMoreTokens())
					level = Integer.parseInt(st.nextToken());
				else
					level = activeChar.getEffects().hasEffect(SKILL_ID) ? 0 : 5;

				activeChar.stopSkillEffects(SKILL_ID);

				L2Skill skill = SkillTable.getInstance().getInfo(SKILL_ID, level);
				if (skill != null)
					skill.getEffects(activeChar, activeChar);

				switch (level)
				{
					case 0:
						activeChar.sendCreatureMessage(SystemChatChannelId.Chat_Normal, "SYS", "No message."); // TODO: Add retail message.
						break;
					case 1:
						activeChar.sendCreatureMessage(SystemChatChannelId.Chat_Normal, "SYS", "Your speed is [50] fast.");
						break;
					case 2:
						activeChar.sendCreatureMessage(SystemChatChannelId.Chat_Normal, "SYS", "Your speed is [100] fast.");
						break;
					case 3:
						activeChar.sendCreatureMessage(SystemChatChannelId.Chat_Normal, "SYS", "Your speed is [150] fast.");
						break;
					case 4:
						activeChar.sendCreatureMessage(SystemChatChannelId.Chat_Normal, "SYS", "Your speed is [200] fast.");
						break;
					case 5:
						activeChar.sendCreatureMessage(SystemChatChannelId.Chat_Normal, "SYS", "Your speed is [250] fast.");
						break;
					default:
						activeChar.sendCreatureMessage(SystemChatChannelId.Chat_Normal, "SYS", "//gmspeed [0...5].");
						break;
				}
			}
			catch (RuntimeException e)
			{
				activeChar.sendCreatureMessage(SystemChatChannelId.Chat_Normal, "SYS", "//gmspeed [0...5].");
			}
			finally
			{
				activeChar.updateEffectIcons();
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
