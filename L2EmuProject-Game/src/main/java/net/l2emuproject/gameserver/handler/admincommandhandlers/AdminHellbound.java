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

import net.l2emuproject.gameserver.handler.IAdminCommandHandler;
import net.l2emuproject.gameserver.instancemanager.hellbound.HellboundManager;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Psycho(killer1888) / L2jFree
 */
public final class AdminHellbound implements IAdminCommandHandler
{
	private static final String[]	ADMIN_COMMANDS	=
													{
			"admin_set_hellbound_level",
			"admin_add_trust_points",
			"admin_remove_trust_points",
			"admin_add_warpgate_points",
			"admin_remove_warpgate_points"			};

	@Override
	public final boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();
		String val = st.nextToken();

		if (cmd.equals("admin_set_hellbound_level"))
		{
			HellboundManager.getInstance().setHellboundLevel(Integer.valueOf(val));
		}
		else if (cmd.equals("admin_add_trust_points"))
		{
			HellboundManager.getInstance().addTrustPoints(Integer.valueOf(val));
			activeChar.sendMessage("Added " + val + " trust points to Hellbound.");
		}
		else if (cmd.equals("admin_remove_trust_points"))
		{
			HellboundManager.getInstance().removeTrustPoints(Integer.valueOf(val));
			activeChar.sendMessage("Removed " + val + " trust points to Hellbound.");
		}
		else if (cmd.equals("admin_add_warpgate_points"))
		{
			HellboundManager.getInstance().addWarpgateEnergy(Integer.valueOf(val));
			activeChar.sendMessage("Added " + val + " points to warpgates.");
		}
		else if (cmd.equals("admin_remove_warpgate_points"))
		{
			HellboundManager.getInstance().subWarpgateEnergy(Integer.valueOf(val));
			activeChar.sendMessage("Removed " + val + " points to warpgates.");
		}

		return true;
	}

	@Override
	public final String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
