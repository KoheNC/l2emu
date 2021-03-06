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
package net.l2emuproject.gameserver.system.taskmanager;

import net.l2emuproject.gameserver.network.serverpackets.PartyMemberPosition;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Used to revalidate/update/broadcast/execute tasks depending on current coordinates.<br>
 * <br>
 * The tasks gets triggered by the change of coordinates, but do not require instant execution.
 * 
 * @author NB4L1
 */
public final class CoordRevalidator extends AbstractFIFOPeriodicTaskManager<L2Object>
{
	private static final class SingletonHolder
	{
		private static final CoordRevalidator INSTANCE = new CoordRevalidator();
	}
	
	public static CoordRevalidator getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	protected CoordRevalidator()
	{
		super(500);
	}
	
	@Override
	protected void callTask(L2Object obj)
	{
		if (obj instanceof L2Character && obj.isVisible())
		{
			final L2Character cha = (L2Character)obj;
			
			cha.getKnownList().updateKnownObjects();
			
			cha.revalidateZone(true);
			
			if (cha instanceof L2Player)
			{
				final L2Player player = (L2Player)cha;
				
				if (player.getParty() != null)
					player.getParty().broadcastToPartyMembers(player, new PartyMemberPosition(player.getParty()));
			}
		}
	}
	
	@Override
	protected String getCalledMethodName()
	{
		return "revalidateCoords()";
	}
}
