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
package net.l2emuproject.gameserver.network.serverpackets;

import net.l2emuproject.gameserver.datatables.CharNameTable;
import net.l2emuproject.gameserver.world.L2World;

/**
 * Support for "Chat with Friends" dialog. Add new friend or delete. <BR>
 * Format: cddSdd <BR>
 * d: action <BR>
 * d: Player Object ID <BR>
 * S: Friend Name <BR>
 * d: Online/Offline <BR>
 * d: Unknown (0 if offline)<BR>
 * 
 * @author JIV
 */
public class FriendPacket extends L2GameServerPacket
{
	private static final String	_S__FA_FRIENDLIST	= "[S] 76 FriendPacket";
	
	public static enum FriendAction
	{
		ADD_FRIEND, REMOVE_FRIEND
	}
	
	private final FriendAction	_action;
	private final boolean		_online;
	private final int			_objid;
	private final String		_name;
	
	public FriendPacket(FriendAction action, int objId)
	{
		_action = action;
		_objid = objId;
		_name = CharNameTable.getInstance().getNameByObjectId(objId);
		_online = L2World.getInstance().getPlayer(objId) != null;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x76);
		writeD(_action == FriendAction.ADD_FRIEND ? 1 : 3); // 1-add 3-remove
		writeD(_objid);
		writeS(_name);
		writeD(_online ? 1 : 0);
		writeD(_online ? _objid : 0);
	}
	
	@Override
	public String getType()
	{
		return _S__FA_FRIENDLIST;
	}
}
