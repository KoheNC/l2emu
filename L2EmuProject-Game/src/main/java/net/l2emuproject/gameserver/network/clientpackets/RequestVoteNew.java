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

import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExBrExtraUserInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExVoteSystemInfo;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.network.serverpackets.UserInfo;
import net.l2emuproject.gameserver.services.recommendation.RecommendationService;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class RequestVoteNew extends L2GameClientPacket
{
	private int	_targetId;

	@Override
	protected final void readImpl()
	{
		_targetId = readD();
	}

	@Override
	protected final void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Object object = activeChar.getTarget();

		if (!(object instanceof L2Player))
		{
			if (object == null)
				requestFailed(SystemMessageId.YOU_MUST_SELECT_A_TARGET);
			else
				requestFailed(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		L2Player target = (L2Player) object;

		if (target.getObjectId() != _targetId)
			return;

		RecommendationService.getInstance().recommend(activeChar, target);

		SystemMessage sm = null;
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT);
		sm.addPcName(target);
		sm.addNumber(activeChar.getEvaluations());
		activeChar.sendPacket(sm);

		sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_C1);
		sm.addPcName(activeChar);
		target.sendPacket(sm);
		sm = null;

		activeChar.sendPacket(new UserInfo(activeChar)); // FIXME: Really needed doubled send user info?
		sendPacket(new ExBrExtraUserInfo(activeChar));
		target.broadcastUserInfo(); // FIXME: Really needed doubled send user info?

		activeChar.sendPacket(new ExVoteSystemInfo(activeChar));
		target.sendPacket(new ExVoteSystemInfo(target));
	}

	@Override
	public final String getType()
	{
		return "[C] D0:7E RequestVoteNew";
	}
}
