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
package net.l2emuproject.gameserver.handler.itemhandlers;

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.model.actor.instance.L2DoorInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ActionFailed;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Object;
import net.l2emuproject.gameserver.world.object.L2Playable;

/**
 * @author chris
 */
public class DoorKey implements IItemHandler
{
	// All the item IDs that this handler knows.
	// TODO skill for 9694
	private static final int[]	ITEM_IDS				= {};

	public static final int		INTERACTION_DISTANCE	= 100;

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{

		int itemId = item.getItemId();
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance) playable;

		// Key of Enigma (Pavel Research Quest)
		if (itemId == 8060)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(2260, 1);
			if (skill != null)
				activeChar.doSimultaneousCast(skill);
			return;
		}

		L2Object target = activeChar.getTarget();

		if (!(target instanceof L2DoorInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2DoorInstance door = (L2DoorInstance) target;

		if (!(activeChar.isInsideRadius(door, INTERACTION_DISTANCE, false, false)))
		{
			activeChar.sendMessage("Too far.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.getAbnormalEffect() > 0 || activeChar.isInCombat())
		{
			activeChar.sendMessage("You are currently engaged in combat.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
