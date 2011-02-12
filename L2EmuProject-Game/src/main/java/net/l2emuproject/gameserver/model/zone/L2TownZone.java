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
package net.l2emuproject.gameserver.model.zone;

import java.util.Map;

import javolution.util.FastMap;
import net.l2emuproject.Config;
import net.l2emuproject.gameserver.instancemanager.ClanHallManager;
import net.l2emuproject.gameserver.instancemanager.TownManager;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.entity.ClanHall;
import net.l2emuproject.gameserver.network.serverpackets.AgitDecoInfo;


public class L2TownZone extends L2Zone
{
	@Override
	protected void register()
	{
		TownManager.getInstance().registerTown(this);
	}
	
	private final Map<Integer, Byte> _map = new FastMap<Integer, Byte>().shared();
	
	@Override
	protected void onEnter(L2Character character)
	{
		byte flag = FLAG_PEACE;
		
		switch (Config.ZONE_TOWN)
		{
			case 1: // PvP allowed for siege participants
			{
				if (character instanceof L2PcInstance && ((L2PcInstance)character).getSiegeState() != 0)
					flag = FLAG_PVP;
				break;
			}
			case 2: // PvP in towns all the time
			{
				flag = FLAG_PVP;
				break;
			}
		}
		
		// TODO: PvP zone with debuffs etc. allowed or just general zone?
		
		_map.put(character.getObjectId(), flag);
		
		character.setInsideZone(flag, true);
		
		character.setInsideZone(FLAG_TOWN, true);
		
		super.onEnter(character);
		
		// Players must always see deco, not only inside clan hall.
		// retail server behavior
		if (character instanceof L2PcInstance)
		{
			ClanHall[] townHalls = ClanHallManager.getInstance().getTownClanHalls(getTownId());
			if (townHalls != null)
				for (ClanHall ch : townHalls)
					if (ch.getOwnerId() > 0)
						character.getActingPlayer().sendPacket(new AgitDecoInfo(ch));
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		Byte flag = _map.remove(character.getObjectId());
		if (flag != null) // just incase something would happen
			character.setInsideZone(flag.byteValue(), false);
		
		character.setInsideZone(FLAG_TOWN, false);
		
		super.onExit(character);
	}
}
