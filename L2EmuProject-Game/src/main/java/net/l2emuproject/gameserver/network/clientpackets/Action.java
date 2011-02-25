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
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * This class represents a packet that is sent by the client clicking an object
 * (also clicking on a "selected"/targeted object).
 * Client also sends this packet after successful /nexttarget.
 */
public final class Action extends L2GameClientPacket
{
	private static final String	_C__ACTION	= "[C] 1F Action c[ddddc]";
	
	private int					_objectId;
	// private int _originX;
	// private int _originY;
	// private int _originZ;
	private boolean				_shift;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD(); // Target object Identifier
		/* _originX = */readD();
		/* _originY = */readD();
		/* _originZ = */readD();
		_shift = (readC() == 1);
	}
	
	@Override
	protected void runImpl()
	{
		if (_log.isDebugEnabled())
		{
			_log.debug("Action shift: " + _shift);
			_log.debug("ObjectID: " + _objectId);
		}
		
		// Get the current L2Player of the player
		L2Player activeChar = getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.getPlayerObserver().inObserverMode())
		{
			requestFailed(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			return;
		}
		
		final L2Object obj;
		// Get object from target
		if (activeChar.getTargetId() == _objectId)
		{
			obj = activeChar.getTarget();
			// removes spawn protection
			activeChar.onActionRequest();
		}
		else
			obj = L2World.getInstance().findObject(_objectId);
		
		if (obj == null)
		{
			// pressing e.g. pickup many times quickly would get you here
			sendAF();
			return;
		}
		else if (obj instanceof L2Player)
		{
			L2Player target = (L2Player) obj;
			if (target.getAppearance().isInvisible() && !activeChar.isGM())
			{
				sendAF();
				return;
			}
		}
		
		if (!activeChar.isSameInstance(obj))
		{
			sendAF();
			return;
		}
		
		if (activeChar.getActiveRequester() == null)
		{
			if (!_shift)
				obj.onAction(activeChar);
			else if (!activeChar.isGM() && !(obj instanceof L2Npc && Config.ALT_GAME_VIEWNPC))
				obj.onAction(activeChar, false);
			else
				obj.onActionShift(activeChar);
		}
		
		sendAF();
	}
	
	@Override
	public String getType()
	{
		return _C__ACTION;
	}
}

