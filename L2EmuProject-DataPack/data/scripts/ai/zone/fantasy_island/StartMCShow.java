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
package ai.zone.fantasy_island;

import net.l2emuproject.gameserver.services.quest.QuestService;

public class StartMCShow implements Runnable
{
	@Override
	public void run()
	{
		QuestService.getInstance().getQuest("MC_Show").notifyEvent("Start", null, null);
	}
}
