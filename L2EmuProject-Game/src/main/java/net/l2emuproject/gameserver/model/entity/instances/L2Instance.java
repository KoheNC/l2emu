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
package net.l2emuproject.gameserver.model.entity.instances;

import net.l2emuproject.gameserver.ai.CtrlIntention;
import net.l2emuproject.gameserver.instancemanager.InstanceManager;
import net.l2emuproject.gameserver.model.actor.L2Character;
import net.l2emuproject.gameserver.model.actor.L2Summon;
import net.l2emuproject.gameserver.model.actor.instance.L2DoorInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.quest.Quest;

/**
 * @author lord_rex
 *<br> A Basic script for all instances and instance types.
 */
public abstract class L2Instance extends Quest
{
	public L2Instance(int questId, String name, String descr, String folder)
	{
		super(questId, name, descr, folder);
	}

	protected abstract boolean canEnter(L2PcInstance player);

	protected abstract void enterInstance(L2PcInstance player);

	protected abstract void exitInstance(L2PcInstance player);

	protected final void setInstanceTime(L2PcInstance player, int instanceId, long time)
	{
		InstanceManager.getInstance().setInstanceTime(player.getObjectId(), instanceId, ((System.currentTimeMillis() + time)));
	}

	protected final void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		L2Summon pet = player.getPet();

		teleport(player, coords, instanceId);
		if (pet != null)
			teleport(pet, coords, instanceId);
	}

	private final void teleport(L2Character cha, int[] coords, int instanceId)
	{
		cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		cha.setInstanceId(instanceId);
		cha.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	protected final void openDoor(int doorId, int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			if (door.getDoorId() == doorId)
				door.openMe();
	}

	protected final void closeDoor(int doorId, int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			if (door.getDoorId() == doorId)
				door.closeMe();
	}

	// TODO: Add more functions...
}
