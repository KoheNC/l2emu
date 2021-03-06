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
package net.l2emuproject.gameserver.system.util;

import net.l2emuproject.gameserver.network.serverpackets.L2GameServerPacket;
import net.l2emuproject.gameserver.world.L2World;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * @author luisantonioa
 */
public final class Broadcast
{
	private Broadcast()
	{
	}
	
	public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		if (!character.getKnownList().getKnownPlayers().isEmpty())
			for (L2Player player : character.getKnownList().getKnownPlayers().values())
				if (player != null)
					player.sendPacket(mov);
	}
	
	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			toKnownPlayers(character, mov);
			return;
		}
		else if (radius > 10000)
		{
			toKnownPlayersInRadius(character, mov, (long)radius);
			return;
		}
		
		if (!character.getKnownList().getKnownPlayers().isEmpty())
			for (L2Player player : character.getKnownList().getKnownPlayers().values())
				if (character.isInsideRadius(player, radius, false, false))
					if (player != null)
						player.sendPacket(mov);
	}
	
	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, long radiusSq)
	{
		if (radiusSq < 0)
		{
			toKnownPlayers(character, mov);
			return;
		}
		else if (radiusSq < 10000)
		{
			toKnownPlayersInRadius(character, mov, (int)radiusSq);
			return;
		}
		
		if (!character.getKnownList().getKnownPlayers().isEmpty())
			for (L2Player player : character.getKnownList().getKnownPlayers().values())
				if (character.getDistanceSq(player) <= radiusSq)
					if (player != null)
						player.sendPacket(mov);
	}
	
	public static void toSelfAndKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		if (character instanceof L2Player)
			((L2Player)character).sendPacket(mov);
		
		toKnownPlayers(character, mov);
	}
	
	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (character instanceof L2Player)
			((L2Player)character).sendPacket(mov);
		
		toKnownPlayersInRadius(character, mov, radius);
	}
	
	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, long radiusSq)
	{
		if (character instanceof L2Player)
			((L2Player)character).sendPacket(mov);
		
		toKnownPlayersInRadius(character, mov, radiusSq);
	}
	
	public static void toAllOnlinePlayers(L2GameServerPacket mov)
	{
		for (L2Player player : L2World.getInstance().getAllPlayers())
			if (player != null)
				player.sendPacket(mov);
	}
}
