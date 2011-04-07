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

import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author JIV
 */
public final class EndScenePlayer extends L2GameClientPacket
{
	private static final String	_C__d05b_EndScenePlayer	= "[C] d0:5b EndScenePlayer";
	private static Log			_log					= LogFactory.getLog(EndScenePlayer.class);

	private int					_movieId;

	@Override
	protected void readImpl()
	{
		_movieId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (_movieId == 0)
			return;
		if (activeChar.getMovieId() != _movieId)
		{
			_log.warn("Player " + getClient() + " sent EndScenePlayer with wrong movie id: " + _movieId);
			return;
		}
		activeChar.setMovieId(0);
		activeChar.setIsTeleporting(true); // avoid to get player removed from L2World
		activeChar.decayMe();
		activeChar.spawnMe(activeChar.getPosition().getX(), activeChar.getPosition().getY(), activeChar.getPosition().getZ());
		activeChar.setIsTeleporting(false);
	}

	@Override
	public String getType()
	{
		return _C__d05b_EndScenePlayer;
	}
}
