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

import net.l2emuproject.gameserver.handler.IItemHandler;
import net.l2emuproject.gameserver.instancemanager.games.HandysBlockCheckerManager;
import net.l2emuproject.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import net.l2emuproject.gameserver.model.actor.L2Playable;
import net.l2emuproject.gameserver.model.actor.instance.L2BlockInstance;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.model.skill.L2Skill;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;

public final class EventItem implements IItemHandler
{
	private static final int[]	ITEMS	=
										{ 13787, 13788 };

	@Override
	public final void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		final L2PcInstance activeChar = (L2PcInstance) playable;

		final int itemId = item.getItemId();
		switch (itemId)
		{
			case 13787: // Handy's Block Checker Bond
				useBlockCheckerItem(activeChar, item);
				break;
			case 13788: // Handy's Block Checker Land Mine
				useBlockCheckerItem(activeChar, item);
				break;
			default:
				_log.warn("EventItemHandler: Item with id: " + itemId + " is not handled");
		}
	}

	private final void useBlockCheckerItem(final L2PcInstance castor, L2ItemInstance item)
	{
		final int blockCheckerArena = castor.getBlockCheckerArena();
		if (blockCheckerArena == -1)
		{
			SystemMessage msg = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			msg.addItemName(item);
			castor.sendPacket(msg);
			return;
		}

		final L2Skill sk = item.getEtcItem().getSkillInfos()[0].getSkill();
		if (sk == null)
			return;

		if (!castor.destroyItem("Consume", item, 1, castor, true))
			return;

		final L2BlockInstance block = castor.getTarget(L2BlockInstance.class);
		
		if (!(block instanceof L2BlockInstance))
			return;

		final ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(blockCheckerArena);
		if (holder != null)
		{
			final int team = holder.getPlayerTeam(castor);
			for (final L2PcInstance pc : block.getKnownList().getKnownPlayersInRadius(sk.getEffectRange()))
			{
				final int enemyTeam = holder.getPlayerTeam(pc);
				if (enemyTeam != -1 && enemyTeam != team)
					sk.getEffects(castor, pc);
			}
		}
		else
			_log.warn("Char: " + castor.getName() + "[" + castor.getObjectId() + "] has unknown block checker arena.");
	}

	@Override
	public final int[] getItemIds()
	{
		return ITEMS;
	}
}
