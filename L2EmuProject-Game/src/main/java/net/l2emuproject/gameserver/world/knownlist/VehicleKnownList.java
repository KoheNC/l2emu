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

import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;

public final class VehicleKnownList extends CharKnownList
{
	public VehicleKnownList(L2Character activeChar)
	{
		super(activeChar);
	}

	@Override
	public final int getDistanceToForgetObject(L2Object object)
	{
		if (!(object instanceof L2Player))
			return 0;

		return 8000;
	}

	@Override
	public final int getDistanceToWatchObject(L2Object object)
	{
		if (!(object instanceof L2Player))
			return 0;

		return 4000;
	}
}
