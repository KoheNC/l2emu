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
package net.l2emuproject.gameserver.datatables;

import java.util.ArrayList;
import java.util.List;

import javolution.util.FastMap;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.L2GameServerPacket;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class stores references to all online game masters. (access level > 100)
 */
public final class GmListTable
{
	private static final Log _log = LogFactory.getLog(GmListTable.class);
	private static final FastMap<L2PcInstance, Boolean> _allGms = new FastMap<L2PcInstance, Boolean>().shared();
	
	/** Shouldn't be instantiated */
	private GmListTable()
	{
	}
	
	static
	{
		_log.info("GmListTable: initialized.");
	}
	
	/** @return Map containing all GM characters with their hiding status */
	private static final FastMap<L2PcInstance, Boolean> getAllGms()
	{
		return _allGms;
	}
	
	/**
	 * Get a list of currently online GM characters.
	 * @param includeHidden whether to include hiding GMs
	 * @return List containing a subset of online GM characters
	 */
	public static List<L2PcInstance> getAllGms(boolean includeHidden)
	{
		List<L2PcInstance> list = new ArrayList<L2PcInstance>(getAllGms().size());
		for (FastMap.Entry<L2PcInstance, Boolean> n = getAllGms().head(), end = getAllGms().tail(); (n = n.getNext()) != end;)
		{
			if (includeHidden || !n.getValue())
				list.add(n.getKey());
		}
		return list;
	}
	
	/**
	 * Get a list of currently online GM character names.
	 * When including hiding GMs, an " (invis)" tag is attached to the name.
	 * @param includeHidden whether to include hiding GMs
	 * @return List containing a subset of online GM character names
	 */
	public static List<String> getAllGmNames(boolean includeHidden)
	{
		List<String> list = new ArrayList<String>(getAllGms().size());
		for (FastMap.Entry<L2PcInstance, Boolean> n = getAllGms().head(), end = getAllGms().tail(); (n = n.getNext()) != end;)
		{
			if (!n.getValue())
				list.add(n.getKey().getName());
			else if (includeHidden)
				list.add(n.getKey().getName() + " (invis)");
		}
		return list;
	}
	
	public static void addGm(L2PcInstance player, boolean hidden)
	{
		if (_log.isDebugEnabled())
			_log.debug("added gm: " + player.getName());
		
		getAllGms().put(player, hidden);
	}
	
	public static void deleteGm(L2PcInstance player)
	{
		if (_log.isDebugEnabled())
			_log.debug("deleted gm: " + player.getName());
		
		getAllGms().remove(player);
	}
	
	/**
	 * If the player is registered as a GM,
	 * update it's hiding status to false.
	 * @param player Any player
	 */
	public static void showGm(L2PcInstance player)
	{
		FastMap.Entry<L2PcInstance, Boolean> hide = getAllGms().getEntry(player);
		if (hide != null)
			hide.setValue(false);
	}
	
	/**
	 * If the player is registered as a GM,
	 * update it's hiding status to true.
	 * @param player Any player
	 */
	public static void hideGm(L2PcInstance player)
	{
		FastMap.Entry<L2PcInstance, Boolean> hide = getAllGms().getEntry(player);
		if (hide != null)
			hide.setValue(true);
	}
	
	/**
	 * @param includeHidden whether to include hiding GMs
	 * @return whether there is at least one online GM
	 */
	public static boolean isAnyGmOnline(boolean includeHidden)
	{
		if (getAllGms().size() > 0)
		{
			if (includeHidden)
				return true;
			
			for (Boolean hiding : getAllGms().values())
				if (!hiding)
					return true;
			return false;
		}
		else
			return false;
	}
	
	/**
	 * Shows a filtered GM list to the given player.
	 * If the receiver is a GM, all online GMs will be shown.
	 * @param player a player
	 */
	public static void sendListToPlayer(L2PcInstance player)
	{
		if (isAnyGmOnline(player.isGM()))
		{
			player.sendPacket(SystemMessageId.GM_LIST);
			for (String name : getAllGmNames(player.isGM()))
				player.sendPacket(new SystemMessage(SystemMessageId.GM_C1).addString(name));
			player.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
		}
		else
			player.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
	}
	
	/**
	 * Sends a packet to all online GMs
	 * @param packet the packet
	 */
	public static void broadcastToGMs(L2GameServerPacket packet)
	{
		for (L2PcInstance gm : getAllGms(true))
			gm.sendPacket(packet);
	}
	
	/**
	 * Sends a system message to all online GMs
	 * @param message the message
	 */
	public static void broadcastMessageToGMs(String message)
	{
		broadcastToGMs(SystemMessage.sendString(message));
	}
}
