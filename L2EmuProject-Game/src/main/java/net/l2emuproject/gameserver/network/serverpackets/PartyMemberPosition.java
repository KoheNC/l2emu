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

import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.model.party.L2Party;
import net.l2emuproject.gameserver.world.Location;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author zabbix
 */
public final class PartyMemberPosition extends L2GameServerPacket
{
	private final Map<Integer, Location>	locations	= new FastMap<Integer, Location>();

	public PartyMemberPosition(L2Party party)
	{
		reuse(party);
	}

	private final void reuse(L2Party party)
	{
		locations.clear();
		for (L2Player member : party.getPartyMembers())
		{
			if (member == null)
				continue;
			locations.put(member.getObjectId(), new Location(member));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xba);
		writeD(locations.size());
		for (Map.Entry<Integer, Location> entry : locations.entrySet())
		{
			Location loc = entry.getValue();
			writeD(entry.getKey());
			writeD(loc.getX());
			writeD(loc.getY());
			writeD(loc.getZ());
		}
	}

	@Override
	public final String getType()
	{
		return "[S] ba PartyMemberPosition";
	}
}
