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
import net.l2emuproject.gameserver.network.serverpackets.ExRotation;
import net.l2emuproject.gameserver.network.serverpackets.SocialAction;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.system.util.Util;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author JIV
 */
public final class AnswerCoupleAction extends L2GameClientPacket
{
	private static final String	_S__3D_SOCIALACTION	= "[C] D0:7A AnswerCoupleAction";

	private int					_objectId;
	private int					_actionId;
	private int					_answer;

	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_answer = readD();
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2Player activeChar = getClient().getActiveChar();
		final L2Player target = L2World.getInstance().getPlayer(_objectId);
		if (activeChar == null || target == null)
			return;
		if (target.getMultiSocialTarget() != activeChar.getObjectId() || target.getMultiSociaAction() != _actionId)
			return;

		switch (_answer)
		{
			case 0:
				target.setMultiSocialAction(0, 0);
				target.sendPacket(SystemMessageId.COUPLE_ACTION_DENIED);
				break;
			case 1:
				final double distance = activeChar.getPlanDistanceSq(target);
				if (distance > 2000 || distance < 70)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
					target.sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
					return;
				}
				int heading = Util.calculateHeadingFrom(activeChar, target);
				activeChar.broadcastPacket(new ExRotation(activeChar.getObjectId(), heading));
				activeChar.setHeading(heading);
				heading = Util.calculateHeadingFrom(target, activeChar);
				target.setHeading(heading);
				target.broadcastPacket(new ExRotation(target.getObjectId(), heading));
				activeChar.broadcastPacket(new SocialAction(activeChar, _actionId));
				target.broadcastPacket(new SocialAction(activeChar, _actionId));
				break;
			case -1:
				SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_COUPLE_ACTIONS);
				sm.addPcName(activeChar);
				target.sendPacket(sm);
				break;
		}
	}

	@Override
	public String getType()
	{
		return _S__3D_SOCIALACTION;
	}
}
