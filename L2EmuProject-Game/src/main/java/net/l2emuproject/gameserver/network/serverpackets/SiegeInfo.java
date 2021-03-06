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

import net.l2emuproject.gameserver.datatables.ClanTable;
import net.l2emuproject.gameserver.events.global.clanhallsiege.ClanHall;
import net.l2emuproject.gameserver.events.global.siege.Castle;
import net.l2emuproject.gameserver.network.L2GameClient;
import net.l2emuproject.gameserver.services.clan.L2Clan;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Shows the Siege Info<BR>
 * <BR>
 * packet type id 0xc9<BR>
 * format: cdddSSdSdd<BR>
 * <BR>
 * c = c9<BR>
 * d = CastleID<BR>
 * d = Show Owner Controls (0x00 default || >=0x02(mask?) owner)<BR>
 * d = Owner ClanID<BR>
 * S = Owner ClanName<BR>
 * S = Owner Clan LeaderName<BR>
 * d = Owner AllyID<BR>
 * S = Owner AllyName<BR>
 * d = current time (seconds)<BR>
 * d = Siege time (seconds) (0 for selectable)<BR>
 * d = (UNKNOW) Siege Time Select Related?
 * @author KenM
 */
public class SiegeInfo extends L2GameServerPacket
{
	private static final String	_S__C9_SIEGEINFO	= "[S] c9 SiegeInfo";
	private static final String DEFAULT_OWNER = "NPC";
	private static final String DEFAULT_CLAN_ALLY = "";
	private final int _siegeableID;
	private final L2Clan _owner;
	private final int _siegeTime;

	public SiegeInfo(Castle castle)
	{
		_siegeableID = castle.getCastleId();
		_owner = ClanTable.getInstance().getClan(castle.getOwnerId());
		_siegeTime = (int) (castle.getSiege().getSiegeDate().getTimeInMillis() / 1000);
	}

	public SiegeInfo(ClanHall hideout)
	{
		_siegeableID = hideout.getId();
		_owner = hideout.getOwnerClan();
		if (hideout.getSiege() == null)
		{
			_siegeTime = 0;
			_log.fatal("Requested siege info for non-contestable hideout!");
		}
		else
			_siegeTime = (int) (hideout.getSiege().getSiegeDate().getTimeInMillis() / 1000);
	}

	@Override
	protected final void writeImpl(L2GameClient client, L2Player activeChar)
	{
		if (activeChar == null)
			return;
		writeC(0xc9);
		writeD(_siegeableID);
		if (_owner != null)
		{
			if (_owner.getClanId() == activeChar.getClanId() && activeChar.isClanLeader())
				writeD(0x01);
			else
				writeD(0x00);
			writeD(_owner.getClanId());
			writeS(_owner.getName()); // Clan Name
			writeS(_owner.getLeaderName()); // Clan Leader Name
			writeD(_owner.getAllyId()); // Ally ID
			writeS(_owner.getAllyName()); // Ally Name
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeS(DEFAULT_OWNER);
			writeS(DEFAULT_CLAN_ALLY);
			writeD(0x00);
			writeS(DEFAULT_CLAN_ALLY);
		}
		writeD((int) (System.currentTimeMillis() / 1000));
		writeD(_siegeTime);
		writeD(0x00);
	}

	/* (non-Javadoc)
	 * @see net.l2emuproject.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__C9_SIEGEINFO;
	}
}
