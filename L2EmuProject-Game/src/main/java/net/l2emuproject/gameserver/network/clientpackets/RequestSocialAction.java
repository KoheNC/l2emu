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
import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SocialAction;
import net.l2emuproject.gameserver.util.FloodProtector.Protected;
import net.l2emuproject.gameserver.util.Util;

public class RequestSocialAction extends L2GameClientPacket
{
	private static final String	_C__1B_REQUESTSOCIALACTION	= "[C] 1B RequestSocialAction";

	// format  cd
	private int					_actionId;

	/**
	 * packet type id 0x1b
	 * format:		cd
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_actionId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (!getClient().getFloodProtector().tryPerformAction(Protected.SOCIAL))
			return;

		// check if its the actionId is allowed
		else if (_actionId < 2 || _actionId > 14)
		{
			Util.handleIllegalPlayerAction(activeChar, "Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName()
					+ " requested an internal Social Action.", Config.DEFAULT_PUNISH);
			sendAF();
			return;
		}

		// You cannot do anything else while fishing
		else if (activeChar.getPlayerFish().isFishing())
		{
			requestFailed(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}

		else if (activeChar.isSitting() || activeChar.getActiveRequester() != null || activeChar.isAlikeDead() || activeChar.isCastingNow()
				|| activeChar.isCastingSimultaneouslyNow() || activeChar.getAI().getIntention() != CtrlIntention.AI_INTENTION_IDLE
				|| (activeChar.isAllSkillsDisabled() && !activeChar.getPlayerDuel().isInDuel()))
		{
			sendAF();
			return;
		}

		activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), _actionId));

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__1B_REQUESTSOCIALACTION;
	}
}
