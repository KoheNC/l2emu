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

import net.l2emuproject.gameserver.instancemanager.PartyRoomManager;
import net.l2emuproject.gameserver.model.party.L2Party;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ListPartyWaiting;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Sent when a player opens the party matching window.
 * Format:(ch) ddd
 * @author Crion/kombat
 */
public class RequestPartyMatchConfig extends L2GameClientPacket
{
	private static final String _C__7F_REQUESTPARTYMATCHCONFIG = "[C] 7F RequestPartyMatchConfig";

	private int _page;
	private int _region;
	private boolean _allLevels;

	@Override
	protected void readImpl()
	{
		_page = readD();
		_region = readD(); // 0 to 15, or -1
		_allLevels = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		L2Player player = getActiveChar();
		if (player == null)
			return;

		L2Party party = player.getParty();
		if (party != null && !party.isLeader(player))
		{
			requestFailed(SystemMessageId.CANT_VIEW_PARTY_ROOMS);
			return;
		}

		player.setPartyMatchingLevelRestriction(_allLevels);
		player.setPartyMatchingRegion(_region);

		PartyRoomManager.getInstance().addToWaitingList(player);
		sendPacket(new ListPartyWaiting(player, _page));

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__7F_REQUESTPARTYMATCHCONFIG;
	}
}
