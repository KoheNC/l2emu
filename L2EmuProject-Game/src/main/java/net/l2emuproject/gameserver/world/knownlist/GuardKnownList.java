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
import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.entity.ai.L2CharacterAI;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2GuardInstance;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class GuardKnownList extends AttackableKnownList
{
	private final static Log _log = LogFactory.getLog(GuardKnownList.class);

	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	public GuardKnownList(L2GuardInstance activeChar)
	{
		super(activeChar);
	}

	// =========================================================
	// Method - Public
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
			return false;

		if (object instanceof L2Player)
		{
			// Check if the object added is a L2Player that owns Karma
			L2Player player = (L2Player) object;
			
			if ( (player.getKarma() > 0) )
			{
				if (_log.isDebugEnabled()) _log.debug(getActiveChar().getObjectId()+": PK "+player.getObjectId()+" entered scan range");
				
				// Set the L2GuardInstance Intention to AI_INTENTION_ACTIVE
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		else if (Config.ALLOW_GUARDS && getActiveChar().isInActiveRegion() && object instanceof L2MonsterInstance)
		{
			// Check if the object added is an aggressive L2MonsterInstance
			L2MonsterInstance mob = (L2MonsterInstance) object;
			
			if (mob.isAggressive() )
			{
				if (_log.isDebugEnabled()) _log.debug(getActiveChar().getObjectId()+": Aggressive mob "+mob.getObjectId()+" entered scan range");
				
				// Set the L2GuardInstance Intention to AI_INTENTION_ACTIVE
				if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}

		return true;
	}

	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
			return false;

		// Check if the _aggroList of the L2GuardInstance is Empty
		if (getActiveChar().noTarget())
		{
			//removeAllKnownObjects();
			
			// Set the L2GuardInstance to AI_INTENTION_IDLE
			L2CharacterAI ai = getActiveChar().getAI();
			if (ai != null) ai.setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
		}

		return true;
	}
	
	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	@Override
	public final L2GuardInstance getActiveChar()
	{
		return (L2GuardInstance) _activeChar;
	}
}
