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
import net.l2emuproject.gameserver.network.SystemMessageId;
import net.l2emuproject.gameserver.network.serverpackets.ExBrExtraUserInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExEnchantSkillInfo;
import net.l2emuproject.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import net.l2emuproject.gameserver.network.serverpackets.ExEnchantSkillResult;
import net.l2emuproject.gameserver.network.serverpackets.SystemMessage;
import net.l2emuproject.gameserver.network.serverpackets.UserInfo;
import net.l2emuproject.gameserver.skills.L2EnchantSkillLearn;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.skills.L2EnchantSkillLearn.EnchantSkillDetail;
import net.l2emuproject.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x06 d: skill id d: skill lvl
 * 
 * @author -Wooden-
 */
public final class RequestExEnchantSkill extends L2GameClientPacket
{
	private static final String	_C__D0_07_REQUESTEXENCHANTSKILL	= "[C] D0:07 RequestExEnchantSkill";
	private static final Log	_log							= LogFactory.getLog(RequestAcquireSkill.class.getName());

	private int					_skillId;
	private int					_skillLvl;

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

		int costMultiplier = SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER;
		int reqItemId = SkillTreeTable.NORMAL_ENCHANT_BOOK;

		L2EnchantSkillLearn s = SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			return;
		}
		EnchantSkillDetail esd = s.getEnchantSkillDetail(_skillLvl);
		if (player.getSkillLevel(_skillId) != esd.getMinSkillLevel())
		{
			return;
		}

		int requiredSp = esd.getSpCost() * costMultiplier;
		int requireditems = (esd.getAdenaCost() * costMultiplier);
		int rate = esd.getRate(player);

		if (player.getSp() >= requiredSp)
		{
			// only first lvl requires book
			boolean usesBook = _skillLvl % 100 == 1; // 101, 201, 301 ...
			L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);

			if (Config.ALT_ES_SP_BOOK_NEEDED && usesBook)
			{
				if (spb == null)// Haven't spellbook
				{
					requestFailed(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
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
			if (Config.ALT_ES_SP_BOOK_NEEDED && usesBook)
			{
				check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
			}

			check &= player.destroyItemByItemId("Consume", 57, requireditems, player, true);

			if (!check)
			{
				requestFailed(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}

			// ok. Destroy ONE copy of the book
			if (Rnd.get(100) <= rate)
			{
				player.addSkill(skill, true);

				if (_log.isDebugEnabled())
					_log.debug("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");

				player.sendPacket(new ExEnchantSkillResult(true));

				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1);
				sm.addSkillName(_skillId);
				player.sendPacket(sm);

			}
			else
			{
				player.addSkill(SkillTable.getInstance().getInfo(_skillId, s.getBaseLevel()), true);
				requestFailed(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL);

				player.sendPacket(new ExEnchantSkillResult(false));
			}
			player.sendPacket(new UserInfo(player));
			player.sendPacket(new ExBrExtraUserInfo(player));
			player.sendSkillList();
			player.sendPacket(new ExEnchantSkillInfo(_skillId, player.getSkillLevel(_skillId)));
			player.sendPacket(new ExEnchantSkillInfoDetail(0, _skillId, player.getSkillLevel(_skillId) + 1, player));

			player.getPlayerSettings().getShortCuts().updateSkillShortcuts(_skillId);
		}
		else
			requestFailed(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
	}

	@Override
	public String getType()
	{
		return _C__D0_07_REQUESTEXENCHANTSKILL;
	}
}
