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
package net.l2emuproject.gameserver.network.clientpackets;

import net.l2emuproject.gameserver.events.global.blockchecker.HandysBlockCheckerManager;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Format: chdd
 * d: Arena
 * d: Team
 * 
 * @author mrTJO
 */
public final class RequestExCubeGameChangeTeam extends L2GameClientPacket
{
	private static final String	_C__D0_5A_REQUESTEXCUBEGAMECHANGETEAM	= "[C] D0:5A RequestExCubeGameChangeTeam";
	private static Log			_log									= LogFactory.getLog(RequestExCubeGameChangeTeam.class);

	private int					_arena;
	private int					_team;

	@Override
	protected final void readImpl()
	{
		// client sends -1,0,1,2 for arena parameter
		_arena = readD() + 1;
		_team = readD();
	}

	@Override
	public final void runImpl()
	{
		// Do not remove players after start
		if (HandysBlockCheckerManager.getInstance().arenaIsBeingUsed(_arena))
			return;
		L2Player player = getClient().getActiveChar();

		switch (_team)
		{
			case 0:
			case 1:
				// Change Player Team
				HandysBlockCheckerManager.getInstance().changePlayerToTeam(player, _arena, _team);
				break;
			case -1:
				// Remove Player (me)			
				int team = HandysBlockCheckerManager.getInstance().getHolder(_arena).getPlayerTeam(player);
				// client sends two times this packet if click on exit
				// client did not send this packet on restart
				if (team > -1)
					HandysBlockCheckerManager.getInstance().removePlayer(player, _arena, team);
				break;
			default:
				_log.warn("Wrong Cube Game Team ID: " + _team);
				break;
		}
	}

	@Override
	public final String getType()
	{
		return _C__D0_5A_REQUESTEXCUBEGAMECHANGETEAM;
	}
}
