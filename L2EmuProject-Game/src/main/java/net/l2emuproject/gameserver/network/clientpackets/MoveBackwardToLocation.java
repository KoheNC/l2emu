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
import net.l2emuproject.gameserver.network.serverpackets.StopMove;
import net.l2emuproject.gameserver.util.IllegalPlayerAction;
import net.l2emuproject.gameserver.util.Util;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.position.L2CharPosition;

public final class MoveBackwardToLocation extends L2GameClientPacket
{
	private static final String _C__01_MOVEBACKWARDTOLOC = "[C] 01 MoveBackwardToLoc";
	
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	
	private int _originX;
	private int _originY;
	private int _originZ;
	
	private int _moveMovement;
	
	@Override
	protected void readImpl()
	{
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		
		// L2Walker is being used
		if (getByteBuffer().remaining() < 4)
			_moveMovement = -1;
		else
			_moveMovement = readD(); // is 0 if cursor keys are used  1 if mouse is used
	}
	
	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new StopMove(activeChar));
			return;
		}
		
		// removes spawn protection
		activeChar.onActionRequest();
		
		if (_moveMovement == -1)
		{
			if (Config.BAN_CLIENT_EMULATORS)
			{
				Util.handleIllegalPlayerAction(activeChar, "Bot usage for movement! " + activeChar,
						IllegalPlayerAction.PUNISH_KICKBAN);
				sendAF();
				return;
			}
			else
				_moveMovement = 1;
		}
		
		// Correcting targetZ from floor level to head level (?)
		// Client is giving floor level as targetZ but that floor level doesn't
		// match our current geodata and teleport coords as good as head level!
		// L2J uses floor, not head level as char coordinates. This is some
		// sort of incompatibility fix.
		// Validate position packets sends head level.
		_targetZ += activeChar.getTemplate().getCollisionHeight();
		
		int curX = activeChar.getX();
		int curY = activeChar.getY();
		//int curZ = activeChar.getZ();
		
		//if (activeChar.isInBoat())
		//	activeChar.setInBoat(false);
		
		if (activeChar.getTeleMode() > 0)
		{
			if (activeChar.getTeleMode() == 1)
				activeChar.setTeleMode(0);
			sendAF();
			activeChar.teleToLocation(_targetX, _targetY, _targetZ, false);
			return;
		}
		
		if (activeChar.isAlikeDead())
		{
			sendAF();
			return;
		}
		
		// L2EMU_ADD
		if (_moveMovement == 0 && !Config.ALLOW_KEYBOARD_MOVEMENT)
		{
			sendAF();
		}
		// L2EMU_ADD
		else
		{
			double dx = _targetX - curX;
			double dy = _targetY - curY;
			
			// Can't move if character is confused, or trying to move a huge distance
			if (activeChar.isOutOfControl() || ((dx * dx + dy * dy) > 98010000)) // 9900*9900
			{
				sendAF();
				return;
			}
			
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
				new L2CharPosition(_targetX, _targetY, _targetZ, 0));
			sendAF();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__01_MOVEBACKWARDTOLOC;
	}
}
