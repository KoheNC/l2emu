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
import net.l2emuproject.gameserver.model.actor.instance.L2DoorInstance;
import net.l2emuproject.gameserver.model.quest.Quest;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.L2Summon;

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

	protected abstract boolean canEnter(final L2Player player);

	protected abstract void enterInstance(final L2Player player);

	protected abstract void exitInstance(final L2Player player);

	protected final void setInstanceTime(final L2Player player, final int instanceId, final long time)
	{
		InstanceManager.getInstance().setInstanceTime(player.getObjectId(), instanceId, ((System.currentTimeMillis() + time)));
	}

	protected final void teleportPlayer(final L2Player player, final int[] coords, final int instanceId)
	{
		final L2Summon pet = player.getPet();

		teleport(player, coords, instanceId);
		if (pet != null)
			teleport(pet, coords, instanceId);
	}

	private final void teleport(final L2Character cha, final int[] coords, final int instanceId)
	{
		cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		cha.setInstanceId(instanceId);
		cha.teleToLocation(coords[0], coords[1], coords[2], true);
	}

	protected final void openDoor(final int doorId, final int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			if (door.getDoorId() == doorId)
				door.openMe();
	}

	protected final void closeDoor(final int doorId, final int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			if (door.getDoorId() == doorId)
				door.closeMe();
	}

	// TODO: Add more functions...
}
