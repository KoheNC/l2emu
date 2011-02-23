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
package net.l2emuproject.gameserver.handler.usercommandhandlers;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.handler.IUserCommandHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance.TeleportMode;
import net.l2emuproject.gameserver.model.mapregion.TeleportWhereType;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.skills.L2Skill;

public class Escape implements IUserCommandHandler
{
	private static final int[]	COMMAND_IDS	=
											{ 52 };

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.l2emuproject.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (!activeChar.canTeleport(TeleportMode.UNSTUCK))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		int unstuckTimer = (activeChar.getAccessLevel() >= Config.GM_ESCAPE ? 1000 : Config.UNSTUCK_INTERVAL * 1000);

		L2Skill GM_escape = SkillTable.getInstance().getInfo(2100, 1); // 1 second escape
		L2Skill escape = SkillTable.getInstance().getInfo(2099, 1); // 5 minutes escape
		if (activeChar.getAccessLevel() >= Config.GM_ESCAPE)
		{
			if (GM_escape != null)
			{
				activeChar.sendMessage("You use Escape: 1 second.");
				activeChar.useMagic(GM_escape, false, false);
				return true;
			}
		}
		else if (Config.UNSTUCK_INTERVAL == 300 && escape  != null)
		{
			activeChar.useMagic(escape, false, false);
			return true;
		}
		else
		{
			if (Config.UNSTUCK_INTERVAL > 100)
			{
				activeChar.sendMessage("You use Escape: " + unstuckTimer / 60000 + " minutes.");
			}
			else
				activeChar.sendMessage("You use Escape: " + unstuckTimer / 1000 + " seconds.");
		}

		// Continue execution later
		activeChar.setTeleportSkillCast(new EscapeFinalizer(activeChar), unstuckTimer);

		return true;
	}

	static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance	_activeChar;

		EscapeFinalizer(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}

		@Override
		public void run()
		{
			_activeChar.setIsIn7sDungeon(false);
			_activeChar.setInstanceId(0);
			_activeChar.teleToLocation(TeleportWhereType.Town);
		}
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
