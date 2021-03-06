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
package net.l2emuproject.gameserver.skills.conditions;

import java.util.Arrays;
import java.util.List;

import net.l2emuproject.gameserver.manager.instances.Instance;
import net.l2emuproject.gameserver.manager.instances.InstanceManager;
import net.l2emuproject.gameserver.manager.instances.InstanceManager.InstanceWorld;
import net.l2emuproject.gameserver.skills.Env;
import net.l2emuproject.gameserver.world.object.L2Player;

import org.apache.commons.lang.ArrayUtils;


public class ConditionPlayerInstanceId extends Condition
{
	private final int[] _instanceIds;

	public ConditionPlayerInstanceId(List<Integer> instanceIds)
	{
		_instanceIds = ArrayUtils.toPrimitive(instanceIds.toArray(new Integer[instanceIds.size()]), 0);

		Arrays.sort(_instanceIds);
	}

	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.getPlayer() instanceof L2Player))
			return false;
		L2Player player = env.getPlayer().getActingPlayer();
		if (!player.isInInstance())
			return false;

		int templateId = -1;
		Instance dyn = InstanceManager.getInstance().getDynamicInstance(player);
		if (dyn != null)
			templateId = dyn.getTemplate();
		if (templateId == -1)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if (world != null && player.isSameInstance(world.instanceId))
				templateId = world.templateId;
		}		
		if (templateId == -1)
			return false;

		return Arrays.binarySearch(_instanceIds, templateId) >= 0;
	}
}
