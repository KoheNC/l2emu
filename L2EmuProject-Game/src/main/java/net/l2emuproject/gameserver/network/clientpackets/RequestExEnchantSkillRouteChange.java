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
package net.l2emuproject.gameserver.network.clientpackets;

import net.l2emuproject.Config;
import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.model.actor.instance.L2PcInstance;
import net.l2emuproject.gameserver.model.item.L2ItemInstance;
import net.l2emuproject.gameserver.model.skill.L2EnchantSkillLearn;
import net.l2emuproject.gameserver.model.skill.L2Skill;
import net.l2emuproject.gameserver.model.skill.L2EnchantSkillLearn.EnchantSkillDetail;
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExBrExtraUserInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExEnchantSkillInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import net.l2emuproject.gameserver.network.serverpackets.ExEnchantSkillResult;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.network.serverpackets.UserInfo;
import net.l2emuproject.tools.random.Rnd;

/**
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x34 d: skill id d: skill lvl
 * 
 * @author -Wooden-
 */
public final class RequestExEnchantSkillRouteChange extends L2GameClientPacket
{
	private int	_skillId;
	private int	_skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (player.getClassId().level() < 3) // requires to have 3rd class quest completed
		{
			requestFailed(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_IN_THIS_CLASS);
			return;
		}

		if (player.getLevel() < 76)
		{
			requestFailed(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ON_THIS_LEVEL);
			return;
		}

		if (!player.isAllowedToEnchantSkills())
		{
			requestFailed(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ATTACKING_TRANSFORMED_BOAT);
			return;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		if (skill == null)
		{
			return;
		}

		int reqItemId = SkillTreeTable.CHANGE_ENCHANT_BOOK;

		L2EnchantSkillLearn s = SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			return;
		}

		int currentLevel = player.getSkillLevel(_skillId);
		// do u have this skill enchanted?
		if (currentLevel <= 100)
		{
			return;
		}

		int currentEnchantLevel = currentLevel % 100;
		// is the requested level valid?
		if (currentEnchantLevel != _skillLvl % 100)
		{
			return;
		}
		EnchantSkillDetail esd = s.getEnchantSkillDetail(_skillLvl);

		int requiredSp = esd.getSpCost();
		int requireditems = esd.getAdenaCost();

		if (player.getSp() >= requiredSp)
		{
			// only first lvl requires book
			L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
			if (Config.ALT_ES_SP_BOOK_NEEDED)
			{
				if (spb == null)// Haven't spellbook
				{
					requestFailed(SystemMessageId.YOU_DONT_HAVE_ALL_ITENS_NEEDED_TO_CHANGE_SKILL_ENCHANT_ROUTE);
					return;
				}
			}

			if (player.getInventory().getAdena() < requireditems)
			{
				requestFailed(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}

			boolean check;
			check = player.getStat().removeExpAndSp(0, requiredSp, false);
			if (Config.ALT_ES_SP_BOOK_NEEDED)
			{
				check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
			}

			check &= player.destroyItemByItemId("Consume", 57, requireditems, player, true);

			if (!check)
			{
				requestFailed(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}

			int levelPenalty = Rnd.get(Math.min(4, currentEnchantLevel));
			_skillLvl -= levelPenalty;
			if (_skillLvl % 100 == 0)
			{
				_skillLvl = s.getBaseLevel();
			}

			skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);

			if (skill != null)
			{
				player.addSkill(skill, true);
				player.sendPacket(new ExEnchantSkillResult(true));
			}

			if (_log.isDebugEnabled())
				_log.debug("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");

			player.sendPacket(new UserInfo(player));
			player.sendPacket(new ExBrExtraUserInfo(player));

			if (levelPenalty == 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WILL_REMAIN);
				sm.addSkillName(_skillId);
				player.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WAS_DECREASED_BY_S2);
				sm.addSkillName(_skillId);
				sm.addNumber(levelPenalty);
				player.sendPacket(sm);
			}
			player.sendSkillList();
			player.sendPacket(new ExEnchantSkillInfo(_skillId, player.getSkillLevel(_skillId)));
			player.sendPacket(new ExEnchantSkillInfoDetail(3, _skillId, player.getSkillLevel(_skillId), player));

			player.getPlayerSettings().getShortCuts().updateSkillShortcuts(_skillId);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			player.sendPacket(sm);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:34 RequestExEnchantSkillRouteChange";
	}
}
