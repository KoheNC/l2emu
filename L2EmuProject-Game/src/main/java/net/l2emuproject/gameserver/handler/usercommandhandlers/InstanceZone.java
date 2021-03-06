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
package net.l2emuproject.gameserver.handler.usercommandhandlers;

import java.util.Map;

import net.l2emuproject.gameserver.handler.IUserCommandHandler;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.world.object.L2Player;


/**
 * @author nille02
 */
public class InstanceZone implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		114
	};
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
	
	@Override
	public boolean useUserCommand(int id, L2Player activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;
		
		Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(activeChar.getObjectId());
		boolean firstMessage = true;
		if (instanceTimes != null)
			for (int instanceId : instanceTimes.keySet())
			{
				long remainingTime = (instanceTimes.get(instanceId) - System.currentTimeMillis()) / 1000;
				if (remainingTime > 60)
				{
					if (firstMessage)
					{
						firstMessage = false;
						activeChar.sendPacket(SystemMessageId.INSTANCE_ZONE_TIME_LIMIT);
					}
					int hours = (int) (remainingTime / 3600);
					int minutes = (int) ((remainingTime % 3600) / 60);
					SystemMessage sm = new SystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
					sm.addString(InstanceManager.getInstance().getInstanceIdName(instanceId));
					sm.addNumber(hours);
					sm.addNumber(minutes);
					activeChar.sendPacket(sm);
				}
				else
					InstanceManager.getInstance().deleteInstanceTime(activeChar.getObjectId(), instanceId);
			}
		if (firstMessage)
			activeChar.sendPacket(SystemMessageId.NO_INSTANCEZONE_TIME_LIMIT);
		return true;
	}
}
