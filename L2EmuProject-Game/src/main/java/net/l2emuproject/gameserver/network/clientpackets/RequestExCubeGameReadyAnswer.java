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

import net.l2emuproject.gameserver.instancemanager.games.HandysBlockCheckerManager;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mrTJO
 * Format: chddd
 * 
 * d: Arena
 * d: Answer
 */
public final class RequestExCubeGameReadyAnswer extends L2GameClientPacket
{
	private static final String	_C__D0_5C_REQUESTEXCUBEGAMEREADYANSWER	= "[C] D0:5C RequestExCubeGameReadyAnswer";
	private static Log			_log									= LogFactory.getLog(RequestExCubeGameReadyAnswer.class);

	private int					_arena;
	private int					_answer;

	@Override
	protected final void readImpl()
	{
		// client sends -1,0,1,2 for arena parameter
		_arena = readD() + 1;
		// client sends 1 if clicked confirm on not clicked, 0 if clicked cancel
		_answer = readD();
	}

	@Override
	public final void runImpl()
	{
		L2Player player = getClient().getActiveChar();

		if (player == null)
			return;

		switch (_answer)
		{
			case 0:
				// Cancel - Answer No
				break;
			case 1:
				// OK or Time Over
				HandysBlockCheckerManager.getInstance().increaseArenaVotes(_arena);
				break;
			default:
				_log.warn("Unknown Cube Game Answer ID: " + _answer);
				break;
		}
	}

	@Override
	public final String getType()
	{
		return _C__D0_5C_REQUESTEXCUBEGAMEREADYANSWER;
	}
}
