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

import net.l2emuproject.gameserver.templates.chars.L2NpcTemplate;
import net.l2emuproject.gameserver.world.knownlist.CharKnownList;
import net.l2emuproject.gameserver.world.knownlist.FriendlyMobKnownList;
import net.l2emuproject.gameserver.world.object.L2Attackable;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * This class represents Friendly Mobs lying over the world.
 * These friendly mobs should only attack players with karma > 0
 * and it is always aggro, since it just attacks players with karma
 * 
 * @version $Revision: 1.20.4.6 $ $Date: 2005/07/23 16:13:39 $
 */
public class L2FriendlyMobInstance extends L2Attackable
{
	public L2FriendlyMobInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList();	// init knownlist
	}
	
	@Override
	protected CharKnownList initKnownList()
	{
		return new FriendlyMobKnownList(this);
	}
	
	@Override
	public final FriendlyMobKnownList getKnownList()
	{
		return (FriendlyMobKnownList)_knownList;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2Player)
			return ((L2Player)attacker).getKarma() > 0;
		return false;
	}
	
	@Override
	public boolean isAggressive()
	{
		return true;
	}
}
