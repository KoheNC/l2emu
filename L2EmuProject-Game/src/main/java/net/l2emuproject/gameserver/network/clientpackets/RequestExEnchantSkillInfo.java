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

import net.l2emuproject.gameserver.datatables.SkillTable;
import net.l2emuproject.gameserver.datatables.SkillTreeTable;
import net.l2emuproject.gameserver.network.serverpackets.ExEnchantSkillInfo;
import net.l2emuproject.gameserver.skills.L2Skill;
import net.l2emuproject.gameserver.world.object.L2Player;

/**
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x06 d: skill id d: skill lvl
 * 
 * @author -Wooden-
 */
public final class RequestExEnchantSkillInfo extends L2GameClientPacket
{
	private static final String	_C__D0_06_REQUESTEXENCHANTSKILLINFO	= "[C] D0:06 RequestExEnchantSkillInfo";

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
		L2Player activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		if (activeChar.getLevel() < 76)
			return;

		/*
		 * L2Npc trainer = activeChar.getLastFolkNPC(); if (!(trainer instanceof L2NpcInstance)) return; if (!trainer.canInteract(activeChar) && !activeChar.isGM()) return;
		 */

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);

		if (skill == null || skill.getId() != _skillId)
		{
			return;
		}

		if (SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(_skillId) == null)
			return;

		int playerSkillLvl = activeChar.getSkillLevel(_skillId);
		if (playerSkillLvl == -1 || playerSkillLvl != _skillLvl)
			return;

		activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, _skillLvl));
	}

	@Override
	public String getType()
	{
		return _C__D0_06_REQUESTEXENCHANTSKILLINFO;
	}
}
