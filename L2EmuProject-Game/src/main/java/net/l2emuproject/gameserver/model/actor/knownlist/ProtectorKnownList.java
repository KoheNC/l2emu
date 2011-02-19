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
package net.l2emuproject.gameserver.model.actor.knownlist;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.model.actor.L2Npc;
import net.l2emuproject.gameserver.model.world.L2Object;

/**
 * @author Ederik
 */
public class ProtectorKnownList extends NpcKnownList
{
	public ProtectorKnownList(L2Npc activeChar)
	{
		super(activeChar);
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return Config.PROTECTOR_RADIUS_ACTION;
	}
}
