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
package net.l2emuproject.gameserver.world.knownlist;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.network.serverpackets.DeleteObject;
import net.l2emuproject.gameserver.network.serverpackets.SpawnItem;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class PcKnownList extends PlayableKnownList
{
	public PcKnownList(L2Player activeChar)
	{
		super(activeChar);
	}
	
	/**
	 * Add a visible L2Object to L2Player _knownObjects and _knownPlayer (if necessary) and send Server-Client Packets needed to inform the L2Player of its state and actions in progress.<BR><BR>
	 *
	 * <B><U> object is a L2ItemInstance </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet DropItem/SpawnItem to the L2Player </li><BR><BR>
	 *
	 * <B><U> object is a L2DoorInstance </U> :</B><BR><BR>
	 * <li> Send Server-Client Packets DoorInfo and DoorStatusUpdate to the L2Player </li>
	 * <li> Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2Player </li><BR><BR>
	 *
	 * <B><U> object is a L2Npc </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet NpcInfo to the L2Player </li>
	 * <li> Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2Player </li><BR><BR>
	 *
	 * <B><U> object is a L2Summon </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet NpcInfo/PetItemList (if the L2Player is the owner) to the L2Player </li>
	 * <li> Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2Player </li><BR><BR>
	 *
	 * <B><U> object is a L2Player </U> :</B><BR><BR>
	 * <li> Send Server-Client Packet CharInfo to the L2Player </li>
	 * <li> If the object has a private store, Send Server-Client Packet PrivateStoreMsgSell to the L2Player </li>
	 * <li> Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2Player </li><BR><BR>
	 *
	 * @param object The L2Object to add to _knownObjects and _knownPlayer
	 */
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
			return false;
		
		if (object instanceof L2Player && ((L2Player)object).getPlayerObserver().inObserverMode())
			return false;
		
		sendInfoOf(object);
		
		return true;
	}
	
	public void refreshInfos()
	{
		for (L2Object object : getKnownObjects().values())
		{
			if (object instanceof L2Player && ((L2Player)object).getPlayerObserver().inObserverMode())
				continue;
			
			sendInfoOf(object);
		}
	}
	
	private void sendInfoOf(L2Object object)
	{
		if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
		{
			//if (object.getPolytype().equals("item"))
			getActiveChar().sendPacket(new SpawnItem(object));
			//else if (object.getPolytype().equals("npc"))
			//	sendPacket(new NpcInfoPoly(object, this));
		}
		else
		{
			object.sendInfo(getActiveChar());
			
			if (object instanceof L2Character)
			{
				// Update the state of the L2Character object client side by sending Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2Player
				L2Character obj = (L2Character)object;
				if (obj.getAI() != null)
					obj.getAI().describeStateToPlayer(getActiveChar());
			}
		}
	}
	
	/**
	 * Remove a L2Object from L2Player _knownObjects and _knownPlayer (if necessary) and send Server-Client Packet DeleteObject to the L2Player.<BR><BR>
	 *
	 * @param object The L2Object to remove from _knownObjects and _knownPlayer
	 *
	 */
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
			return false;
		
		// Send Server-Client Packet DeleteObject to the L2Player
		getActiveChar().sendPacket(new DeleteObject(object));
		
		if (Config.TEST_KNOWNLIST && getActiveChar().isGM() && object instanceof L2Npc)
			getActiveChar().sendMessage("Knownlist,removed NPC: " + object.getName());
		return true;
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	@Override
	public final L2Player getActiveChar()
	{
		return (L2Player)_activeChar;
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		// when knownlist grows, the distance to forget should be at least
		// the same as the previous watch range, or it becomes possible that
		// extra charinfo packets are being sent (watch-forget-watch-forget)
		int knownlistSize = getKnownObjects().size();
		if (knownlistSize <= 25)
			return 4000;
		
		if (knownlistSize <= 35)
			return 3500;
		
		if (knownlistSize <= 70)
			return 2910;
		
		return 2310;
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		int knownlistSize = getKnownObjects().size();
		
		if (knownlistSize <= 25)
			return 3400; // empty field
			
		if (knownlistSize <= 35)
			return 2900;
		if (knownlistSize <= 70)
			return 2300;
		
		return 1700; // Siege, TOI, city
	}
	
	@Override
	public final boolean tryRemoveObject(L2Object obj)
	{		
		return super.tryRemoveObject(obj);
	}
}
