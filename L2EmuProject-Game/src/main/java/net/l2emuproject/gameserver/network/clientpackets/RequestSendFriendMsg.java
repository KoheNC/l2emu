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

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.L2FriendSay;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Receive Private (Friend) Message - 0xCC
 * 
 * Format: c SS
 * 
 * S: Message
 * S: Receiving Player
 * 
 * @author Tempy
 * 
 */
public class RequestSendFriendMsg extends L2GameClientPacket
{
	private static final String	_C__CC_REQUESTSENDMSG	= "[C] CC RequestSendMsg";
	private static Log			_logChat				= LogFactory.getLog("chat");

	private String				_message;
	private String				_receiver;

	@Override
	protected void readImpl()
	{
		_message = readS();
		_receiver = readS();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Player targetPlayer = L2World.getInstance().getPlayer(_receiver);
		if (targetPlayer == null)
		{
			requestFailed(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}

		if (Config.LOG_CHAT)
			_logChat.info("PRIV_MSG" + "[" + activeChar.getName() + " to " + _receiver + "]" + _message);

		targetPlayer.sendPacket(new L2FriendSay(activeChar.getName(), _receiver, _message));

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__CC_REQUESTSENDMSG;
	}
}
