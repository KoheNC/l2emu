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
package net.l2emuproject.gameserver.system.restriction.global;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.GMSkillTable;
import net.l2emuproject.gameserver.datatables.GmListTable;
import net.l2emuproject.gameserver.handler.AdminCommandHandler;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.system.announcements.Announcements;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class GMRestriction extends AbstractRestriction
{
	@Override
	public final void playerLoggedIn(L2Player activeChar)
	{
		if (activeChar.isGM())
		{
			if (Config.SHOW_GM_LOGIN)
				Announcements.getInstance().announceToAll("GM " + activeChar.getName() + " has logged on.");

			if (Config.GM_STARTUP_INVISIBLE)
				AdminCommandHandler.getInstance().useAdminCommand(activeChar, "admin_invisible");

			if (Config.GM_STARTUP_SILENCE)
				AdminCommandHandler.getInstance().useAdminCommand(activeChar, "admin_silence");

			if (Config.GM_STARTUP_INVULNERABLE)
				AdminCommandHandler.getInstance().useAdminCommand(activeChar, "admin_invul on");

			// Don't add GM to GMList if hide is on..
			if (!Config.GM_HIDE && Config.GM_STARTUP_AUTO_LIST)
				GmListTable.addGm(activeChar, false);
			else
				GmListTable.addGm(activeChar, true);

			if (Config.GM_GIVE_SPECIAL_SKILLS)
				for (L2Skill skills : GMSkillTable.getGMSkills())
					activeChar.addSkill(skills, false); // Don't save GM Skills to DataBase.

			// Official like GMHide mode.
			if (Config.GM_HIDE)
				activeChar.setHiding(true, true);
		}
	}
}
