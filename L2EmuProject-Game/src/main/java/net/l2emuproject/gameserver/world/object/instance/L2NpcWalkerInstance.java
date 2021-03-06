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
package net.l2emuproject.gameserver.world.object.instance;

import net.l2emuproject.gameserver.entity.ai.L2CharacterAI;
import net.l2emuproject.gameserver.entity.ai.L2NpcWalkerAI;
import net.l2emuproject.gameserver.entity.status.CharStatus;
import net.l2emuproject.gameserver.entity.status.NpcWalkerStatus;
import net.l2emuproject.gameserver.network.SystemChatChannelId;
import net.l2emuproject.gameserver.network.serverpackets.CreatureSay;
import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Npc;

/**
 * This class manages some npcs can walk in the city. <br>
 * It inherits all methods from L2NpcInstance. <br><br>
 *
 * @original author Rayan RPG for L2Emu Project
 */
public class L2NpcWalkerInstance extends L2Npc
{
	/**
	 * Constructor of L2NpcWalkerInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 * @param objectId given object id
	 * @param template L2NpcTemplateForThisAi
	 */
	public L2NpcWalkerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getAI();
	}
	
	@Override
	protected L2CharacterAI initAI()
	{
		return new L2NpcWalkerAI(new L2NpcWalkerAIAccessor());
	}
	
	/**
	 * AI can't be deattached, npc must move always with the same AI instance.
	 * @param newAI AI to set for this L2NpcWalkerInstance
	 */
	@Override
	protected boolean canReplaceAI()
	{
		return false;
	}

	@Override
	public void onSpawn()
	{
		/*L2NpcWalkerAI ai = (L2NpcWalkerAI)getAI();
		
		ai.setHomeX(getX());
		ai.setHomeY(getY());
		ai.setHomeZ(getZ());*/
	}

	/**
	 * Sends a chat to all _knowObjects
	 * @param chat message to say
	 */
	public void broadcastChat(String chat)
	{
		if (!getKnownList().getKnownPlayers().isEmpty())
		{
			broadcastPacket(new CreatureSay(getObjectId(), SystemChatChannelId.Chat_Normal, getName(), chat));
		}
	}

	/**
	 * NPCs are immortal
	 * @param killer ignore it
	 * @return false
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		return false;
	}

	protected class L2NpcWalkerAIAccessor extends L2Character.AIAccessor
	{
		/**
		 * AI can't be deattached.
		 */
		/*@Override
		public void detachAI()
		{
		}*/
	}
	
	@Override
	protected CharStatus initStatus()
	{
		return new NpcWalkerStatus(this);
	}
	
	@Override
	public final NpcWalkerStatus getStatus()
	{
		return (NpcWalkerStatus)_status;
	}
}
