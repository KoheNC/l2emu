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
package net.l2emuproject.gameserver.handler.skillhandlers;

import net.l2emuproject.gameserver.entity.ai.CtrlIntention;
import net.l2emuproject.gameserver.handler.ISkillHandler;
import net.l2emuproject.gameserver.items.L2ItemInstance;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.PlaySound;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.services.manor.L2Manor;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.templates.skills.L2SkillType;
import net.l2emuproject.gameserver.world.object.L2Character;
import net.l2emuproject.gameserver.world.object.L2Player;
import net.l2emuproject.gameserver.world.object.instance.L2MonsterInstance;
import net.l2emuproject.tools.random.Rnd;


/**
 * @author l3x
 */
public class Sow implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.SOW };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (!(activeChar instanceof L2Player))
			return;

		if (targets == null || targets.length == 0)
			return;

		L2Player activePlayer = (L2Player) activeChar;
		if (_log.isDebugEnabled())
			_log.info("Casting sow");

		for (L2Character element : targets)
		{
			if (!(element instanceof L2MonsterInstance))
				continue;

			L2MonsterInstance target = (L2MonsterInstance) element;

			if (target.isSeeded())
				continue;

			if (target.isDead())
				continue;

			if (target.getSeeder() != activeChar)
				continue;

			int seedId = target.getSeedType();
			if (seedId == 0)
				continue;

			L2ItemInstance item = activePlayer.getInventory().getItemByItemId(seedId);
			if (item == null)
				return;

			// Consuming used seed
			activePlayer.destroyItem("Consume", item.getObjectId(), 1, null, false);

			SystemMessage sm;
			if (calcSuccess(activePlayer, target, seedId))
			{
				activePlayer.sendPacket(new PlaySound(0, "Itemsound.quest_itemget"));
				target.setSeeded();
				sm = SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN.getSystemMessage();
			}
			else
			{
				sm = SystemMessageId.THE_SEED_WAS_NOT_SOWN.getSystemMessage();
			}
			if (activePlayer.getParty() == null)
			{
				activePlayer.sendPacket(sm);
			}
			else
			{
				activePlayer.getParty().broadcastToPartyMembers(sm);
			}
			// FIXME: Mob should not become aggro against player, this way
			// doesn't work really nice
			target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}

	private boolean calcSuccess(L2Player activeChar, L2MonsterInstance target, int seedId)
	{
		int basicSuccess = (L2Manor.getInstance().isAlternative(seedId) ? 20 : 90);
		int minlevelSeed = 0;
		int maxlevelSeed = 0;
		minlevelSeed = L2Manor.getInstance().getSeedMinLevel(seedId);
		maxlevelSeed = L2Manor.getInstance().getSeedMaxLevel(seedId);

		int levelPlayer = activeChar.getLevel(); // Attacker Level
		int levelTarget = target.getLevel(); // target Level

		// seed level
		if (levelTarget < minlevelSeed)
			basicSuccess -= 5 * (minlevelSeed - levelTarget);
		if (levelTarget > maxlevelSeed)
			basicSuccess -= 5 * (levelTarget - maxlevelSeed);

		// 5% decrease in chance if player level
		// is more than +/- 5 levels to _target's_ level
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
			diff = -diff;
		if (diff > 5)
			basicSuccess -= 5 * (diff - 5);

		// Chance can't be less than 1%
		if (basicSuccess < 1)
			basicSuccess = 1;

		int rate = Rnd.nextInt(100);

		return (rate < basicSuccess);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
