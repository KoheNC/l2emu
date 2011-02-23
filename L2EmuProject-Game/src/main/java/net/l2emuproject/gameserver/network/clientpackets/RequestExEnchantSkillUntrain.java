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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x33 d: skill id d: skill lvl
 * 
 * @author -Wooden-
 */
public final class RequestExEnchantSkillUntrain extends L2GameClientPacket
{
	private static final Log	_log	= LogFactory.getLog(RequestExEnchantSkillUntrain.class);

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

		L2EnchantSkillLearn s = SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
			return;

		if (_skillLvl % 100 == 0)
		{
			_skillLvl = s.getBaseLevel();
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		if (skill == null)
			return;

		int reqItemId = SkillTreeTable.UNTRAIN_ENCHANT_BOOK;

		int currentLevel = player.getSkillLevel(_skillId);
		if (currentLevel - 1 != _skillLvl && (currentLevel % 100 != 1 || _skillLvl != s.getBaseLevel()))
			return;

		EnchantSkillDetail esd = s.getEnchantSkillDetail(currentLevel);

		int requiredSp = esd.getSpCost();
		int requireditems = esd.getAdenaCost();

		L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
		if (Config.ALT_ES_SP_BOOK_NEEDED)
		{
			if (spb == null) // Haven't spellbook
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

		boolean check = true;
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

		player.getStat().addSp((int) (requiredSp * 0.8));

		player.addSkill(skill, true);
		player.sendPacket(new ExEnchantSkillResult(true));

		if (_log.isDebugEnabled())
			_log.debug("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");

		player.sendPacket(new UserInfo(player));
		player.sendPacket(new ExBrExtraUserInfo(player));

		if (_skillLvl > 100)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_DECREASED_BY_ONE);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_RESETED);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		player.sendSkillList();
		player.sendPacket(new ExEnchantSkillInfo(_skillId, player.getSkillLevel(_skillId)));
		player.sendPacket(new ExEnchantSkillInfoDetail(2, _skillId, player.getSkillLevel(_skillId) - 1, player));
		player.getPlayerSettings().getShortCuts().updateSkillShortcuts(_skillId);
	}

	@Override
	public String getType()
	{
		return "[C] D0:33 RequestExEnchantSkillUntrain";
	}
}
